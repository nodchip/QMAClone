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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.Transition;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus.GamePlayerStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingPlayer;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketReadyForGame;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketResult;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

public class Game {
  private static final Logger logger = Logger.getLogger(Game.class.getName());
  private static final int SECONDS_FROM_READY_TO_PROBLEM = 10;
  private static final int SECONDS_FROM_PROBLEM_TO_ANSWER = 30;
  private static final int SECONDS_FROM_ANSWER_TO_PROBLEM_OR_RESULT = 5;
  private static final int SECONDS_FROM_RESULT_TO_FINISHED = 600;
  private final GameManager gameManager;
  private static volatile int playerId = 0;
  private final int classLevel;
  private final int sessionId;
  // TODO enum型に変更
  // TODO(nodchip): Atomic***に変更
  /**
   * 現在の状態遷移
   */
  private final AtomicReference<Transition> transition = new AtomicReference<Transition>(
      Transition.Matching);
  /**
   * 次の状態への残り秒数
   */
  private final AtomicInteger secondsToNextState = new AtomicInteger();
  private final List<Integer> problemIds = Lists.newArrayList(); // 問題データ等
  private volatile List<PacketProblem> problems = null;
  private final Set<Integer> selectedProblemIds = Sets.newHashSet();
  // TODO(nodchip):CopyOnWriteArrayへ変更する
  private final List<PlayerStatus> playerStatuses = Lists.newArrayList();
  private final AtomicInteger numberOfInitialHumanPlayers = new AtomicInteger();
  private final Set<ProblemGenre> selectedGenres = EnumSet.noneOf(ProblemGenre.class); // プレイヤーを追加する
  private final Set<ProblemType> selectedTypes = EnumSet.noneOf(ProblemType.class);
  private final Set<Integer> setDifficult = Sets.newHashSet();
  private final Set<NewAndOldProblems> setNewAndOldProblems = Sets.newHashSet();
  private final AtomicInteger numberOfRequestStartingGame = new AtomicInteger(); // 直ちにゲームを開始するように迫る
  private final List<PacketMatchingPlayer> matchingPlayers = Lists.newArrayList(); // マッチング情報を返す
  private final AtomicInteger problemCounter = new AtomicInteger();
  private volatile long questionStartTime;
  private final Random random = new Random();
  private volatile List<PacketResult> packetResult; // 最終結果を返す
  private volatile ComputerPlayer computerPlayer = null;
  private final boolean event;
  private final boolean alone;
  private volatile Set<ProblemGenre> firstGenre;
  private volatile Set<ProblemType> firstType;
  private volatile int firstDifficultSelect;
  private volatile NewAndOldProblems firstNewAndOldProblems;
  private final String theme;
  private final boolean publicEvent;
  private final Set<Integer> unavailableUserCodesForProblems = Sets.newHashSet();
  private final Set<Integer> unavailableCreatorHashes = Sets.newHashSet();
  private final ServerStatusManager serverStatusManager;
  private final ScheduledFuture<?> timer;
  private final NormalModeProblemManager normalModeProblemManager;
  private final ThemeModeProblemManager themeModeProblemManager;
  private final Database database;
  private final ComputerPlayer.Factory computerPlayerFactory;
  private final ThreadPool threadPool;
  private final RestrictedUserUtils restrictedUserUtils;
  private final GameMode gameMode;

  public static interface Factory {
    Game create(@Assisted("sessionId") int sessionId, @Assisted("classLevel") int classLevel,
        @Assisted("EVENT") boolean event, @Assisted("alone") boolean alone,
        @Assisted("THEME") String theme, @Assisted("publicEvent") boolean publicEvent,
        @Assisted("gameMode") GameMode gameMode);
  }

