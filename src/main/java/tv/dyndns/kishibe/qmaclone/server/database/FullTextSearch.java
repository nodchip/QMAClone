package tv.dyndns.kishibe.qmaclone.server.database;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThisQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.util.HasIndex;
import tv.dyndns.kishibe.qmaclone.server.QMACloneModule;
import tv.dyndns.kishibe.qmaclone.server.ThreadPool;
import tv.dyndns.kishibe.qmaclone.server.relevance.NGramAnalyzer;
import tv.dyndns.kishibe.qmaclone.server.relevance.ViterbiAnalyzer;
import tv.dyndns.kishibe.qmaclone.server.relevance.ViterbiTokenizer;
import tv.dyndns.kishibe.qmaclone.server.util.DevelopmentUtil;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;
import tv.dyndns.kishibe.qmaclone.server.util.Normalizer;

public class FullTextSearch {
	private static final Logger logger = Logger.getLogger(FullTextSearch.class.toString());
	private static final Path INDEX_FILE_DIRECTORY = FileSystems.getDefault().getPath("/tmp/qmaclone/lucene");
	private static final int TIME_OUT_SEC = 10;
	private static final int MAX_NUMBER_OF_SEARCH_REUSLTS = 10000;
	private static final String FIELD_PROBLEM_ID = "problemId";
	private static final String FIELD_SENTENCE = "sentence";
	private static final String FIELD_SEARCH = "search";
	private static final String FIELD_CREATOR = "creator";
	private static final String FIELD_CREATOR_DIRECTLY = "creatorDirectly";
	private static final String FIELD_GENRE = "genre";
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_RANDOM_FLAG = "randomFlag";
	private static final String FIELD_SIMILAR = "similar";
	private static final String LUCENE_ESCAPE_CHARS = "(&&)|(\\|\\|)|[\\+\\-\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\\\]";
	private static final Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
	private static final String REPLACEMENT_STRING = "\\\\$0";
	private static final String PREFIX_GENRE = "ジャンル:";
	private static final String PREFIX_TYPE = "問題形式:";
	private static final String PREFIX_CREATOR = "問題作成者:";
	private static final String PREFIX_RANDOM = "ランダム:";
	private static final int MAX_CLAUSE_COUNT = 65536;
	private static final String TERM_OR = "OR";
	private final Object lockIndexWriter = new Object();
	private final ThreadPool threadPool;
	private final DevelopmentUtil developmentUtil;
	private final QueryRunner queryRunner;
	private final ViterbiTokenizer.Factory viterbiTokenizerfactory;
	private final ViterbiAnalyzer.Factory viterbiAnalyzerFactory;

	@Inject
	public FullTextSearch(ThreadPool threadPool, DevelopmentUtil developmentUtil, QueryRunner queryRunner,
			ViterbiTokenizer.Factory viterbiTokenizerfactory, ViterbiAnalyzer.Factory viterbiAnalyzerFactory) {
		BooleanQuery.setMaxClauseCount(MAX_CLAUSE_COUNT);
		this.threadPool = Preconditions.checkNotNull(threadPool);
		this.developmentUtil = Preconditions.checkNotNull(developmentUtil);
		this.queryRunner = Preconditions.checkNotNull(queryRunner);
		this.viterbiTokenizerfactory = Preconditions.checkNotNull(viterbiTokenizerfactory);
		this.viterbiAnalyzerFactory = Preconditions.checkNotNull(viterbiAnalyzerFactory);

		if (!isIndexExists()) {
			try {
				generateIndex();
			} catch (IOException | DatabaseException e) {
				logger.log(Level.WARNING, "インデクスの作成に失敗しました", e);
			}
		}
	}

	private boolean isIndexExists() {
		try {
			return DirectoryReader.indexExists(FSDirectory.open(INDEX_FILE_DIRECTORY));
		} catch (IOException e) {
			logger.log(Level.WARNING, "インデクスの存在確認に失敗しました", e);
		}
		return false;
	}

