package tv.dyndns.kishibe.qmaclone.client.creation;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

/**
 * 問題作成ウィザード確認ステップのサマリー表示を検証するテスト。
 */
public class CreationUiStep4SummaryTest extends QMACloneGWTTestCaseBase {
  private CreationUi ui;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    CreationUi.SUPPRESS_GET_SIMILAR_PROBLEM_FOR_TESTING = true;
    ui = new CreationUi(new WrongAnswerPresenter(new WrongAnswerViewImpl()));
  }

  @Override
  protected void gwtTearDown() throws Exception {
    CreationUi.SUPPRESS_GET_SIMILAR_PROBLEM_FOR_TESTING = false;
    super.gwtTearDown();
  }

  @Test
  public void step4ShouldShowThreeSectionSummary() {
    ui.widgetProblemForm.setProblem(createProblem("これは確認画面の表示を確認するための問題文です。", "テスト解答"));

    ui.goToStep(4);

    assertTrue(ui.htmlStep4SummaryBasic.getText().contains("ジャンル"));
    assertTrue(ui.htmlStep4SummaryQuestion.getText().contains("問題文"));
    assertTrue(ui.htmlStep4SummaryAnswer.getText().contains("解答1"));
  }

  @Test
  public void step4QuestionSummaryShouldBeTruncatedAt40Characters() {
    String sentence = "1234567890123456789012345678901234567890ABCDE";
    ui.widgetProblemForm.setProblem(createProblem(sentence, "テスト解答"));

    ui.goToStep(4);

    assertTrue(ui.htmlStep4SummaryQuestion.getText().contains("1234567890123456789012345678901234567890..."));
  }

  @Test
  public void toggleDetailButtonShouldOpenAndCloseQuestionDetail() {
    ui.widgetProblemForm.setProblem(createProblem("問題文", "解答"));
    ui.goToStep(4);

    assertFalse(ui.htmlStep4DetailQuestion.isVisible());
    ui.onButtonToggleStep2Detail(null);
    assertTrue(ui.htmlStep4DetailQuestion.isVisible());
    ui.onButtonToggleStep2Detail(null);
    assertFalse(ui.htmlStep4DetailQuestion.isVisible());
  }

  @Test
  public void sectionWithValidationErrorShouldHaveErrorStyle() {
    PacketProblem problem = createProblem("", "");
    problem.genre = ProblemGenre.Random;
    problem.type = ProblemType.Random;
    ui.widgetProblemForm.setProblem(problem);

    ui.goToStep(4);

    assertTrue(ui.panelStep4CardBasic.getStyleName().contains("creationSummaryError"));
    assertTrue(ui.panelStep4CardQuestion.getStyleName().contains("creationSummaryError"));
    assertTrue(ui.panelStep4CardAnswer.getStyleName().contains("creationSummaryError"));
  }

  /**
   * テスト用の問題データを生成する。
   *
   * @param sentence 問題文
   * @param answer1 解答1
   * @return 問題データ
   */
  private PacketProblem createProblem(String sentence, String answer1) {
    PacketProblem problem = new PacketProblem();
    problem.id = PacketProblem.CREATING_PROBLEM_ID;
    problem.genre = ProblemGenre.Anige;
    problem.type = ProblemType.Effect;
    problem.randomFlag = RandomFlag.Random1;
    problem.sentence = sentence;
    problem.answers = new String[] { answer1, null, null, null };
    problem.choices = new String[] { null, null, null, null };
    problem.creator = "tester";
    problem.note = "確認用ノート";
    problem.numberOfDisplayedChoices = 4;
    return problem;
  }
}
