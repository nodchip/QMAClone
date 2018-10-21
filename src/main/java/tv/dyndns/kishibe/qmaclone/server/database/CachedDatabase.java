//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.server.database;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsResponse;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsThread;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMonth;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor.ThemeModeEditorStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.PageView;
import tv.dyndns.kishibe.qmaclone.server.ThreadPool;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class CachedDatabase implements Database {
	private static final Logger logger = Logger.getLogger(CachedDatabase.class.toString());
	private static final boolean ENABLE_CACHE_STATS = true;
	private static final Object STATIC_KEY = new Object();
	@VisibleForTesting
	final DirectDatabase database;

	// //////////////////////////////////////////////////////////////////////////////
	// キャッシュ
	@Inject
	public CachedDatabase(DirectDatabase database, ThreadPool threadPool) {
		this.database = Preconditions.checkNotNull(database);

		if (ENABLE_CACHE_STATS) {
			threadPool.addDailyTask(new Runnable() {
				@Override
				public void run() {
					writeCacheStatsToLog();
				}
			});
			caches.put("themeRankingCache", themeRankingCache);
			caches.put("themeRankingDateRangesCache", themeRankingDateRangesCache);
			caches.put("rankingDataCache", rankingDataCache);
			caches.put("serverIgnoreUserCodeCache", serverIgnoreUserCodeCache);
			caches.put("numberOfActiveUsersCache", numberOfActiveUsersCache);
			caches.put("lastestProblemsCache", lastestProblemsCache);
			caches.put("themeModeEditorsCache", themeModeEditorsCache);
		}
	}

	private void writeCacheStatsToLog() {
		for (Entry<String, Cache<?, ?>> e : caches.entrySet()) {
			String name = e.getKey();
			CacheStats stats = e.getValue().stats();
			double averageLoadPenalty = stats.averageLoadPenalty();
			double hitRate = stats.hitRate();
			String message = String.format("%s hitRate=%.2f averageLoadPenalty=%.2f %s", name,
					hitRate, averageLoadPenalty, stats.toString());
			logger.log(Level.INFO, message);
		}
	}

	@Override
	public void addChatLog(PacketChatMessage data) throws DatabaseException {
		database.addChatLog(data);
	}

	@Override
	public Map<Integer, PacketChatMessage> getLatestChatData() throws DatabaseException {
		return database.getLatestChatData();
	}

	@Override
	public void addPlayerAnswers(int problemID, ProblemType type, List<String> answers)
			throws DatabaseException {
		database.addPlayerAnswers(problemID, type, answers);
	}

	@Override
	public void addProblemIdsToReport(int userCode, List<Integer> problemIds)
			throws DatabaseException {
		database.addProblemIdsToReport(userCode, problemIds);
	}

	@Override
	public void addRatingHistory(int userCode, int rating) throws DatabaseException {
		database.addRatingHistory(userCode, rating);
	}

	@Override
	public void clearProblemFeedback(int problemId) throws DatabaseException {
		database.clearProblemFeedback(problemId);
	}

	@Override
	public void clearProblemIdFromReport(int userCode) throws DatabaseException {
		database.clearProblemIdFromReport(userCode);
	}

	@Override
	public List<PacketWrongAnswer> getPlayerAnswers(int problemID) throws DatabaseException {
		return database.getPlayerAnswers(problemID);
	}

	@Override
	public List<String> getProblemFeedback(int problemId) throws DatabaseException {
		return database.getProblemFeedback(problemId);
	}

	@Override
	public Map<Integer, List<Integer>> getRatingGroupedByPrefecture() throws DatabaseException {
		return database.getRatingGroupedByPrefecture();
	}

	@Override
	public List<Integer> getRatingHistory(int userCode) throws DatabaseException {
		return database.getRatingHistory(userCode);
	}

	@Override
	public List<PacketProblem> getUserProblemReport(int userCode) throws DatabaseException {
		return database.getUserProblemReport(userCode);
	}

	@Override
	public List<Integer> getWholeRating() throws DatabaseException {
		return database.getWholeRating();
	}

	@Override
	public boolean isUsedUserCode(int userCode) throws DatabaseException {
		return database.isUsedUserCode(userCode);
	}

	@Override
	public PageView loadPageView() throws DatabaseException {
		return database.loadPageView();
	}

	@Override
	public void removePlayerAnswers(int problemID) throws DatabaseException {
		database.removePlayerAnswers(problemID);
	}

	@Override
	public void removeProblemIdFromReport(int userCode, int problemID) throws DatabaseException {
		database.removeProblemIdFromReport(userCode, problemID);
	}

	@Override
	public void savePageView(PageView pageView) throws DatabaseException {
		database.savePageView(pageView);
	}

	@Override
	public void updateThemeModeScore(int userCode, String theme, int score)
			throws DatabaseException {
		database.updateThemeModeScore(userCode, theme, score);
	}

	@Override
	public void voteToProblem(int problemId, boolean good, String feedback)
			throws DatabaseException {
		database.voteToProblem(problemId, good, feedback);
	}

	@Override
	public void resetVote(int problemId) throws DatabaseException {
		database.resetVote(problemId);
	}

	@Override
	public int getNumberOfCreationLogWithUserCode(int userCode, long dateFrom)
			throws DatabaseException {
		return database.getNumberOfCreationLogWithUserCode(userCode, dateFrom);
	}

	@Override
	public int getNumberOfCreationLogWithMachineIp(String machineIp, long dateFrom)
			throws DatabaseException {
		return database.getNumberOfCreationLogWithMachineIp(machineIp, dateFrom);
	}

	@Override
	public int getChatLogId(int year, int month, int day, int hour, int minute, int second)
			throws DatabaseException {
		return database.getChatLogId(year, month, day, hour, minute, second);
	}

	@Override
	public List<PacketChatMessage> getChatLog(int start) throws DatabaseException {
		return database.getChatLog(start);
	}

	@Override
	public int getNumberOfChatLog() throws DatabaseException {
		return database.getNumberOfChatLog();
	}

	@Override
	public List<PacketProblem> getIndicatedProblems() throws DatabaseException {
		return database.getIndicatedProblems();
	}

	private final Map<String, Cache<?, ?>> caches = Maps.newHashMap();

	private <K, V> LoadingCache<K, V> build(String name, CacheLoader<K, V> loader) {
		CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().softValues()
				.expireAfterAccess(1, TimeUnit.HOURS);
		if (ENABLE_CACHE_STATS) {
			builder.recordStats();
		}
		LoadingCache<K, V> cache = builder.build(loader);
		caches.put(name, cache);
		return cache;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// ユーザーデータ
	private final LoadingCache<Integer, PacketUserData> userDataCache = build("userDataCache",
			new CacheLoader<Integer, PacketUserData>() {
				@Override
				public PacketUserData load(Integer key) throws Exception {
					return database.getUserData(key);
				}
			});

	public PacketUserData getUserData(int userCode) throws DatabaseException {
		try {
			return userDataCache.get(userCode);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	public void setUserData(PacketUserData data) throws DatabaseException {
		database.setUserData(data);
		userDataCache.invalidate(data.userCode);
	}

	public void removeIgnoreUserCode(int userCode, int targetUserCode) throws DatabaseException {
		database.removeIgnoreUserCode(userCode, targetUserCode);
		userDataCache.invalidate(userCode);
	}

	public void addIgnoreUserCode(int userCode, int targetUserCode) throws DatabaseException {
		database.addIgnoreUserCode(userCode, targetUserCode);
		userDataCache.invalidate(userCode);
	}

	@Override
	public List<PacketUserData> lookupUserCodeByGooglePlusId(String googlePlusId)
			throws DatabaseException {
		return database.lookupUserCodeByGooglePlusId(googlePlusId);
	}

	@Override
	public void disconnectUserCodeFromGooglePlus(int userCode) throws DatabaseException {
		database.disconnectUserCodeFromGooglePlus(userCode);
	}

	// //////////////////////////////////////////////////////////////////////////////
	// ランキング
	private final LoadingCache<Object, List<List<PacketRankingData>>> rankingDataCache = CacheBuilder
			.newBuilder().recordStats().expireAfterWrite(1, TimeUnit.HOURS).concurrencyLevel(1)
			.build(new CacheLoader<Object, List<List<PacketRankingData>>>() {
				@Override
				public List<List<PacketRankingData>> load(Object arg0) throws Exception {
					return database.getGeneralRankingData();
				}
			});

	public List<List<PacketRankingData>> getGeneralRankingData() throws DatabaseException {
		try {
			return rankingDataCache.get(STATIC_KEY);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////
	// サーバー全体に適用される無視コード
	private final LoadingCache<Object, Set<Integer>> serverIgnoreUserCodeCache = CacheBuilder
			.newBuilder().recordStats().expireAfterWrite(1, TimeUnit.DAYS).concurrencyLevel(1)
			.build(new CacheLoader<Object, Set<Integer>>() {
				@Override
				public Set<Integer> load(Object arg0) throws Exception {
					return database.getServerIgnoreUserCode();
				}
			});

	public Set<Integer> getServerIgnoreUserCode() throws DatabaseException {
		try {
			return serverIgnoreUserCodeCache.get(STATIC_KEY);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void addServerIgnoreUserCode(int userCode) throws DatabaseException {
		database.addServerIgnoreUserCode(userCode);
		serverIgnoreUserCodeCache.invalidateAll();
	}

	// キャッシュがヒットしないためキャッシュ化解除
	public List<PacketProblemCreationLog> getProblemCreationHistory(int problemId)
			throws DatabaseException {
		return database.getProblemCreationHistory(problemId);
	}

	public void addCreationLog(PacketProblem problem, int userCode, String machineIP)
			throws DatabaseException {
		database.addCreationLog(problem, userCode, machineIP);
	}

	private class BbsResponseCacheKey {
		private final int threadId;
		private final int count;

		private BbsResponseCacheKey(int threadId, int count) {
			this.threadId = threadId;
			this.count = count;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BbsResponseCacheKey)) {
				return false;
			}
			BbsResponseCacheKey rh = (BbsResponseCacheKey) obj;
			return threadId == rh.threadId && count == rh.count;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(threadId, count);
		}
	}

	private final LoadingCache<BbsResponseCacheKey, List<PacketBbsResponse>> bbsResponseCache = build(
			"bbsResponseCache", new CacheLoader<BbsResponseCacheKey, List<PacketBbsResponse>>() {
				@Override
				public List<PacketBbsResponse> load(BbsResponseCacheKey key) throws Exception {
					return database.getBbsResponses(key.threadId, key.count);
				}
			});

	public List<PacketBbsThread> getBbsThreads(int bbsId, int start, int count)
			throws DatabaseException {
		return database.getBbsThreads(bbsId, start, count);
	}

	public List<PacketBbsResponse> getBbsResponses(int threadId, int count)
			throws DatabaseException {
		try {
			return bbsResponseCache.get(new BbsResponseCacheKey(threadId, count));
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	public void buildBbsThread(int bbsId, PacketBbsThread thread, PacketBbsResponse response)
			throws DatabaseException {
		database.buildBbsThread(bbsId, thread, response);
		bbsResponseCache.invalidateAll();
	}

	public void writeToBbs(PacketBbsResponse response, boolean age) throws DatabaseException {
		database.writeToBbs(response, age);
		bbsResponseCache.invalidateAll();
	}

	public int getNumberOfBbsThread(int bbsId) throws DatabaseException {
		return database.getNumberOfBbsThread(bbsId);
	}

	// //////////////////////////////////////////////////////////////////////////////
	// リンク
	private class LinkCacheKey {
		private final int start;
		private final int count;

		private LinkCacheKey(int start, int count) {
			this.start = start;
			this.count = count;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof LinkCacheKey && start == ((LinkCacheKey) obj).start
					&& count == ((LinkCacheKey) obj).count;
		}

		@Override
		public int hashCode() {
			int hash = start;
			hash = 31 * hash + count;
			return hash;
		}
	}

	private final LoadingCache<LinkCacheKey, List<PacketLinkData>> linkCache = build("linkCache",
			new CacheLoader<LinkCacheKey, List<PacketLinkData>>() {
				@Override
				public List<PacketLinkData> load(LinkCacheKey key) throws Exception {
					return database.getLinkDatas(key.start, key.count);
				}
			});

	private final LoadingCache<Object, Integer> numberOfLinkDataCache = CacheBuilder.newBuilder()
			.recordStats().expireAfterWrite(1, TimeUnit.DAYS).concurrencyLevel(1)
			.build(new CacheLoader<Object, Integer>() {
				@Override
				public Integer load(Object arg0) throws Exception {
					return database.getNumberOfLinkDatas();
				}
			});

	public void addLinkData(PacketLinkData linkData) throws DatabaseException {
		database.addLinkData(linkData);
		linkCache.invalidateAll();
		numberOfLinkDataCache.invalidateAll();
	}

	public void updateLinkData(PacketLinkData linkData) throws DatabaseException {
		database.updateLinkData(linkData);
		linkCache.invalidateAll();
	}

	public void removeLinkData(int id) throws DatabaseException {
		database.removeLinkData(id);
		linkCache.invalidateAll();
		numberOfLinkDataCache.invalidateAll();
	}

	public List<PacketLinkData> getLinkDatas(int start, int count) throws DatabaseException {
		try {
			return linkCache.get(new LinkCacheKey(start, count));
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	public int getNumberOfLinkDatas() throws DatabaseException {
		try {
			return numberOfLinkDataCache.get(STATIC_KEY);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////
	// アクティブユーザ数
	private final LoadingCache<Object, Integer> numberOfActiveUsersCache = CacheBuilder
			.newBuilder().recordStats().expireAfterWrite(1, TimeUnit.HOURS).concurrencyLevel(1)
			.build(new CacheLoader<Object, Integer>() {
				@Override
				public Integer load(Object arg0) throws Exception {
					return database.getNumberOfActiveUsers();
				}
			});

	public int getNumberOfActiveUsers() throws DatabaseException {
		try {
			return numberOfActiveUsersCache.get(STATIC_KEY);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////
	// テーマモード
	@Override
	public List<PacketThemeQuery> getThemeModeQueries() throws DatabaseException {
		return database.getThemeModeQueries();
	}

	@Override
	public List<PacketThemeQuery> getThemeModeQueries(String theme) throws DatabaseException {
		return database.getThemeModeQueries(theme);
	}

	@Override
	public int getNumberOfThemeQueries() throws DatabaseException {
		return database.getNumberOfThemeQueries();
	}

	@Override
	public void addThemeModeQuery(String theme, String query) throws DatabaseException {
		database.addThemeModeQuery(theme, query);
	}

	@Override
	public void removeThemeModeQuery(String theme, String query) throws DatabaseException {
		database.removeThemeModeQuery(theme, query);
	}

	@Override
	public Map<String, IntArray> getThemeToProblems(Map<String, List<String>> themeAndQueryStrings)
			throws DatabaseException {
		return database.getThemeToProblems(themeAndQueryStrings);
	}

	// //////////////////////////////////////////////////////////////////////////////
	// テーマモードランキング

	private static class ThemeRankingKey {
		private static final int ALL = Integer.MAX_VALUE;
		private static final int OLD = Integer.MIN_VALUE;
		private final String theme;
		private final int year;
		private final int month;

		public ThemeRankingKey(String theme, int year, int month) {
			this.theme = theme;
			this.year = year;
			this.month = month;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(theme, year, month);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ThemeRankingKey)) {
				return false;
			}
			ThemeRankingKey rh = (ThemeRankingKey) obj;
			return Objects.equal(theme, rh.theme) && year == rh.year && year == rh.month;
		}
	}

	private final LoadingCache<ThemeRankingKey, List<PacketRankingData>> themeRankingCache = CacheBuilder
			.newBuilder().recordStats().expireAfterWrite(1, TimeUnit.HOURS).softValues()
			.build(new CacheLoader<ThemeRankingKey, List<PacketRankingData>>() {
				@Override
				public List<PacketRankingData> load(ThemeRankingKey key) throws Exception {
					if (key.year == ThemeRankingKey.ALL && key.month == ThemeRankingKey.ALL) {
						return database.getThemeRankingAll(key.theme);
					} else if (key.year == ThemeRankingKey.OLD && key.month == ThemeRankingKey.OLD) {
						return database.getThemeRankingOld(key.theme);
					} else if (key.month == ThemeRankingKey.ALL) {
						return database.getThemeRanking(key.theme, key.year);
					} else {
						return database.getThemeRanking(key.theme, key.year, key.month);
					}
				}
			});

	@Override
	public List<PacketRankingData> getThemeRanking(String theme, int year) throws DatabaseException {
		try {
			return themeRankingCache.get(new ThemeRankingKey(theme, year, ThemeRankingKey.ALL));
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public List<PacketRankingData> getThemeRanking(String theme, int year, int month)
			throws DatabaseException {
		try {
			return themeRankingCache.get(new ThemeRankingKey(theme, year, month));
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public List<PacketRankingData> getThemeRankingAll(String theme) throws DatabaseException {
		try {
			return themeRankingCache.get(new ThemeRankingKey(theme, ThemeRankingKey.ALL,
					ThemeRankingKey.ALL));
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public List<PacketRankingData> getThemeRankingOld(String theme) throws DatabaseException {
		try {
			return themeRankingCache.get(new ThemeRankingKey(theme, ThemeRankingKey.OLD,
					ThemeRankingKey.OLD));
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	private final LoadingCache<Object, List<PacketMonth>> themeRankingDateRangesCache = CacheBuilder
			.newBuilder().concurrencyLevel(1).maximumSize(1).recordStats()
			.refreshAfterWrite(1, TimeUnit.HOURS)
			.build(new CacheLoader<Object, List<PacketMonth>>() {
				@Override
				public List<PacketMonth> load(Object key) throws Exception {
					return database.getThemeRankingDateRanges();
				}
			});

	@Override
	public List<PacketMonth> getThemeRankingDateRanges() throws DatabaseException {
		try {
			return themeRankingDateRangesCache.get(STATIC_KEY);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////
	// 最新の投稿問題
	private final LoadingCache<Object, List<PacketProblem>> lastestProblemsCache = CacheBuilder
			.newBuilder().recordStats().expireAfterWrite(1, TimeUnit.HOURS).concurrencyLevel(1)
			.build(new CacheLoader<Object, List<PacketProblem>>() {
				@Override
				public List<PacketProblem> load(Object arg0) throws Exception {
					return database.getLastestProblems();
				}
			});

	@Override
	public List<PacketProblem> getLastestProblems() throws DatabaseException {
		try {
			return lastestProblemsCache.get(STATIC_KEY);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////
	// テーマモード編集者
	private final LoadingCache<Object, List<PacketThemeModeEditor>> themeModeEditorsCache = CacheBuilder
			.newBuilder().recordStats().expireAfterWrite(1, TimeUnit.HOURS).concurrencyLevel(1)
			.build(new CacheLoader<Object, List<PacketThemeModeEditor>>() {
				@Override
				public List<PacketThemeModeEditor> load(Object arg0) throws Exception {
					return database.getThemeModeEditors();
				}
			});

	@Override
	public List<PacketThemeModeEditor> getThemeModeEditors() throws DatabaseException {
		try {
			return themeModeEditorsCache.get(STATIC_KEY);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public ThemeModeEditorStatus getThemeModeEditorsStatus(int userCode) throws DatabaseException {
		return database.getThemeModeEditorsStatus(userCode);
	}

	@Override
	public void updateThemeModeEdtorsStatus(int userCode, ThemeModeEditorStatus status)
			throws DatabaseException {
		database.updateThemeModeEdtorsStatus(userCode, status);
		themeModeEditorsCache.invalidateAll();
	}

	// //////////////////////////////////////////////////////////////////////////////
	// パスワード
	private final LoadingCache<String, String> passwordCache = build("passwordCache",
			new CacheLoader<String, String>() {
				@Override
				public String load(String key) throws Exception {
					return database.getPassword(key);
				}
			});

	@Override
	public String getPassword(String type) throws DatabaseException {
		try {
			return passwordCache.get(type);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////
	// 問題
	private final LoadingCache<Object, List<PacketProblemMinimum>> problemMinimums = CacheBuilder
			.newBuilder().concurrencyLevel(1)
			.build(new CacheLoader<Object, List<PacketProblemMinimum>>() {
				@Override
				public List<PacketProblemMinimum> load(Object arg0) throws Exception {
					final List<PacketProblemMinimum> problemMinimums = Lists.newArrayList();
					problemMinimums.add(new PacketProblemMinimum());
					// databaseのprocessProblems()を使用しないと無限再帰となってしまう
					database.processProblemMinimums(new ProblemMinimumProcessable() {
						@Override
						public void process(PacketProblemMinimum problem) throws Exception {
							problemMinimums.add(problem);
						}
					});

					Collections.sort(problemMinimums, new Comparator<PacketProblemMinimum>() {
						@Override
						public int compare(PacketProblemMinimum o1, PacketProblemMinimum o2) {
							return o1.id - o2.id;
						}
					});

					return problemMinimums;
				}
			});

	private List<PacketProblemMinimum> getProblemMinimums() throws DatabaseException {
		try {
			return problemMinimums.get(STATIC_KEY);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public int addProblem(PacketProblem data) throws DatabaseException {
		synchronized (problemMinimums) {
			data.id = database.addProblem(data);
			List<PacketProblemMinimum> minimums = getProblemMinimums();
			Preconditions.checkState(data.id <= minimums.size() && minimums.size() <= data.id + 1,
					"|minimums|=%d data.id=%d", minimums.size(), data.id);
			if (minimums.size() == data.id) {
				minimums.add(data.asMinimum());
			} else {
				minimums.set(data.id, data.asMinimum());
			}
			return data.id;
		}
	}

	@Override
	public List<PacketProblem> getProblem(Collection<Integer> ids) throws DatabaseException {
		return database.getProblem(ids);
	}

	@Override
	public PacketProblemMinimum getProblemMinimum(int problemId) throws DatabaseException {
		synchronized (problemMinimums) {
			return getProblemMinimums().get(problemId);
		}
	}

	@Override
	public void updateMinimumProblem(PacketProblemMinimum data) throws DatabaseException {
		synchronized (problemMinimums) {
			database.updateMinimumProblem(data);
			getProblemMinimums().set(data.id, data);
		}
	}

	@Override
	public void updateProblem(PacketProblem data) throws DatabaseException {
		synchronized (problemMinimums) {
			database.updateProblem(data);
			getProblemMinimums().set(data.id, data);
		}
	}

	@Override
	public void processProblems(ProblemProcessable processor) throws DatabaseException {
		database.processProblems(processor);
	}

	@Override
	public void processProblemMinimums(ProblemMinimumProcessable processer)
			throws DatabaseException {
		database.processProblemMinimums(processer);
	}

	@Override
	public List<PacketProblem> searchProblem(String query, String creator,
			boolean creatorPerfectMatching, Set<ProblemGenre> genres, Set<ProblemType> types,
			Set<RandomFlag> randomFlags) throws DatabaseException {
		return database.searchProblem(query, creator, creatorPerfectMatching, genres, types,
				randomFlags);
	}

	@Override
	public List<PacketProblem> searchSimilarProblemFromDatabase(PacketProblem problem)
			throws DatabaseException {
		return database.searchSimilarProblemFromDatabase(problem);
	}

	@Override
	public List<PacketProblem> getAdsenseProblems(String query) throws DatabaseException {
		return database.getAdsenseProblems(query);
	}

	// ////////////////////////////////////////////////////////////////////////
	// テーマモード編集ログ
	@Override
	public void addThemeModeEditLog(PacketThemeModeEditLog log) throws DatabaseException {
		database.addThemeModeEditLog(log);
	}

	@Override
	public List<PacketThemeModeEditLog> getThemeModeEditLog(int start, int length)
			throws DatabaseException {
		return database.getThemeModeEditLog(start, length);
	}

	@Override
	public int getNumberOfThemeModeEditLog() throws DatabaseException {
		return database.getNumberOfThemeModeEditLog();
	}

	// //////////////////////////////////////////////////////////////////////////////
	// 制限ユーザーコード
	// //////////////////////////////////////////////////////////////////////////////
	private final LoadingCache<RestrictionType, Set<Integer>> restrictedUserCodeCache = build(
			"restrictedUserCode", new CacheLoader<RestrictionType, Set<Integer>>() {
				@Override
				public Set<Integer> load(RestrictionType key) throws Exception {
					return database.getRestrictedUserCodes(key);
				}
			});
	private final LoadingCache<RestrictionType, Set<String>> restrictedRemoteAddressCache = build(
			"restrictedRemoteAddress", new CacheLoader<RestrictionType, Set<String>>() {
				@Override
				public Set<String> load(RestrictionType key) throws Exception {
					return database.getRestrictedRemoteAddresses(key);
				}
			});

	@Override
	public void addRestrictedUserCode(int userCode, RestrictionType restrictionType)
			throws DatabaseException {
		database.addRestrictedUserCode(userCode, restrictionType);
		restrictedUserCodeCache.invalidate(restrictionType);
	}

	@Override
	public void removeRestrictedUserCode(int userCode, RestrictionType restrictionType)
			throws DatabaseException {
		database.removeRestrictedUserCode(userCode, restrictionType);
		restrictedUserCodeCache.invalidate(restrictionType);
	}

	@Override
	public Set<Integer> getRestrictedUserCodes(RestrictionType restrictionType)
			throws DatabaseException {
		try {
			return restrictedUserCodeCache.get(restrictionType);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void clearRestrictedUserCodes(RestrictionType restrictionType) throws DatabaseException {
		database.clearRestrictedUserCodes(restrictionType);
		restrictedUserCodeCache.invalidateAll();
	}

	@Override
	public void addRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType)
			throws DatabaseException {
		database.addRestrictedRemoteAddress(remoteAddress, restrictionType);
		restrictedRemoteAddressCache.invalidate(restrictionType);

	}

	@Override
	public void removeRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType)
			throws DatabaseException {
		database.removeRestrictedRemoteAddress(remoteAddress, restrictionType);
		restrictedRemoteAddressCache.invalidate(restrictionType);
	}

	@Override
	public Set<String> getRestrictedRemoteAddresses(RestrictionType restrictionType)
			throws DatabaseException {
		try {
			return restrictedRemoteAddressCache.get(restrictionType);
		} catch (ExecutionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void clearRestrictedRemoteAddresses(RestrictionType restrictionType)
			throws DatabaseException {
		database.clearRestrictedRemoteAddresses(restrictionType);
		restrictedRemoteAddressCache.invalidate(restrictionType);
	}

	@VisibleForTesting
	void clearCache() {
		userDataCache.invalidateAll();
		rankingDataCache.invalidateAll();
		serverIgnoreUserCodeCache.invalidateAll();
		bbsResponseCache.invalidateAll();
		linkCache.invalidateAll();
		numberOfLinkDataCache.invalidateAll();
		numberOfActiveUsersCache.invalidateAll();
		themeRankingCache.invalidateAll();
		themeRankingDateRangesCache.invalidateAll();
		lastestProblemsCache.invalidateAll();
		themeModeEditorsCache.invalidateAll();
		passwordCache.invalidateAll();
		restrictedUserCodeCache.invalidateAll();
		restrictedRemoteAddressCache.invalidateAll();
		// 速度向上の為クリアしない
		// problemMinimums = null;
	}

	@Override
	public Map<Integer, Integer> getUserCodeToIndicatedProblems() throws DatabaseException {
		return database.getUserCodeToIndicatedProblems();
	}

}
