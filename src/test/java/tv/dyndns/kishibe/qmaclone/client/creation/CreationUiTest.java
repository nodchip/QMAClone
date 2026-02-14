package tv.dyndns.kishibe.qmaclone.client.creation;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

import com.google.gwt.user.client.ui.HTML;

public class CreationUiTest extends QMACloneGWTTestCaseBase {
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

	// @Test
	// public void testOnLoad() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testCreationUi() {
	// fail("Not yet implemented");
	// }

	@Test
	public void testReset() {
		ui.reset();
		assertEquals("", ui.textBoxGetProblem.getText());
	}

	// @Test
	// public void testSetTypeCaution() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetWrongAnswers() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testSetProblem() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testOnButtonNewProblem() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testOnButtonMoveToVerification() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testOnButtonSendProblem() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testOnButtonGetProblem() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testOnButtonCopyProblem() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testOnButtonNextProblem() {
	// fail("Not yet implemented");
	// }
	@Test
	public void testValidateProblemShouldNotFailIfProblemIsInvalid() {
		ui.widgetProblemForm.listBoxType.setSelectedIndex(ProblemType.Tato.getIndex());
		ui.widgetProblemForm.textBoxAnswer[0].setText("あいうえお");
		for (int i = 0; i < 3; ++i) {
			ui.widgetProblemForm.textBoxChoice[i].setText("あいうえお");
		}

		assertFalse(ui.validateProblem());
	}

	@Test
	public void testStep1ShouldDisablePrevButton() {
		ui.goToStep(1);
		assertFalse(ui.buttonPrevStep.isEnabled());
	}

	@Test
	public void testStep2ShouldEnablePrevButton() {
		ui.goToStep(2);
		assertTrue(ui.buttonPrevStep.isEnabled());
	}

	@Test
	public void testStep1ShouldKeepPrevButtonDisabledAfterEnable() {
		ui.goToStep(1);
		ui.setEnable(true);

		assertFalse(ui.buttonPrevStep.isEnabled());
	}

	@Test
	public void testWizardButtonsShouldUsePrimarySecondaryStyles() {
		assertTrue(ui.buttonPrevStep.getStyleName().contains("creationButtonSecondary"));
		assertTrue(ui.buttonNextStep.getStyleName().contains("creationButtonPrimary"));
	}

	@Test
	public void testResetButtonShouldBeInWizardUtilityBar() {
		assertTrue(ui.buttonNewProblem.getParent().getStyleName().contains("creationWizardUtilityBar"));
	}

	private boolean doesPanelWarningHaveUpdateNoteMessage() {
		for (int i = 0; i < ui.panelWarning.getWidgetCount(); ++i) {
			HTML html = (HTML) ui.panelWarning.getWidget(i);
			if (html.getText().equals(CreationUi.MESSAGE_UPDATE_NOTE)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testValidateProblemShouldNotReturnNoteWarningAfterReset() {
		ui.reset();
		ui.validateProblem();
		assertFalse(doesPanelWarningHaveUpdateNoteMessage());
	}

	// @Ignore
	// @Test
	// public void testValidateProblemShouldShowUpdateNoteWarningIfFixButtonIsPushed() {
	// ui.textBoxGetProblem.setText("1");
	// ui.onButtonGetProblem(null);
	//
	// Timer timer = new Timer() {
	// public void run() {
	// ui.validateProblem();
	// assertTrue(doesPanelWarningHaveUpdateNoteMessage());
	// finishTest();
	// }
	// };
	//
	// delayTestFinish(10 * 1000);
	//
	// timer.schedule(1000);
	// }
	//
	// @Ignore
	// @Test
	// public void testValidateProblemShouldNotShowUpdateNoteWarningIfCopyButtonIsPushed() {
	// ui.textBoxGetProblem.setText("1");
	// ui.onButtonCopyProblem(null);
	//
	// Timer timer = new Timer() {
	// public void run() {
	// ui.validateProblem();
	// assertFalse(doesPanelWarningHaveUpdateNoteMessage());
	// finishTest();
	// }
	// };
	//
	// delayTestFinish(10 * 1000);
	//
	// timer.schedule(1000);
	// }
	//
	// @Ignore
	// @Test
	// public void
	// testValidateProblemShouldShowUpdateNoteWarningIfGetButtonIsPushedAfterCopyButton() {
	// ui.textBoxGetProblem.setText("1");
	// ui.onButtonCopyProblem(null);
	//
	// final Timer timer2 = new Timer() {
	// public void run() {
	// ui.validateProblem();
	// assertTrue(doesPanelWarningHaveUpdateNoteMessage());
	// finishTest();
	// }
	// };
	//
	// Timer timer1 = new Timer() {
	// public void run() {
	// ui.validateProblem();
	// assertFalse(doesPanelWarningHaveUpdateNoteMessage());
	//
	// ui.onButtonGetProblem(null);
	//
	// timer2.schedule(1000);
	// }
	// };
	//
	// delayTestFinish(10 * 1000);
	//
	// timer1.schedule(1000);
	// }
	//
	// @Ignore
	// @Test
	// public void
	// testValidateProblemShouldNotShowUpdateNoteWarningIfCopyButtonIsPushedAfterGetButton() {
	// ui.textBoxGetProblem.setText("1");
	// ui.onButtonGetProblem(null);
	//
	// final Timer timer2 = new Timer() {
	// public void run() {
	// ui.validateProblem();
	// assertFalse(doesPanelWarningHaveUpdateNoteMessage());
	// finishTest();
	// }
	// };
	//
	// Timer timer1 = new Timer() {
	// public void run() {
	// ui.validateProblem();
	// assertTrue(doesPanelWarningHaveUpdateNoteMessage());
	//
	// ui.onButtonCopyProblem(null);
	//
	// timer2.schedule(1000);
	// }
	// };
	//
	// delayTestFinish(10 * 1000);
	//
	// timer1.schedule(1000);
	// }

}
