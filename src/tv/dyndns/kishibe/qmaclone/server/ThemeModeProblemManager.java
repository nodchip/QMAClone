package tv.dyndns.kishibe.qmaclone.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.inject.Inject;

public class ThemeModeProblemManager extends ProblemManager {
	private static final Logger logger = Logger.getLogger(ThemeModeProblemManager.class.toString());
	private final Database database;
	private final ThreadPool threadPool;
	/**
	 * テーマと検索クエリのマップ。
	 */
	private volatile Map<String, IntArray> themeToProblems;
	/**
	 * ジャンル毎のテーマリスト。一時間毎に更新される。
	 */
	private volatile List<List<String>> themes;

	@Inject
	public ThemeModeProblemManager(Database database, ThreadPool threadPool) {
		super(database);
		this.database = database;
		this.threadPool = threadPool;
	}

	private void initializeIfNotInitialized() {
		if (themeToProblems == null) {
			synchronized (this) {
				if (themeToProblems == null) {
					try {
						updateProblemTablesForThemeMode();
					} catch (DatabaseException e) {
						logger.log(Level.WARNING, "テーマモードの読み込みに失敗しました", e);
					}

					threadPool.addHourTask(new Runnable() {
						public void run() {
							try {
								updateProblemTablesForThemeMode();
							} catch (DatabaseException e) {
								logger.log(Level.WARNING, "テーマモードの読み込みに失敗しました", e);
							}
						}
					});
				}
			}
		}
	}

	private void updateProblemTablesForThemeMode() throws DatabaseException {
		List<List<String>> themes = Lists.newArrayList();
		for (int i = 0; i < ProblemGenre.values().length; ++i) {
			themes.add(new ArrayList<String>());
		}

		Map<String, IntArray> themeToProblems = database.getThemeToProblems(getThemeModeQueries());

		double[] min = new double[ProblemGenre.values().length];
		double[] max = new double[ProblemGenre.values().length];
		svm_model model = createSvmModel(themeToProblems, min, max);

		for (Entry<String, IntArray> entry : themeToProblems.entrySet()) {
			String theme = entry.getKey();
			IntArray problemIds = entry.getValue();

			// 問題数が少なすぎる場合はロビーに表示しない
			if (problemIds.size() < Constant.MIN_NUMBER_OF_THEME_MODE_PROBLEMS) {
				continue;
			}

			svm_node[] x = createNode(problemIds);
			scale(x, min, max);
			double[] prob = new double[ProblemGenre.values().length];
			double y = svm.svm_predict_probability(model, x, prob);

			ProblemGenre themeBySvm = ProblemGenre.values()[(int) Math.rint(y)];
			themes.get(themeBySvm.getIndex()).add(theme);
		}

		for (List<String> list : themes) {
			Collections.sort(list);
		}

		this.themeToProblems = themeToProblems;
		this.themes = themes;
	}

	private Map<String, List<String>> getThemeModeQueries() throws DatabaseException {
		Map<String, List<String>> themetoQueries = Maps.newHashMap();
		for (PacketThemeQuery query : database.getThemeModeQueries()) {
			if (!themetoQueries.containsKey(query.getTheme())) {
				themetoQueries.put(query.getTheme(), new ArrayList<String>());
			}
			themetoQueries.get(query.getTheme()).add(query.query);
		}
		return themetoQueries;
	}

	public PacketProblemMinimum selectProblem(String theme, int difficultSelect, int classLevel,
			Set<Integer> selectedProblemIds) throws Exception {
		initializeIfNotInitialized();

		// 難易度調整
		switch (difficultSelect) {
		case Constant.DIFFICULT_SELECT_DIFFICULT:
			classLevel = Constant.CLASS_LEVEL_DIFFICULT;
			break;
		case Constant.DIFFICULT_SELECT_LITTLE_DIFFICULT:
			classLevel = Constant.CLASS_LEVEL_LITTLE_DIFFICULT;
			break;
		case Constant.DIFFICULT_SELECT_LITTLE_EASY:
			classLevel = Constant.CLASS_LEVEL_LITTLE_EASY;
			break;
		case Constant.DIFFICULT_SELECT_EASY:
			classLevel = Constant.CLASS_LEVEL_EASY;
			break;
		case Constant.DIFFICULT_SELECT_NORMAL:
			classLevel = Constant.CLASS_LEVEL_NORMAL;
			break;
		}

		// 問題の選択
		PacketProblemMinimum data = null;
		IntArray problemIds = themeToProblems.get(theme);
		for (int findLoop = 0; findLoop < MAX_FIND_LOOP && data == null; ++findLoop) {
			data = selectProblemFromList(problemIds, selectedProblemIds, classLevel,
					NewAndOldProblems.Both, false, new HashSet<Integer>(), new HashSet<Integer>(),
					false);
		}

		if (data == null && difficultSelect != Constant.DIFFICULT_SELECT_NORMAL) {
			// 問題が選択されなかった場合は全難易度から選択しなおす
			data = selectProblem(theme, Constant.DIFFICULT_SELECT_NORMAL, classLevel,
					selectedProblemIds);
		}

		if (data == null) {
			throw new Exception("問題が見つかりませんでした " + theme + " " + difficultSelect + " " + classLevel
					+ " " + selectedProblemIds);
		}

		selectedProblemIds.add(data.id);

		return data;
	}

