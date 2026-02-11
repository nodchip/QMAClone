package tv.dyndns.kishibe.qmaclone.client.game.judge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

public class JudgeTatoTest extends JudgeTestBase {
	private JudgeTato judge;

	@BeforeEach
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
