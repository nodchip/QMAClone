package tv.dyndns.kishibe.qmaclone.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Inject;

public class BadUserDetector implements Runnable {

  @VisibleForTesting
  static class SessionIdAndUserCode {
    private final int sessionId;
    private final int userCode;

    public SessionIdAndUserCode(int sessionId, int userCode) {
      this.sessionId = sessionId;
      this.userCode = userCode;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(sessionId, userCode);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SessionIdAndUserCode)) {
        return false;
      }
      SessionIdAndUserCode rh = (SessionIdAndUserCode) obj;
      return Objects.equal(sessionId, rh.sessionId) && Objects.equal(userCode, rh.userCode);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("sessionId", sessionId).add("userCode", userCode)
          .toString();
    }
  }

  private static final Logger logger = Logger.getLogger(BadUserDetector.class.getName());
  private static final FilenameFilter LOG_FILENAME_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.startsWith("catalina.") && name.endsWith(".log");
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

    Set<Integer> userCodes = Sets.newHashSet();
    Set<SessionIdAndUserCode> startLogs = Sets.newHashSet();
    Set<SessionIdAndUserCode> finishLogs = Sets.newHashSet();
    Map<Integer, List<Double>> userCodeToResponseTimes = Maps.newHashMap();
    Map<SessionIdAndUserCode, Double> sessionIdAndUserCodeToTimeUpCount = Maps.newHashMap();

    // ゲーム開始回数とゲーム終了回数をカウントする
    // ゲーム開始カウントは2人以上でゲームを開始した場合
    // ゲーム終了カウントは常時
    for (File file : new File("/var/log/tomcat7").listFiles(LOG_FILENAME_FILTER)) {
      System.out.println(file);
      String body;
      try {
        body = Files.toString(file, Charsets.UTF_8);
      } catch (IOException e) {
        logger.log(Level.WARNING, "ログファイルの読み込みに失敗しました", e);
        return;
      }
      extractStartAndFinish(body, startLogs, finishLogs, userCodeToResponseTimes,
          sessionIdAndUserCodeToTimeUpCount, userCodes);
    }

    // ユーザーコード
    Multiset<Integer> startCounts = HashMultiset.create();
    Multiset<Integer> finishCounts = HashMultiset.create();
    for (SessionIdAndUserCode log : startLogs) {
      startCounts.add(log.userCode);
      if (finishLogs.contains(log)) {
        finishCounts.add(log.userCode);
      }
    }

    // Map<Integer, List<Double>> userCodeToTimeUpCounts = Maps.newHashMap();
    // for (Entry<SessionIdAndUserCode, Double> entry : sessionIdAndUserCodeToTimeUpCount
    // .entrySet()) {
    // int userCode = entry.getKey().userCode;
    // if (!userCodeToTimeUpCounts.containsKey(userCode)) {
    // userCodeToTimeUpCounts.put(userCode, new ArrayList<Double>());
    // }
    // userCodeToTimeUpCounts.get(userCode).add(entry.getValue());
    // }

    // System.out.println("userCode\t" + "startCount\t" + "finishCount\t" + "dropRatio\t"
    // + "responseTimeGeometricMean\t" + "responseTimeMax\t" + "responseTimeMean\t"
    // + "responseTimeMin\t" + "responseTimePopulationVariance\t"
    // + "responseTimeVariance\t" + "timeUpCountGeometricMean\t" + "timeUpCountMax\t"
    // + "timeUpCountMean\t" + "timeUpCountMin\t" + "timeUpCountPopulationVariance\t"
    // + "timeUpCountVariance");
    // for (int userCode : userCodes) {
    // double startCount = (double) Objects.firstNonNull(startCounts.count(userCode), 0);
    // double finishCount = (double) Objects.firstNonNull(finishCounts.count(userCode), 0);
    // double dropRatio = (finishCount + 1) / (startCount + 1);
    // double[] responseTimes = Doubles.toArray(Objects.firstNonNull(
    // userCodeToResponseTimes.get(userCode), ImmutableList.<Double> of(0.0)));
    // double responseTimeGeometricMean = StatUtils.geometricMean(responseTimes);
    // double responseTimeMax = StatUtils.max(responseTimes);
    // double responseTimeMean = StatUtils.mean(responseTimes);
    // double responseTimeMin = StatUtils.min(responseTimes);
    // double responseTimePopulationVariance = StatUtils.populationVariance(responseTimes);
    // double responseTimeVariance = StatUtils.variance(responseTimes);
    // double[] timeUpCounts = Doubles.toArray(Objects.firstNonNull(
    // userCodeToTimeUpCounts.get(userCode), ImmutableList.<Double> of(0.0)));
    // double timeUpCountGeometricMean = StatUtils.geometricMean(timeUpCounts);
    // double timeUpCountMax = StatUtils.max(timeUpCounts);
    // double timeUpCountMean = StatUtils.mean(timeUpCounts);
    // double timeUpCountMin = StatUtils.min(timeUpCounts);
    // double timeUpCountPopulationVariance = StatUtils.populationVariance(timeUpCounts);
    // double timeUpCountVariance = StatUtils.variance(timeUpCounts);
    // System.out.printf("%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\n",
    // userCode, startCount, finishCount, dropRatio, responseTimeGeometricMean,
    // responseTimeMax, responseTimeMean, responseTimeMin,
    // responseTimePopulationVariance, responseTimeVariance, timeUpCountGeometricMean,
    // timeUpCountMax, timeUpCountMean, timeUpCountMin, timeUpCountPopulationVariance,
    // timeUpCountVariance);
    // }

    List<List<PacketRankingData>> rankingData;
    try {
      rankingData = database.getGeneralRankingData();
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "ランキングの読み込みに失敗しました", e);
      return;
    }

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

      String message = String.format("プレイ中断回数が一定値を超えたため制限ユーザーに追加しました: "
          + "user=%s startCount=%d finishCount=%d", user.toString(), startCount, finishCount);
      logger.log(Level.INFO, message);
    }

    logger.info("悪質な対戦ユーザーの追加が完了しました");
  }

  @VisibleForTesting
  void extractStartAndFinish(String body, Set<SessionIdAndUserCode> startLogs,
      Set<SessionIdAndUserCode> finishLogs, Map<Integer, List<Double>> userCodeToResponseTimes,
      Map<SessionIdAndUserCode, Double> sessionIdAndUserCodeToTimeUpCount, Set<Integer> userCodes) {
    Matcher gameStartMatcher = GAME_START_PATTERN.matcher(body);
    while (gameStartMatcher.find()) {
      int sessionId = Integer.valueOf(gameStartMatcher.group(1));
      int userCode = Integer.valueOf(gameStartMatcher.group(2));
      startLogs.add(new SessionIdAndUserCode(sessionId, userCode));
      userCodes.add(userCode);
    }

    Matcher gameFinishedMatcher = GAME_FINISHED_PATTERN.matcher(body);
    while (gameFinishedMatcher.find()) {
      int userCode = Integer.valueOf(gameFinishedMatcher.group(1));
      int sessionId = Integer.valueOf(gameFinishedMatcher.group(2));
      finishLogs.add(new SessionIdAndUserCode(sessionId, userCode));
      userCodes.add(userCode);
    }

    Matcher sendAnswerResponseTimeMatcher = SEND_ANSWER_RESPONSE_TIME_PATTERN.matcher(body);
    while (sendAnswerResponseTimeMatcher.find()) {
      int userCode = Integer.valueOf(sendAnswerResponseTimeMatcher.group(1));
      double responseTime = Integer.valueOf(sendAnswerResponseTimeMatcher.group(2));
      if (!userCodeToResponseTimes.containsKey(userCode)) {
        userCodeToResponseTimes.put(userCode, new ArrayList<Double>());
      }
      userCodeToResponseTimes.get(userCode).add(responseTime);
      userCodes.add(userCode);
    }

    Matcher notifyTimeUpMatcher = NOTIFY_TIME_UP_PATTERN.matcher(body);
    while (notifyTimeUpMatcher.find()) {
      int sessionId = Integer.valueOf(notifyTimeUpMatcher.group(1));
      int userCode = Integer.valueOf(notifyTimeUpMatcher.group(2));
      SessionIdAndUserCode sessionIdAndUserCode = new SessionIdAndUserCode(sessionId, userCode);
      if (!sessionIdAndUserCodeToTimeUpCount.containsKey(sessionIdAndUserCode)) {
        sessionIdAndUserCodeToTimeUpCount.put(sessionIdAndUserCode, 0.0);
      }
      sessionIdAndUserCodeToTimeUpCount.put(sessionIdAndUserCode,
          sessionIdAndUserCodeToTimeUpCount.get(sessionIdAndUserCode) + 1);
      userCodes.add(userCode);
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
