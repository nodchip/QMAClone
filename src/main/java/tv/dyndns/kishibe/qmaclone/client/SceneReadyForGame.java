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
package tv.dyndns.kishibe.qmaclone.client;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.widgetideas.graphics.client.ImageLoader;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.SceneGame;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingPlayer;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketReadyForGame;

public class SceneReadyForGame extends SceneBase {

  private static class ReadyForGameStatusUpdater extends StatusUpdater<PacketReadyForGame> {

    private final SceneReadyForGame scene;
    private final SessionData sessionData;

    public ReadyForGameStatusUpdater(SceneReadyForGame scene, SessionData sessionData) {
      super(PacketReadyForGame.class.getName() + "?" + Constant.KEY_GAME_SESSION_ID + "="
          + sessionData.getSessionId(), UPDATE_INTERVAL);
      this.scene = Preconditions.checkNotNull(scene);
      this.sessionData = Preconditions.checkNotNull(sessionData);
    }

    @Override
    protected void request(AsyncCallback<PacketReadyForGame> callback) {
      int sessionId = sessionData.getSessionId();
      Service.Util.getInstance().waitForGame(sessionId, callback);
    }

    @Override
    protected void onReceived(PacketReadyForGame status) {
      scene.callbackWaitForGame.onSuccess(status);
    }

    @Override
    protected PacketReadyForGame parse(String json) {
      return PacketReadyForGame.Json.READER.read(json);
    }

  }

  private static final Logger logger = Logger.getLogger(SceneReadyForGame.class.getName());
  private static final int UPDATE_INTERVAL = 1000;
  private final PanelReadyForGame panel = new PanelReadyForGame();
  private List<PacketProblem> problems;
  private List<PacketMatchingPlayer> players;
  private boolean transited = false;
  private final StatusUpdater<PacketReadyForGame> updater;
  private final SessionData sessionData;

  public SceneReadyForGame(SessionData sessionData) {
    this.sessionData = Preconditions.checkNotNull(sessionData);
    this.updater = new ReadyForGameStatusUpdater(this, sessionData);
    Controller.getInstance().setGamePanel(panel);

    SoundPlayer.getInstance().play(Constant.SOUND_URL_READY_FOR_GAME);
  }

  private final AsyncCallback<PacketReadyForGame> callbackWaitForGame = new AsyncCallback<PacketReadyForGame>() {
    @Override
    public void onSuccess(PacketReadyForGame result) {
      if (transited) {
        return;
      }

      if (panel == null) {
        return;
      }

      int restSeconds = result.restSeconds;
      if (restSeconds > 0) {
        panel.setRestSecond(restSeconds);
        return;
      }

      // ゲーム画面へ移行する
      if (problems == null) {
        logger.log(Level.SEVERE, "問題の取得に失敗しました。F5を押してリロードしてください。");
        updater.stop();
        return;
      }

      if (players == null) {
        logger.log(Level.SEVERE, "プレイヤー情報の取得に失敗しました。F5を押してリロードしてください");
        updater.stop();
        return;
      }

      transited = true;
      Controller.getInstance().setScene(new SceneGame(problems, players, sessionData));
      updater.stop();
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "待機状態の取得中にエラーが発生しました", caught);
    }
  };

  /**
   * 問題を取得する
   */
  private void recieveProblem() {
    final int sessionId = sessionData.getSessionId();

    new RetryAsyncCallback<List<PacketProblem>>() {
      @Override
      protected void request() {
        Service.Util.getInstance().getProblem(sessionId, this);
      }

      @Override
      protected boolean onReceived(List<PacketProblem> result) {
        if (result == null) {
          return false;
        }

        problems = result;
        recievePlayerData();

        // 1問目の画像の事前読み込みを行う
        String[] urls = result.get(0).getResizedImageUrls().toArray(new String[0]);
        ImageLoader.loadImages(urls, null);
        return true;
      }

      @Override
      protected void onLightFailure(Throwable caught) {
        logger.log(Level.WARNING, "問題の取得中にエラーが発生しました。リクエストを再送します。", caught);
      }

      @Override
      protected void onHeavyFailure(Throwable caught) {
        logger.log(Level.SEVERE, "問題の取得に失敗しました。ゲームを中断します。");
        updater.stop();
      }
    }.start();
  }

  /**
   * プレイヤー一覧を取得する
   */
  private void recievePlayerData() {
    final int sessionId = sessionData.getSessionId();
    new RetryAsyncCallback<PacketMatchingStatus>() {
      @Override
      protected void request() {
        Service.Util.getInstance().getMatchingStatus(sessionId, this);
      }

      @Override
      protected boolean onReceived(PacketMatchingStatus result) {
        if (result == null || result.players == null) {
          return false;
        }

        players = result.players;
        panel.setPlayerList(players);
        return true;
      }

      @Override
      protected void onLightFailure(Throwable caught) {
        logger.log(Level.WARNING, "プレイヤーリストの取得にエラーが発生しました。リクエストトを再送します。", caught);
      }

      @Override
      protected void onHeavyFailure(Throwable caught) {
        logger.log(Level.SEVERE, "プレイヤーリストの取得にエラーが発生しました。ゲームを中断します。", caught);
        updater.stop();
      }
    }.start();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    new Timer() {
      @Override
      public void run() {
        // 問題取得 -> プレイヤー一覧取得の順に行う
        recieveProblem();
      }
    }.schedule(3 * 1000);
    updater.start();
  }

  @Override
  protected void onUnload() {
    updater.stop();
    super.onUnload();
  }
}
