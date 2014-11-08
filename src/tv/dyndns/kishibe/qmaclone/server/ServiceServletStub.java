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
package tv.dyndns.kishibe.qmaclone.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsResponse;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsThread;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLogin;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMonth;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRatingDistribution;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketReadyForGame;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRegistrationData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketResult;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKey;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.packet.ProblemIndicationEligibility;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.client.service.ServiceException;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.handwriting.Recognizable;
import tv.dyndns.kishibe.qmaclone.server.image.BrokenImageLinkDetector;
import tv.dyndns.kishibe.qmaclone.server.service.DatabaseAccessible;
import tv.dyndns.kishibe.qmaclone.server.sns.SnsClient;
import tv.dyndns.kishibe.qmaclone.server.sns.SnsClients;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;
import tv.dyndns.kishibe.qmaclone.server.util.diff_match_patch;
import tv.dyndns.kishibe.qmaclone.server.util.diff_match_patch.Diff;
import tv.dyndns.kishibe.qmaclone.server.websocket.WebSocketServer;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

@SuppressWarnings("serial")
public class ServiceServletStub extends RemoteServiceServlet implements Service {
  private static final Logger logger = Logger.getLogger(ServiceServletStub.class.toString());
  private static final File PROBLEM_CREATION_LOG_FILE = new File("/tmp/qmaclone/problem.log");
  private static final Set<String> LOGGING_EXCLUDED_METHODS = ImmutableSet.of("keepAlive",
      "getServerStatus", "getGameStatus", "keepAliveGame", "waitForGame", "receiveMessageFromChat");
  private final Random random = new Random();
  private final ChatManager chatManager;
  private final NormalModeProblemManager normalModeProblemManager;
  private final ThemeModeProblemManager themeModeProblemManager;;
  private final GameManager gameManager;
  private final ServerStatusManager serverStatusManager;
  private final PlayerHistoryManager playerHistoryManager;
  private final VoteManager voteManager;
  private final Recognizable recognizer;
  private final ThemeModeEditorManager themeModeEditorManager;
  private final WebSocketServer webSocketServer;
  private final Database database;
  private final PrefectureRanking prefectureRanking;
  private final RatingDistribution ratingDistribution;
  private final SnsClient snsClient;
  private final GameLogger gameLogger;
  private final ThreadPool threadPool;
  private final RestrictedUserUtils restrictedUserUtils;
  private final ProblemCorrectCounterResetCounter problemCorrectCounterResetCounter;
  private final ProblemIndicationCounter problemIndicationCounter;
  private final BrokenImageLinkDetector brokenImageLinkDetector;

  /**
   * Only for testing.
   */
  public ServiceServletStub() {
    Injector injector = Guice.createInjector(new QMACloneModule());
    this.chatManager = injector.getInstance(ChatManager.class);
    this.normalModeProblemManager = injector.getInstance(NormalModeProblemManager.class);
    this.themeModeProblemManager = injector.getInstance(ThemeModeProblemManager.class);
    this.gameManager = injector.getInstance(GameManager.class);
    this.serverStatusManager = injector.getInstance(ServerStatusManager.class);
    this.playerHistoryManager = injector.getInstance(PlayerHistoryManager.class);
    this.voteManager = injector.getInstance(VoteManager.class);
    this.recognizer = injector.getInstance(Recognizable.class);
    this.themeModeEditorManager = injector.getInstance(ThemeModeEditorManager.class);
    this.webSocketServer = injector.getInstance(WebSocketServer.class);
    this.database = injector.getInstance(Database.class);
    this.prefectureRanking = injector.getInstance(PrefectureRanking.class);
    this.ratingDistribution = injector.getInstance(RatingDistribution.class);
    this.snsClient = injector.getInstance(SnsClients.class);
    this.gameLogger = injector.getInstance(GameLogger.class);
    this.threadPool = injector.getInstance(ThreadPool.class);
    this.restrictedUserUtils = injector.getInstance(RestrictedUserUtils.class);
    this.problemCorrectCounterResetCounter = injector
        .getInstance(ProblemCorrectCounterResetCounter.class);
    this.problemIndicationCounter = injector.getInstance(ProblemIndicationCounter.class);
    this.brokenImageLinkDetector = injector.getInstance(BrokenImageLinkDetector.class);
  }

  @Inject
  public ServiceServletStub(ChatManager chatManager,
      NormalModeProblemManager normalModeProblemManager,
      ThemeModeProblemManager themeModeProblemManager, GameManager gameManager,
      ServerStatusManager serverStatusManager, PlayerHistoryManager playerHistoryManager,
      VoteManager voteManager, Recognizable recognizer,
      ThemeModeEditorManager themeModeEditorManager, WebSocketServer webSocketServer,
      Database database, PrefectureRanking prefectureRanking,
      RatingDistribution ratingDistribution, @Named("SnsClients") SnsClient snsClient,
      GameLogger gameLogger, ThreadPool threadPool, BadUserDetector badUserDetector,
      RestrictedUserUtils restrictedUserUtils,
      ProblemCorrectCounterResetCounter problemCorrectCounterResetCounter,
      ProblemIndicationCounter problemIndicationCounter,
      BrokenImageLinkDetector brokenImageLinkDetector) throws SocketException {
    this.chatManager = chatManager;
    this.normalModeProblemManager = normalModeProblemManager;
    this.themeModeProblemManager = themeModeProblemManager;
    this.gameManager = gameManager;
    this.serverStatusManager = serverStatusManager;
    this.playerHistoryManager = playerHistoryManager;
    this.voteManager = voteManager;
    this.recognizer = recognizer;
    this.themeModeEditorManager = themeModeEditorManager;
    this.webSocketServer = webSocketServer;
    this.database = database;
    this.prefectureRanking = prefectureRanking;
    this.ratingDistribution = ratingDistribution;
    this.snsClient = snsClient;
    this.gameLogger = gameLogger;
    this.threadPool = threadPool;
    this.restrictedUserUtils = Preconditions.checkNotNull(restrictedUserUtils);
    this.problemCorrectCounterResetCounter = Preconditions
        .checkNotNull(problemCorrectCounterResetCounter);
    this.problemIndicationCounter = Preconditions.checkNotNull(problemIndicationCounter);
    this.brokenImageLinkDetector = Preconditions.checkNotNull(brokenImageLinkDetector);

    // テーマモードのTwitter通知タイミング調整
    threadPool.scheduleWithFixedDelay(commandUpdateThemeModeNotificationCounter, 1, 1,
        TimeUnit.SECONDS);
    try {
      webSocketServer.start();
    } catch (Exception e) {
      logger.log(Level.WARNING, "WebSocketの開始に失敗しました", e);
    }

    threadPool.addHourTask(badUserDetector);
    threadPool.addHourTask(problemCorrectCounterResetCounter);
    threadPool.addHourTask(problemIndicationCounter);
    threadPool.addDailyTask(brokenImageLinkDetector);
    if (!onDevelopmentMachine()) {
      threadPool.execute(brokenImageLinkDetector);
    }
  }