	/**
	 * テーマモード一覧を返す。
	 * 
	 * @return [ジャンル][テーマ]
	 */
	public List<List<String>> getThemes() {
		initializeIfNotInitialized();

		return themes;
	}

	/**
	 * テーマモード問題一覧を返す
	 * 
	 * @return テーマモード問題一覧
	 */
	public Map<String, IntArray> getThemesAndProblems() {
		initializeIfNotInitialized();

		return themeToProblems;
	}

	private static final Map<String, ProblemGenre> LEARNING_DATA = ImmutableMap
			.<String, ProblemGenre> builder().put("数字で答えなさい", ProblemGenre.Random)
			.put("「唯一」", ProblemGenre.Random).put("？？？", ProblemGenre.Random)
			.put("ガンダム", ProblemGenre.Anige).put("ドラえもん", ProblemGenre.Anige)
			.put("コナミ", ProblemGenre.Anige).put("名言・名台詞", ProblemGenre.Anige)
			.put("アイドル", ProblemGenre.Geinou).put("映画", ProblemGenre.Geinou)
			.put("お笑い", ProblemGenre.Geinou).put("プロ野球", ProblemGenre.Sports)
			.put("ワールドカップ", ProblemGenre.Sports).put("格闘技", ProblemGenre.Sports)
			.put("ファッション", ProblemGenre.Zatsugaku).put("漢字", ProblemGenre.Zatsugaku)
			.put("新聞", ProblemGenre.Zatsugaku).put("神話", ProblemGenre.Gakumon)
			.put("日本史", ProblemGenre.Gakumon).put("科学", ProblemGenre.Gakumon).build();

	private svm_model createSvmModel(Map<String, IntArray> themeToProblems, double[] min,
			double[] max) throws DatabaseException {
		Preconditions.checkArgument(min.length == ProblemGenre.values().length);
		Preconditions.checkArgument(max.length == ProblemGenre.values().length);
		for (int i = 0; i < min.length; ++i) {
			min[i] = Double.POSITIVE_INFINITY;
			max[i] = Double.NEGATIVE_INFINITY;
		}

		svm_parameter param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 1.0 / ProblemGenre.values().length; // 1/num_features
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		List<Double> y = Lists.newArrayList();
		List<svm_node[]> x = Lists.newArrayList();

		for (Entry<String, ProblemGenre> entry : LEARNING_DATA.entrySet()) {
			String theme = entry.getKey();
			IntArray problemIds = themeToProblems.get(theme);
			if (problemIds == null) {
				continue;
			}

			y.add((double) entry.getValue().getIndex());

			svm_node[] node = createNode(problemIds);
			for (int i = 0; i < ProblemGenre.values().length; ++i) {
				min[i] = Math.min(min[i], node[i].value);
				max[i] = Math.max(max[i], node[i].value);
			}
			x.add(node);
		}

		for (svm_node[] node : x) {
			scale(node, min, max);
		}

		svm_problem problem = new svm_problem();
		problem.l = y.size();
		problem.y = Doubles.toArray(y);
		problem.x = x.toArray(new svm_node[0][]);

		return svm.svm_train(problem, param);
	}

	private svm_node[] createNode(IntArray problemIds) throws DatabaseException {
		svm_node[] node = new svm_node[ProblemGenre.values().length];
		for (int i = 0; i < node.length; ++i) {
			node[i] = new svm_node();
			node[i].index = i + 1;
		}
		for (int problemId : problemIds.data()) {
			++node[database.getProblemMinimum(problemId).genre.getIndex()].value;
		}
		for (svm_node e : node) {
			e.value /= problemIds.data().length;
		}
		return node;
	}

	private void scale(svm_node[] x, double[] min, double[] max) {
		Preconditions.checkArgument(x.length == min.length);
		Preconditions.checkArgument(x.length == max.length);
		for (int i = 0; i < x.length; ++i) {
			if (max[i] == min[i]) {
				x[i].value = 0.0;
			} else {
				x[i].value = (x[i].value - min[i]) / (max[i] - min[i]) * 2.0 - 1.0;
			}
		}
	}
}
