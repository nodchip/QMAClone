package tv.dyndns.kishibe.qmaclone.client.game.judge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

@RunWith(JUnit4.class)
public class JudgeTatoTest extends JudgeTestBase {
	private JudgeTato judge;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		judge = new JudgeTato();
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
		problem.shuffledAnswers = toArray("a", "b", "c");
		problem.shuffledChoices = toArray("a", "b", "c", "d");
		String d = Constant.DELIMITER_GENERAL;
		String playerAnswer = "a" + d + "b" + d + "c";
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrect() {
		problem.shuffledAnswers = toArray("a", "b", "c");
		problem.shuffledChoices = toArray("a", "b", "c", "d");
		String d = Constant.DELIMITER_GENERAL;
		String playerAnswer = "a" + d + "b" + d + "c" + d + "d";
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnTrueIfCorrectWith3Display() {
		problem.shuffledAnswers = toArray("a", "b");
		problem.shuffledChoices = toArray("a", "b", "c");
		String d = Constant.DELIMITER_GENERAL;
		String playerAnswer = "a" + d + "b";
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrectWith3Display() {
		problem.shuffledAnswers = toArray("a", "b");
		problem.shuffledChoices = toArray("a", "b", "c");
		String d = Constant.DELIMITER_GENERAL;
		String playerAnswer = "a" + d + "b" + d + "c";
		assertFalse(judge.judge(problem, playerAnswer));
	}

}
