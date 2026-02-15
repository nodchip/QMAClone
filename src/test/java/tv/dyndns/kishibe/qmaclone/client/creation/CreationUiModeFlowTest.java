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
  public void testInitialModeShouldBeNewCreation() {
    assertEquals("新規作成", ui.labelCurrentCreationMode.getText());
    assertFalse(ui.panelProblemLoader.isVisible());
  }

  @Test
  public void testEditModeShouldShowProblemLoader() {
    ui.onButtonSelectEditMode(null);

    assertTrue(ui.panelProblemLoader.isVisible());
    assertTrue(ui.buttonGetProblem.isVisible());
    assertFalse(ui.buttonCopyProblem.isVisible());
  }

  @Test
  public void testCloneModeShouldShowCopyLoader() {
    ui.onButtonSelectCloneMode(null);

    assertTrue(ui.panelProblemLoader.isVisible());
    assertFalse(ui.buttonGetProblem.isVisible());
    assertTrue(ui.buttonCopyProblem.isVisible());
  }

  @Test
  public void testEditModeShouldRequireProblemLoadBeforeNextStep() {
    ui.onButtonSelectEditMode(null);
    ui.loadedProblemInCurrentMode = false;

    StepValidationResult result = ui.validateStepForTransition(1);
    assertTrue(result.hasErrors());
  }

  @Test
  public void testEditModeShouldDisableNextStepDuringLiveValidationUntilProblemIsLoaded() {
    ui.onButtonSelectEditMode(null);
    ui.loadedProblemInCurrentMode = false;

    assertFalse(ui.validateCurrentStepLive());
  }

  @Test
  public void testEditModeShouldDisableNextButtonUntilProblemIsLoaded() {
    ui.onButtonSelectEditMode(null);

    assertFalse(ui.buttonNextStep.isEnabled());
  }

  @Test
  public void testCloneModeShouldDisableNextButtonUntilProblemIsLoaded() {
    ui.onButtonSelectCloneMode(null);

    assertFalse(ui.buttonNextStep.isEnabled());
  }
}
