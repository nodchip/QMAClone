package tv.dyndns.kishibe.qmaclone.client.game.judge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.left.AnswerPopup;

@RunWith(JUnit4.class)
public class JudgeClickTest extends JudgeTestBase {
	private JudgeClick judge;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		judge = new JudgeClick();
	}

	@Test
	public void judgeShouldReturnFalseIfNoAnswer() {
		String playerAnswer = "";
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfTimeUp() {
		String playerAnswer = null;
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnTrueIfCorrect() {
		problem.shuffledAnswers = toArray("0 0 100 0 100 100 0 100");
		String playerAnswer = "50 50";
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrect() {
		problem.shuffledAnswers = toArray("0 0 100 0 100 100 0 100");
		String playerAnswer = "150 50";
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfNoAnswer2() {
		problem.shuffledAnswers = toArray("0 0 100 0 100 100 0 100");
		String playerAnswer = AnswerPopup.LABEL_NO_ANSWER;
		assertFalse(judge.judge(problem, playerAnswer));
	}
}
