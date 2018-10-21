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
package tv.dyndns.kishibe.qmaclone.client.game;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.widgetideas.graphics.client.ImageLoader;

import tv.dyndns.kishibe.qmaclone.client.Controller;
import tv.dyndns.kishibe.qmaclone.client.SceneBase;
import tv.dyndns.kishibe.qmaclone.client.SceneResult;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.SoundPlayer;
import tv.dyndns.kishibe.qmaclone.client.StatusUpdater;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.left.WidgetPlayerList;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanelFactory;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus.GamePlayerStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingPlayer;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class SceneGame extends SceneBase implements ClosingHandler, CloseHandler<Window> {

  private static class GameStatusUpdater extends StatusUpdater<PacketGameStatus> {
    private final SceneGame scene;
    private final SessionData sessionData;

    public GameStatusUpdater(SceneGame scene, SessionData sessionData) {
      super(PacketGameStatus.class.getName() + "?" + Constant.KEY_GAME_SESSION_ID + "="
          + sessionData.getSessionId(), 1000);
      this.scene = Preconditions.checkNotNull(scene);
      this.sessionData = Preconditions.checkNotNull(sessionData);
    }

    @Override
    protected void request(AsyncCallback<PacketGameStatus> callback) {
      int sessionId = sessionData.getSessionId();
      Service.Util.getInstance().getGameStatus(sessionId, callback);
    }

    @Override
    protected void onReceived(PacketGameStatus status) {
      scene.updateGameStatus(status);
    }

    @Override
    protected PacketGameStatus parse(String json) {
      return PacketGameStatus.Json.READER.read(json);
    }
  }

  private static final Logger logger = Logger.getLogger(SceneGame.class.getName());
  private final PanelGame panel = new PanelGame();
  private final WidgetPlayerList playerList;
  private QuestionPanel question;
  private List<PacketProblem> problems;
  private int problemCounter = 0;
  private WidgetTimeProgressBar widgetTimeProgressBar;
  private Transition lastTransition = null;
  private HandlerRegistration handlerRegistrationCloseHandler = null;
  private HandlerRegistration handlerRegistrationClosingHandler = null;
  private boolean first = true;
  private boolean transited = false;
  private final StatusUpdater<PacketGameStatus> updater;
  private final SessionData sessionData;

  public SceneGame(List<PacketProblem> problems, List<PacketMatchingPlayer> players,
      SessionData sessionData) {
    this.problems = problems;
    this.playerList = new WidgetPlayerList(players);
    this.sessionData = Preconditions.checkNotNull(sessionData);
    this.updater = new GameStatusUpdater(this, sessionData);
    panel.setPlayerList(playerList);
    Controller.getInstance().setGamePanel(panel);
  }

  /**
   * プレイヤー名を更新する
   */
  private void updatePlayerNames() {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        int sessionId = sessionData.getSessionId();
        Service.Util.getInstance().getPlayerSummaries(sessionId, callbackGetPalyerSummaries);
      }
    });
  }

  private final AsyncCallback<List<PacketPlayerSummary>> callbackGetPalyerSummaries = new AsyncCallback<List<PacketPlayerSummary>>() {
    public void onSuccess(List<PacketPlayerSummary> result) {
      try {
        if (result == null) {
          logger.log(Level.WARNING, "無効なプレイヤー情報リストが返されました");
          return;
        }

        for (int i = 0; i < result.size(); ++i) {
          if (result.get(i) != null) {
            continue;
          }

          logger.log(Level.WARNING, "無効なプレイヤー情報が返されました - " + result.toString());
          result.set(i, PacketPlayerSummary.getDefaultPlayerSummary());
        }

        playerList.setPlayerSummary(result);

      } catch (Exception e) {
        logger.log(Level.WARNING, "プレイヤー情報反映中にエラーが発生しました", e);
      }
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "プレイヤー情報リストの取得に失敗しました", caught);
    }
  };

  public void onSendAnswer() {
    playerList.onSendAnswer();
  }

  @Override
  protected void onLoad() {
    super.onLoad();

    updater.start();

    if (sessionData.isAddPenalty()) {
      handlerRegistrationCloseHandler = Window.addCloseHandler(this);
      handlerRegistrationClosingHandler = Window.addWindowClosingHandler(this);
    }
  }

  @Override
  protected void onUnload() {
    if (sessionData.isAddPenalty()) {
      if (handlerRegistrationCloseHandler != null) {
        handlerRegistrationCloseHandler.removeHandler();
        handlerRegistrationCloseHandler = null;
      }
      if (handlerRegistrationClosingHandler != null) {
        handlerRegistrationClosingHandler.removeHandler();
        handlerRegistrationClosingHandler = null;
      }
    }

    updater.stop();

    super.onUnload();
  }

  @Override
  public void onWindowClosing(ClosingEvent event) {
    event.setMessage("ゲームプレイ中にウィンドウを閉じた場合\nレーティングにペナルティが加わります\nよろしいですか？");
  }

  @Override
  public void onClose(CloseEvent<Window> event) {
    int newRating = UserData.get().getRating() * 99 / 100;
    UserData.get().setRating(newRating);
    UserData.get().save();
  }

  private void updateGameStatus(PacketGameStatus gameStatus) {
    try {
      if (gameStatus == null) {
        logger.log(Level.WARNING, "無効なゲーム状態が返されました");
        return;
      }

      if (gameStatus.numberOfPlayingHumans <= 1) {
        if (handlerRegistrationClosingHandler != null) {
          handlerRegistrationClosingHandler.removeHandler();
          handlerRegistrationClosingHandler = null;
        }
      }

      if (gameStatus.status != null) {
        playerList.onGamePlayerStatusReceived(gameStatus.status);
      }

      if (widgetTimeProgressBar != null) {
        widgetTimeProgressBar.setTime(gameStatus.restMs / 1000);
      }

      if (question != null) {
        question.onReceiveGameStatus(gameStatus);
      }

      if (first || lastTransition != gameStatus.transition) {
        first = false;
        lastTransition = gameStatus.transition;

        keepAlive();

        int playerListId = sessionData.getPlayerListIndex();

        switch (gameStatus.transition) {
        case Problem: {
          // 次の問題へ
          // プレイヤー情報窓クローズ
          problemCounter = gameStatus.problemCounter;

          PacketProblem problem = problems.get(problemCounter);
          widgetTimeProgressBar = new WidgetTimeProgressBar();
          question = QuestionPanelFactory.create(problem, widgetTimeProgressBar, sessionData);
          panel.setQuestionPanel(question);
          panel.setQuestionNumber(problemCounter);

          updatePlayerNames();

          playerList.onNextProblem(problem);
          break;
        }
        case Answer: {
          // 問題の解答発表
          // プレイヤー情報窓オープン
          question.enableInput(false);
          question.showCorrectRatioAndCreator();

          PacketProblem problem = problems.get(problemCounter);
          playerList.onAnswer();

          GamePlayerStatus player = gameStatus.status[playerListId];
          panel.setScore(player.score);

          // 正答数更新
          if (!sessionData.isEvent() || UserData.get().isReflectEventResult()) {
            boolean isCorrect = problem.isCorrect(player.answer);
            int[][][] correctCount = UserData.get().getCorrectCount();
            int goodBadIndex = isCorrect ? 0 : 1;
            for (ProblemGenre genre : Arrays.asList(ProblemGenre.Random, problem.genre)) {
              for (ProblemType type : Arrays.asList(ProblemType.Random, problem.type,
                  ProblemType.fromRandomFlag(problem.randomFlag.getIndex()))) {
                ++correctCount[genre.ordinal()][type.ordinal()][goodBadIndex];
              }
            }
          }

          // 時間切れの通知
          if (Strings.isNullOrEmpty(player.answer)) {
            Service.Util.getInstance().notifyTimeUp(sessionData.getSessionId(), playerListId,
                UserData.get().getUserCode(), callbackNotifyTimeUp);
          }

          // サウンド再生
          if (Strings.isNullOrEmpty(player.answer)) {
            SoundPlayer.getInstance().play(Constant.SOUND_URL_TIME_UP);
          } else if (problem.isCorrect(player.answer)) {
            SoundPlayer.getInstance().play(Constant.SOUND_URL_GOOD);
          } else {
            SoundPlayer.getInstance().play(Constant.SOUND_URL_BAD);
          }

          if (problemCounter + 1 < problems.size()) {
            PacketProblem nextProblem = problems.get(problemCounter + 1);
            ImageLoader.loadImages(nextProblem.getResizedImageUrls().toArray(new String[0]),
                new ImageLoader.CallBack() {
                  @Override
                  public void onImagesLoaded(ImageElement[] imageElements) {
                  }
                });
          }

          break;
        }
        case Result: {
          // リザルト画面へ遷移
          if (!transited) {
            transited = true;
            Controller.getInstance().setScene(new SceneResult(problems, sessionData));
            updater.stop();
          }
          break;
        }
        default:
          break;
        }
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "ゲーム状態反映中にエラーが発生しました", e);
    }
  }

  private final AsyncCallback<Void> callbackNotifyTimeUp = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "時間切れの通知に失敗しました", caught);
    }
  };

  /**
   * ゲームのkeepAliveを行う
   */
  private void keepAlive() {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        int sessionId = sessionData.getSessionId();
        int playerListId = sessionData.getPlayerListIndex();
        Service.Util.getInstance().keepAliveGame(sessionId, playerListId, callbackKeepAliveGame);
      }
    });
  }

  private final AsyncCallback<Void> callbackKeepAliveGame = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.SEVERE, "ゲーム参加のkeepAliveに失敗しました", caught);
    }
  };
}