  private static boolean onDevelopmentMachine() throws SocketException {
    for (NetworkInterface networkInterface : Collections.list(NetworkInterface
        .getNetworkInterfaces())) {
      for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
        if (inetAddress.getHostAddress().contains("192.168.100.5")) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected boolean shouldCompressResponse(HttpServletRequest request,
      HttpServletResponse response, String responsePayload) {
    // nginx側で圧縮するためtomcat側で圧縮しないようにする
    return false;
  }

  @Override
  public String processCall(String payload) {
    // // ロギング
    // RPCRequest rpcRequest = RPC.decodeRequest(payload, getClass(), this);
    // if (!LOGGING_EXCLUDED_METHODS.contains(rpcRequest.getMethod().getName())) {
    // logger.info(getRemoteAddress() + " " + rpcRequest.getMethod());
    // }

    // processCall()から送出される例外を捕捉してログに出力する
    try {
      return super.processCall(payload);
    } catch (Throwable e) {
      String message = "processCall()中にエラーが発生しました: "
          + MoreObjects.toStringHelper(this).add("remoteAddress", getRemoteAddress())
              .add("rpcRequest", RPC.decodeRequest(payload, null, this)).add("payload", payload)
              .toString();
      logger.log(Level.WARNING, message, e);

      try {
        return RPC.encodeResponseForFailure(null,
            new ServiceException(Throwables.getStackTraceAsString(e)));
      } catch (SerializationException e1) {
        logger.log(Level.SEVERE, "エラー情報の返信に失敗しました", e1);
      }
    }

    // フォールバック
    return "";
  }

  @Override
  public void destroy() {
    try {
      webSocketServer.stop();
    } catch (Exception e) {
      logger.log(Level.WARNING, "WebSocketの停止に失敗しました", e);
    }

    threadPool.shutdown();
    super.destroy();
  }

  @Override
  public PacketServerStatus getServerStatus() {
    return serverStatusManager.getServerStatus();
  }

  @Override
  public PacketLogin login(int userCode) {
    serverStatusManager.login();
    serverStatusManager.keepAlive(userCode);
    PacketLogin login = new PacketLogin();
    // login.removeAddress = getRemoteAddress();
    return login;
  }

  @Override
  public void keepAlive(int userCode) {
    serverStatusManager.keepAlive(userCode);
  }

  // プレイヤー情報を登録する
  private Object lockObjectRegister = new Object();

  @Override
  public PacketRegistrationData register(PacketPlayerSummary playerSummary,
      Set<ProblemGenre> genres, Set<ProblemType> types, String greeting, GameMode gameMode,
      String roomName, String theme, String imageFileName, int classLevel, int difficultSelect,
      int rating, int userCode, int volatility, int playCount, NewAndOldProblems newAndOldProblems,
      boolean publicEvent) throws ServiceException {
    synchronized (lockObjectRegister) {
      try {
        gameLogger
            .write(MoreObjects.toStringHelper(this).add("method", "register")
                .add("playerSummary", playerSummary).add("genres", genres).add("types", types)
                .add("greeting", greeting).add("gameMode", gameMode).add("roomName", roomName)
                .add("THEME", theme).add("imageFileName", imageFileName)
                .add("classLevel", classLevel).add("difficultSelect", difficultSelect)
                .add("rating", rating).add("userCode", userCode).add("volatility", volatility)
                .add("playCount", playCount).add("newAndOldProblems", newAndOldProblems)
                .add("publicEvent", publicEvent).toString());

        playerHistoryManager.push(playerSummary);

        PlayerStatus status;
        Game session = gameManager.getOrCreateMatchingSession(gameMode, roomName, classLevel,
            theme, genres, types, publicEvent, serverStatusManager, userCode, getRemoteAddress());
        status = session
            .addPlayer(playerSummary, genres, types, greeting, imageFileName, classLevel,
                difficultSelect, rating, userCode, volatility, playCount, newAndOldProblems);

        PacketRegistrationData data = new PacketRegistrationData();
        data.playerListIndex = status.getPlayerListId();
        data.sessionId = status.getSessionId();
        return data;

      } catch (Exception e) {
        String parameters = MoreObjects.toStringHelper(this).add("playerSummary", playerSummary)
            .add("genres", genres).add("types", types).add("greeting", greeting)
            .add("gameMode", gameMode).add("roomName", roomName).add("THEME", theme)
            .add("imageFileName", imageFileName).add("classLevel", classLevel)
            .add("difficultSelect", difficultSelect).add("rating", rating)
            .add("userCode", userCode).add("volatility", volatility).add("playCount", playCount)
            .add("newAndOldProblems", newAndOldProblems).add("publicEvent", publicEvent).toString();
        logger.log(Level.SEVERE, "プレイヤー登録に失敗しました。" + parameters, e);
        throw new ServiceException(e);
      }
    }
  }

  // マッチング
  @Override
  public PacketMatchingData getMatchingData(int sessionId) {
    Game session = gameManager.getSession(sessionId);
    Preconditions.checkNotNull(session, "ゲームセッションが見つかりませんでした: sessionId=" + sessionId);
    return session.getMatchingData();
  }

  // 強制的にゲームをスタートさせる
  @Override
  public int requestSkip(int sessionId, int playerListId) {
    Game session = gameManager.getSession(sessionId);
    Preconditions.checkNotNull(session, "ゲームセッションが見つかりませんでした: sessionId=" + sessionId);
    session.requestStartingGame(playerListId);
    return session.getNumberOfPlayer();
  }

  // ゲーム開始待機
  // ゲームの開始を待つ
  @Override
  public PacketReadyForGame waitForGame(int sessionId) {
    Game session = gameManager.getSession(sessionId);
    Preconditions.checkNotNull(session, "ゲームセッションが見つかりませんでした: sessionId=" + sessionId);
    return session.getReadyForGameStatus();
  }

  // 問題を取得する
  @Override
  public List<PacketProblem> getProblem(int sessionId) {
    Game session = gameManager.getSession(sessionId);
    Preconditions.checkNotNull(session, "ゲームセッションが見つかりませんでした: sessionId=" + sessionId);
    return session.getProblem();
  }

  // 他のプレイヤーの名前を取得する
  @Override
  public List<PacketPlayerSummary> getPlayerSummaries(int sessionId) {
    Game session = gameManager.getSession(sessionId);
    Preconditions.checkNotNull(session, "ゲームセッションが見つかりませんでした: sessionId=" + sessionId);
    return session.getPlayerSummaries();
  }

  @Override
  public void sendAnswer(int sessionId, int playerListId, String answer, int userCode,
      int responseTime) {
    Game session = gameManager.getSession(sessionId);
    Preconditions.checkNotNull(session, "ゲームセッションが見つかりませんでした: sessionId=" + sessionId);
    session.receiveAnswer(playerListId, answer);
    gameLogger.write(MoreObjects
        .toStringHelper(this)
        .add("method", "sendAnswer")
        .add("sessionId", sessionId)
        .add("playerListId", playerListId)
        .add("answer",
            Arrays.deepToString(Strings.nullToEmpty(answer).split(Constant.DELIMITER_GENERAL)))
        .add("userCode", userCode).add("responseTime", responseTime)
        .add("remoteAddress", getRemoteAddress()).toString());
  }

  @Override
  public void notifyTimeUp(int sessionId, int playerListId, int userCode) {
    gameLogger.write(MoreObjects.toStringHelper(this).add("method", "notifyTimeUp")
        .add("sessionId", sessionId).add("playerListId", playerListId).add("userCode", userCode)
        .add("remoteAddress", getRemoteAddress()).toString());
  }

  @Override
  public void notifyGameFinished(int userCode, int oldRating, int newRating, int sessionId)
      throws ServiceException {
    gameLogger.write(MoreObjects.toStringHelper(this).add("method", "notifyGameFinished")
        .add("userCode", userCode).add("sessionId", sessionId).add("oldRating", oldRating)
        .add("newRating", newRating).add("remoteAddress", getRemoteAddress()).toString());
  }

  // ゲームの進行状態を取得する
  @Override
  public PacketGameStatus getGameStatus(int sessionId) {
    Game session = gameManager.getSession(sessionId);
    Preconditions.checkNotNull(session, "ゲームセッションが見つかりませんでした: sessionId=" + sessionId);
    return session.getGameStatus();
  }

  @Override
  public void keepAliveGame(int sessionId, int playerListId) {
    Game session = gameManager.getSession(sessionId);
    Preconditions.checkNotNull(session, "ゲームセッションが見つかりませんでした: sessionId=" + sessionId);
    session.keepAlive(playerListId);
  }

  // 結果表示
  // 最終結果を取得する
  @Override
  public List<PacketResult> getResult(int sessionId) {
    Game session = gameManager.getSession(sessionId);
    Preconditions.checkNotNull(session, "ゲームセッションが見つかりませんでした: sessionId=" + sessionId);
    return session.getPacketResult();
  }

  // チャットにメッセージを送信する
  @Override
  public void sendMessageToChat(PacketChatMessage chatData) {
    int userCode = chatData.userCode;
    String remoteAddress = getRemoteAddress();
    try {
      restrictedUserUtils.checkAndUpdateRestrictedUser(userCode, remoteAddress,
          RestrictionType.CHAT);
    } catch (DatabaseException e) {
      logger.log(Level.INFO, "制限ユーザーのチェックに失敗しました。処理を続行します。", e);
    }

    // ロックはChatManager側で行う
    chatManager.write(chatData, remoteAddress);
  }

  @Override
  public PacketChatMessages receiveMessageFromChat(int lastestResId) {
    // ロックはChatManager側で行う
    return chatManager.read(lastestResId);
  }

  // 問題を投稿する
  private final Object fileLock = new Object();

  @Override
  public int uploadProblem(final PacketProblem problem, final int userCode, boolean resetAnswerCount)
      throws ServiceException {
    final String remoteAddress = getRemoteAddress();

    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        synchronized (fileLock) {
          try {
            Files.createParentDirs(PROBLEM_CREATION_LOG_FILE);
          } catch (IOException e) {
            logger.log(Level.WARNING, "問題作成ログディレクトリの作成に失敗しました", e);
          }
          try (PrintStream stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(
              PROBLEM_CREATION_LOG_FILE, true)), false, "MS932")) {
            stream.println(problem.toString());
            stream.println(userCode + "\t" + remoteAddress + "\t"
                + Calendar.getInstance().getTime());
          } catch (Exception e) {
            logger.log(Level.WARNING, "問題作成ログの書き込みに失敗しました", e);
          }
        }
      }
    });

    // BugTrack-QMAClone/695 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F695
    try {
      if (restrictedUserUtils.checkAndUpdateRestrictedUser(userCode, remoteAddress,
          RestrictionType.PROBLEM_SUBMITTION)) {
        if (problem.id == -1) {
          return normalModeProblemManager.getNumberOfProblem() + 1;
        } else {
          // #699 (ルール違反プレイヤーの報告と指摘に関する要望) – QMAClone
          // http://kishibe.dyndns.tv/trac/qmaclone/ticket/699
          return problem.id;
        }
      }
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "制限ユーザーの判定に失敗しました");
      throw new ServiceException(e);
    }

    final int problemId;

    if (problem.id == -1) {
      problemId = wrap("問題の追加に失敗しました", new DatabaseAccessible<Integer>() {
        @Override
        public Integer access() throws DatabaseException {
          return normalModeProblemManager.addProblem(problem);
        }
      });
    } else {
      wrap("問題の更新に失敗しました", new DatabaseAccessible<Void>() {
        @Override
        public Void access() throws DatabaseException {
          normalModeProblemManager.updateProblem(problem);
          return null;
        }
      });
      problemId = problem.id;
    }
    problem.id = problemId;

    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          database.addCreationLog(problem, userCode, remoteAddress);
        } catch (DatabaseException e) {
          logger.log(Level.WARNING, "問題作成ログの保存に失敗しました", e);
        }
      }
    });

    // SNSに投稿
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        snsClient.postProblem(problem);
      }
    });

    // BugTrack-QMAClone/591 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F591
    if (resetAnswerCount) {
      problemCorrectCounterResetCounter.add(userCode);
    }

    return problemId;
  }

  // 問題を取得する
  @Override
  public List<PacketProblem> getProblemList(final List<Integer> problemIds) throws ServiceException {
    return wrap("問題リストの取得に失敗しました", new DatabaseAccessible<List<PacketProblem>>() {
      @Override
      public List<PacketProblem> access() throws DatabaseException {
        return database.getProblem(problemIds);
      }
    });
  }

  @Override
  public int[][] getStatisticsOfProblemCount() {
    return normalModeProblemManager.getTableProblemCount();
  }

  @Override
  public int[][] getStatisticsOfAccuracyRate() {
    return normalModeProblemManager.getTableProblemRatio();
  }

  @Override
  public List<PacketProblem> searchProblem(final String query, final String creator,
      final boolean creatorPerfectMatching, final Set<ProblemGenre> genres,
      final Set<ProblemType> types, final Set<RandomFlag> randomFlags) throws ServiceException {
    return wrap("問題の検索に失敗しました", new DatabaseAccessible<List<PacketProblem>>() {
      @Override
      public List<PacketProblem> access() throws DatabaseException {
        List<PacketProblem> problems = database.searchProblem(query, creator,
            creatorPerfectMatching, genres, types, randomFlags);
        ImmutableSet<Integer> usedProblems = ImmutableSet.copyOf(gameManager.getTestingProblemIds());
        for (PacketProblem problem : problems) {
          problem.testing = usedProblems.contains(problem.id);
        }
        return problems;
      }
    });
  }

  @Override
  public List<PacketProblem> searchSimilarProblem(final PacketProblem problem)
      throws ServiceException {
    return wrap("類似問題の検索に失敗しました", new DatabaseAccessible<List<PacketProblem>>() {
      @Override
      public List<PacketProblem> access() throws DatabaseException {
        return database.searchSimilarProblemFromDatabase(problem);
      }
    });
  }

  @Override
  public int getNewUserCode() throws ServiceException {
    return wrap("新規ユーザーコードの作成に失敗しました", new DatabaseAccessible<Integer>() {
      @Override
      public Integer access() throws DatabaseException {
        int userCode;
        do {
          userCode = random.nextInt(100000000);
        } while (database.isUsedUserCode(userCode));
        return userCode;
      }
    });
  }

  @Override
  public void addProblemIdsToReport(final int userCode, final List<Integer> problemIds)
      throws ServiceException {
    wrap("正解率統計への問題登録に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.addProblemIdsToReport(userCode, problemIds);
        return null;
      }
    });
  }

  @Override
  public void removeProblemIDFromReport(final int userCode, final int problemID)
      throws ServiceException {
    wrap("正解率統計からの問題登録解除に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.removeProblemIdFromReport(userCode, problemID);
        return null;
      }
    });
  }

  @Override
  public void clearProblemIDFromReport(final int userCode) throws ServiceException {
    wrap("正解率統計のクリアに失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.clearProblemIdFromReport(userCode);
        return null;
      }
    });
  }

  @Override
  public List<PacketProblem> getUserProblemReport(final int userCode) throws ServiceException {
    return wrap("正解率統計の読み込みに失敗しました", new DatabaseAccessible<List<PacketProblem>>() {
      @Override
      public List<PacketProblem> access() throws DatabaseException {
        return database.getUserProblemReport(userCode);
      }
    });
  }

  @Override
  public PacketUserData loadUserData(final int userCode) throws ServiceException {
    return wrap("ユーザーデータの読み込みに失敗しました", new DatabaseAccessible<PacketUserData>() {
      @Override
      public PacketUserData access() throws DatabaseException {
        return database.getUserData(userCode);
      }
    });
  }

  @Override
  public void saveUserData(final PacketUserData userData) throws ServiceException {
    wrap("ユーザーデータの保存に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.setUserData(userData);
        return null;
      }
    });
  }

  @Override
  public List<PacketUserData> getLoginUsers() {
    return serverStatusManager.getLoginUsers();
  }

  @Override
  public List<PacketWrongAnswer> getWrongAnswers(final int problemID) throws ServiceException {
    return wrap("プレイヤー解答の読み込みに失敗しました", new DatabaseAccessible<List<PacketWrongAnswer>>() {
      @Override
      public List<PacketWrongAnswer> access() throws DatabaseException {
        return database.getPlayerAnswers(problemID);
      }
    });
  }

  @Override
  public void removePlayerAnswers(final int problemID) throws ServiceException {
    wrap("プレイヤー解答の削除に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.removePlayerAnswers(problemID);
        return null;
      }
    });
  }

  @Override
  public List<List<PacketRankingData>> getGeneralRanking() throws ServiceException {
    return wrap("ランキングデータの読み込みに失敗しました", new DatabaseAccessible<List<List<PacketRankingData>>>() {
      @Override
      public List<List<PacketRankingData>> access() throws DatabaseException {
        return database.getGeneralRankingData();
      }
    });
  }

  @Override
  public void addIgnoreUserCode(int userCode, int targetUserCode) throws ServiceException {
    try {
      database.addIgnoreUserCode(userCode, targetUserCode);
    } catch (DatabaseException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void removeIgnoreUserCode(int userCode, int targetUserCode) throws ServiceException {
    try {
      database.removeIgnoreUserCode(userCode, targetUserCode);
    } catch (DatabaseException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public List<PacketProblemCreationLog> getProblemCreationLog(final int problemId)
      throws ServiceException {
    return wrap("問題作成ログの取得に失敗しました", new DatabaseAccessible<List<PacketProblemCreationLog>>() {
      @Override
      public List<PacketProblemCreationLog> access() throws DatabaseException {
        return database.getProblemCreationHistory(problemId);
      }
    });
  }

  @Override
  public List<PacketBbsResponse> getBbsResponses(final int threadId, final int count)
      throws ServiceException {
    return wrap("BBSレスポンスの取得に失敗しました", new DatabaseAccessible<List<PacketBbsResponse>>() {
      @Override
      public List<PacketBbsResponse> access() throws DatabaseException {
        return database.getBbsResponses(threadId, count);
      }
    });
  }

  @Override
  public List<PacketBbsThread> getBbsThreads(final int bbsId, final int start, final int count)
      throws ServiceException {
    return wrap("BBSスレッドの取得に失敗しました", new DatabaseAccessible<List<PacketBbsThread>>() {
      @Override
      public List<PacketBbsThread> access() throws DatabaseException {
        return database.getBbsThreads(bbsId, start, count);
      }
    });
  }

  @Override
  public void buildBbsThread(final int bbsId, final PacketBbsThread thread,
      final PacketBbsResponse response) throws ServiceException {
    response.remoteAddress = getRemoteAddress();

    int userCode = response.userCode;
    String remoteAddress = response.remoteAddress;
    try {
      if (restrictedUserUtils.checkAndUpdateRestrictedUser(userCode, remoteAddress,
          RestrictionType.BBS)) {
        return;
      }
    } catch (DatabaseException e) {
      logger.log(Level.INFO, "制限ユーザーのチェックに失敗しました。処理を続行します。", e);
    }

    wrap("BBSスレッドの設置に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.buildBbsThread(bbsId, thread, response);
        return null;
      }
    });
  }

  @Override
  public void writeToBbs(final PacketBbsResponse response, final boolean age)
      throws ServiceException {
    response.remoteAddress = getRemoteAddress();

    int userCode = response.userCode;
    String remoteAddress = response.remoteAddress;
    try {
      if (restrictedUserUtils.checkAndUpdateRestrictedUser(userCode, remoteAddress,
          RestrictionType.BBS)) {
        return;
      }
    } catch (DatabaseException e) {
      logger.log(Level.INFO, "制限ユーザーのチェックに失敗しました。処理を続行します。", e);
    }

    wrap("BBSへの書き込みに失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.writeToBbs(response, age);
        return null;
      }
    });
  }

  @Override
  public int getNumberOfBbsThreads(final int bbsId) throws ServiceException {
    return wrap("BBSスレッド数の取得に失敗しました", new DatabaseAccessible<Integer>() {
      @Override
      public Integer access() throws DatabaseException {
        return database.getNumberOfBbsThread(bbsId);
      }
    });
  }

  @Override
  public int[][] getPrefectureRanking() {
    return prefectureRanking.get();
  }

  @Override
  public void addRatingHistory(final int userCode, final int rating) throws ServiceException {
    wrap("レーティング履歴の追加に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.addRatingHistory(userCode, rating);
        return null;
      }
    });
  }

  @Override
  public List<Integer> getRatingHistory(final int userCode) throws ServiceException {
    return wrap("レーティング履歴の取得に失敗しました", new DatabaseAccessible<List<Integer>>() {
      @Override
      public List<Integer> access() throws DatabaseException {
        return database.getRatingHistory(userCode);
      }
    });
  }

  @Override
  public PacketRatingDistribution getRatingDistribution() {
    return ratingDistribution.get();
  }

  @Override
  public List<List<String>> getThemeModeThemes() {
    return themeModeProblemManager.getThemes();
  }

  @Override
  public List<PacketRoomKey> getEventRooms() {
    return gameManager.getPublicMatchingEventRooms();
  }

  @Override
  public void voteToProblem(final int userCode, final int problemId, final boolean good,
      final String feedback, final String playerName) throws ServiceException {
    wrap("問題への投票に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        voteManager.vote(userCode, problemId, good, feedback, playerName, getRemoteAddress());
        return null;
      }
    });
  };

  @Override
  public void resetVote(final int problemId) throws ServiceException {
    wrap("良問投票のリセットに失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        voteManager.reset(problemId);
        return null;
      }
    });
  }

  @Override
  public String[] recognizeHandwriting(double[][][] strokes) {
    return recognizer.recognize(strokes);
  }

  @Override
  public String getAvailableChalactersForHandwriting() {
    return recognizer.getAvailableCharacters();
  }

  @Override
  public void clearProblemFeedback(final int problemId) throws ServiceException {
    wrap("問題フィードバックのクリアに失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.clearProblemFeedback(problemId);
        return null;
      }
    });
  }

  @Override
  public List<String> getProblemFeedback(final int problemId) throws ServiceException {
    return wrap("問題フィードバックの取得に失敗しました", new DatabaseAccessible<List<String>>() {
      @Override
      public List<String> access() throws DatabaseException {
        return database.getProblemFeedback(problemId);
      }
    });
  }

  @Override
  public List<PacketTheme> getThemes() throws ServiceException {
    List<PacketThemeQuery> themeModeQueries;
    try {
      themeModeQueries = database.getThemeModeQueries();
    } catch (DatabaseException e) {
      throw new ServiceException(e);
    }

    Set<String> themeNames = Sets.newHashSet();
    for (PacketThemeQuery themeQuery : themeModeQueries) {
      themeNames.add(themeQuery.theme);
    }

    List<PacketTheme> themes = Lists.newArrayList();
    Map<String, IntArray> themesAndProblems = themeModeProblemManager.getThemesAndProblems();
    for (String themeName : themeNames) {
      PacketTheme theme = new PacketTheme();
      theme.setName(themeName);

      if (themesAndProblems.containsKey(themeName)) {
        theme.setNumberOfProblems(themesAndProblems.get(themeName).size());
      }

      themes.add(theme);
    }

    Collections.sort(themes, new Comparator<PacketTheme>() {
      @Override
      public int compare(PacketTheme o1, PacketTheme o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    return themes;
  }

  @Override
  public List<PacketThemeQuery> getThemeQueries(final String theme) throws ServiceException {
    return wrap("テーマクエリの取得に失敗しました", new DatabaseAccessible<List<PacketThemeQuery>>() {
      @Override
      public List<PacketThemeQuery> access() throws DatabaseException {
        return database.getThemeModeQueries(theme);
      }
    });
  }

  @Override
  public int getNumberofThemeQueries() throws ServiceException {
    return wrap("テーマクエリの数の取得に失敗しました", new DatabaseAccessible<Integer>() {
      @Override
      public Integer access() throws DatabaseException {
        return database.getNumberOfThemeQueries();
      }
    });
  }

  private Map<String, AtomicInteger> themeModeNotificationCounter = Maps.newHashMap();
  private transient final Runnable commandUpdateThemeModeNotificationCounter = new Runnable() {
    @Override
    public void run() {
      synchronized (themeModeNotificationCounter) {
        Set<Entry<String, AtomicInteger>> entrySet = themeModeNotificationCounter.entrySet();
        for (Entry<String, AtomicInteger> entry : entrySet) {
          if (entry.getValue().decrementAndGet() >= 0) {
            continue;
          }

          final String theme = entry.getKey();
          threadPool.execute(new Runnable() {
            @Override
            public void run() {
              snsClient.postThemeModeUpdate(theme);
            }
          });

          entrySet.remove(entry);
        }
      }
    }
  };

  private static final int THEME_MODE_NOTIFICATION_WAIT = 60;

  @Override
  public void addThemeModeQuery(final String theme, final String query, final int userCode)
      throws ServiceException {
    wrap("テーマモードクエリの追加に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.addThemeModeQuery(theme, query);

        PacketThemeModeEditLog log = new PacketThemeModeEditLog();
        log.setUserCode(userCode);
        log.setTimeMs(System.currentTimeMillis());
        log.setType(PacketThemeModeEditLog.Type.Add.name());
        log.setTheme(theme);
        log.setQuery(query);
        database.addThemeModeEditLog(log);
        return null;
      }
    });

    synchronized (themeModeNotificationCounter) {
      themeModeNotificationCounter.put(theme, new AtomicInteger(THEME_MODE_NOTIFICATION_WAIT));
    }
  }

  @Override
  public void removeThemeModeQuery(final String theme, final String query, final int userCode)
      throws ServiceException {
    wrap("テーマモードクエリの削除に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.removeThemeModeQuery(theme, query);

        PacketThemeModeEditLog log = new PacketThemeModeEditLog();
        log.setUserCode(userCode);
        log.setTimeMs(System.currentTimeMillis());
        log.setType(PacketThemeModeEditLog.Type.Remove.name());
        log.setTheme(theme);
        log.setQuery(query);
        database.addThemeModeEditLog(log);
        return null;
      }
    });

    synchronized (themeModeNotificationCounter) {
      themeModeNotificationCounter.put(theme, new AtomicInteger(THEME_MODE_NOTIFICATION_WAIT));
    }
  }

  @Override
  public List<PacketThemeModeEditLog> getThemeModeEditLog(final int start, final int length)
      throws ServiceException {
    return wrap("テーマモード編集ログの取得に失敗しました", new DatabaseAccessible<List<PacketThemeModeEditLog>>() {
      @Override
      public List<PacketThemeModeEditLog> access() throws DatabaseException {
        return database.getThemeModeEditLog(start, length);
      }
    });
  }

  @Override
  public int getNumberOfThemeModeEditLog() throws ServiceException {
    return wrap("テーマモード編集ログの数の取得に失敗しました", new DatabaseAccessible<Integer>() {
      @Override
      public Integer access() throws DatabaseException {
        return database.getNumberOfThemeModeEditLog();
      }
    });
  }

  @Override
  public boolean isThemeModeEditor(final int userCode) throws ServiceException {
    return wrap("テーマモード編集者のチェックに失敗しました", new DatabaseAccessible<Boolean>() {
      @Override
      public Boolean access() throws DatabaseException {
        return themeModeEditorManager.isThemeModeEditor(userCode);
      }
    });
  }

  @Override
  public void applyThemeModeEditor(final int userCode, final String text) throws ServiceException {
    wrap("テーマモード編集者の申請に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        try {
          themeModeEditorManager.applyThemeModeEditor(userCode, text);
        } catch (MessagingException e) {
          logger.log(Level.WARNING, "メールの送信に失敗しました", e);
        }
        return null;
      }
    });
  }

  @Override
  public void acceptThemeModeEditor(final int userCode) throws ServiceException {
    wrap("テーマモード編集者の承認に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        themeModeEditorManager.acceptThemeModeEditor(userCode);
        return null;
      }
    });
  }

  @Override
  public void rejectThemeModeEditor(final int userCode) throws ServiceException {
    wrap("テーマモード編集者の却下に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        themeModeEditorManager.rejectThemeModeEditor(userCode);
        return null;
      }
    });
  }

  @Override
  public List<PacketThemeModeEditor> getThemeModeEditors() throws ServiceException {
    return wrap("テーマモード編集者の取得に失敗しました", new DatabaseAccessible<List<PacketThemeModeEditor>>() {
      @Override
      public List<PacketThemeModeEditor> access() throws DatabaseException {
        return themeModeEditorManager.getThemeModeEditors();
      }
    });
  }

  @Override
  public boolean isApplyingThemeModeEditor(final int userCode) throws ServiceException {
    return wrap("テーマモード編集者の申請状態の取得に失敗しました", new DatabaseAccessible<Boolean>() {
      @Override
      public Boolean access() throws DatabaseException {
        return themeModeEditorManager.isApplyingThemeModeEditor(userCode);
      }
    });
  }

  private String getRemoteAddress() {
    // Proxyを通すとlocalhostが帰ってくる場合があるので、X-Forwarded-Forを優先する
    return MoreObjects.firstNonNull(getThreadLocalRequest().getHeader("X-Forwarded-For"),
        getThreadLocalRequest().getRemoteAddr());
  }

  @Override
  public int getNumberOfChatLog() throws ServiceException {
    return wrap("チャットログの長さの取得に失敗しました", new DatabaseAccessible<Integer>() {
      @Override
      public Integer access() throws DatabaseException {
        return database.getNumberOfChatLog();
      }
    });
  }

  @Override
  public int getChatLogId(final int year, final int month, final int day, final int hour,
      final int minute, final int second) throws ServiceException {
    return wrap("チャットログ日時の検索に失敗しました", new DatabaseAccessible<Integer>() {
      @Override
      public Integer access() throws DatabaseException {
        return database.getChatLogId(year, month, day, hour, minute, second);
      }
    });
  }

  @Override
  public List<PacketChatMessage> getChatLog(final int start) throws ServiceException {
    return wrap("チャットログの取得に失敗しました", new DatabaseAccessible<List<PacketChatMessage>>() {
      @Override
      public List<PacketChatMessage> access() throws DatabaseException {
        return database.getChatLog(start);
      }
    });
  }

  @Override
  public List<PacketImageLink> getWrongImageLinks() throws ServiceException {
    return wrap("リンク切れ画像の取得に失敗しました", new DatabaseAccessible<List<PacketImageLink>>() {
      @Override
      public List<PacketImageLink> access() throws DatabaseException {
        return brokenImageLinkDetector.getBrokenImageLinks();
      }
    });
  }

  @Override
  public boolean canUploadProblem(final int userCode) throws ServiceException {
    return wrap("問題投稿制限のチェックに失敗しました", new DatabaseAccessible<Boolean>() {
      @Override
      public Boolean access() throws DatabaseException {
        long dateFrom = System.currentTimeMillis() - 60 * 60 * 1000;
        return database.getNumberOfCreationLogWithMachineIp(getRemoteAddress(), dateFrom) <= Constant.MAX_NUMBER_OF_CREATION_PER_HOUR
            && database.getNumberOfCreationLogWithUserCode(userCode, dateFrom) <= Constant.MAX_NUMBER_OF_CREATION_PER_HOUR;
      }
    });
  }

  @Override
  public List<PacketProblem> getIndicatedProblems() throws ServiceException {
    return wrap("指摘された問題の取得に失敗しました", new DatabaseAccessible<List<PacketProblem>>() {
      @Override
      public List<PacketProblem> access() throws DatabaseException {
        return database.getIndicatedProblems();
      }
    });
  }

  @Override
  public void indicateProblem(final int problemId, int userCode) throws ServiceException {
    try {
      if (restrictedUserUtils.checkAndUpdateRestrictedUser(userCode, getRemoteAddress(),
          RestrictionType.INDICATION)) {
        throw new ServiceException("指摘フラグの更新に失敗しました");
      }
    } catch (DatabaseException e) {
      throw new ServiceException("指摘フラグの更新に失敗しました", e);
    }

    wrap("指摘フラグの更新に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        List<PacketProblem> problems = database.getProblem(ImmutableList.of(problemId));
        PacketProblem problem = problems.get(0);
        problem.indication = new Date();
        database.updateProblem(problem);
        return null;
      }
    });
    problemIndicationCounter.add(userCode);
  }

  private <T> T wrap(String message, DatabaseAccessible<T> accessor) throws ServiceException {
    try {
      return accessor.access();
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, message, e);
      throw new ServiceException(Throwables.getStackTraceAsString(e));
    }
  }

  @Override
  public boolean resetProblemCorrectCounter(int userCode, int problemId) throws ServiceException {
    if (!problemCorrectCounterResetCounter.isAbleToReset(userCode)) {
      return false;
    }

    try {
      List<PacketProblem> problems = database.getProblem(ImmutableList.of(problemId));
      PacketProblem problem = problems.get(0);
      problem.good = problem.bad = 0;
      database.updateProblem(problem);
      return true;

    } catch (DatabaseException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public ProblemIndicationEligibility getProblemIndicationEligibility(int userCode)
      throws ServiceException {
    if (!problemIndicationCounter.isAbleToIndicate(userCode)) {
      return ProblemIndicationEligibility.REACHED_MAX_NUMBER_OF_REQUESTS_PER_UNIT_TIME;
    }
    try {
      if (database.getUserData(userCode).playerName.equals("未初期化です")) {
        return ProblemIndicationEligibility.PLAYER_NAME_UNCHANGED;
      }
    } catch (DatabaseException e) {
      throw new ServiceException("ユーザー情報の取得に失敗しました", e);
    }
    return ProblemIndicationEligibility.OK;
  }

  @Override
  public String generateDiffHtml(String before, String after) throws ServiceException {
    diff_match_patch differ = new diff_match_patch();
    LinkedList<Diff> diffs = differ.diff_main(before, after);
    differ.diff_cleanupSemantic(diffs);
    String html = differ.diff_prettyHtml(diffs);
    html = html.replaceAll("&para;", "");
    return html;
  }

  @Override
  public void addRestrictedUserCode(final int userCode, final RestrictionType restrictionType)
      throws ServiceException {
    wrap("制限ユーザーコードの追加に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.addRestrictedUserCode(userCode, restrictionType);
        return null;
      }
    });
  }

  @Override
  public void removeRestrictedUserCode(final int userCode, final RestrictionType restrictionType)
      throws ServiceException {
    wrap("制限ユーザーコードの削除に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.removeRestrictedUserCode(userCode, restrictionType);
        return null;
      }
    });
  }

  @Override
  public Set<Integer> getRestrictedUserCodes(final RestrictionType restrictionType)
      throws ServiceException {
    return wrap("制限ユーザーコードの取得に失敗しました", new DatabaseAccessible<Set<Integer>>() {
      @Override
      public Set<Integer> access() throws DatabaseException {
        return database.getRestrictedUserCodes(restrictionType);
      }
    });
  }

  @Override
  public void clearRestrictedUserCodes(final RestrictionType restrictionType)
      throws ServiceException {
    wrap("制限ユーザーコードのクリアに失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.clearRestrictedUserCodes(restrictionType);
        return null;
      }
    });
  }

  @Override
  public void addRestrictedRemoteAddress(final String remoteAddress,
      final RestrictionType restrictionType) throws ServiceException {
    wrap("制限リモートアドレスの追加に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.addRestrictedRemoteAddress(remoteAddress, restrictionType);
        return null;
      }
    });
  }

  @Override
  public void removeRestrictedRemoteAddress(final String remoteAddress,
      final RestrictionType restrictionType) throws ServiceException {
    wrap("制限リモートアドレスの削除に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.removeRestrictedRemoteAddress(remoteAddress, restrictionType);
        return null;
      }
    });
  }

  @Override
  public Set<String> getRestrictedRemoteAddresses(final RestrictionType restrictionType)
      throws ServiceException {
    return wrap("制限リモートアドレスの取得に失敗しました", new DatabaseAccessible<Set<String>>() {
      @Override
      public Set<String> access() throws DatabaseException {
        return database.getRestrictedRemoteAddresses(restrictionType);
      }
    });
  }

  @Override
  public void clearRestrictedRemoteAddresses(final RestrictionType restrictionType)
      throws ServiceException {
    wrap("制限リモートアドレスのクリアに失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.clearRestrictedRemoteAddresses(restrictionType);
        return null;
      }
    });
  }

  @Override
  public List<PacketRankingData> getThemeRankingOld(final String theme) throws ServiceException {
    return wrap("旧テーマモードランキングの取得に失敗しました", new DatabaseAccessible<List<PacketRankingData>>() {
      @Override
      public List<PacketRankingData> access() throws DatabaseException {
        return database.getThemeRankingOld(theme);
      }
    });
  }

  @Override
  public List<PacketRankingData> getThemeRankingAll(final String theme) throws ServiceException {
    return wrap("全テーマモードランキングの取得に失敗しました", new DatabaseAccessible<List<PacketRankingData>>() {
      @Override
      public List<PacketRankingData> access() throws DatabaseException {
        return database.getThemeRankingAll(theme);
      }
    });
  }

  @Override
  public List<PacketRankingData> getThemeRanking(final String theme, final int year)
      throws ServiceException {
    return wrap("年別テーマモードランキングの取得に失敗しました", new DatabaseAccessible<List<PacketRankingData>>() {
      @Override
      public List<PacketRankingData> access() throws DatabaseException {
        return database.getThemeRanking(theme, year);
      }
    });
  }

  @Override
  public List<PacketRankingData> getThemeRanking(final String theme, final int year, final int month)
      throws ServiceException {
    return wrap("月別テーマモードランキングの取得に失敗しました", new DatabaseAccessible<List<PacketRankingData>>() {
      @Override
      public List<PacketRankingData> access() throws DatabaseException {
        return database.getThemeRanking(theme, year, month);
      }
    });
  }

  @Override
  public List<PacketMonth> getThemeRankingDateRanges() throws ServiceException {
    return wrap("月別テーマモードランキングの取得に失敗しました", new DatabaseAccessible<List<PacketMonth>>() {
      @Override
      public List<PacketMonth> access() throws DatabaseException {
        return database.getThemeRankingDateRanges();
      }
    });
  }

  @Override
  public List<PacketUserData> lookupUserDataByGooglePlusId(final String googlePlusId)
      throws ServiceException {
    return wrap("ユーザーコードの検索に失敗しました", new DatabaseAccessible<List<PacketUserData>>() {
      @Override
      public List<PacketUserData> access() throws DatabaseException {
        return database.lookupUserCodeByGooglePlusId(googlePlusId);
      }
    });
  }

  @Override
  public void disconnectUserCode(final int userCode) throws ServiceException {
    wrap("Google+連携の解除に失敗しました", new DatabaseAccessible<Void>() {
      @Override
      public Void access() throws DatabaseException {
        database.disconnectUserCodeFromGooglePlus(userCode);
        return null;
      }
    });
  }
}
