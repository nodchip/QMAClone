package tv.dyndns.kishibe.qmaclone.client.creation;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class CreationUiStepValidationTest extends QMACloneGWTTestCaseBase {
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
  public void validateStep2ShouldFailWhenGenreAndTypeAreNotSelected() {
    StepValidationResult result = ui.validateStepForTransition(2);
    assertTrue(result.hasErrors());
  }

  @Test
  public void validateStep3ShouldFailWhenSentenceIsEmpty() {
    PacketProblem problem = createProblem("", "解答");
    ui.widgetProblemForm.setProblem(problem);

    StepValidationResult result = ui.validateStepForTransition(3);
    assertTrue(result.hasErrors());
  }

  @Test
  public void validateStep4ShouldFailWhenAnswer1IsEmpty() {
    PacketProblem problem = createProblem("問題文", "");
    ui.widgetProblemForm.setProblem(problem);

    StepValidationResult result = ui.validateStepForTransition(4);
    assertTrue(result.hasErrors());
  }

  @Test
  public void validateStep2ShouldFailWhenRandomFlagIsRandom5() {
    PacketProblem problem = createProblem("問題文", "解答");
    problem.randomFlag = RandomFlag.Random5;
    ui.widgetProblemForm.setProblem(problem);

    StepValidationResult result = ui.validateStepForTransition(2);
    assertTrue(result.hasErrors());
  }

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
    problem.note = "";
    problem.numberOfDisplayedChoices = 4;
    return problem;
  }
}
