package tv.dyndns.kishibe.qmaclone.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.websocket.MessageSender;

public class ServerStatusManager {
  private static final Logger logger = Logger.getLogger(ServerStatusManager.class.getName());
  private static final int UPDATE_DURATION = 10; // 秒
  private final Database database;
  @VisibleForTesting
  final AtomicInteger numberOfPageView = new AtomicInteger();
  private final AtomicInteger numberOfTotalSessions = new AtomicInteger();
  private final AtomicInteger numberOfTotalPlayers = new AtomicInteger();
  private final AtomicInteger numberOfCurrentSessions = new AtomicInteger();
  private final AtomicInteger numberOfCurrentPlayers = new AtomicInteger();
  @VisibleForTesting
  volatile Set<Integer> loginUserCodes = Collections.synchronizedSet(new HashSet<Integer>());
  private volatile List<PacketUserData> loginUsers = Lists.newArrayList();
  private final GameManager gameManager;
  private final NormalModeProblemManager normalModeProblemManager;
  private final PlayerHistoryManager playerHistoryManager;
  private final MessageSender<PacketServerStatus> serverStatusMessageSender;
  private volatile PacketServerStatus serverStatus;
  private final Runnable saveServerStatusRunner = new Runnable() {
    public void run() {
      saveServerStatus();
    }
  };
  private final Runnable updateLoginUsersRunner = new Runnable() {
    public void run() {
      updateLoginUsers();
    }
  };
  private final Runnable updateServerStatusRunner = new Runnable() {
    @Override
    public void run() {
      try {
        updateServerStatus();
      } catch (DatabaseException e) {
        logger.log(Level.WARNING, "サーバーステータスの更新に失敗しました", e);
      }
    }
  };

  @Inject
  public ServerStatusManager(Database database, GameManager gameManager,
      NormalModeProblemManager normalModeProblemManager, PlayerHistoryManager playerHistoryManager,
      MessageSender<PacketServerStatus> serverStatusMessageSender, ThreadPool threadPool) {
    this.database = Preconditions.checkNotNull(database);
    this.gameManager = Preconditions.checkNotNull(gameManager);
    this.normalModeProblemManager = Preconditions.checkNotNull(normalModeProblemManager);
    this.playerHistoryManager = Preconditions.checkNotNull(playerHistoryManager);
    this.serverStatusMessageSender = Preconditions.checkNotNull(serverStatusMessageSender);

    threadPool.addMinuteTasks(saveServerStatusRunner);
    threadPool.addMinuteTasks(updateLoginUsersRunner);
    threadPool.scheduleAtFixedRate(updateServerStatusRunner, UPDATE_DURATION, UPDATE_DURATION,
        TimeUnit.SECONDS);
    try {
      updateServerStatus();
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "サーバーステータスの更新に失敗しました", e);
    }

    loadPageView();
  }

  private void loadPageView() {
    // データベースから読み込み
    PageView pageView;
    try {
      pageView = database.loadPageView();
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "ページビューの読み込みに失敗しました", e);
      return;
    }
    numberOfPageView.addAndGet(pageView.numberOfPageView);
    numberOfTotalSessions.addAndGet(pageView.numberOfSessions);
    numberOfTotalPlayers.addAndGet(pageView.numberOfPlayers);
  }

  public void login() {
    numberOfPageView.getAndIncrement();
  }

  @VisibleForTesting
  void updateServerStatus() throws DatabaseException {
    PacketServerStatus status = new PacketServerStatus();
    status.numberOfCurrentSessions = numberOfCurrentSessions.get();
    status.numberOfTotalSessions = numberOfTotalSessions.get();
    status.numberOfCurrentPlayers = numberOfCurrentPlayers.get();
    status.numberOfTotalPlayers = numberOfTotalPlayers.get();
    status.numberOfProblems = normalModeProblemManager.getNumberOfProblem();
    status.numberOfPageView = numberOfPageView.get();
    // 読み取りのみなので同期不要
    status.numberOfLoginPlayers = Math.max(loginUsers.size(), loginUserCodes.size());
    status.numberOfActivePlayers = database.getNumberOfActiveUsers();
    status.numberOfPlayersInWhole = gameManager.getNumberOfPlayersInWhole();
    status.lastestPlayers = playerHistoryManager.get();
    if (Objects.equal(serverStatus, status)) {
      return;
    }
    serverStatus = status;
    serverStatusMessageSender.send(status);
  }

  public PacketServerStatus getServerStatus() {
    return serverStatus;
  }

  public void saveServerStatus() {
    PageView pageView = new PageView();
    pageView.numberOfPageView = numberOfPageView.get();
    pageView.numberOfPlayers = numberOfTotalPlayers.get();
    pageView.numberOfSessions = numberOfTotalSessions.get();

    if (pageView.numberOfPageView < 10000 || pageView.numberOfPlayers < 10000
        || pageView.numberOfSessions < 10000) {
      logger.info("ページビューが読み込まれていない可能性があります。再読み込みを行います。");
      loadPageView();
      return;
    }

    try {
      database.savePageView(pageView);
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "ページビューの保存に失敗しました", e);
    }
  }

  public void keepAlive(int userCode) {
    loginUserCodes.add(userCode);
  }

  public List<PacketUserData> getLoginUsers() {
    return loginUsers;
  }

  private void updateLoginUsers() {
    Set<Integer> userCodes = loginUserCodes;
    loginUserCodes = Collections.synchronizedSet(new HashSet<Integer>());

    List<PacketUserData> list = Lists.newArrayList();
    for (int userCode : userCodes) {
      try {
        list.add(database.getUserData(userCode));
      } catch (DatabaseException e) {
        logger.log(Level.WARNING, "ユーザー情報の読み込みに失敗しました", e);
      }
    }

    loginUsers = list;
  }

  public void changeStatics(int sessionDelta, int playDelta) {
    numberOfCurrentSessions.set(gameManager.getNumberOfSessions());
    numberOfCurrentPlayers.set(gameManager.getNumberOfPlayers());
    numberOfTotalSessions.addAndGet(sessionDelta);
    numberOfTotalPlayers.addAndGet(playDelta);
  }

  public MessageSender<PacketServerStatus> getServerStatusMessageSender() {
    return serverStatusMessageSender;
  }
}