  @Inject
  public Game(GameManager gameManager, ServerStatusManager serverStatusManager,
      NormalModeProblemManager normalModeProblemManager,
      ThemeModeProblemManager themeModeProblemManager, Database database,
      ComputerPlayer.Factory computerPlayerFactory, ThreadPool threadPool,
      RestrictedUserUtils restrictedUserUtils, @Assisted("sessionId") int sessionId,
      @Assisted("classLevel") int classLevel, @Assisted("EVENT") boolean event,
      @Assisted("alone") boolean alone, @Nullable @Assisted("THEME") String theme,
      @Assisted("publicEvent") boolean publicEvent, @Assisted("gameMode") GameMode gameMode) {
    this.gameManager = gameManager;
    this.serverStatusManager = serverStatusManager;
    this.normalModeProblemManager = normalModeProblemManager;
    this.themeModeProblemManager = themeModeProblemManager;
    this.database = database;
    this.computerPlayerFactory = computerPlayerFactory;
    this.threadPool = threadPool;
    this.sessionId = sessionId;
    this.classLevel = classLevel;
    this.event = event;
    this.alone = alone;
    this.theme = theme;
    this.publicEvent = publicEvent;
    this.gameMode = Preconditions.checkNotNull(gameMode);
    this.restrictedUserUtils = Preconditions.checkNotNull(restrictedUserUtils);

    if (sessionId == 0) {
      String object = MoreObjects.toStringHelper(this).add("gameManager", gameManager)
          .add("serverStatusManager", serverStatusManager)
          .add("normalModeProblemManager", normalModeProblemManager)
          .add("themeModeProblemManager", themeModeProblemManager).add("database", database)
          .add("computerPlayerFactory", computerPlayerFactory).add("threadPool", threadPool)
          .add("sessionId", sessionId).add("classLevel", classLevel).add("EVENT", event)
          .add("alone", alone).add("THEME", theme).add("publicEvent", publicEvent).toString();
      logger.log(Level.SEVERE, "不正なセッションIDが指定されました: " + object);
    }

    secondsToNextState.set(Constant.WAIT_SECOND_FOR_MATCHING);

    timer = threadPool.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        updateState();
      }
    }, 1, 1, TimeUnit.SECONDS);
    updateState();
  }

  /**
   * 一秒毎に状態を更新する
   */
  private synchronized void updateState() {
    Transition next = null;
    switch (transition.get()) {
    case Matching:
      next = updateMatchingState();
      break;
    case Ready:
      next = updateReadyState();
      break;
    case Problem:
      next = updateProblemState();
      break;
    case Answer:
      next = updateAnswerState();
      break;
    case Result:
      next = updateResultState();
      break;
    case Finished:
      next = Transition.Finished;
      break;
    }

    transition.set(next);

    updateMatchingData();
    updateReadyForGame();
    updateGameStatus();
  }

  private synchronized Transition updateMatchingState() {
    if (Transition.Matching.compareTo(transition.get()) < 0) {
      return transition.get();
    }

    if (secondsToNextState.decrementAndGet() < 0) {
      // ゲーム待機へ遷移する
      return transitFromMachingToReady();
    }

    return Transition.Matching;
  }

  public int getRestMatchingSecond() {
    if (transition.get() != Transition.Matching) {
      return 0;
    }

    return secondsToNextState.get();
  }

  public Transition getTransition() {
    return transition.get();
  }

  public int getNumberOfHumanPlayer() {
    int numberOfHumanPlayer = 0;
    for (PlayerStatus status : playerStatuses) {
      numberOfHumanPlayer += status.isHuman() ? 1 : 0;
    }
    return numberOfHumanPlayer;
  }

  public synchronized int getNumberOfPlayer() {
    return playerStatuses.size();
  }

  public synchronized List<PacketProblem> getProblem() {
    return problems;
  }

  /**
   * プレイヤーを追加する
   * 
   * @param playerSummary
   *          プレイヤーサマリー
   * @param genre
   *          問題ジャンル
   * @param type
   *          問題タイプ
   * @param greeting
   *          挨拶
   * @param imageFileName
   *          アイコン画像ファイル名
   * @param classLevel
   *          クラスレベル
   * @param difficultSelect
   *          難易度
   * @param rating
   *          レーティング
   * @param userCode
   *          ユーザーコード
   * @param newAndOldProblem
   *          旧問/新問
   * @return プレイヤーステータス
   */
  public synchronized PlayerStatus addPlayer(PacketPlayerSummary playerSummary,
      Set<ProblemGenre> genres, Set<ProblemType> types, String greeting, String imageFileName,
      int classLevel, int difficultSelect, int rating, int userCode, int volatility, int playCount,
      NewAndOldProblems newAndOldProblem) {
    if (matchingPlayers.isEmpty()) {
      firstGenre = genres;
      firstType = types;
      firstDifficultSelect = difficultSelect;
      firstNewAndOldProblems = newAndOldProblem;
    }

    // テーマモードの場合は全ジャンル全問題形式から出題する
    if (!Strings.isNullOrEmpty(theme)) {
      genres.clear();
      types.clear();
    }

    // 書き込み同期はServiceImplで取る
    PlayerStatus status = new PlayerStatus(playerSummary, Game.playerId++,
        this.playerStatuses.size(), this.sessionId, true, greeting, imageFileName, classLevel,
        rating, userCode, volatility, playCount);

    playerStatuses.add(status);
    PacketMatchingPlayer matchingPlayer = new PacketMatchingPlayer();
    matchingPlayer.playerSummary = playerSummary;
    matchingPlayer.isRequestSkip = false;
    matchingPlayer.greeting = greeting;
    matchingPlayer.imageFileName = imageFileName;
    matchingPlayers.add(matchingPlayer);

    for (int i = 0; i < Constant.MAX_PROBLEMS_PER_PLAYER; ++i) {
      int problemID = selectProblem(genres, types, classLevel, difficultSelect, theme,
          newAndOldProblem);
      problemIds.add(problemID);
    }
    selectedGenres.addAll(genres);
    selectedTypes.addAll(types);
    setDifficult.add(difficultSelect);
    setNewAndOldProblems.add(newAndOldProblem);

    numberOfInitialHumanPlayers.incrementAndGet();

    // 人数が集まったらReady状態へ遷移する
    if (playerStatuses.size() >= Constant.MAX_PLAYER_PER_SESSION || alone) {
      // 時間経過による状態遷移でないため特別扱いする
      transition.set(transitFromMachingToReady());
    }

    return status;
  }

  /**
   * 問題を選択する
   * 
   * @param genre
   *          ジャンル
   * @param type
   *          タイプ
   * @param classLevel
   *          クラスレベル
   * @param difficultSelect
   *          難易度
   * @param THEME
   *          テーマ
   * @param newAndOldProblems
   *          新問/旧問
   * @return 問題番号
   */
  private synchronized int selectProblem(Set<ProblemGenre> genres, Set<ProblemType> types,
      int classLevel, int difficultSelect, String theme, NewAndOldProblems newAndOldProblems) {
    if (event) {
      genres = firstGenre;
      types = firstType;
      difficultSelect = firstDifficultSelect;
      newAndOldProblems = firstNewAndOldProblems;
    }

    try {
      if (theme == null) {
        boolean tegaki = event;
        return normalModeProblemManager.selectProblem(genres, types, classLevel, difficultSelect,
            selectedProblemIds, true, newAndOldProblems, tegaki, unavailableUserCodesForProblems,
            unavailableCreatorHashes).id;
      } else {
        // テーマモード
        return themeModeProblemManager.selectProblem(theme, difficultSelect, classLevel,
            selectedProblemIds).id;
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "問題の選択に失敗しました", e);
    }

    // 問題選択時に問題が発生した場合はランダムに問題を選択する
    return random.nextInt(200000);
  }

  /**
   * プレイヤーがゲーム開始ボタンを押した場合の処理を行う
   * 
   * @param playerListId
   *          ボタンを押したプレイヤーのプレイヤーリストID
   */
  public synchronized void requestStartingGame(int playerListId) {
    PlayerStatus status = (PlayerStatus) playerStatuses.get(playerListId);
    if (status.isRequestStartingGame()) {
      return;
    }

    status.setRequestStartingGame();
    matchingPlayers.get(playerListId).isRequestSkip = true;

    // すべてのプレイヤーがゲーム開始ボタンを押したらゲーム状態へ移行する
    if (numberOfRequestStartingGame.incrementAndGet() >= playerStatuses.size()) {
      // 時間経過による状態遷移でないため特別扱いする
      transition.set(transitFromMachingToReady());
    }
  }

  private volatile PacketMatchingData matchnigData;

  private synchronized void updateMatchingData() {
    PacketMatchingData matchingData = new PacketMatchingData();

    Transition t = transition.get();
    if (t != Transition.Matching) {
      matchingData.restSeconds = 0;
    } else {
      matchingData.restSeconds = secondsToNextState.get();
    }

    if (t == Transition.Matching || t == Transition.Ready) {
      // matchingPlayersのSerialize中にほかのスレッドが変更する可能性があるためコピーを返す
      matchingData.players = Lists.newArrayList(matchingPlayers);
    }

    this.matchnigData = matchingData;
  }

  /**
   * マッチングデータを返す
   * 
   * @return マッチングデータ
   */
  public synchronized PacketMatchingData getMatchingData() {
    Preconditions.checkNotNull(matchnigData, "マッチング情報がnullです: sessionId=" + sessionId);
    return matchnigData;
  }

  private volatile PacketReadyForGame readyForGame;

  private synchronized void updateReadyForGame() {
    PacketReadyForGame readyForGame = new PacketReadyForGame();
    if (transition.get() != Transition.Ready) {
      readyForGame.restSeconds = 0;
    } else {
      readyForGame.restSeconds = secondsToNextState.get();
    }

    this.readyForGame = readyForGame;
  }

  /**
   * Ready状態からProblem状態へ移行するまでの残り秒数を返す
   * 
   * @return 残り秒数
   */
  public synchronized PacketReadyForGame getReadyForGameStatus() {
    Preconditions.checkNotNull(readyForGame, "readyForGame  == null: sessionId=" + sessionId);
    return readyForGame;
  }

  /**
   * Matching状態からReady状態へ移行する
   */
  private synchronized Transition transitFromMachingToReady() {
    // タイムアウトと人数制限が同時に起こり、既にReady状態になった場合は処理しない
    if (Transition.Ready.compareTo(transition.get()) <= 0) {
      return transition.get();
    }

    // 故意の回線落ちチェックのためゲーム開始したプレイヤーのログを取る
    if (1 < getNumberOfHumanPlayer()) {
      for (PlayerStatus player : playerStatuses) {
        String message = MoreObjects.toStringHelper(this).add("method", "transitFromMachingToReady")
            .add("sessionId", sessionId).add("userCode", player.getUserCode()).toString();
        logger.log(Level.INFO, message);
      }
    }

    secondsToNextState.set(SECONDS_FROM_READY_TO_PROBLEM);

    gameManager.notifyMatchingCompleted();

    // サーバー統計変更
    serverStatusManager.changeStatics(1, numberOfInitialHumanPlayers.get());

    int difficultSelect = (setDifficult.size() == 1) ? setDifficult.iterator().next()
        : Constant.DIFFICULT_SELECT_NORMAL;
    NewAndOldProblems newAndOldProblems = (setNewAndOldProblems.size() == 1)
        ? setNewAndOldProblems.iterator().next() : NewAndOldProblems.Both;

    problems = prepareProblems(difficultSelect, newAndOldProblems, problemIds, selectedGenres,
        selectedTypes, classLevel, theme);

    computerPlayer = computerPlayerFactory.create(problemIds);

    // COMプレイヤー追加
    while (playerStatuses.size() < Constant.MAX_PLAYER_PER_SESSION) {
      PacketPlayerSummary playerSummary = computerPlayer.newPlayer(difficultSelect);
      String greeting = computerPlayer.getGreeting();

      PlayerStatus status = new PlayerStatus(playerSummary, -1, playerStatuses.size(),
          this.sessionId, false, greeting, computerPlayer.selectIconFileName(),
          Constant.MAX_CLASS_LEVEL / 2, 0, -1, -1, -1);
      playerStatuses.add(status);

      PacketMatchingPlayer matchingPlayer = new PacketMatchingPlayer();
      matchingPlayer.playerSummary = playerSummary;
      matchingPlayer.isRequestSkip = false;
      matchingPlayer.greeting = greeting;
      matchingPlayer.imageFileName = status.getImageFileName();
      matchingPlayers.add(matchingPlayer);
    }

    return Transition.Ready;
  }

  @VisibleForTesting
  List<PacketProblem> prepareProblems(int difficultSelect, NewAndOldProblems newAndOldProblems,
      List<Integer> problemIds, Set<ProblemGenre> selectedGenres, Set<ProblemType> selectedTypes,
      int classLevel, String theme) {
    // 一人が複数のジャンル・出題形式を選択した場合にランダム扱いになるバグへの対処
    // BugTrack-QMAClone/381 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F381

    // 出題される問題のジャンルと出題形式が偏る場合がある問題への対処
    // BugTrack-QMAClone/418 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F418#1328272472

    // テーマモードでselectedGenresとselectedTypesが空の場合に落ちるバグへの対処
    // BugTrack-QMAClone/424 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F424
    if (selectedGenres.isEmpty()) {
      selectedGenres = EnumSet.of(ProblemGenre.Random);
    }
    if (selectedTypes.isEmpty()) {
      selectedTypes = EnumSet.of(ProblemType.Random);
    }

    int numberOfProblemsToAdd = Constant.MAX_PROBLEMS_PER_SESSION - problemIds.size();

    List<ProblemGenre> genres = Lists.newArrayList(selectedGenres);
    for (int i = 0; i < numberOfProblemsToAdd; ++i) {
      genres.add(genres.get(i));
    }
    Collections.shuffle(genres);

    List<ProblemType> types = Lists.newArrayList(selectedTypes);
    for (int i = 0; i < numberOfProblemsToAdd; ++i) {
      types.add(types.get(i));
    }
    Collections.shuffle(types);

    for (int i = 0; i < numberOfProblemsToAdd; ++i) {
      int problemID = selectProblem(EnumSet.of(genres.get(i)), EnumSet.of(types.get(i)), classLevel,
          difficultSelect, theme, newAndOldProblems);
      problemIds.add(problemID);
    }

    List<PacketProblem> problems;
    try {
      problems = database.getProblem(problemIds);
    } catch (DatabaseException e) {
      logger.log(Level.SEVERE, "問題の読み込みに失敗しました", e);
      return null;
    }

    // 問題順番の入れ替え
    Collections.shuffle(problems);

    // 解答欄・選択肢シャッフル
    for (PacketProblem problem : problems) {
      problem.prepareShuffledAnswersAndChoices();
    }

    return problems;
  }

  /**
   * Ready状態を更新する
   */
  private synchronized Transition updateReadyState() {
    if (Transition.Ready.compareTo(transition.get()) < 0) {
      return transition.get();
    }

    if (secondsToNextState.decrementAndGet() < 0) {
      // Problem状態へ遷移する
      return transitFromReadyToProblem();
    }

    return Transition.Ready;
  }

  /**
   * Problem状態へ移行する
   */
  private synchronized Transition transitFromReadyToProblem() {
    // 既に問題状態へ遷移した場合は処理しない
    if (Transition.Problem.compareTo(transition.get()) <= 0) {
      return transition.get();
    }

    calculateRanking();

    return transitFromReadyOrAnswerToProblem();
  }

  /**
   * 残り回答時間をミリ秒単位で返す
   * 
   * @return 残り解答時間
   */
  private int getRestProblemMs() {
    // questionStartTimeは代入操作のみなのでsynchronized不要
    // ただし64bit変数のため64bitのJDKで動かさなければならない
    long currentTime = Calendar.getInstance().getTimeInMillis();
    int rest = (int) (currentTime - questionStartTime);
    rest = SECONDS_FROM_PROBLEM_TO_ANSWER * 1000 - rest;
    return rest;
  }

  /**
   * プレイヤーの解答を受け取る
   * 
   * @param playerListId
   *          プレイヤーリストID
   * @param answer
   *          プレイヤーの解答
   */
  public synchronized void receiveAnswer(int playerListId, String answer) {
    // Problem状態以外の場合は処理しない
    if (transition.get() != Transition.Problem) {
      return;
    }

    PlayerStatus player = playerStatuses.get(playerListId);
    player.clearSkipCount();
    if (player.isAnswered()) {
      return;
    }
    player.setAnswer(answer, Math.max(1, getRestProblemMs()));

    // HUM全員が回答した場合は時間経過による状態遷移でないため特別扱いする
    boolean allHumAnswered = true;
    for (PlayerStatus status : playerStatuses) {
      if (status.isHuman() && !status.isAnswered()) {
        allHumAnswered = false;
        break;
      }
    }
    if (allHumAnswered) {
      transition.set(transitFromProblemToAnswer());
    }
  }

  /**
   * 次の問題へ移行する
   */
  private synchronized Transition transitFromReadyOrAnswerToProblem() {
    secondsToNextState.set(SECONDS_FROM_PROBLEM_TO_ANSWER);

    for (PlayerStatus player : playerStatuses) {
      player.incSkipCount();
      player.clearAnswer();
    }

    // 時間切れタイマーセット
    questionStartTime = Calendar.getInstance().getTimeInMillis();

    return Transition.Problem;
  }

  /**
   * Problem状態を更新する
   */
  private synchronized Transition updateProblemState() {
    if (getRestProblemMs() < 0) {
      return transitFromProblemToAnswer();
    }

    int computerTiming = (classLevel == Constant.CLASS_LEVEL_NORMAL) ? Constant.MAX_CLASS_LEVEL / 2
        : classLevel;
    if (getRestProblemMs() < 20000 + 10000 * computerTiming / Constant.MAX_CLASS_LEVEL) {
      int index = random.nextInt(Constant.MAX_PLAYER_PER_SESSION);

      PlayerStatus playerStatus = playerStatuses.get(index);
      if (!playerStatus.isHuman() && !playerStatus.isAnswered()) {
        PacketProblem problem = problems.get(problemCounter.get());
        String answer = computerPlayer.getAnswer(problem, getPlayerAnswers());
        playerStatus.setAnswer(answer, getRestProblemMs());
      }
    }

    return Transition.Problem;
  }

  private List<String> getPlayerAnswers() {
    List<String> playerAnswers = Lists.newArrayList();
    for (GamePlayerStatus status : getGameStatus().status) {
      playerAnswers.add(status.answer);
    }
    return playerAnswers;
  }

  /**
   * Problem状態からAnswer状態へ移行する
   */
  private synchronized Transition transitFromProblemToAnswer() {
    secondsToNextState.set(SECONDS_FROM_ANSWER_TO_PROBLEM_OR_RESULT);

    // COM回答作成
    // HUM再度解答送信
    // PacketProblem problem = (PacketProblem)
    // problems[problemCounter];
    final PacketProblem problem = problems.get(problemCounter.get());

    for (PlayerStatus player : playerStatuses) {
      if (!player.isHuman() && !player.isAnswered()) {
        String answer = computerPlayer.getAnswer(problem, getPlayerAnswers());
        int restTime = random.nextInt(Math.max(1, getRestProblemMs()));
        player.setAnswer(answer, restTime);
        // pushAnswer(player.getPlayerListId(), answer);
      }

      if (player.isHuman() && player.isAnswered()) {
        player.clearSkipCount();
      }
    }

    // 全員の正解チェック
    final List<String> playerAnswers = new ArrayList<String>();
    for (PlayerStatus player : playerStatuses) {
      boolean correct = problem.isCorrect(player.getAnswer());
      if (correct) {
        int point = calcPoint(problem, player.getClassLevel(), player.getTimeRemain());
        player.addScore(point);
      }

      if (player.isHuman()) {
        if (correct) {
          ++problem.good;
        } else {
          ++problem.bad;
        }

        if (player.getAnswer() != null && !player.getAnswer().isEmpty()) {
          playerAnswers.add(player.getAnswer());
        }
      }
    }

    if (!playerAnswers.isEmpty()) {
      threadPool.execute(new Runnable() {
        public void run() {
          try {
            database.addPlayerAnswers(problem.id, problem.type, playerAnswers);
          } catch (DatabaseException e) {
            logger.log(Level.WARNING, "プレイヤー解答の保存に失敗しました", e);
          }
        }
      });
    }

    // データベース更新
    // 全体対戦モードでのみ正解率を更新するようにした
    if (gameMode == GameMode.WHOLE) {
      threadPool.execute(new Runnable() {
        public void run() {
          try {
            normalModeProblemManager.updateMinimumProblem(problem);
          } catch (DatabaseException e) {
            logger.log(Level.WARNING, "問題の更新に失敗しました", e);
          }
        }
      });
    }

    // 回線落ち
    for (PlayerStatus player : playerStatuses) {
      if (player.isHuman() && player.shouldBeDropped()) {
        player.drop();
        String message = "プレイヤーをドロップしました: " + MoreObjects.toStringHelper(this)
            .add("sessionId", sessionId).add("playerListId", player.getPlayerListId()).toString();
        logger.log(Level.INFO, message);
      }
    }

    // 通信エラーによりプレイヤーが0人になった後にプレイヤーが復帰する可能性があるため、終了状態へ遷移しないようにする
    // // 誰もいなくなった場合は結果発表へ進む
    // if (getNumberOfHumanPlayer() == 0) {
    // return transitFromAnswerToResult();
    // }

    // 途中順位決定
    calculateRanking();

    // 問題数カウント
    problemCounter.incrementAndGet();

    return Transition.Answer;
  }

  /**
   * 解答の得点を計算する
   * 
   * @param problem
   *          問題
   * @param classLevel
   *          クラスレベル
   * @param restTime
   *          残り時間
   * @return 解答の得点
   */
  private synchronized int calcPoint(PacketProblem problem, int classLevel, int restTime) {
    if (theme == null) {
      return calculateNormalProblemScore(classLevel, restTime);
    } else {
      return calculateThemeModeProblemScore(problem, classLevel, restTime);
    }
  }

  private int calculateThemeModeProblemScore(PacketProblem problem, int classLevel, int restTime) {
    // 検定モード
    double accuracyRate = problem.getAccuracyRate() * 0.01;
    double scale = accuracyRate;
    if (scale < 0) {
      scale = 0.5;
    }
    scale = 4.0 - 3.0 * scale;
    double basePoint = scale * Constant.MAX_POINT / Constant.MAX_PROBLEMS_PER_SESSION;

    // 残りの部分は通常モードの採点方式ほとんど同じ
    int maxQuestionTime = SECONDS_FROM_PROBLEM_TO_ANSWER * 1000;
    int maxClassLevel = Constant.MAX_CLASS_LEVEL;
    double rc = (double) (maxClassLevel - classLevel) / (double) maxClassLevel;
    int perfectBoderTime = (int) (Constant.MAX_PERFECT_BORDER_TIME * rc);

    if (restTime + perfectBoderTime > maxQuestionTime) {
      return (int) basePoint;
    }

    double r = (double) restTime / (double) (maxQuestionTime - perfectBoderTime);
    int compressPoint = (int) (basePoint
        * ((Constant.MAX_POINT_COMPRESS - Constant.MIN_POINT_COMPRESS) * rc
            + Constant.MIN_POINT_COMPRESS));
    int point = (int) (basePoint * r + compressPoint * (1.0 - r));
    return point;
  }

  private int calculateNormalProblemScore(int classLevel, int restTime) {
    // 通常モード
    int maxQuestionTime = SECONDS_FROM_PROBLEM_TO_ANSWER * 1000;
    int maxClassLevel = Constant.MAX_CLASS_LEVEL;
    double rc = (double) (maxClassLevel - classLevel) / (double) maxClassLevel;
    int perfectBoderTime = (int) (Constant.MAX_PERFECT_BORDER_TIME * rc);
    int perfectPoint = Constant.MAX_POINT / Constant.MAX_PROBLEMS_PER_SESSION;

    if (restTime + perfectBoderTime > maxQuestionTime) {
      return perfectPoint;
    }

    double r = (double) restTime / (double) (maxQuestionTime - perfectBoderTime);
    int compressPoint = (int) (perfectPoint
        * ((Constant.MAX_POINT_COMPRESS - Constant.MIN_POINT_COMPRESS) * rc
            + Constant.MIN_POINT_COMPRESS));
    int point = (int) (perfectPoint * r + compressPoint * (1.0 - r));
    return point;
  }

  /**
   * Answer状態を更新する
   * 
   * @return 状態
   */
  private synchronized Transition updateAnswerState() {
    if (secondsToNextState.decrementAndGet() < 0) {
      // 次の問題、又は結果画面へ遷移する
      return transitFromAnswerToProblemOrResult();
    }

    return Transition.Answer;
  }

  /**
   * Answer状態からProblem/Result状態へ移行する
   * 
   * @return 状態
   */
  private synchronized Transition transitFromAnswerToProblemOrResult() {
    if (problemCounter.get() >= Constant.MAX_PROBLEMS_PER_SESSION) {
      return transitFromAnswerToResult();
    } else {
      return transitFromReadyOrAnswerToProblem();
    }
  }

  /**
   * Answer状態からResult状態へ移行する
   * 
   * @return 状態
   */
  private synchronized Transition transitFromAnswerToResult() {
    secondsToNextState.set(SECONDS_FROM_RESULT_TO_FINISHED);

    // 順位決定
    PlayerStatus players[] = playerStatuses.toArray(new PlayerStatus[0]);
    Arrays.sort(players, new Comparator<PlayerStatus>() {
      public int compare(PlayerStatus o1, PlayerStatus o2) {
        return o2.getScore() - o1.getScore();
      }
    });

    // BugTrack-QMAClone/401
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack-QMAClone%2F401
    for (int i = 0; i < players.length; ++i) {
      players[i].setRank(i + 1);
    }

    for (PlayerStatus player : players) {
      player.setNewRating(player.getRating());

      // volatilityが0になる対策
      if (player.getVolatility() == 0) {
        player.setVolatility(300);
      }
      player.setNewVolatility(player.getVolatility());
    }

    if (2 <= numberOfInitialHumanPlayers.get()) {
      List<PlayerStatus> humanPlayers = Lists.newArrayList();
      for (int i = 0; i < numberOfInitialHumanPlayers.get(); ++i) {
        humanPlayers.add(playerStatuses.get(i));
      }

      calculateRating(humanPlayers);
    }

    // スレッド同期のためローカルでリストを作ってからフィールドに代入する
    List<PacketResult> packetResult = new ArrayList<PacketResult>();
    for (int i = 0; i < players.length; ++i) {
      packetResult.add(players[i].toResult());
    }
    this.packetResult = packetResult;

    if (theme != null) {
      for (PlayerStatus playerStatus : players) {
        int userCode = playerStatus.getUserCode();
        if (userCode < 0) {
          continue;
        }

        int score = playerStatus.getScore();
        try {
          database.updateThemeModeScore(userCode, theme, score);
        } catch (DatabaseException e) {
          logger.log(Level.WARNING, "テーマモードスコアの保存に失敗しました", e);
        }
      }
    }

    return Transition.Result;
  }

  @VisibleForTesting
  void calculateRating(List<PlayerStatus> players) {
    NormalDistribution normalDistribution = new NormalDistribution();

    // レーティング計算
    // http://topcoder.g.hatena.ne.jp/n4_t/20081222/
    // http://apps.topcoder.com/wiki/display/tc/Algorithm+Competition+Rating+System
    Preconditions.checkState(2 <= players.size());

    int numCoders = players.size();
    double sumRating = 0.0;
    for (PlayerStatus player : players) {
      sumRating += player.getRating();
    }
    double aveRating = sumRating / numCoders;

    // The competition factor is calculated:
    double sumVolatility2 = 0.0;
    double sumDiffRatingAveRating = 0.0;
    for (PlayerStatus player : players) {
      sumVolatility2 += player.getVolatility() * player.getVolatility();
      double diffRatingAveRating = player.getRating() - aveRating;
      sumDiffRatingAveRating += diffRatingAveRating * diffRatingAveRating;
    }
    double cf = Math.sqrt(sumVolatility2 / numCoders + sumDiffRatingAveRating / (numCoders - 1));

    // 順位を計算する
    Collections.sort(players, new Comparator<PlayerStatus>() {
      @Override
      public int compare(PlayerStatus o1, PlayerStatus o2) {
        int black1;
        int black2;
        try {
          int userCode1 = o1.getUserCode();
          int rating1 = o1.getRating();
          int userCode2 = o2.getUserCode();
          int rating2 = o2.getRating();
          black1 = (restrictedUserUtils.checkAndUpdateRestrictedUser(userCode1, "127.0.0.1",
              RestrictionType.MATCH) && rating1 > 1700) ? 1 : 0;
          black2 = (restrictedUserUtils.checkAndUpdateRestrictedUser(userCode2, "127.0.0.1",
              RestrictionType.MATCH) && rating2 > 1700) ? 1 : 0;
        } catch (DatabaseException e) {
          throw Throwables.propagate(e);
        }
        return black1 != black2 ? black1 - black2 : o2.getScore() - o1.getScore();
      }
    });
    for (int i = 0; i < players.size(); ++i) {
      if (0 < i && players.get(i - 1).getScore() == players.get(i).getScore()) {
        // 同点ならどう順位
        players.get(i).setHumanRank(players.get(i - 1).getHumanRank());
      } else {
        players.get(i).setHumanRank(i + 1);
      }
    }
    // 制限ユーザーは最下位扱いとする
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack-QMAClone%2F490
    // for (PlayerStatus playerStatus : players) {
    // if (badUserManager.isLimitedUser(playerStatus.getUserCode(), null)) {
    // playerStatus.setHumanRank(players.size());
    // }
    // }

    for (PlayerStatus my : players) {
      if (!my.isHuman()) {
        continue;
      }

      double myRating = my.getRating();
      double myVolatility = my.getVolatility();

      // Win Probability Estimation Algorithm:
      double eRank = 0.5;
      for (PlayerStatus player : players) {
        double hisVolatility = player.getVolatility();
        double wp = 0.5;
        wp = 0.5 * (Erf
            .erf((player.getRating() - myRating)
                / Math.sqrt(2 * (hisVolatility * hisVolatility + myVolatility * myVolatility)))
            + 1.0);

        // BugTrack-QMAClone/603 - QMAClone wiki
        // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F603
        if (my != player && my.getUserCode() == player.getUserCode()) {
          wp = 0.0;
        }
        eRank += wp;
      }

      // The expected performance of the coder is calculated:
      double ePerf = -normalDistribution.inverseCumulativeProbability((eRank - 0.5) / numCoders);

      // The actual performance of each coder is calculated:
      double aPerf = -normalDistribution
          .inverseCumulativeProbability((my.getHumanRank() - 0.5) / numCoders);

      // The performed as rating of the coder is calculated:
      double perfAs = myRating + cf * (aPerf - ePerf);

      // The weight of the competition for the coder is calculated:
      double weight = 1.0 / (1 - (0.42 / (my.getPlayCount() + 1) + 0.18)) - 1.0;

      // A cap is calculated:
      double cap = 150 + 1500 / (my.getPlayCount() + 2);

      // The new rating of the coder is calculated:
      double newRating = (myRating + weight * perfAs) / (1.0 + weight);
      newRating = Math.min(newRating, myRating + cap);
      newRating = Math.max(newRating, myRating - cap);

      // The new volatility of the coder is calculated:
      double diffRating = newRating - myRating;
      double newVolatility = Math
          .sqrt(diffRating * diffRating / weight + myVolatility * myVolatility / (weight + 1));

      my.setNewRating((int) Math.rint(newRating));
      my.setNewVolatility((int) Math.rint(newVolatility));
    }

    // ブラックリスト入りを含めずにランキング再計算
    Collections.sort(players, new Comparator<PlayerStatus>() {
      @Override
      public int compare(PlayerStatus o1, PlayerStatus o2) {
        return o2.getScore() - o1.getScore();
      }
    });
    for (int i = 0; i < players.size(); ++i) {
      if (0 < i && players.get(i - 1).getScore() == players.get(i).getScore()) {
        // 同点ならどう順位
        players.get(i).setHumanRank(players.get(i - 1).getHumanRank());
      } else {
        players.get(i).setHumanRank(i + 1);
      }
    }
  }

  /**
   * Ready状態を更新する
   * 
   * @return 状態
   */
  private synchronized Transition updateResultState() {
    if (secondsToNextState.decrementAndGet() < 0) {
      return transitFromResultToFinished();
    }

    return Transition.Result;
  }

  /**
   * Result状態からFinished状態へ移行する
   * 
   * @return 状態
   */
  private synchronized Transition transitFromResultToFinished() {
    // タイマーを終了させる
    timer.cancel(false);
    return Transition.Finished;
  }

  // 回線が生きていることを通知する
  public synchronized void keepAlive(int playerListId) {
    if (playerListId >= 0) {
      playerStatuses.get(playerListId).clearSkipCount();
    }
  }

  public synchronized List<PacketResult> getPacketResult() {
    return packetResult;
  }

  private volatile PacketGameStatus gameStatus;

  public synchronized PacketGameStatus getGameStatus() {
    Preconditions.checkNotNull(gameStatus, "ゲーム状態がnullです: sessionId=" + sessionId);
    return gameStatus;
  }

  public synchronized void updateGameStatus() {
    PacketGameStatus status = new PacketGameStatus();
    status.problemCounter = problemCounter.get();
    status.restMs = getRestProblemMs();
    status.transition = transition.get();
    status.status = new PacketGameStatus.GamePlayerStatus[playerStatuses.size()];
    for (int playerIndex = 0; playerIndex < playerStatuses.size(); ++playerIndex) {
      GamePlayerStatus gamePlayerStatus = new GamePlayerStatus();
      PlayerStatus playerStatus = playerStatuses.get(playerIndex);
      gamePlayerStatus.score = playerStatus.getScore();
      gamePlayerStatus.answer = playerStatus.getAnswer();
      gamePlayerStatus.rank = playerStatus.getTempRanking();
      status.status[playerIndex] = gamePlayerStatus;
    }

    for (PlayerStatus playerStatus : playerStatuses) {
      if (playerStatus.isHuman() && !playerStatus.shouldBeDropped()) {
        ++status.numberOfPlayingHumans;
      }
    }

    this.gameStatus = status;
  }

  public synchronized List<PacketPlayerSummary> getPlayerSummaries() {
    List<PacketPlayerSummary> summaries = Lists.newArrayList();
    for (PlayerStatus player : playerStatuses) {
      summaries.add(player.getPlayerSummary());
    }
    return summaries;
  }

  public int getSessionId() {
    // sessionIdは変更されないのでsynchronized不要
    return sessionId;
  }

  private synchronized void calculateRanking() {
    // 順位決定
    PlayerStatus[] players = playerStatuses.toArray(new PlayerStatus[0]);
    Arrays.sort(players, new Comparator<PlayerStatus>() {
      public int compare(PlayerStatus o1, PlayerStatus o2) {
        return o2.getScore() - o1.getScore();
      }
    });
    for (int i = 0; i < players.length; ++i) {
      players[i].setTempRanking(i + 1);
    }
  }

  public boolean isEvent() {
    // eventは更新されることがないのでsynchronized不要
    return event;
  }

  public boolean isPublicEvent() {
    // publicEventは更新されることがないのでsynchronized不要
    return publicEvent;
  }

  public synchronized Set<Integer> getTestingProblemIds() {
    if (getTransition() == Transition.Problem) {
      return ImmutableSet.of(problems.get(problemCounter.get()).id);
    }
    return Sets.newHashSet();
  }
}
