package tv.dyndns.kishibe.qmaclone.client.creation;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;

/**
 * 問題作成ウィザードのモード切替導線を検証するテスト。
 */
public class CreationUiModeFlowTest extends QMACloneGWTTestCaseBase {
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
  public void initialModeShouldBeNewCreation() {
    assertEquals("新規作成", ui.labelCurrentCreationMode.getText());
    assertFalse(ui.panelProblemLoader.isVisible());
  }

  @Test
  public void editModeShouldShowProblemLoader() {
    ui.onButtonSelectEditMode(null);

    assertTrue(ui.panelProblemLoader.isVisible());
    assertTrue(ui.buttonGetProblem.isVisible());
    assertFalse(ui.buttonCopyProblem.isVisible());
  }

  @Test
  public void cloneModeShouldShowCopyLoader() {
    ui.onButtonSelectCloneMode(null);

    assertTrue(ui.panelProblemLoader.isVisible());
    assertFalse(ui.buttonGetProblem.isVisible());
    assertTrue(ui.buttonCopyProblem.isVisible());
  }

  @Test
  public void editModeShouldRequireProblemLoadBeforeNextStep() {
    ui.onButtonSelectEditMode(null);
    ui.loadedProblemInCurrentMode = false;

    StepValidationResult result = ui.validateStepForTransition(1);
    assertTrue(result.hasErrors());
  }

  @Test
  public void editModeShouldNotShowStep1ErrorDuringLiveValidation() {
    ui.onButtonSelectEditMode(null);
    ui.loadedProblemInCurrentMode = false;

    assertTrue(ui.validateCurrentStepLive());
  }
}
