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

import tv.dyndns.kishibe.qmaclone.client.ClientReloadPrompter;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.bbs.PanelBbs;
import tv.dyndns.kishibe.qmaclone.client.creation.ChangeHistoryView.ChangeHistoryPresenter;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.Evaluation;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.WidgetTimeProgressBar;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanelFactory;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketSimilarProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.report.ProblemReportUi;
import tv.dyndns.kishibe.qmaclone.client.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;
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
  HTMLPanel panelCreationModeCards;
  @UiField
  HTMLPanel panelCreationModeNew;
  @UiField
  HTMLPanel panelCreationModeEdit;
  @UiField
  HTMLPanel panelCreationModeClone;
  @UiField
  Label labelCurrentCreationMode;
  @UiField
  HTMLPanel panelProblemLoader;
  @UiField
  Button buttonSelectNewMode;
  @UiField
  Button buttonSelectEditMode;
  @UiField
  Button buttonSelectCloneMode;
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
  @UiField
  HTML htmlStepErrorSummary;
  @UiField
  HTMLPanel panelStep4CardBasic;
  @UiField
  HTMLPanel panelStep4CardQuestion;
  @UiField
  HTMLPanel panelStep4CardAnswer;
  @UiField
  HTML htmlStep4SummaryBasic;
  @UiField
  HTML htmlStep4SummaryQuestion;
  @UiField
  HTML htmlStep4SummaryAnswer;
  @UiField
  HTML htmlStep4DetailBasic;
  @UiField
  HTML htmlStep4DetailQuestion;
  @UiField
  HTML htmlStep4DetailAnswer;
  @UiField
  Button buttonToggleStep1Detail;
  @UiField
  Button buttonToggleStep2Detail;
  @UiField
  Button buttonToggleStep3Detail;
  @UiField
  Button buttonBackToStep1FromSummary;
  @UiField
  Button buttonBackToStep2FromSummary;
  @UiField
  Button buttonBackToStep3FromSummary;
  @UiField
  Label labelStep1;
  @UiField
  Label labelStep2;
  @UiField
  Label labelStep3;
  @UiField
  Label labelStep4;
  @UiField
  Label labelStep5;
  @UiField
  HTMLPanel panelStep1;
  @UiField
  HTMLPanel panelWizardFormHost;
  @UiField
  HTMLPanel panelStep2;
  @UiField
  HTMLPanel panelStep3;
  @UiField
  HTMLPanel panelStep4;
  @UiField
  HTMLPanel panelStep5;
  @UiField
  Button buttonPrevStep;
  @UiField
  Button buttonNextStep;

  @VisibleForTesting
  WidgetProblemForm widgetProblemForm;
  @VisibleForTesting
  int currentStep = 1;
  private static final int STEP_MODE_SELECTION = 1;
  private static final int STEP_BASIC_INFORMATION = 2;
  private static final int STEP_SENTENCE = 3;
  private static final int STEP_ANSWER_SETTING = 4;
  private static final int STEP_CONFIRMATION = 5;
  private static final int MIN_STEP = STEP_MODE_SELECTION;
  private static final int MAX_STEP = STEP_CONFIRMATION;
  private static final String CREATION_WIZARD_DRAFT_KEY = "qmaclone.creationWizardDraft";
  private static final String ERROR_SELECT_GENRE = "ジャンルを選択してください";
  private static final String ERROR_SELECT_TYPE = "出題形式を選択してください";
  private static final String ERROR_INPUT_SENTENCE = "問題文を入力してください";
  private static final String ERROR_INPUT_NON_BLANK_SENTENCE = "問題文は空白のみでは登録できません";
  private static final String ERROR_INPUT_ANSWER1 = "解答1を入力してください";
  private static final String ERROR_CONFIRM_ANSWER_SETTING = "解答設定の入力内容を確認してください";
  private static final String ERROR_SELECT_RANDOM_FLAG = "ランダムフラグを1～4の中から選択してください";
  private static final String FIELD_MODE_SELECTION = "modeSelection";
  private static final String ERROR_LOAD_PROBLEM_BEFORE_NEXT = "問題番号を読み込んでから次へ進んでください";
  private static final int STEP4_SENTENCE_SUMMARY_MAX_LENGTH = 40;
  private static final String STEP4_DETAIL_OPEN_TEXT = "詳細を開く";
  private static final String STEP4_DETAIL_CLOSE_TEXT = "詳細を閉じる";
  private boolean sendingProblem = false;
  private String lastSavedSnapshot = "";
  @VisibleForTesting
  CreationMode creationMode = CreationMode.NEW;
  @VisibleForTesting
  boolean loadedProblemInCurrentMode = true;
  @VisibleForTesting
  int loadingProblemId = -1;
  @VisibleForTesting
  boolean retriedProblemLoad = false;
  private boolean step4BasicDetailOpened = false;
  private boolean step4QuestionDetailOpened = false;
  private boolean step4AnswerDetailOpened = false;
  private boolean step5VerificationReady = false;
  private boolean wizardNavigationEnabled = true;
  private final RepeatingCommand commandCheckProblem = new RepeatingCommand() {
    @Override
    public boolean execute() {
      if (currentStep < MAX_STEP) {
        boolean valid = validateCurrentStepLive();
        buttonNextStep.setEnabled(valid);
        buttonMoveToVerification.setEnabled(false);
        buttonSendProblem.setEnabled(false);
        return isAttached();
      }
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

  /**
   * 問題作成モード。
   */
  enum CreationMode {
    NEW("新規作成"), EDIT("既存を修正"), CLONE("コピーして新規作成");

    private final String label;

    CreationMode(String label) {
      this.label = label;
    }
  }

  public CreationUi(WrongAnswerPresenter wrongAnswerPresenter) {
    this.wrongAnswerPresenter = Preconditions.checkNotNull(wrongAnswerPresenter);
    initWidget(uiBinder.createAndBindUi(this));
    reset();
  }

  public void reset() {
    currentStep = 1;
    buttonSendProblem.setVisible(false);

    htmlPanelSorry.setVisible(false);
    htmlRequireGooglePlusLogin.setVisible(false);
    htmlPanelMain.setVisible(false);
    htmlPanelDone.setVisible(false);
    htmlPanelWrongAnswer.setVisible(false);
    htmlPanelBbs.setVisible(false);

    // TODO(nodchip): 問題作成時に外部アカウントログインを強制する
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
    step5VerificationReady = false;
    panelWarning.clear();
    panelChangeHistory.clear();

    widgetProblemForm = new WidgetProblemForm(this);
    panelProblemForm.setWidget(widgetProblemForm);
    widgetProblemForm.setWizardStep(currentStep);
    widgetProblemForm.clearStepErrors();
    htmlStepErrorSummary.setHTML("");
    resetStep4DetailState();
    wizardNavigationEnabled = true;
    lastSavedSnapshot = createProblemSnapshot();
    textBoxGetProblem.setText(null);
    applyCreationMode(CreationMode.NEW);
    goToStep(STEP_MODE_SELECTION);
    // previousProblemNote = null;
  }

  @VisibleForTesting
  void goToStep(int step) {
    currentStep = Math.max(MIN_STEP, Math.min(MAX_STEP, step));
    if (widgetProblemForm != null) {
      widgetProblemForm.setWizardStep(currentStep);
    }
    updateStepVisibility();
    updateStepIndicator();
    validateCurrentStepLive();
    if (currentStep == STEP_CONFIRMATION) {
      updateStep4Summary();
      if (step5VerificationReady) {
        updateStep5RelatedPanels();
      } else {
        clearStep5RelatedPanels();
      }
    }
    updateWizardNavigationButtons();
  }

  /**
   * ステップと有効状態に応じて、ウィザードの戻る/次へボタン状態を更新する。
   */
  private void updateWizardNavigationButtons() {
    buttonPrevStep.setEnabled(wizardNavigationEnabled && currentStep > MIN_STEP);
    buttonNextStep.setEnabled(wizardNavigationEnabled && currentStep < MAX_STEP);
  }

  private void updateStepVisibility() {
    panelWizardFormHost.setVisible(currentStep >= STEP_BASIC_INFORMATION && currentStep <= STEP_ANSWER_SETTING);
    panelStep1.setVisible(currentStep == STEP_MODE_SELECTION);
    panelStep2.setVisible(currentStep == STEP_BASIC_INFORMATION);
    panelStep3.setVisible(currentStep == STEP_SENTENCE);
    panelStep4.setVisible(currentStep == STEP_ANSWER_SETTING);
    panelStep5.setVisible(currentStep == STEP_CONFIRMATION);
  }

  @VisibleForTesting
  void updateStepIndicator() {
    updateStepLabelStyle(labelStep1, 1);
    updateStepLabelStyle(labelStep2, 2);
    updateStepLabelStyle(labelStep3, 3);
    updateStepLabelStyle(labelStep4, 4);
    updateStepLabelStyle(labelStep5, 5);
  }

  private void updateStepLabelStyle(Label label, int step) {
    label.removeStyleName("creationWizardStepCurrent");
    label.removeStyleName("creationWizardStepDone");
    if (step == currentStep) {
      label.addStyleName("creationWizardStepCurrent");
      return;
    }
    if (step < currentStep) {
      label.addStyleName("creationWizardStepDone");
    }
  }

  /**
   * 問題作成モードを画面に反映する。
   *
   * @param mode 問題作成モード
   */
  @VisibleForTesting
  void applyCreationMode(CreationMode mode) {
    creationMode = Preconditions.checkNotNull(mode);
    loadedProblemInCurrentMode = mode == CreationMode.NEW;
    step5VerificationReady = false;
    labelCurrentCreationMode.setText(mode.label);
    clearStep5RelatedPanels();

    panelCreationModeNew.removeStyleName("creationModeCardSelected");
    panelCreationModeEdit.removeStyleName("creationModeCardSelected");
    panelCreationModeClone.removeStyleName("creationModeCardSelected");

    panelProblemLoader.setVisible(mode != CreationMode.NEW);
    buttonGetProblem.setVisible(mode == CreationMode.EDIT);
    buttonCopyProblem.setVisible(mode == CreationMode.CLONE);

    buttonMoveToVerification.setVisible(true);
    buttonSendProblem.setVisible(false);
    buttonMoveToVerification.removeStyleName("creationButtonSecondary");
    buttonMoveToVerification.addStyleName("creationButtonPrimary");
    buttonSendProblem.removeStyleName("creationButtonPrimary");
    buttonSendProblem.addStyleName("creationButtonSecondary");

    if (mode == CreationMode.NEW) {
      textBoxGetProblem.setText("");
      panelCreationModeNew.addStyleName("creationModeCardSelected");
      buttonMoveToVerification.setText("送信確認画面に移動する");
      if (currentStep == STEP_MODE_SELECTION) {
        buttonNextStep.setEnabled(validateCurrentStepLive());
      }
      return;
    }

    if (mode == CreationMode.EDIT) {
      panelCreationModeEdit.addStyleName("creationModeCardSelected");
      buttonMoveToVerification.setText("修正内容を確認する");
      if (currentStep == STEP_MODE_SELECTION) {
        buttonNextStep.setEnabled(validateCurrentStepLive());
      }
      return;
    }

    panelCreationModeClone.addStyleName("creationModeCardSelected");
    buttonMoveToVerification.setText("コピー内容を確認する");
    if (currentStep == STEP_MODE_SELECTION) {
      buttonNextStep.setEnabled(validateCurrentStepLive());
    }
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

  private final AsyncCallback<List<PacketSimilarProblem>> callbackSearchSimilarProblem = new AsyncCallback<List<PacketSimilarProblem>>() {
    public void onSuccess(List<PacketSimilarProblem> result) {
      List<PacketProblem> problems = Lists.newArrayList();
      for (PacketSimilarProblem similarProblem : result) {
        if (similarProblem == null || similarProblem.problem == null) {
          continue;
        }
        problems.add(similarProblem.problem);
      }
      panelSimilar.setWidget(new ProblemReportUi(problems, true, false, MAX_SIMILER_PROBLEMS_PER_PAGE));
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "類似問題の検索に失敗しました", caught);
      panelSimilar.setWidget(createEmptyProblemReportUi());
    }
  };

  /**
   * Step5 表示用の関連パネル（類似問題・出題プレビュー）を更新する。
   */
  private void updateStep5RelatedPanels() {
    PacketProblem problem = widgetProblemForm.getProblem();
    setProblemSample(problem);
    panelSimilar.setWidget(createEmptyProblemReportUi());
    getSimilarProblems(problem);
  }

  /**
   * Step5 の類似問題・問題表示サンプルをクリアする。
   */
  private void clearStep5RelatedPanels() {
    panelSimilar.clear();
    panelSample.clear();
  }

  /**
   * 類似問題パネルの空状態表示を生成する。
   *
   * @return 空状態の問題レポートUI
   */
  private ProblemReportUi createEmptyProblemReportUi() {
    return new ProblemReportUi(Lists.<PacketProblem>newArrayList(), true, false, MAX_SIMILER_PROBLEMS_PER_PAGE);
  }

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
        Service.Util.getInstance().addProblemIdsToReport(userCode, createRpcIntegerList(result),
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

  @VisibleForTesting
  void setEnable(boolean enabled) {
    FocusWidget[] widgets = { buttonNewProblem, buttonMoveToVerification, buttonSendProblem, textBoxGetProblem,
        buttonGetProblem, buttonCopyProblem, buttonNextProblem, buttonSelectNewMode, buttonSelectEditMode,
        buttonSelectCloneMode };
    for (FocusWidget widget : widgets) {
      widget.setEnabled(enabled);
    }
    wizardNavigationEnabled = enabled;
    updateWizardNavigationButtons();
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
    loadingProblemId = problemId;
    retriedProblemLoad = false;

    Service.Util.getInstance().getProblemList(createRpcIntegerList(problemId), callbackGetProblemList);

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
        loadedProblemInCurrentMode = false;
        setEnable(true);
        Window.alert("問題が見つかりませんでした。問題番号を確認してください。");
        return;
      }

      PacketProblem problem = result.get(0);
      if (problem == null) {
        logger.log(Level.WARNING, message);
        loadedProblemInCurrentMode = false;
        setEnable(true);
        Window.alert("問題が見つかりませんでした。問題番号を確認してください。");
        return;
      }

      if (copyProblem) {
        problem = problem.cloneForCopyingProblem();
      }
      widgetProblemForm.setProblem(problem);
      loadedProblemInCurrentMode = true;
      retriedProblemLoad = false;

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

      lastSavedSnapshot = createProblemSnapshot();
      setEnable(true);
    }

    public void onFailure(Throwable caught) {
      String failureDetail = summarizeProblemLoadFailure(caught);
      logger.log(Level.WARNING, "問題の取得中にエラーが発生しました: " + failureDetail, caught);
      if (ClientReloadPrompter.maybePrompt(caught)) {
        loadedProblemInCurrentMode = false;
        setEnable(true);
        return;
      }
      if (!retriedProblemLoad && loadingProblemId >= 0) {
        retriedProblemLoad = true;
        Service.Util.getInstance().getProblemList(createRpcIntegerList(loadingProblemId), callbackGetProblemList);
        return;
      }
      loadedProblemInCurrentMode = false;
      String occurredAt = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
      Window.alert("問題の読み込みに失敗しました。問題番号と通信状態を確認してください。\n発生時刻: " + occurredAt
          + "\n詳細: " + failureDetail);
      setEnable(true);
    }
  };

  /**
   * 問題読み込み失敗時に、調査へ必要な最小限の情報を文字列化して返す。
   */
  private static String summarizeProblemLoadFailure(Throwable caught) {
    if (caught == null) {
      return "unknown";
    }
    String message = caught.getMessage();
    if (Strings.isNullOrEmpty(message)) {
      return caught.getClass().getName();
    }
    return caught.getClass().getName() + ": " + message;
  }

  /**
   * GWT RPC の引数に ImmutableList を渡さないためのヘルパー。
   */
  @VisibleForTesting
  static List<Integer> createRpcIntegerList(int value) {
    return Lists.newArrayList(value);
  }
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

  @UiHandler("buttonSelectNewMode")
  void onButtonSelectNewMode(ClickEvent e) {
    applyCreationMode(CreationMode.NEW);
  }

  @UiHandler("buttonSelectEditMode")
  void onButtonSelectEditMode(ClickEvent e) {
    applyCreationMode(CreationMode.EDIT);
  }

  @UiHandler("buttonSelectCloneMode")
  void onButtonSelectCloneMode(ClickEvent e) {
    applyCreationMode(CreationMode.CLONE);
  }

  @UiHandler("buttonMoveToVerification")
  void onButtonMoveToVerification(ClickEvent e) {
    if (!validateProblem()) {
      return;
    }

    buttonMoveToVerification.setText("入力内容を再確認する");
    buttonSendProblem.setVisible(true);
    buttonMoveToVerification.removeStyleName("creationButtonPrimary");
    buttonMoveToVerification.addStyleName("creationButtonSecondary");
    buttonSendProblem.removeStyleName("creationButtonSecondary");
    buttonSendProblem.addStyleName("creationButtonPrimary");

    step5VerificationReady = true;
    updateStep5RelatedPanels();
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
    applyCreationMode(CreationMode.EDIT);
    getProblemFromServer(false);
  }

  @UiHandler("buttonCopyProblem")
  void onButtonCopyProblem(ClickEvent e) {
    applyCreationMode(CreationMode.CLONE);
    getProblemFromServer(true);
  }

  @UiHandler("buttonNextProblem")
  void onButtonNextProblem(ClickEvent e) {
    reset();

    // 連続投稿制限のチェック
    int userCode = UserData.get().getUserCode();
    Service.Util.getInstance().canUploadProblem(userCode, null, callbackCanUploadProblemOnLoad);
  }

  @UiHandler("buttonPrevStep")
  void onButtonPrevStep(ClickEvent e) {
    goToStep(currentStep - 1);
  }

  @UiHandler("buttonNextStep")
  void onButtonNextStep(ClickEvent e) {
    StepValidationResult result = validateStepForTransition(currentStep);
    applyStepValidationResult(result);
    if (result.hasErrors()) {
      widgetProblemForm.focusField(result.getFirstErrorFieldId());
      return;
    }
    if (!autoSaveIfDirty()) {
      return;
    }
    goToStep(currentStep + 1);
  }

  @UiHandler("buttonBackToStep1FromSummary")
  void onButtonBackToStep1FromSummary(ClickEvent e) {
    goToStep(STEP_BASIC_INFORMATION);
  }

  @UiHandler("buttonToggleStep1Detail")
  void onButtonToggleStep1Detail(ClickEvent e) {
    step4BasicDetailOpened = !step4BasicDetailOpened;
    updateStep4DetailState();
  }

  @UiHandler("buttonBackToStep2FromSummary")
  void onButtonBackToStep2FromSummary(ClickEvent e) {
    goToStep(STEP_SENTENCE);
  }

  @UiHandler("buttonToggleStep2Detail")
  void onButtonToggleStep2Detail(ClickEvent e) {
    step4QuestionDetailOpened = !step4QuestionDetailOpened;
    updateStep4DetailState();
  }

  @UiHandler("buttonBackToStep3FromSummary")
  void onButtonBackToStep3FromSummary(ClickEvent e) {
    goToStep(STEP_ANSWER_SETTING);
  }

  @UiHandler("buttonToggleStep3Detail")
  void onButtonToggleStep3Detail(ClickEvent e) {
    step4AnswerDetailOpened = !step4AnswerDetailOpened;
    updateStep4DetailState();
  }

  /**
   * ステップ遷移時の入力検証を行う。
   *
   * @param step 現在のステップ
   * @return 検証成功ならtrue
   */
  @VisibleForTesting
  boolean validateCurrentStepLive() {
    StepValidationResult result = validateStep(currentStep, false);
    applyStepValidationResult(result);
    if (currentStep == STEP_MODE_SELECTION && creationMode != CreationMode.NEW && !loadedProblemInCurrentMode) {
      return false;
    }
    return !result.hasErrors();
  }

  StepValidationResult validateStepForTransition(int step) {
    return validateStep(step, true);
  }

  private StepValidationResult validateStep(int step, boolean includeDeepValidation) {
    StepValidationResult result = new StepValidationResult();
    if (widgetProblemForm == null) {
      result.addError(WidgetProblemForm.FIELD_SENTENCE, ERROR_INPUT_SENTENCE);
      return result;
    }

    if (step == STEP_MODE_SELECTION) {
      if (includeDeepValidation && creationMode != CreationMode.NEW && !loadedProblemInCurrentMode) {
        result.addError(FIELD_MODE_SELECTION, ERROR_LOAD_PROBLEM_BEFORE_NEXT);
      }
      return result;
    }

    if (step == STEP_BASIC_INFORMATION) {
      PacketProblem problem = widgetProblemForm.getProblem();
      if (problem.genre == ProblemGenre.Random) {
        result.addError(WidgetProblemForm.FIELD_GENRE, ERROR_SELECT_GENRE);
      }
      if (problem.type == ProblemType.Random) {
        result.addError(WidgetProblemForm.FIELD_TYPE, ERROR_SELECT_TYPE);
      }
      if (problem.randomFlag == tv.dyndns.kishibe.qmaclone.client.game.RandomFlag.Random5) {
        result.addError(WidgetProblemForm.FIELD_RANDOM_FLAG, ERROR_SELECT_RANDOM_FLAG);
      }
      if (Strings.isNullOrEmpty(problem.creator) || Strings.isNullOrEmpty(problem.creator.trim())) {
        result.addError(WidgetProblemForm.FIELD_CREATOR, "問題作成者を入力してください");
      }
      return result;
    }

    if (step == STEP_SENTENCE) {
      PacketProblem problem = widgetProblemForm.getProblem();
      if (Strings.isNullOrEmpty(problem.sentence)) {
        result.addError(WidgetProblemForm.FIELD_SENTENCE, ERROR_INPUT_SENTENCE);
        return result;
      }
      if (Strings.isNullOrEmpty(problem.getProblemCreationSentence().trim())) {
        result.addError(WidgetProblemForm.FIELD_SENTENCE, ERROR_INPUT_NON_BLANK_SENTENCE);
      }
      return result;
    }

    if (step == STEP_ANSWER_SETTING) {
      PacketProblem problem = widgetProblemForm.getProblem();
      if (Strings.isNullOrEmpty(problem.answers[0])) {
        result.addError(WidgetProblemForm.FIELD_ANSWER1, ERROR_INPUT_ANSWER1);
      }
      if (includeDeepValidation && !validateProblem()) {
        if (!result.hasErrors()) {
          result.addError(WidgetProblemForm.FIELD_ANSWER1, ERROR_CONFIRM_ANSWER_SETTING);
        }
      }
      return result;
    }

    return result;
  }

  private void applyStepValidationResult(StepValidationResult result) {
    if (widgetProblemForm == null) {
      htmlStepErrorSummary.setHTML("");
      return;
    }

    if (result == null) {
      widgetProblemForm.clearStepErrors();
      htmlStepErrorSummary.setHTML("");
      return;
    }

    widgetProblemForm.applyStepErrors(result.getFieldErrors());
    if (result.hasErrors()) {
      htmlStepErrorSummary.setHTML("入力エラーが" + result.getFieldErrors().size() + "件あります。");
      return;
    }
    htmlStepErrorSummary.setHTML("");
  }

  /**
   * 現在の入力内容が保存済みスナップショットから変更されているかを返す。
   *
   * @return 変更があればtrue
   */
  @VisibleForTesting
  boolean isDirty() {
    return !createProblemSnapshot().equals(lastSavedSnapshot);
  }

  private String createProblemSnapshot() {
    if (widgetProblemForm == null) {
      return "";
    }
    return widgetProblemForm.getProblem().toChangeSummary();
  }

  /**
   * 変更がある場合のみ入力内容を自動保存する。
   *
   * @return 保存成功ならtrue
   */
  @VisibleForTesting
  boolean autoSaveIfDirty() {
    if (!isDirty()) {
      return true;
    }

    String snapshot = createProblemSnapshot();
    try {
      Storage localStorage = Storage.getLocalStorageIfSupported();
      if (localStorage != null) {
        localStorage.setItem(CREATION_WIZARD_DRAFT_KEY, snapshot);
      }
      lastSavedSnapshot = snapshot;
      return true;
    } catch (Exception ex) {
      logger.log(Level.WARNING, "問題作成ウィザードの自動保存に失敗しました", ex);
      addWarnings("自動保存に失敗しました。ページを更新せず再試行してください。");
      return false;
    }
  }

  /**
   * 確認ステップのサマリー表示を更新する。
   */
  private void updateStep4Summary() {
    if (widgetProblemForm == null) {
      htmlStep4SummaryBasic.setHTML("");
      htmlStep4SummaryQuestion.setHTML("");
      htmlStep4SummaryAnswer.setHTML("");
      htmlStep4DetailBasic.setHTML("");
      htmlStep4DetailQuestion.setHTML("");
      htmlStep4DetailAnswer.setHTML("");
      return;
    }

    PacketProblem problem = widgetProblemForm.getProblem();
    StepValidationResult basicValidation = validateStep(STEP_BASIC_INFORMATION, false);
    StepValidationResult questionValidation = validateStep(STEP_SENTENCE, false);
    StepValidationResult answerValidation = validateStep(STEP_ANSWER_SETTING, false);

    htmlStep4SummaryBasic.setHTML(buildBasicSummaryHtml(problem, basicValidation).toSafeHtml());
    htmlStep4SummaryQuestion.setHTML(buildQuestionSummaryHtml(problem, questionValidation).toSafeHtml());
    htmlStep4SummaryAnswer.setHTML(buildAnswerSummaryHtml(problem, answerValidation).toSafeHtml());
    htmlStep4DetailBasic.setHTML(buildBasicDetailHtml(problem).toSafeHtml());
    htmlStep4DetailQuestion.setHTML(buildQuestionDetailHtml(problem).toSafeHtml());
    htmlStep4DetailAnswer.setHTML(buildAnswerDetailHtml(problem).toSafeHtml());

    updateSummaryCardErrorState(panelStep4CardBasic, basicValidation);
    updateSummaryCardErrorState(panelStep4CardQuestion, questionValidation);
    updateSummaryCardErrorState(panelStep4CardAnswer, answerValidation);
    updateStep4DetailState();
  }

  /**
   * Step4 の詳細開閉状態を初期化する。
   */
  private void resetStep4DetailState() {
    step4BasicDetailOpened = false;
    step4QuestionDetailOpened = false;
    step4AnswerDetailOpened = false;
    updateStep4DetailState();
  }

  /**
   * Step4 の詳細開閉状態を画面に反映する。
   */
  private void updateStep4DetailState() {
    htmlStep4DetailBasic.setVisible(step4BasicDetailOpened);
    htmlStep4DetailQuestion.setVisible(step4QuestionDetailOpened);
    htmlStep4DetailAnswer.setVisible(step4AnswerDetailOpened);
    buttonToggleStep1Detail.setText(step4BasicDetailOpened ? STEP4_DETAIL_CLOSE_TEXT : STEP4_DETAIL_OPEN_TEXT);
    buttonToggleStep2Detail.setText(step4QuestionDetailOpened ? STEP4_DETAIL_CLOSE_TEXT : STEP4_DETAIL_OPEN_TEXT);
    buttonToggleStep3Detail.setText(step4AnswerDetailOpened ? STEP4_DETAIL_CLOSE_TEXT : STEP4_DETAIL_OPEN_TEXT);
  }

  /**
   * 基本情報カードのサマリーHTMLを生成する。
   *
   * @param problem 問題
   * @param validation 検証結果
   * @return サマリーHTML
   */
  private SafeHtmlBuilder buildBasicSummaryHtml(PacketProblem problem, StepValidationResult validation) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    appendValidationSummary(builder, validation);
    builder.appendEscaped("ジャンル: ").appendEscaped(String.valueOf(problem.genre));
    builder.appendHtmlConstant(" / ");
    builder.appendEscaped("出題形式: ").appendEscaped(String.valueOf(problem.type));
    builder.appendHtmlConstant(" / ");
    builder.appendEscaped("ランダムフラグ: ").appendEscaped(String.valueOf(problem.randomFlag));
    return builder;
  }

  /**
   * 問題文カードのサマリーHTMLを生成する。
   *
   * @param problem 問題
   * @param validation 検証結果
   * @return サマリーHTML
   */
  private SafeHtmlBuilder buildQuestionSummaryHtml(PacketProblem problem, StepValidationResult validation) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    appendValidationSummary(builder, validation);
    builder.appendEscaped("問題文: ").appendEscaped(buildSentenceSummary(problem.getProblemCreationSentence()));
    return builder;
  }

  /**
   * 解答設定カードのサマリーHTMLを生成する。
   *
   * @param problem 問題
   * @param validation 検証結果
   * @return サマリーHTML
   */
  private SafeHtmlBuilder buildAnswerSummaryHtml(PacketProblem problem, StepValidationResult validation) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    appendValidationSummary(builder, validation);
    builder.appendEscaped("解答1: ").appendEscaped(Strings.nullToEmpty(problem.answers[0]));
    builder.appendHtmlConstant(" / ");
    builder.appendEscaped("問題作成者: ").appendEscaped(Strings.nullToEmpty(problem.creator));
    return builder;
  }

  /**
   * 基本情報カードの詳細HTMLを生成する。
   *
   * @param problem 問題
   * @return 詳細HTML
   */
  private SafeHtmlBuilder buildBasicDetailHtml(PacketProblem problem) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    builder.appendEscaped("ジャンル: ").appendEscaped(String.valueOf(problem.genre));
    builder.appendHtmlConstant("<br/>");
    builder.appendEscaped("出題形式: ").appendEscaped(String.valueOf(problem.type));
    builder.appendHtmlConstant("<br/>");
    builder.appendEscaped("ランダムフラグ: ").appendEscaped(String.valueOf(problem.randomFlag));
    return builder;
  }

  /**
   * 問題文カードの詳細HTMLを生成する。
   *
   * @param problem 問題
   * @return 詳細HTML
   */
  private SafeHtmlBuilder buildQuestionDetailHtml(PacketProblem problem) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    builder.appendEscaped("問題文: ").appendEscaped(Strings.nullToEmpty(problem.getProblemCreationSentence()));
    return builder;
  }

  /**
   * 解答設定カードの詳細HTMLを生成する。
   *
   * @param problem 問題
   * @return 詳細HTML
   */
  private SafeHtmlBuilder buildAnswerDetailHtml(PacketProblem problem) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    builder.appendEscaped("解答1: ").appendEscaped(Strings.nullToEmpty(problem.answers[0]));
    builder.appendHtmlConstant("<br/>");
    builder.appendEscaped("問題作成者: ").appendEscaped(Strings.nullToEmpty(problem.creator));
    builder.appendHtmlConstant("<br/>");
    builder.appendEscaped("問題ノート: ").appendEscaped(Strings.nullToEmpty(problem.note));
    return builder;
  }

  /**
   * 問題文の要約文字列を生成する。
   *
   * @param sentence 問題文
   * @return 要約済み文字列
   */
  @VisibleForTesting
  String buildSentenceSummary(String sentence) {
    String normalized = Strings.nullToEmpty(sentence).replace('\n', ' ').replace('\r', ' ');
    if (normalized.length() <= STEP4_SENTENCE_SUMMARY_MAX_LENGTH) {
      return normalized;
    }
    return normalized.substring(0, STEP4_SENTENCE_SUMMARY_MAX_LENGTH) + "...";
  }

  /**
   * カードのエラー表示を更新する。
   *
   * @param panel 対象カード
   * @param validation 検証結果
   */
  private void updateSummaryCardErrorState(HTMLPanel panel, StepValidationResult validation) {
    panel.removeStyleName("creationSummaryError");
    if (validation != null && validation.hasErrors()) {
      panel.addStyleName("creationSummaryError");
    }
  }

  /**
   * 検証結果サマリーを追加する。
   *
   * @param builder HTMLビルダー
   * @param validation 検証結果
   */
  private void appendValidationSummary(SafeHtmlBuilder builder, StepValidationResult validation) {
    if (validation == null || !validation.hasErrors()) {
      return;
    }
    builder.appendEscaped("入力エラー").appendEscaped(String.valueOf(validation.getFieldErrors().size()))
        .appendEscaped("件");
    builder.appendHtmlConstant("<br/>");
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
      ChangeHistoryView view = (ChangeHistoryView) panelChangeHistory.getWidget();
      view.setDiffHtml(SafeHtmlUtils.fromString("差分の取得に失敗しました。履歴を再選択すると再試行できます。"));
    }
  };
}
