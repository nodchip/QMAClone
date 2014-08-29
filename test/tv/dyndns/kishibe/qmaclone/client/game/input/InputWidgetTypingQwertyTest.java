package tv.dyndns.kishibe.qmaclone.client.game.input;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerViewImpl;
import tv.dyndns.kishibe.qmaclone.client.game.LetterType;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanelTyping;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;

public class InputWidgetTypingQwertyTest extends QMACloneGWTTestCaseBase {

	private PacketProblem problem;
	private AnswerView answerView;
	private QuestionPanel questionPanel;
	private InputWidgetTypingQwerty widget;
	private SessionData sessionData;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		sessionData = TestDataProvider.getSessionData();
		problem = TestDataProvider.getProblem();
		answerView = new AnswerViewImpl(8, 10, true);
		questionPanel = new QuestionPanelTyping(problem, sessionData);
		widget = new InputWidgetTypingQwerty(problem, answerView, questionPanel,
				LetterType.Hiragana, sessionData);
	}

	@Test
	public void testAddLetterToAnswer() {
		// ローマ字入力のカナキーボードの8文字目で「CHI」が「チ」にならない
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack-QMAClone%2F542
		widget.addLetterToAnswer("Ａ");
		widget.addLetterToAnswer("Ａ");
		widget.addLetterToAnswer("Ａ");
		widget.addLetterToAnswer("Ａ");
		widget.addLetterToAnswer("Ａ");
		widget.addLetterToAnswer("Ａ");
		widget.addLetterToAnswer("Ａ");
		widget.addLetterToAnswer("Ｃ");
		widget.addLetterToAnswer("Ｈ");
		widget.addLetterToAnswer("Ｉ");

		assertEquals("あああああああち", widget.answerView.get());
	}

}
