package tv.dyndns.kishibe.qmaclone.client.game.input;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerViewImpl;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanelTyping;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;

public class InputWidgetTypingTest extends QMACloneGWTTestCaseBase {
	private PacketProblem problem;
	private InputWidgetTyping input;
	private AnswerView answerView;
	private QuestionPanel questionPanel;
	private SessionData sessionData;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		problem = TestDataProvider.getProblem();
		sessionData = TestDataProvider.getSessionData();
		answerView = new AnswerViewImpl(Integer.MAX_VALUE);
		questionPanel = new QuestionPanelTyping(problem, sessionData);
	}

	public void testNormalKeyboardIsUsed() {
		UserData.get().setQwertyHiragana(false);
		problem.shuffledAnswers = new String[] { "あ" };
		input = new InputWidgetTyping(problem, answerView, questionPanel, sessionData);

		assertTrue(input.panel.getWidget() instanceof InputWidgetTyping50);
	}

	public void testQwertyKeyboardIsUsed() {
		UserData.get().setQwertyKatakana(true);
		problem.shuffledAnswers = new String[] { "ア" };
		input = new InputWidgetTyping(problem, answerView, questionPanel, sessionData);

		assertTrue(input.panel.getWidget() instanceof InputWidgetTypingQwerty);
	}

	public void testOnClickShouldSwitchToQwerty() {
		UserData.get().setQwertyAlphabet(false);
		problem.shuffledAnswers = new String[] { "０" };
		input = new InputWidgetTyping(problem, answerView, questionPanel, sessionData);

		input.onChangeButton();
		assertTrue(input.panel.getWidget() instanceof InputWidgetTypingQwerty);
		assertEquals(true, UserData.get().isQwertyAlphabet());
	}

	public void testOnClickShouldSwitchToNormal() {
		UserData.get().setQwertyHiragana(true);
		problem.shuffledAnswers = new String[] { "あ" };
		input = new InputWidgetTyping(problem, answerView, questionPanel, sessionData);

		input.onChangeButton();
		assertTrue(input.panel.getWidget() instanceof InputWidgetTyping50);
		assertEquals(false, UserData.get().isQwertyHiragana());
	}
}