	private void generateIndex() throws IOException, DatabaseException {
		FileUtils.deleteDirectory(INDEX_FILE_DIRECTORY.toFile());
		INDEX_FILE_DIRECTORY.toFile().mkdirs();

		synchronized (lockIndexWriter) {
			FSDirectory d = FSDirectory.open(INDEX_FILE_DIRECTORY);
			try (IndexWriter writer = new IndexWriter(d,
					new IndexWriterConfig(new NGramAnalyzer()).setOpenMode(OpenMode.CREATE))) {
				// 循環参照のため直接インスタンス化する
				new DirectDatabase(queryRunner, null, null, null).processProblems(new ProblemIndexWriter(writer));
			}
		}
	}

	private IndexWriter newIndexWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		return new IndexWriter(FSDirectory.open(INDEX_FILE_DIRECTORY),
				new IndexWriterConfig(new NGramAnalyzer()).setOpenMode(OpenMode.APPEND));
	}

	private Document convertProblemToDocument(PacketProblem problem) {
		Document document = new Document();

		// 問題番号
		document.add(new StringField(FIELD_PROBLEM_ID, String.valueOf(problem.id), Store.YES));

		// 問題文
		String sentence = problem.sentence;
		document.add(new TextField(FIELD_SENTENCE, sentence, Store.NO));

		// 問題文+選択肢+解答+問題ノート
		document.add(new TextField(FIELD_SEARCH, problem.getSearchDocument(), Store.NO));

		// 作問者
		document.add(new TextField(FIELD_CREATOR, problem.creator, Store.NO));

		// 作問者
		document.add(new StringField(FIELD_CREATOR_DIRECTLY, problem.creator, Store.NO));

		// ジャンル
		document.add(new IntPoint(FIELD_GENRE, problem.genre.getIndex()));

		// 出題形式
		document.add(new IntPoint(FIELD_TYPE, problem.type.getIndex()));

		// ランダム
		document.add(new IntPoint(FIELD_RANDOM_FLAG, problem.randomFlag.getIndex()));

		// 類似問題検索
		document.add(new TextField(FIELD_SIMILAR,
				viterbiTokenizerfactory.create(new StringReader(problem.getSearchDocument()))));

		return document;
	}

	private class ProblemIndexWriter implements ProblemProcessable {
		private final IndexWriter indexWriter;

		public ProblemIndexWriter(IndexWriter indexWriter) {
			this.indexWriter = indexWriter;
		}

		@Override
		public void process(PacketProblem problem) throws Exception {
			Document document = convertProblemToDocument(problem);
			try {
				indexWriter.addDocument(document);
			} catch (CorruptIndexException e) {
				logger.log(Level.WARNING, "インデクスが破損しています", e);
			} catch (IOException e) {
				logger.log(Level.WARNING, "インデクスへの書き込みに失敗しました", e);
			}
		}
	}

	private Query wordToQuery(String field, String word) throws IOException {
		BooleanQuery.Builder query = new BooleanQuery.Builder();
		try (NGramAnalyzer analyzer = new NGramAnalyzer();
				TokenStream ts = analyzer.tokenStream(field, new StringReader(word))) {
			ts.reset();
			CharTermAttribute termAttribute = ts.getAttribute(CharTermAttribute.class);
			while (ts.incrementToken()) {
				String term = termAttribute.toString();
				query.add(new TermQuery(new Term(field, term)), Occur.MUST);
			}
			ts.end();

			return query.build();
		}
	}

	private Query concatenateWithOr(List<Query> queries) {
		BooleanQuery.Builder or = new BooleanQuery.Builder();
		for (Query query : queries) {
			or.add(query, Occur.SHOULD);
		}
		return or.build();
	}

	private Query stringToQuery(String field, String string) throws IOException {
		List<Query> queries = new ArrayList<>();

		BooleanQuery.Builder query = new BooleanQuery.Builder();

		StringTokenizer st = new StringTokenizer(string);
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			if (word.equals(TERM_OR)) {
				// OR が現れた場合は後ほど結合する
				queries.add(query.build());
				query = new BooleanQuery.Builder();

			} else if (word.startsWith("-")) {
				query.add(wordToQuery(field, word.substring(1)), Occur.MUST_NOT);

			} else {
				query.add(wordToQuery(field, word), Occur.MUST);
			}
		}

		if (queries.isEmpty()) {
			// OR が現れていない場合はクエリをそのまま返す
			return query.build();
		}

		// OR が現れた場合はすべてのクエリを OR でつないで返す
		queries.add(query.build());
		return concatenateWithOr(queries);
	}

	public static String escapeQuery(String t) {
		return LUCENE_PATTERN.matcher(t).replaceAll(REPLACEMENT_STRING);
	}

	public Map<String, IntArray> getThemeModeProblemMinimums(Map<String, List<String>> themeAndQueryStrings) {
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		final Map<String, IntArray> themeToProblems = Maps.newConcurrentMap();

		for (final Entry<String, List<String>> entry : themeAndQueryStrings.entrySet()) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					String theme = entry.getKey();
					List<String> queryStrings = entry.getValue();

					IntArray problemIds = null;
					try {
						problemIds = searchProblemsForThemeMode(queryStrings);
					} catch (Exception e) {
						logger.log(Level.WARNING, "テーマモードの問題検索に失敗しました: " + theme, e);
					}

					if (problemIds == null) {
						return;
					}

					themeToProblems.put(theme, problemIds);
				}
			});
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			// 何もしない
		}

		return ImmutableMap.copyOf(themeToProblems);
	}

	private Query queryStringToThemeModeQuery(String string) throws IOException {
		BooleanQuery.Builder query = new BooleanQuery.Builder();

		StringTokenizer st = new StringTokenizer(string);
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			Occur occur = Occur.MUST;
			if (word.startsWith("-")) {
				word = word.substring(1);
				occur = Occur.MUST_NOT;
			}

			if (word.startsWith(PREFIX_GENRE)) {
				// ジャンル
				ProblemGenre genre = ProblemGenre.fromName(word.substring(PREFIX_GENRE.length()));
				if (genre == null) {
					continue;
				}
				query.add(IntPoint.newExactQuery(FIELD_GENRE, genre.getIndex()), occur);

			} else if (word.startsWith(PREFIX_TYPE)) {
				// 出題形式
				ProblemType type = ProblemType.fromName(word.substring(PREFIX_TYPE.length()));
				if (type == null) {
					continue;
				}
				query.add(IntPoint.newExactQuery(FIELD_TYPE, type.getIndex()), occur);

			} else if (word.startsWith(PREFIX_CREATOR)) {
				// 問題作成者
				String creator = word.substring(PREFIX_CREATOR.length());
				query.add(new TermQuery(new Term(FIELD_CREATOR_DIRECTLY, creator)), occur);

			} else if (word.startsWith(PREFIX_RANDOM)) {
				// 問題作成者
				String random = word.substring(PREFIX_RANDOM.length());
				random = Normalizer.normalize(random);
				if (Ints.tryParse(random) == null) {
					continue;
				}
				int randomFlag = Ints.tryParse(random);
				query.add(IntPoint.newExactQuery(FIELD_RANDOM_FLAG, randomFlag), occur);

			} else {
				// 問題文
				query.add(wordToQuery(FIELD_SENTENCE, word), occur);
			}
		}

		return query.build();
	}

	@VisibleForTesting
	IntArray searchProblemsForThemeMode(List<String> queryStrings) throws CorruptIndexException, IOException {
		Stopwatch stopwatch = Stopwatch.createStarted();

		try (IndexReader reader = DirectoryReader.open(FSDirectory.open(INDEX_FILE_DIRECTORY))) {

			// クエリ生成 加法標準形
			BooleanQuery.Builder query = new BooleanQuery.Builder();
			for (String queryString : queryStrings) {
				query.add(queryStringToThemeModeQuery(queryString), Occur.SHOULD);
			}

			IntArray problemIds = new IntArray();

			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(query.build(), Integer.MAX_VALUE);

			for (ScoreDoc doc : docs.scoreDocs) {
				Document document = reader.document(doc.doc);
				int problemId = Integer.parseInt(document.get(FIELD_PROBLEM_ID));
				problemIds.add(problemId);
			}

			logger.log(Level.INFO,
					String.format("searchProblem(): time=%d result=%d query=%s",
							stopwatch.elapsed(TimeUnit.MILLISECONDS), problemIds.size(),
							MoreObjects.toStringHelper(this).add("queryStrings", queryStrings).toString()));

			return problemIds;
		}
	}

	public void addProblem(PacketProblem problem) throws CorruptIndexException, LockObtainFailedException, IOException {
		synchronized (lockIndexWriter) {
			Document document = convertProblemToDocument(problem);
			try (IndexWriter indexWriter = newIndexWriter()) {
				indexWriter.addDocument(document);
			}
		}
	}

	public void updateProblem(PacketProblem problem)
			throws CorruptIndexException, LockObtainFailedException, IOException {
		synchronized (lockIndexWriter) {
			Document document = convertProblemToDocument(problem);
			try (IndexWriter indexWriter = newIndexWriter()) {
				indexWriter.updateDocument(new Term(FIELD_PROBLEM_ID, String.valueOf(problem.id)), document);
			}
		}
	}

	private <T extends Enum<T> & HasIndex> Query toRangeQuery(String field, Set<T> set) {
		Set<Integer> indexes = Sets.newHashSet();
		for (T element : set) {
			indexes.add(element.getIndex());
		}

		BooleanQuery.Builder query = new BooleanQuery.Builder();
		int begin = -1;
		for (int i = 0; i < 31; ++i) {
			if (indexes.contains(i)) {
				if (begin == -1) {
					begin = i;
				}
			} else {
				if (begin == -1) {
					continue;
				}
				Query q = IntPoint.newRangeQuery(field, begin, i - 1);
				query.add(q, Occur.SHOULD);
				begin = -1;
			}
		}

		return query.build();
	}

	/**
	 * 問題を検索する
	 * 
	 * @param queryString            検索クエリ
	 * @param creator                問題作成者 (部分一致)
	 * @param creatorPerfectMatching 問題作成者 (完全一致)
	 * @param genresFinal            ジャンル
	 * @param typesFinal             出題形式
	 * @param randomFlagsFinal       ランダムフラグ
	 * @return
	 */
	public List<Integer> searchProblem(final String queryString, final String creator,
			final boolean creatorPerfectMatching, final Set<ProblemGenre> genresFinal,
			final Set<ProblemType> typesFinal, final Set<RandomFlag> randomFlagsFinal) {
		Stopwatch stopwatch = Stopwatch.createStarted();

		Future<List<Integer>> future = threadPool.submit(new Callable<List<Integer>>() {
			@Override
			public List<Integer> call() throws Exception {
				Set<ProblemGenre> genres = genresFinal == null ? EnumSet.noneOf(ProblemGenre.class)
						: Sets.newEnumSet(genresFinal, ProblemGenre.class);
				Set<ProblemType> types = typesFinal == null ? EnumSet.noneOf(ProblemType.class)
						: Sets.newEnumSet(typesFinal, ProblemType.class);
				Set<RandomFlag> randomFlags = randomFlagsFinal == null ? EnumSet.noneOf(RandomFlag.class)
						: Sets.newEnumSet(randomFlagsFinal, RandomFlag.class);

				boolean queryEmpty = Strings.isNullOrEmpty(queryString);
				boolean creatorEmpty = Strings.isNullOrEmpty(creator);
				boolean genreEmpty = (genres.isEmpty() || genres.equals(EnumSet.of(ProblemGenre.Random)));
				boolean typeEmpty = (types.isEmpty() || types.equals(EnumSet.of(ProblemType.Random)));
				if (queryEmpty && creatorEmpty && genreEmpty && typeEmpty) {
					return new ArrayList<Integer>();
				}

				if (genres.isEmpty() || genres.contains(ProblemGenre.Random)) {
					genres = EnumSet.complementOf(EnumSet.of(ProblemGenre.Random));
				}

				if (types.isEmpty() || types.contains(ProblemType.Random)) {
					types = EnumSet.complementOf(EnumSet.of(ProblemType.Random));
				}

				if (randomFlags.isEmpty() || randomFlags.contains(RandomFlag.NotSelected)) {
					randomFlags = EnumSet.complementOf(EnumSet.of(RandomFlag.NotSelected));
				}

				BooleanQuery.Builder query = new BooleanQuery.Builder();

				// 問題文
				if (!queryEmpty) {
					try {
						query.add(stringToQuery(FIELD_SEARCH, queryString), Occur.MUST);
					} catch (Exception e) {
						logger.log(Level.WARNING, "クエリの追加に失敗しました", e);
						return null;
					}
				}

				// 作成者
				if (!creatorEmpty) {
					if (creatorPerfectMatching) {
						query.add(new TermQuery(new Term(FIELD_CREATOR_DIRECTLY, creator)), Occur.MUST);
					} else {
						try {
							query.add(stringToQuery(FIELD_CREATOR, creator), Occur.MUST);
						} catch (Exception e) {
							logger.log(Level.WARNING, "クエリの追加に失敗しました", e);
							return null;
						}
					}
				}

				// ジャンル
				query.add(toRangeQuery(FIELD_GENRE, genres), Occur.MUST);

				// 出題形式
				query.add(toRangeQuery(FIELD_TYPE, types), Occur.MUST);

				// ランダムフラグ
				query.add(toRangeQuery(FIELD_RANDOM_FLAG, randomFlags), Occur.MUST);

				try (IndexReader reader = DirectoryReader.open(FSDirectory.open(INDEX_FILE_DIRECTORY))) {
					IndexSearcher searcher = new IndexSearcher(reader);
					TopDocs docs = searcher.search(query.build(), MAX_NUMBER_OF_SEARCH_REUSLTS);
					List<Integer> problemIds = new ArrayList<Integer>(docs.scoreDocs.length);
					for (ScoreDoc doc : docs.scoreDocs) {
						Document document = reader.document(doc.doc);
						int problemId = Integer.parseInt(document.get(FIELD_PROBLEM_ID));
						problemIds.add(problemId);
					}

					return problemIds;
				}
			}
		});

		try {
			List<Integer> problemIds = future.get(getTimeOutSec(), TimeUnit.SECONDS);
			logger.log(Level.INFO, String.format("searchProblem(): time=%d result=%d query=%s",
					stopwatch.elapsed(TimeUnit.MILLISECONDS), problemIds.size(),
					MoreObjects.toStringHelper(this).add("queryString", queryString).add("creator", creator)
							.add("creatorPerfectMatching", creatorPerfectMatching).add("genresFinal", genresFinal)
							.add("typesFinal", typesFinal).add("randomFlagsFinal", randomFlagsFinal).toString()));
			return problemIds;
		} catch (Exception e) {
			Object[] args = { queryString, creator, genresFinal, typesFinal, randomFlagsFinal };
			logger.log(Level.WARNING, "問題検索でタイムアウトが発生しました " + Arrays.deepToString(args), e);
			return null;
		}
	}

	/**
	 * 類似問題を検索する
	 * 
	 * @param problem 問題
	 * @return
	 */
	public List<Integer> searchSimilarProblemFromDatabase(final PacketProblem problem) {
		Stopwatch stopwatch = Stopwatch.createStarted();

		Future<List<Integer>> future = threadPool.submit(new Callable<List<Integer>>() {
			@Override
			public List<Integer> call() throws Exception {
				try (IndexReader reader = DirectoryReader.open(FSDirectory.open(INDEX_FILE_DIRECTORY))) {
					String[] fields = new String[] { FIELD_SIMILAR };
					String searchQuery = problem.getSearchDocument();
					IndexSearcher searcher = new IndexSearcher(reader);
					MoreLikeThisQuery query = new MoreLikeThisQuery(searchQuery, fields,
							viterbiAnalyzerFactory.create(), "similar_problem_search_field");

					TopDocs docs = searcher.search(query, 10);
					List<Integer> problemIds = new ArrayList<Integer>(docs.scoreDocs.length);
					for (ScoreDoc doc : docs.scoreDocs) {
						Document document = reader.document(doc.doc);
						int problemId = Integer.parseInt(document.get(FIELD_PROBLEM_ID));
						problemIds.add(problemId);
					}

					return problemIds;

				}
			}
		});

		try {
			List<Integer> problemIds = future.get(getTimeOutSec(), TimeUnit.SECONDS);
			logger.log(Level.INFO, String.format("searchSimilarProblemFromDatabase(): time=%d result=%d query=%s",
					stopwatch.elapsed(TimeUnit.MILLISECONDS), problemIds.size(), problem.toString()));
			return problemIds;
		} catch (Exception e) {
			logger.log(Level.WARNING, "類似問題検索でタイムアウトが発生しました " + problem.toString(), e);
			return Lists.newArrayList();
		}
	}

	private int getTimeOutSec() {
		return developmentUtil.isDev() ? Integer.MAX_VALUE / 2 : TIME_OUT_SEC;
	}

	public List<Integer> getAdsenseProblems(final String queryString) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		Future<List<Integer>> future = threadPool.submit(new Callable<List<Integer>>() {
			@Override
			public List<Integer> call() throws Exception {
				try (IndexReader reader = DirectoryReader.open(FSDirectory.open(INDEX_FILE_DIRECTORY))) {
					IndexSearcher searcher = new IndexSearcher(reader);

					Query query = stringToQuery(FIELD_SENTENCE, queryString);
					TopDocs docs = searcher.search(query, 1000);
					List<Integer> problemIds = new ArrayList<Integer>(docs.scoreDocs.length);
					for (ScoreDoc doc : docs.scoreDocs) {
						Document document = reader.document(doc.doc);
						int problemId = Integer.parseInt(document.get(FIELD_PROBLEM_ID));
						problemIds.add(problemId);
					}

					return problemIds;
				}
			}
		});
		try {
			List<Integer> problems = future.get(getTimeOutSec(), TimeUnit.SECONDS);
			logger.log(Level.INFO, String.format("getAdsenseProblems(): time=%d queryString=%s",
					stopwatch.elapsed(TimeUnit.MILLISECONDS), queryString));
			return problems;
		} catch (Exception e) {
			logger.log(Level.WARNING, "広告誘導文章作成用の問題検索に失敗しました : query=" + queryString, e);
			return null;
		}
	}

	public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new QMACloneModule());
		FullTextSearch search = injector.getInstance(FullTextSearch.class);

		String word = "KAITO";
		Query query = search.queryStringToThemeModeQuery(word);
		// Query query = search.wordToQuery(FIELD_SEARCH, word);
		// Query query = new TermQuery(new Term(FIELD_SEARCH, word));

		try (IndexReader reader = DirectoryReader.open(FSDirectory.open(INDEX_FILE_DIRECTORY))) {
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(query, MAX_NUMBER_OF_SEARCH_REUSLTS);
			for (ScoreDoc doc : docs.scoreDocs) {
				Document document = reader.document(doc.doc);
				System.out.println(document.get(FIELD_SENTENCE));
			}
		}

		{
			List<Integer> problemIds = search.searchProblem(word, null, false, EnumSet.noneOf(ProblemGenre.class),
					EnumSet.noneOf(ProblemType.class), EnumSet.noneOf(RandomFlag.class));
			System.out.println(problemIds);
		}

		{
			IntArray problemIds = search.searchProblemsForThemeMode(ImmutableList.of(word));
			System.out.println(problemIds.asList());
		}

		injector.getInstance(ThreadPool.class).shutdown();
	}
}
