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
package tv.dyndns.kishibe.qmaclone.client.game.input;

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Controller;
import tv.dyndns.kishibe.qmaclone.client.SceneBase;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.SoundPlayer;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.SceneGame;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.WidgetTimeProgressBar;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class InputWidget extends VerticalPanel {
  private static final Logger logger = Logger.getLogger(InputWidget.class.getName());
  private static final String[][] LETTERS = { { "1", "2", "3", "4", "5", "6", "7", "8" },
      { "A", "B", "C", "D", "E", "F", "G", "H" } };
  protected static final String ANSWERED = "(解答済)";
  protected static final int DIGIT = 0;
  protected static final int ALPHA = 1;
  protected WidgetTimeProgressBar widgetTimeProgressBar;
  protected final PacketProblem problem;
  protected final AnswerView answerView;
  private final QuestionPanel questionPanel;
  private long startTime;
  private boolean answered = false;
  private final SessionData sessionData;

  protected InputWidget(PacketProblem problem, AnswerView answerView, QuestionPanel questionPanel,
      SessionData sessionData) {
    this.problem = Preconditions.checkNotNull(problem);
    this.answerView = Preconditions.checkNotNull(answerView);
    this.questionPanel = Preconditions.checkNotNull(questionPanel);
    this.sessionData = Preconditions.checkNotNull(sessionData);
    setVerticalAlignment(ALIGN_TOP);
    setHorizontalAlignment(ALIGN_CENTER);
  }

  public abstract void enable(boolean b);

  /**
   * プレイヤーの解答を非表示にする。このクラスを継承したクラスはこのメソッドをオーバーライドすることで、回答非表示の動作を変更することができる。
   */
  protected void hideAnswer() {
    answerView.set(ANSWERED, false);
  }

  /**
   * ゲーム状態を受け取る
   * 
   * @param gameStatus
   *          ゲーム状態
   */
  public void onReceivedGameStatus(PacketGameStatus gameStatus) {
  }

  protected void sendAnswer(String answer) {
    playSound(Constant.SOUND_URL_BUTTON_OK);
    enable(false);

    if (questionPanel != null) {
      questionPanel.showCorrectRatioAndCreator();
    }

    if (UserData.get().isHideAnswer()) {
      hideAnswer();
    }

    if (answered) {
      String message = "解答が重複して送信されました: "
          + MoreObjects.toStringHelper(this).add("sessionId", sessionData.getSessionId())
              .add("playerListIndex", sessionData.getPlayerListIndex())
              .add("userCode", UserData.get().getUserCode()).add("problem", problem).toString();
      logger.log(Level.WARNING, message);
      return;
    }

    int sessionId = sessionData.getSessionId();
    if (sessionId <= 0) {
      String message = "セッションIDが不正です: "
          + MoreObjects.toStringHelper(this).add("sessionId", sessionId)
              .add("playerListIndex", sessionData.getPlayerListIndex())
              .add("userCode", UserData.get().getUserCode()).add("problem", problem).toString();
      logger.log(Level.WARNING, message);
      return;
    }

    int playerListId = sessionData.getPlayerListIndex();
    int responseTime = (int) (System.currentTimeMillis() - startTime);
    if (responseTime > 30 * 1000 + 1000) {
      String message = "時間制限を超えた解答が送信されました: "
          + MoreObjects.toStringHelper(this).add("sessionId", sessionId)
              .add("playerListIndex", sessionData.getPlayerListIndex())
              .add("responseTime", responseTime).add("userCode", UserData.get().getUserCode())
              .add("problem", problem).toString();
      logger.log(Level.WARNING, message);
      return;
    }

    Service.Util.getInstance().sendAnswer(sessionId, playerListId, answer,
        UserData.get().getUserCode(), responseTime, callbackSendAnswer);
    answered = true;
  }

  private final AsyncCallback<Void> callbackSendAnswer = new AsyncCallback<Void>() {
    public void onSuccess(Void result) {
      SceneBase sceneBase = Controller.getInstance().getScene();
      if (!(sceneBase instanceof SceneGame)) {
        logger.log(Level.WARNING, "既にゲームシーンから移行しています: " + sceneBase.toString());
        return;
      }

      SceneGame scene = (SceneGame) sceneBase;
      scene.onSendAnswer();
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "解答の送信中にエラーが発生しました", caught);
    }
  };

  protected void playSound(String url) {
    SoundPlayer.getInstance().play(url);
  }

  public String getAnswerMark(String answer) {
    return answer;
  }

  public String getChoiceMark(String choice) {
    return choice;
  }

  protected String getLetter(int letterType, int letterIndex) {
    return LETTERS[letterType][letterIndex];
  }

  /**
   * %nによる改行が含まれる文字列をSafeHtmlに変換する
   * 
   * @param s
   *          文字列.
   * @return {@link SafeHtml}.
   */
  protected static SafeHtml toMultilineSafeHtml(String s) {
    return new SafeHtmlBuilder().appendEscapedLines(s.replaceAll("%n", "\n")).toSafeHtml();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    startTime = System.currentTimeMillis();
  }
}
