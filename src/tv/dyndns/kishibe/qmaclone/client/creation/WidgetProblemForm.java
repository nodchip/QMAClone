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

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static tv.dyndns.kishibe.qmaclone.client.constant.Constant.MAX_NUMBER_OF_ANSWERS;
import static tv.dyndns.kishibe.qmaclone.client.constant.Constant.MAX_NUMBER_OF_CHOICES;
import static tv.dyndns.kishibe.qmaclone.client.constant.Constant.MAX_PLAYER_NAME_LENGTH;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.PlusOne;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsResponse;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsThread;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.ProblemIndicationEligibility;
import tv.dyndns.kishibe.qmaclone.client.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WidgetProblemForm extends VerticalPanel implements ClickHandler, ChangeHandler {
  private static final Logger logger = Logger.getLogger(WidgetProblemForm.class.getName());
  private static final int MAX_PROBLEM_NOTE_LENGTH = 1024;
  private final Label labelProblemNumber = new Label("新規問題を作成中");
  private final ListBox listBoxGenre = new ListBox();
  @VisibleForTesting
  final ListBox listBoxType = new ListBox();
  private final ListBox listBoxRandomFlag = new ListBox();
  private final TextArea textAreaSentence = new TextArea();
  @VisibleForTesting
  final TextBox[] textBoxAnswer = new TextBox[MAX_NUMBER_OF_ANSWERS];
  @VisibleForTesting
  final TextBox[] textBoxChoice = new TextBox[MAX_NUMBER_OF_CHOICES];
  private final RadioButton radioButtonNone = new RadioButton("external", "使用しない");
  private final RadioButton radioButtonImage = new RadioButton("external", "画像");
  private final RadioButton radioButtonYouTube = new RadioButton("external", "YouTube");
  private final ListBox listBoxNumberOfDisplayedChoices = new ListBox();
  private final TextBox textBoxExternalUrl = new TextBox();
  private final HorizontalPanel panelExternalUrl = new HorizontalPanel();
  private final TextBox textBoxCreator = new TextBox();
  private final TextArea textAreaNote = new TextArea();
  private final CreationUi creationUi;
  private final Label labelAnswerCounter = new Label("0/0");
  private final CheckBox checkBoxResetAnswerCount = new CheckBox("回答数をリセットする");
  @VisibleForTesting
  final HTML htmlPlusOne = new HTML();
  private final Label labelGood = new Label("0");
  private final CheckBox checkBoxResetVote = new CheckBox("良問投票をリセットする");
  private final CheckBox checkBoxImageChoice = new CheckBox("画像選択肢");
  private final CheckBox checkBoxImageAnswer = new CheckBox("画像回答");
  private final CheckBox checkBoxRemovePlayerAnswers = new CheckBox("回答履歴を削除する");
  private int problemId = PacketProblem.CREATING_PROBLEM_ID;
  private final List<Button> buttonPolygonCreation = new ArrayList<Button>();
  private final VerticalPanel panelProblemFeedback = new VerticalPanel();
  private final Button buttonClearProblemFeedback = new Button("クリア", this);
  private Date indication;
  private Date indicationResolved;
  private final Button buttonIndicate = new Button("問題の不備を指摘する");
  @VisibleForTesting
  final CheckBox checkBoxUnindicate = new CheckBox("指摘マークを消す (ページ下の掲示板の指摘内容を確認して下さい)");
  private boolean reserveResetAnswerCount = false;

  public WidgetProblemForm(CreationUi creationUi) {
    this.creationUi = creationUi;

    // 問題番号
    add(labelProblemNumber);

    Grid grid = new Grid(15, 2);
    grid.addStyleName("gridFrame");
    grid.addStyleName("gridFontNormal");

    int row = 0;

    // ジャンル
    for (ProblemGenre genre : ProblemGenre.values()) {
      String name = genre == ProblemGenre.Random ? "ジャンルを選んでください" : genre.toString();
      listBoxGenre.addItem(name, Integer.toString(genre.getIndex()));
    }
    listBoxGenre.setWidth("200px");
    grid.setText(row, 0, "ジャンル");
    grid.setWidget(row++, 1, listBoxGenre);

    // 出題形式
    for (ProblemType type : ProblemType.values()) {
      if (ProblemType.Random1.compareTo(type) <= 0) {
        break;
      }
      String item = type == ProblemType.Random ? "出題形式を選んでください" : type.toString();
      listBoxType.addItem(item, type.name());
    }
    listBoxType.setWidth("200px");
    listBoxType.addChangeHandler(this);
    grid.setText(row, 0, "出題形式");
    grid.setWidget(row++, 1, listBoxType);

    // ランダムフラグ
    for (int i = 1; i <= 5; ++i) {
      String s = Integer.toString(i);
      listBoxRandomFlag.addItem(s, RandomFlag.values()[i].name());
    }
    listBoxRandomFlag.setSelectedIndex(4);
    listBoxRandomFlag.setWidth("100px");
    grid.setText(row, 0, "ランダムフラグ");
    grid.setWidget(row++, 1, listBoxRandomFlag);

    // 問題文
    textAreaSentence.setCharacterWidth(60);
    textAreaSentence.setVisibleLines(5);
    grid.setText(row, 0, "問題文");
    grid.setWidget(row++, 1, textAreaSentence);

    // 選択肢
    VerticalPanel choicePanel = new VerticalPanel();
    choicePanel.setStyleName("gridNoFrame");
    choicePanel.addStyleName("gridFontNormal");
    for (int i = 0; i < MAX_NUMBER_OF_CHOICES; ++i) {
      textBoxChoice[i] = new TextBox();
      textBoxChoice[i].setWidth("400px");
      choicePanel.add(textBoxChoice[i]);
    }
    choicePanel.add(checkBoxImageChoice);
    grid.setText(row, 0, "選択肢");
    grid.setWidget(row++, 1, choicePanel);

    // 解答
    VerticalPanel answerPanel = new VerticalPanel();
    answerPanel.setStyleName("gridNoFrame");
    answerPanel.addStyleName("gridFontNormal");
    for (int i = 0; i < MAX_NUMBER_OF_ANSWERS; ++i) {
      HorizontalPanel panel = new HorizontalPanel();
      panel.setVerticalAlignment(ALIGN_MIDDLE);
      answerPanel.add(panel);

      textBoxAnswer[i] = new TextBox();
      textBoxAnswer[i].setWidth("400px");
      panel.add(textBoxAnswer[i]);

      Button button = new Button("領域作成", this);
      panel.add(button);
      buttonPolygonCreation.add(button);
    }
    answerPanel.add(checkBoxImageAnswer);
    grid.setText(row, 0, "解答");
    grid.setWidget(row++, 1, answerPanel);

    // 表示する選択肢の数
    grid.setText(row, 0, "表示する選択肢の数");
    grid.setWidget(row++, 1, listBoxNumberOfDisplayedChoices);
    listBoxNumberOfDisplayedChoices.setWidth("50px");
    listBoxNumberOfDisplayedChoices.addItem("3");
    listBoxNumberOfDisplayedChoices.addItem("4");
    listBoxNumberOfDisplayedChoices.setSelectedIndex(1);

    // 外部コンテンツ
    grid.setText(row, 0, "外部コンテンツ");
    {
      VerticalPanel panel = new VerticalPanel();
      panel.setStyleName("gridNoFrame");
      panel.addStyleName("gridFontNormal");
      {
        radioButtonNone.setValue(true);
        radioButtonNone.addClickHandler(this);
        radioButtonImage.addClickHandler(this);
        radioButtonYouTube.addClickHandler(this);

        HorizontalPanel panel2 = new HorizontalPanel();
        panel2.add(radioButtonNone);
        panel2.add(radioButtonImage);
        panel2.add(radioButtonYouTube);
        panel.add(panel2);
      }
      {
        textBoxExternalUrl.setWidth("400px");

        panelExternalUrl.add(new Label("URL"));
        panelExternalUrl.add(textBoxExternalUrl);
        panel.add(panelExternalUrl);
      }
      grid.setWidget(row++, 1, panel);
    }

    // 作成者
    textBoxCreator.setWidth("200px");
    textBoxCreator.setMaxLength(MAX_PLAYER_NAME_LENGTH);
    textBoxCreator.setText(UserData.get().getPlayerName());
    grid.setText(row, 0, "問題作成者");
    grid.setWidget(row++, 1, textBoxCreator);

    // 回答数
    grid.setText(row, 0, "回答数");
    HorizontalPanel panelAnswerCount = new HorizontalPanel();
    panelAnswerCount.add(labelAnswerCounter);
    panelAnswerCount.add(checkBoxResetAnswerCount);
    panelAnswerCount.add(checkBoxRemovePlayerAnswers);
    checkBoxResetAnswerCount.setVisible(false);
    checkBoxRemovePlayerAnswers.setVisible(false);
    grid.setWidget(row++, 1, panelAnswerCount);

    // 回答数
    grid.setText(row, 0, "+1");
    HorizontalPanel panelVoteCount = new HorizontalPanel();
    panelVoteCount.add(htmlPlusOne);
    grid.setWidget(row++, 1, panelVoteCount);

    // 評価
    grid.setText(row, 0, "良問");
    HorizontalPanel panelGood = new HorizontalPanel();
    panelGood.add(labelGood);
    panelGood.add(checkBoxResetVote);
    checkBoxResetVote.setVisible(false);
    grid.setWidget(row++, 1, panelGood);

    // 指摘
    grid.setText(row, 0, "指摘");
    HorizontalPanel panelIndicate = new HorizontalPanel();
    buttonIndicate.setVisible(false);
    checkBoxUnindicate.setVisible(false);
    panelIndicate.add(buttonIndicate);
    panelIndicate.add(checkBoxUnindicate);
    grid.setWidget(row++, 1, panelIndicate);
    buttonIndicate.addClickHandler(this);

    // 問題ノート
    grid.setText(row, 0, "問題ノート");
    textAreaNote.setCharacterWidth(60);
    textAreaNote.setVisibleLines(5);
    grid.setWidget(row++, 1, textAreaNote);

    // 問題評価
    grid.setText(row, 0, "問題評価");
    grid.setWidget(row++, 1, panelProblemFeedback);

    add(grid);

    updateForm();
  }

  /**
   * 問題を設定する
   * 
   * @param problem
   *          問題
   */
  public void setProblem(PacketProblem problem) {
    // TODO 範囲外の値をセットしようとしたときにJavaとブラウザで挙動が違うのを報告する
    listBoxGenre.setSelectedIndex(problem.genre.getIndex());
    listBoxType.setSelectedIndex(problem.type.getIndex());
    listBoxRandomFlag.setSelectedIndex(problem.randomFlag.getIndex() - 1);
    textAreaSentence.setText(problem.getProblemCreationSentence());

    if (problem.answers != null) {
      for (int i = 0; i < problem.answers.length; ++i) {
        String answer = nullToEmpty(problem.answers[i]);
        textBoxAnswer[i].setText(answer);
      }
    }

    if (problem.choices != null) {
      for (int i = 0; i < problem.choices.length; ++i) {
        String choice = nullToEmpty(problem.choices[i]);
        textBoxChoice[i].setText(choice);
      }
    }

    if (problem.type.isNumberOfDisplayedChoicesChangeable()) {
      listBoxNumberOfDisplayedChoices.setSelectedIndex(problem.numberOfDisplayedChoices - 3);
    } else {
      listBoxNumberOfDisplayedChoices.setSelectedIndex(1);
    }

    if (problem.creator != null) {
      textBoxCreator.setText(problem.creator);
    }

    labelAnswerCounter.setText(problem.good + "/" + problem.bad);

    if (problem.isCopiedProblem()) {
      htmlPlusOne.setHTML("");
    } else {
      htmlPlusOne.setHTML(PlusOne.getButton(problem.id, true));
      try {
        PlusOne.render();
      } catch (JavaScriptException e) {
        logger.warning("+1ボタンのレンダリングに失敗しました");
      }
    }

    textAreaNote.setText(problem.note.trim());
    checkBoxImageAnswer.setValue(problem.imageAnswer);
    checkBoxImageChoice.setValue(problem.imageChoice);

    problemId = problem.id;

    if (problemId == PacketProblem.CREATING_PROBLEM_ID) {
      labelProblemNumber.setText("新規問題入力中");
      checkBoxRemovePlayerAnswers.setVisible(false);
    } else {
      labelProblemNumber.setText("問題番号" + problemId + "の問題を修正中");
      checkBoxRemovePlayerAnswers.setVisible(true);
    }
    checkBoxResetAnswerCount.setValue(problem.needsResetAnswerCount);
    checkBoxResetVote.setValue(problem.needsResetVote);
    checkBoxRemovePlayerAnswers.setValue(problem.needsRemovePlayerAnswers);
    checkBoxResetAnswerCount.setVisible(!problem.isCopiedProblem());
    checkBoxRemovePlayerAnswers.setVisible(!problem.isCopiedProblem());
    checkBoxResetVote.setVisible(!problem.isCopiedProblem());

    if (problem.imageUrl != null) {
      textBoxExternalUrl.setText(problem.imageUrl);
      radioButtonImage.setValue(true);

    } else if (problem.movieUrl != null) {
      textBoxExternalUrl.setText(problem.movieUrl);
      radioButtonYouTube.setValue(true);

    } else {
      radioButtonNone.setValue(true);
    }

    // 良問
    labelGood.setText(String.valueOf(problem.voteGood));

    // 指摘
    // BugTrack-QMAClone/595 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F595
    if (problem.isCopiedProblem()) {
      // 問題をコピーした場合は問題指摘フラグは折る
      indication = null;
      indicationResolved = null;
    } else {
      if (problem.indication == null) {
        indication = null;
        indicationResolved = problem.indicationResolved;
        buttonIndicate.setVisible(true);
        checkBoxUnindicate.setVisible(false);
      } else {
        indication = problem.indication;
        indicationResolved = null;
        buttonIndicate.setVisible(false);
        checkBoxUnindicate.setVisible(true);
      }
    }

    checkBoxImageAnswer.setValue(false);

    // 問題評価文
    panelProblemFeedback.clear();
    if (problemId != PacketProblem.CREATING_PROBLEM_ID) {
      Service.Util.getInstance().getProblemFeedback(problemId, callbackGetProblemFeedback);
    }

    updateForm();
  }

  private final AsyncCallback<List<String>> callbackGetProblemFeedback = new AsyncCallback<List<String>>() {
    public void onSuccess(List<String> result) {
      panelProblemFeedback.add(new HTML(new SafeHtmlBuilder().appendEscapedLines(
          Joiner.on('\n').join(result)).toSafeHtml()));

      if (!result.isEmpty()) {
        panelProblemFeedback.add(buttonClearProblemFeedback);
      }
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "問題フィードバッグの取得に失敗しました", caught);
    }
  };

  private void updateForm() {
    ProblemType type = getSelectedType();

    creationUi.setTypeDescription(type.getDescription());

    for (TextBox textBox : textBoxChoice) {
      textBox.setEnabled(false);
      textBox.setVisible(false);
    }
    for (TextBox textBox : textBoxAnswer) {
      textBox.setEnabled(false);
      textBox.setVisible(false);
    }

    for (int i = 0; i < type.getNumberOfAnswers(); ++i) {
      textBoxAnswer[i].setEnabled(true);
      textBoxAnswer[i].setVisible(true);
    }
    for (int i = type.getNumberOfAnswers(); i < MAX_NUMBER_OF_ANSWERS; ++i) {
      textBoxAnswer[i].setText("");
    }

    for (int i = 0; i < type.getNumberOfChoices(); ++i) {
      textBoxChoice[i].setEnabled(true);
      textBoxChoice[i].setVisible(true);
    }
    for (int i = type.getNumberOfChoices(); i < MAX_NUMBER_OF_CHOICES; ++i) {
      textBoxChoice[i].setText("");
    }

    checkBoxImageAnswer.setVisible(type.isImageAnswer());
    checkBoxImageChoice.setVisible(type.isImageChoice());

    for (Button button : buttonPolygonCreation) {
      button.setVisible(type.isPolygonCreation());
    }

    if (radioButtonNone.getValue()) {
      textBoxExternalUrl.setText("");
      panelExternalUrl.setVisible(false);
    } else {
      panelExternalUrl.setVisible(true);
    }

    listBoxNumberOfDisplayedChoices.setVisible(type.isNumberOfDisplayedChoicesChangeable());
  }

  public void setEnable(boolean enabled) {
    List<FocusWidget> focusWidgets = Lists.newArrayList(listBoxGenre, listBoxType,
        listBoxRandomFlag, textAreaSentence, textBoxAnswer[0], textBoxAnswer[1], textBoxAnswer[2],
        textBoxAnswer[3], textBoxChoice[0], textBoxChoice[1], textBoxChoice[2], textBoxChoice[3],
        radioButtonNone, radioButtonImage, radioButtonYouTube, textBoxExternalUrl, textBoxCreator,
        textAreaNote, checkBoxResetAnswerCount, checkBoxImageChoice, checkBoxImageAnswer,
        checkBoxRemovePlayerAnswers, buttonClearProblemFeedback, checkBoxResetVote);
    focusWidgets.addAll(buttonPolygonCreation);

    for (FocusWidget focusWidget : focusWidgets) {
      focusWidget.setEnabled(enabled);
    }
  }

  /**
   * 問題を取得する
   * 
   * @return 問題
   */
  public PacketProblem getProblem() {
    PacketProblem problem = new PacketProblem();

    // 問題番号
    problem.id = problemId;

    // ジャンル・出題形式
    problem.genre = ProblemGenre.values()[Integer.parseInt(listBoxGenre.getValue(listBoxGenre
        .getSelectedIndex()))];
    problem.type = getSelectedType();
    problem.randomFlag = RandomFlag.values()[listBoxRandomFlag.getSelectedIndex() + 1];

    // 問題文
    problem.setSentence(textAreaSentence.getText());

    // 答え
    problem.answers = new String[MAX_NUMBER_OF_ANSWERS];
    for (int i = 0; i < MAX_NUMBER_OF_ANSWERS; ++i) {
      problem.answers[i] = emptyToNull(textBoxAnswer[i].getText().replaceAll("&", "＆"));

      if (problem.type == ProblemType.Typing || problem.type == ProblemType.Effect
          || problem.type == ProblemType.Flash) {
        problem.answers[i] = StringUtils.toFullWidth(problem.answers[i]);
      }
    }

    // 選択肢
    problem.choices = new String[MAX_NUMBER_OF_CHOICES];
    for (int i = 0; i < MAX_NUMBER_OF_CHOICES; ++i) {
      problem.choices[i] = emptyToNull(textBoxChoice[i].getText().replaceAll("&", "＆"));
    }

    if (problem.type == ProblemType.YonTaku || problem.type == ProblemType.Rensou) {
      problem.answers = new String[MAX_NUMBER_OF_ANSWERS];
      problem.answers[0] = problem.choices[0];
    }

    if (radioButtonImage.getValue()) {
      problem.imageUrl = textBoxExternalUrl.getText();
    } else if (radioButtonYouTube.getValue()) {
      problem.movieUrl = textBoxExternalUrl.getText();
    }

    // 表示する選択肢の数
    if (problem.type.isNumberOfDisplayedChoicesChangeable()) {
      problem.numberOfDisplayedChoices = Integer.valueOf(listBoxNumberOfDisplayedChoices
          .getItemText(listBoxNumberOfDisplayedChoices.getSelectedIndex()));
    } else {
      problem.numberOfDisplayedChoices = 4;
    }

    // 作成者
    problem.creator = textBoxCreator.getText();

    // 回答数
    String answerCount[] = labelAnswerCounter.getText().split("/");
    problem.good = Integer.parseInt(answerCount[0]);
    problem.bad = Integer.parseInt(answerCount[1]);
    problem.needsResetAnswerCount = checkBoxResetAnswerCount.getValue();
    problem.needsRemovePlayerAnswers = checkBoxRemovePlayerAnswers.getValue();

    // 投票数
    problem.voteGood = Integer.valueOf(labelGood.getText());
    problem.needsResetVote = checkBoxResetVote.getValue();

    // 指摘
    if (indication != null) {
      if (!checkBoxUnindicate.getValue()) {
        problem.indication = indication;
        problem.indicationResolved = null;
      } else {
        // 指摘を解除した場合
        problem.indication = null;
        problem.indicationResolved = new Date();
      }
    } else {
      problem.indication = null;
      problem.indicationResolved = indicationResolved;
    }

    // 問題ノート
    problem.note = textAreaNote.getText().trim();
    if (problem.note.length() > MAX_PROBLEM_NOTE_LENGTH) {
      problem.note = problem.note.substring(0, MAX_PROBLEM_NOTE_LENGTH);
    }

    problem.imageAnswer = isImageAnswer();
    problem.imageChoice = isImageChoice();

    return problem;
  }

  private ProblemType getSelectedType() {
    int selectedIndex = listBoxType.getSelectedIndex();
    String value = listBoxType.getValue(selectedIndex);
    return ProblemType.valueOf(value);
  }

  private void clearProblemFeedback() {
    if (problemId == PacketProblem.CREATING_PROBLEM_ID) {
      return;
    }

    panelProblemFeedback.clear();
    Service.Util.getInstance().clearProblemFeedback(problemId, callbackClearProblemFeedback);
  }

  private final AsyncCallback<Void> callbackClearProblemFeedback = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "問題フィードバックの削除に失敗しました", caught);
    }
  };

  private boolean isImageAnswer() {
    ProblemType type = getSelectedType();
    return type.isImageAnswer() && checkBoxImageAnswer.getValue();
  }

  private boolean isImageChoice() {
    ProblemType type = getSelectedType();
    return type.isImageChoice() && checkBoxImageChoice.getValue();
  }

  @Override
  public void onClick(ClickEvent event) {
    Widget sender = (Widget) event.getSource();

    if (buttonPolygonCreation.contains(sender)) {
      final int answerIndex = buttonPolygonCreation.indexOf(sender);
      final DialogBoxPolygonCreation polygonCreation = new DialogBoxPolygonCreation(
          textBoxChoice[0].getText(), textBoxAnswer[answerIndex].getText());
      polygonCreation.setPopupPosition(Window.getScrollLeft() + 10, Window.getScrollTop() + 10);
      polygonCreation.addDialogBoxPolygonCreationListener(new DialogBoxPolygonCreationListener() {
        public void onOk() {
          textBoxAnswer[answerIndex].setText(polygonCreation.getPolygonDescription());
        }

        public void onCancel() {
        }
      });
      polygonCreation.show();
    } else if (sender == buttonClearProblemFeedback) {
      clearProblemFeedback();
    } else if (sender == radioButtonNone) {
      updateForm();
    } else if (sender == radioButtonImage) {
      updateForm();
    } else if (sender == radioButtonYouTube) {
      updateForm();
    } else if (sender == buttonIndicate) {
      onButtonIndicate();
    }
  }

  public void onButtonIndicate() {
    Service.Util.getInstance().getProblemIndicationEligibility(UserData.get().getUserCode(),
        callbackGetProblemIndicationEligibility);
  }

  private final AsyncCallback<ProblemIndicationEligibility> callbackGetProblemIndicationEligibility = new AsyncCallback<ProblemIndicationEligibility>() {
    @Override
    public void onSuccess(ProblemIndicationEligibility result) {
      switch (result) {
      case OK:
        indicate();
        break;

      case PLAYER_NAME_UNCHANGED:
        Window.alert("プレイヤー名が「未初期化です」から変更されていません\n" + "プレイヤー名を変更し一回以上プレイしてから指摘してください");
        break;

      case REACHED_MAX_NUMBER_OF_REQUESTS_PER_UNIT_TIME:
        Window.alert("問題指摘回数の上限に達しました\n" + "時間をおいて再度お試しください。");
        break;

      default:
        break;
      }
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "");
    }
  };

  private void indicate() {
    String prompt = Window.prompt("指摘内容をお書きください", "(指摘内容をお書きください)");
    if (prompt == null) {
      return;
    }

    final String message = prompt.trim();
    if (Strings.isNullOrEmpty(message)) {
      return;
    }

    if (indicationResolved != null
        && System.currentTimeMillis() < indicationResolved.getTime() + 7L * 24 * 60 * 60 * 1000) {
      // 指摘解除されてから7日以内の場合
      AsyncCallback<List<PacketBbsThread>> callbackGetBbsThreads = new AsyncCallback<List<PacketBbsThread>>() {
        @Override
        public void onSuccess(List<PacketBbsThread> result) {
          if (result == null || result.isEmpty()) {
            logger.log(Level.WARNING, "スレッドの取得に失敗しました");
            return;
          }

          PacketBbsThread thread = result.get(0);
          PacketBbsResponse response = new PacketBbsResponse();
          response.threadId = thread.id;
          response.name = UserData.get().getPlayerName();
          response.userCode = UserData.get().getUserCode();
          response.dispInfo = 2;
          response.postTime = System.currentTimeMillis();
          response.body = message;

          Service.Util.getInstance().writeToBbs(response, true, callbackBuildBbsThread);
        }

        @Override
        public void onFailure(Throwable caught) {
        }
      };
      Service.Util.getInstance().getBbsThreads(problemId, 0, 1, callbackGetBbsThreads);
    } else {
      // 初めて指摘する場合、または指摘解除されてから7日以上経過した場合
      PacketBbsThread thread = new PacketBbsThread();
      thread.title = "不具合が指摘されました (" + Utility.toDateFormat(new Date()) + ")";
      PacketBbsResponse response = new PacketBbsResponse();
      response.body = message;
      response.dispInfo = 2; // 全て表示
      response.name = UserData.get().getPlayerName();
      response.userCode = UserData.get().getUserCode();

      Service.Util.getInstance()
          .buildBbsThread(problemId, thread, response, callbackBuildBbsThread);
    }
  }

  private final AsyncCallback<Void> callbackBuildBbsThread = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
      creationUi.reloadBbs();
      indication = new Date();
      Service.Util.getInstance().indicateProblem(problemId, UserData.get().getUserCode(),
          callbackPointOutInProblem);
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "BBSスレッド立てに失敗しました", caught);
    }
  };
  private final AsyncCallback<Void> callbackPointOutInProblem = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
      indication = new Date();
      checkBoxUnindicate.setValue(false);
      setProblem(getProblem());

      if (UserData.get().isRegisterIndicatedProblem()) {
        Service.Util.getInstance().addProblemIdsToReport(UserData.get().getUserCode(),
            ImmutableList.of(problemId), callbackAddProblemIdsToReport);
      }

      Window.alert("ご指摘ありがとうございました");
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "指摘フラグの更新に失敗しました", caught);
    }
  };
  private final AsyncCallback<Void> callbackAddProblemIdsToReport = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "正解率統計への問題追加に失敗しました", caught);
    }
  };

  @Override
  public void onChange(ChangeEvent event) {
    Widget sender = (Widget) event.getSource();
    if (sender == listBoxType) {
      updateForm();
    }
  }

  public boolean isReserveResetAnswerCount() {
    return reserveResetAnswerCount;
  }
}
