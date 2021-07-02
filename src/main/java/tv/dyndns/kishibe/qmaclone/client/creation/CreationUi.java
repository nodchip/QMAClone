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
package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.bbs.PanelBbs;
import tv.dyndns.kishibe.qmaclone.client.creation.ChangeHistoryView.ChangeHistoryPresenter;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.Evaluation;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.WidgetTimeProgressBar;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanelFactory;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.report.ProblemReportUi;
import tv.dyndns.kishibe.qmaclone.client.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CreationUi extends Composite implements ChangeHistoryPresenter {
  private static final Logger logger = Logger.getLogger(CreationUi.class.getName());
  private static final int MAX_SIMILER_PROBLEMS_PER_PAGE = 10;
  private static final CreationUiUiBinder uiBinder = GWT.create(CreationUiUiBinder.class);
  @VisibleForTesting
  static final String MESSAGE_UPDATE_NOTE = "問題ノートを更新してください";
  // TODO(nodchip): テストが落ちる原因を特定して削除する
  @VisibleForTesting
  static boolean SUPPRESS_GET_SIMILAR_PROBLEM_FOR_TESTING = false;
  private static final int MIN_NUMBER_OF_PROBLEM_TO_SHOW_PROBLEM_FORM = 30;

  interface CreationUiUiBinder extends UiBinder<Widget, CreationUi> {
  }

  @UiField
  HTMLPanel htmlPanelSorry;
  @UiField
  HTMLPanel htmlRequireGooglePlusLogin;
  @UiField
  HTMLPanel htmlPanelMain;
  @UiField
  HTMLPanel htmlPanelDone;
  @UiField
  Button buttonNewProblem;
  @UiField
  Button buttonMoveToVerification;
  @UiField
  Button buttonSendProblem;
  @UiField
  SimplePanel panelSimilar;
  @UiField
  HTMLPanel htmlPanelWrongAnswer;
  @UiField
  SimplePanel panelWrongAnswer;
  @UiField
  HTMLPanel htmlPanelBbs;
  @UiField
  SimplePanel panelBbs;
  @UiField
  SimplePanel panelSample;
  @UiField
  TextBox textBoxGetProblem;
  @UiField
  Button buttonGetProblem;
  @UiField
  Button buttonCopyProblem;
  @UiField
  SimplePanel panelProblemForm;
  @UiField
  VerticalPanel panelWarning;
  @UiField
  HTML htmlTypeCaution;
  @UiField
  SimplePanel panelChangeHistory;
  @UiField
  Label labelProblemId;
  @UiField
  Button buttonNextProblem;

  @VisibleForTesting
  WidgetProblemForm widgetProblemForm;
  private boolean sendingProblem = false;
  private final RepeatingCommand commandCheckProblem = new RepeatingCommand() {
    @Override
    public boolean execute() {
      boolean enabled = validateProblem();
      buttonMoveToVerification.setEnabled(enabled && !sendingProblem);
      buttonSendProblem.setEnabled(enabled && !sendingProblem);
      return isAttached();
    }
  };
  private boolean copyProblem;
  private final RepeatingCommand commandCheckProblemId = new RepeatingCommand() {
    @Override
    public boolean execute() {
      boolean enabled = checkProblemId();
      buttonCopyProblem.setEnabled(enabled);
      buttonGetProblem.setEnabled(enabled);
      return isAttached();
    }
  };

  private final WrongAnswerPresenter wrongAnswerPresenter;

  public CreationUi(WrongAnswerPresenter wrongAnswerPresenter) {
    this.wrongAnswerPresenter = Preconditions.checkNotNull(wrongAnswerPresenter);
    initWidget(uiBinder.createAndBindUi(this));
    reset();
  }

  public void reset() {
    buttonSendProblem.setVisible(false);

    htmlPanelSorry.setVisible(false);
    htmlRequireGooglePlusLogin.setVisible(false);
    htmlPanelMain.setVisible(false);
    htmlPanelDone.setVisible(false);
    htmlPanelWrongAnswer.setVisible(false);
    htmlPanelBbs.setVisible(false);

    // TODO(nodchip): 問題作成時にGoogle+ログインを強制する
    if (UserData.get().getPlayCount() < MIN_NUMBER_OF_PROBLEM_TO_SHOW_PROBLEM_FORM) {
      htmlPanelSorry.setVisible(true);
      // } else if (Strings.isNullOrEmpty(UserData.get().getGooglePlusId())) {
      // htmlRequireGooglePlusLogin.setVisible(true);
    } else {
      htmlPanelMain.setVisible(true);
    }

    panelSimilar.clear();
    panelWrongAnswer.clear();
    panelSample.clear();
    panelWarning.clear();
    panelChangeHistory.clear();

    widgetProblemForm = new WidgetProblemForm(this);
    panelProblemForm.setWidget(widgetProblemForm);
    textBoxGetProblem.setText(null);
    // previousProblemNote = null;
  }

  /**
   * 出題形式の説明文を設定する
   * 
   * @param text 説明文
   */
  public void setTypeDescription(String text) {
    if (Strings.isNullOrEmpty(text)) {
      htmlTypeCaution.setHTML("");
      return;
    }

    htmlTypeCaution.setHTML(new SafeHtmlBuilder().appendEscapedLines(text).toSafeHtml());
  }

  /**
   * 入力されている問題の形式が正しいかどうか調べる
   * 
   * @return 正しいならtrue
   */
  @VisibleForTesting
  boolean validateProblem() {
    clearWarnings();

    if (widgetProblemForm == null) {
      return false;
    }

    PacketProblem problem = widgetProblemForm.getProblem();
    Evaluation eval = problem.type.validate(problem);

    // 問題ノートの更新
    // BugTrack-QMAClone/589 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F589
    // if (previousProblemNote != null && previousProblemNote.equals(problem.note))
    // {
    // eval.warn.add(MESSAGE_UPDATE_NOTE);
    // }

    for (String warning : eval.warn) {
      addWarnings(warning);
    }

    for (String info : eval.info) {
      addInfo(info);
    }

    return !eval.hasWarning();
  }

  private void setProblemSample(PacketProblem problem) {
    problem.prepareShuffledAnswersAndChoices();
    WidgetTimeProgressBar widgetTimeProgressBar = new WidgetTimeProgressBar();
    QuestionPanel panelQuestion = QuestionPanelFactory.create(problem, widgetTimeProgressBar,
        new SessionData(-1, -1, false, false, false));
    panelQuestion.enableInput(false);
    panelQuestion.showCorrectRatioAndCreator();
    panelSample.setWidget(panelQuestion);
  }

  private void getSimilarProblems(PacketProblem problem) {
    if (SUPPRESS_GET_SIMILAR_PROBLEM_FOR_TESTING) {
      return;
    }
    Service.Util.getInstance().searchSimilarProblem(problem, callbackSearchSimilarProblem);
  }

  private final AsyncCallback<List<PacketProblem>> callbackSearchSimilarProblem = new AsyncCallback<List<PacketProblem>>() {
    public void onSuccess(List<PacketProblem> result) {
      panelSimilar.setWidget(new ProblemReportUi(result, true, true, MAX_SIMILER_PROBLEMS_PER_PAGE));
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "類似問題の検索に失敗しました", caught);
    }
  };

  public void getWrongAnswers(int problemID) {
    Service.Util.getInstance().getWrongAnswers(problemID, callbackGetWrongAnswers);
  }

  @VisibleForTesting
  final AsyncCallback<List<PacketWrongAnswer>> callbackGetWrongAnswers = new AsyncCallback<List<PacketWrongAnswer>>() {

    public void onSuccess(List<PacketWrongAnswer> result) {
      panelWrongAnswer.setWidget(wrongAnswerPresenter.asWidget());
      wrongAnswerPresenter.setWrongAnswers(result, widgetProblemForm.getProblem());
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "誤回答の取得に失敗しました", caught);
    }
  };

  private final AsyncCallback<Integer> callbackUploadProblem = new AsyncCallback<Integer>() {
    public void onSuccess(Integer result) {
      int userCode = UserData.get().getUserCode();

      htmlPanelMain.setVisible(false);
      htmlPanelDone.setVisible(true);
      labelProblemId.setText(Integer.toString(result));

      if (UserData.get().isRegisterCreatedProblem()) {
        Service.Util.getInstance().addProblemIdsToReport(userCode, ImmutableList.of(result),
            callbackAddProblemIdsToReport);
      }

      sendingProblem = false;
      setEnable(true);

      // 回答数リセット
      PacketProblem problem = widgetProblemForm.getProblem();
      if (problem.needsResetAnswerCount) {
        Service.Util.getInstance().resetProblemCorrectCounter(UserData.get().getUserCode(), result,
            callbackResetProblemCorrectCounter);
      }

    }

    public void onFailure(Throwable caught) {
      sendingProblem = false;
      setEnable(true);

      logger.log(Level.WARNING, "問題の送信中にエラーが発生しました", caught);
    }
  };
  private final AsyncCallback<Void> callbackAddProblemIdsToReport = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "問題の登録に失敗しました", caught);
    }
  };

  private void setEnable(boolean enabled) {
    FocusWidget[] widgets = { buttonNewProblem, buttonMoveToVerification, buttonSendProblem, textBoxGetProblem,
        buttonGetProblem, buttonCopyProblem, buttonNextProblem };
    for (FocusWidget widget : widgets) {
      widget.setEnabled(enabled);
    }
    widgetProblemForm.setEnable(enabled);
  }

  private void getProblemFromServer(boolean copy) {
    // TODO(nodchip): テストを書く
    copyProblem = copy;

    int problemId;
    try {
      problemId = Integer.parseInt(StringUtils.toHalfWidth(textBoxGetProblem.getText()));
    } catch (NumberFormatException e) {
      logger.log(Level.WARNING, "入力された問題番号を数字として解釈できませんでした", e);
      return;
    }

    setEnable(false);

    Service.Util.getInstance().getProblemList(ImmutableList.of(problemId), callbackGetProblemList);

    if (copy) {
      panelChangeHistory.clear();
      panelBbs.clear();
      htmlPanelWrongAnswer.setVisible(false);
      htmlPanelBbs.setVisible(false);
    } else {
      panelChangeHistory.setWidget(new ChangeHistoryViewImpl(this));
      Service.Util.getInstance().getProblemCreationLog(problemId, callbackGetProblemCreationLog);

      panelBbs.setWidget(new PanelBbs(problemId));
      htmlPanelWrongAnswer.setVisible(true);
      htmlPanelBbs.setVisible(true);
    }
  }

  private final AsyncCallback<List<PacketProblem>> callbackGetProblemList = new AsyncCallback<List<PacketProblem>>() {
    public void onSuccess(List<PacketProblem> result) {
      String message = "無効な問題番号が指定されました";
      if (result == null || result.isEmpty()) {
        logger.log(Level.WARNING, message);
        return;
      }

      PacketProblem problem = result.get(0);
      if (problem == null) {
        logger.log(Level.WARNING, message);
        return;
      }

      if (copyProblem) {
        problem = problem.cloneForCopyingProblem();
      }
      widgetProblemForm.setProblem(problem);

      if (copyProblem) {
        panelSimilar.clear();
        panelWrongAnswer.clear();
        panelSample.clear();
        panelChangeHistory.clear();
        // previousProblemNote = null;
      } else {
        setProblemSample(problem);
        getWrongAnswers(problem.id);
        panelSimilar.clear();
        getSimilarProblems(problem);
        // previousProblemNote = problem.note;
      }

      setEnable(true);
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "問題の取得中にエラーが発生しました", caught);
      setEnable(true);
    }
  };
  private final AsyncCallback<List<PacketProblemCreationLog>> callbackGetProblemCreationLog = new AsyncCallback<List<PacketProblemCreationLog>>() {
    @Override
    public void onSuccess(List<PacketProblemCreationLog> result) {
      ChangeHistoryView view = (ChangeHistoryView) panelChangeHistory.getWidget();
      view.setCreationLog(result);
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "問題変更ログの取得に失敗しました", caught);
    }
  };

  private void clearWarnings() {
    panelWarning.clear();
  }

  private void addWarnings(String warning) {
    HTML w = new HTML(SafeHtmlUtils.fromString(warning));
    w.addStyleName("gwt-HTML-problemCreationWarning");
    panelWarning.add(w);
  }

  private void addInfo(String info) {
    HTML w = new HTML(SafeHtmlUtils.fromString(info));
    w.addStyleName("gwt-HTML-problemCreationInfo");
    panelWarning.add(w);
  }

  public void setProblem(int problemID) {
    textBoxGetProblem.setText(String.valueOf(problemID));
    getProblemFromServer(false);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    Scheduler.get().scheduleFixedDelay(commandCheckProblem, 1000);
    Scheduler.get().scheduleFixedDelay(commandCheckProblemId, 1000);

    // 連続投稿制限のチェック
    int userCode = UserData.get().getUserCode();
    Service.Util.getInstance().canUploadProblem(userCode, null, callbackCanUploadProblemOnLoad);
  }

  private final AsyncCallback<Boolean> callbackCanUploadProblemOnLoad = new AsyncCallback<Boolean>() {
    @Override
    public void onSuccess(Boolean result) {
      if (result) {
        return;
      }

      showRepeatedPostWarning();
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "連続投稿制限のチェックに失敗しました", caught);

      sendingProblem = false;
      widgetProblemForm.setEnable(true);
      setEnable(true);
    }
  };

  private void showRepeatedPostWarning() {
    final DialogBox dialogBox = new DialogBox(true);

    VerticalPanel panel = new VerticalPanel();
    panel.add(new HTML(new SafeHtmlBuilder()
        .appendEscapedLines("現在アニメジャンルにおいて連続投稿制限中です。\n" + "送信した問題が受け付けられない場合があります。\n" + "その他のジャンルは通常通り投稿できます。")
        .toSafeHtml()));
    panel.add(new Button("OK", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    }));
    dialogBox.setWidget(panel);
    dialogBox.setAnimationEnabled(true);
    dialogBox.setGlassEnabled(true);
    dialogBox.setHTML(SafeHtmlUtils.fromString("連続投稿制限"));
    dialogBox.setPopupPosition(100, 100);
    dialogBox.show();
  }

  @UiHandler("buttonNewProblem")
  void onButtonNewProblem(ClickEvent e) {
    reset();
  }

  @UiHandler("buttonMoveToVerification")
  void onButtonMoveToVerification(ClickEvent e) {
    if (!validateProblem()) {
      return;
    }

    buttonMoveToVerification.setText("問題を修正して再度送信確認画面に移動する");
    buttonSendProblem.setVisible(true);

    PacketProblem problem = widgetProblemForm.getProblem();
    setProblemSample(problem);
    panelSimilar.clear();
    getSimilarProblems(problem);
  }

  @UiHandler("buttonSendProblem")
  void onButtonSendProblem(ClickEvent e) {
    if (!validateProblem()) {
      return;
    }

    sendingProblem = true;
    widgetProblemForm.setEnable(false);
    setEnable(false);

    PacketProblem problem = widgetProblemForm.getProblem();
    int userCode = UserData.get().getUserCode();

    if (problem.genre == ProblemGenre.Anige) {
      // アニゲの場合、連続投稿制限に引っかかっていないかどうかのチェック
      Service.Util.getInstance().canUploadProblem(userCode, problem.id == -1 ? null : problem.id,
          callbackCanUploadProblem);
    } else {
      uploadProblem();
    }
  }

  private final AsyncCallback<Boolean> callbackCanUploadProblem = new AsyncCallback<Boolean>() {
    @Override
    public void onSuccess(Boolean result) {
      if (!result) {
        Window.alert("連続投稿制限: 時間を置いて再度お試しください\n" + "http://kishibe.dyndns.tv/QMAClone/　からアクセスしている場合は\n"
            + "http://kishibe.dyndns.tv:8080/QMAClone/　から御アクセスください。");
        sendingProblem = false;
        widgetProblemForm.setEnable(true);
        setEnable(true);
        return;
      }

      uploadProblem();
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "連続投稿制限のチェックに失敗しました", caught);

      sendingProblem = false;
      widgetProblemForm.setEnable(true);
      setEnable(true);
    }
  };

  private void uploadProblem() {
    PacketProblem problem = widgetProblemForm.getProblem();

    // 同一人物による同一タイトルの作問について · Issue #1087 · nodchip/QMAClone
    // https://github.com/nodchip/QMAClone/issues/1087
    if (problem.indication != null) {
      if (!Window.confirm("指摘が解除されていません。キャンセルを押し、指摘を解除してから再度送信することをお勧めいたします。\nそのまま送信しますか？")) {
        sendingProblem = false;
        widgetProblemForm.setEnable(true);
        setEnable(true);
        return;
      }
    }

    int userCode = UserData.get().getUserCode();
    boolean resetAnswerCount = widgetProblemForm.isReserveResetAnswerCount();

    // プレイヤー回答削除
    if (problem.needsRemovePlayerAnswers) {
      Service.Util.getInstance().removePlayerAnswers(problem.id, callbackRemovePlayerAnswers);
    }

    // 良問悪問投票リセット
    if (problem.needsResetVote) {
      Service.Util.getInstance().resetVote(problem.id, callbackResetVote);
    }

    Service.Util.getInstance().uploadProblem(problem, userCode, resetAnswerCount, callbackUploadProblem);
  }

  private final AsyncCallback<Void> callbackRemovePlayerAnswers = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "誤解答の削除に失敗しました", caught);
    }
  };
  private final AsyncCallback<Boolean> callbackResetProblemCorrectCounter = new AsyncCallback<Boolean>() {
    @Override
    public void onSuccess(Boolean result) {
      if (!result) {
        Window.alert("回答数リセット回数の上限に達しました。\n回答数はリセットされませんでした。\n時間をおいて操作をしてください。");
      }
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "問題正答数リセット権限のチェックに失敗しました");
    }
  };
  private final AsyncCallback<Void> callbackResetVote = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "投票のリセットに失敗しました", caught);
    }
  };

  @UiHandler("buttonGetProblem")
  void onButtonGetProblem(ClickEvent e) {
    getProblemFromServer(false);
  }

  @UiHandler("buttonCopyProblem")
  void onButtonCopyProblem(ClickEvent e) {
    getProblemFromServer(true);
  }

  @UiHandler("buttonNextProblem")
  void onButtonNextProblem(ClickEvent e) {
    reset();

    // 連続投稿制限のチェック
    int userCode = UserData.get().getUserCode();
    Service.Util.getInstance().canUploadProblem(userCode, null, callbackCanUploadProblemOnLoad);
  }

  private boolean checkProblemId() {
    try {
      Integer.parseInt(textBoxGetProblem.getText());
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  public void reloadBbs() {
    PanelBbs bbs = (PanelBbs) panelBbs.getWidget();
    if (bbs == null) {
      return;
    }
    bbs.reload();
  }

  @Override
  public void onUpdateDiffTarget(PacketProblemCreationLog before, PacketProblemCreationLog after) {
    if (before == null || after == null) {
      return;
    }

    Service.Util.getInstance().generateDiffHtml(before.summary, after.summary, callbackGenerateDiffHtml);
  }

  private final AsyncCallback<String> callbackGenerateDiffHtml = new AsyncCallback<String>() {
    @Override
    public void onSuccess(String result) {
      ChangeHistoryView view = (ChangeHistoryView) panelChangeHistory.getWidget();
      view.setDiffHtml(SafeHtmlUtils.fromSafeConstant(result));
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "差分htmlの生成に失敗しました", caught);
    }
  };
}
