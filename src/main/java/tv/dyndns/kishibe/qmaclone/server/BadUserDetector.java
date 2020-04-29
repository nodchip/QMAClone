package tv.dyndns.kishibe.qmaclone.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gwt.thirdparty.guava.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

public class BadUserDetector implements Runnable {
	private static final Logger logger = Logger.getLogger(BadUserDetector.class.getName());
	private static final FilenameFilter LOG_FILENAME_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith("catalina.out") && !name.endsWith(".gz");
		}
	};
	private static final Pattern GAME_START_PATTERN = Pattern
			.compile("\\{method=transitFromMachingToReady, sessionId=([0-9]+?), userCode=([0-9]+?)\\}");
	private static final Pattern GAME_FINISHED_PATTERN = Pattern
			.compile("\\{method=notifyGameFinished, userCode=([0-9]+?), sessionId=([0-9]+?),");
	private static final Pattern SEND_ANSWER_RESPONSE_TIME_PATTERN = Pattern
			.compile("\\{method=sendAnswer, .+?userCode=([0-9]+?), responseTime=([0-9]+?),");
	private static final Pattern NOTIFY_TIME_UP_PATTERN = Pattern
			.compile("\\{method=notifyTimeUp, sessionId=([0-9]+?), .+?userCode=([0-9]+?),");
	private static final int MAX_INDICATED_PROBLEM_PER_USER = 10;
	private final Database database;

	@Inject
	public BadUserDetector(Database database) {
		this.database = Preconditions.checkNotNull(database);
	}

	@Override
	public void run() {
		detectBadMatchPlayer();
		try {
			detectBadProblemCreator();
		} catch (DatabaseException e) {
			logger.log(Level.WARNING, "悪質な問題作成者の判定に失敗しました。", e);
		}
	}

	private void detectBadMatchPlayer() {
		logger.info("悪質な対戦ユーザーを追加中です");

		Map<Integer, Set<Integer>> startLogs = Maps.newHashMap();
		Map<Integer, Set<Integer>> finishLogs = Maps.newHashMap();

		// ゲーム開始回数とゲーム終了回数をカウントする
		// ゲーム開始カウントは2人以上でゲームを開始した場合
		// ゲーム終了カウントは常時
		for (File file : new File("/var/log/tomcat9").listFiles(LOG_FILENAME_FILTER)) {
			System.out.println(file);
			String body;
			try {
				body = Files.asCharSource(file, Charsets.UTF_8).read();
			} catch (IOException e) {
				logger.log(Level.WARNING, "ログファイルの読み込みに失敗しました", e);
				return;
			}
			extractStartAndFinish(body, startLogs, finishLogs);
		}

		// 2名以上のプレイヤーで開始したゲームについて
		// そこにいたプレイヤーのユーザーコードを抽出する
		Multiset<Integer> startCounts = HashMultiset.create();
		Multiset<Integer> finishCounts = HashMultiset.create();
		for (Map.Entry<Integer, Set<Integer>> entry : startLogs.entrySet()) {
			Set<Integer> startedUserId = entry.getValue();
			if (startedUserId.size() == 1) {
				continue;
			}
			startCounts.addAll(startedUserId);

			int sessionId = entry.getKey();
			finishCounts.addAll(finishLogs.getOrDefault(sessionId, ImmutableSet.of()));
		}

		// ランキング上位100名を取得する
		List<List<PacketRankingData>> rankingData;
		try {
			rankingData = database.getGeneralRankingData();
		} catch (DatabaseException e) {
			logger.log(Level.WARNING, "ランキングの読み込みに失敗しました", e);
			return;
		}

		// ランキング上位100名について処理する
		for (PacketRankingData user : rankingData.get(3)) {
			int userCode = user.userCode;
			int startCount = startCounts.count(userCode);
			int finishCount = finishCounts.count(userCode);

			// プレイ開始回数が一定回数以下なら何もしない。
			if (startCount < 10) {
				continue;
			}

			// プレイ終了回数がプレイ開始回数に比べて十分に大きい場合は何もしない。
			if (startCount <= finishCount * 2) {
				continue;
			}

			try {
				database.addRestrictedUserCode(userCode, RestrictionType.MATCH);
			} catch (DatabaseException e) {
				logger.log(Level.WARNING, "制限ユーザー(MATCH)の追加に失敗しました", e);
				return;
			}

			String message = String.format("プレイ中断回数が一定値を超えたため制限ユーザーに追加しました: " + "user=%s startCount=%d finishCount=%d",
					user.toString(), startCount, finishCount);
			logger.log(Level.INFO, message);
		}

		logger.info("悪質な対戦ユーザーの追加が完了しました");
	}

	/**
	 * ログからユーザーの行動を抽出する
	 * 
	 * @param body       ログファイルコンテンツ
	 * @param startLogs  各ゲームを開始したユーザー sessionId -> [userCode]
	 * @param finishLogs 各ゲームを最後までプレイしたユーザー sessionId -> [userCode]
	 */
	@VisibleForTesting
	void extractStartAndFinish(String body, Map<Integer, Set<Integer>> startLogs,
			Map<Integer, Set<Integer>> finishLogs) {
		Matcher gameStartMatcher = GAME_START_PATTERN.matcher(body);
		while (gameStartMatcher.find()) {
			int sessionId = Integer.valueOf(gameStartMatcher.group(1));
			int userCode = Integer.valueOf(gameStartMatcher.group(2));
			if (!startLogs.containsKey(sessionId)) {
				startLogs.put(sessionId, Sets.newHashSet());
			}
			startLogs.get(sessionId).add(userCode);
		}

		Matcher gameFinishedMatcher = GAME_FINISHED_PATTERN.matcher(body);
		while (gameFinishedMatcher.find()) {
			int userCode = Integer.valueOf(gameFinishedMatcher.group(1));
			int sessionId = Integer.valueOf(gameFinishedMatcher.group(2));
			if (!finishLogs.containsKey(sessionId)) {
				finishLogs.put(sessionId, Sets.newHashSet());
			}
			finishLogs.get(sessionId).add(userCode);
		}
	}

	@VisibleForTesting
	void detectBadProblemCreator() throws DatabaseException {
		logger.info("悪質な問題投稿ユーザーを追加中です");

		// database.clearRestrictedUserCodes(RestrictionType.PROBLEM_SUBMITTION);
		for (Entry<Integer, Integer> entry : database.getUserCodeToIndicatedProblems().entrySet()) {
			int userCode = entry.getKey();
			int count = entry.getValue();
			if (count >= MAX_INDICATED_PROBLEM_PER_USER) {
				String message = String.format("問題投稿制限ユーザーを追加しました: userCode=%d count=%d", userCode, count);
				logger.info(message);
				database.addRestrictedUserCode(userCode, RestrictionType.PROBLEM_SUBMITTION);
			}
		}

		logger.info("悪質な問題投稿ユーザーの追加が完了しました");
	}

	public static void main(String[] args) throws Exception {
		Guice.createInjector(new QMACloneModule()).getInstance(BadUserDetector.class).run();
	}

}
