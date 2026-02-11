package tv.dyndns.kishibe.qmaclone.client.game.judge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JudgeDefaultTest extends JudgeTestBase {
	private JudgeDefault judge;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		judge = new JudgeDefault();
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
		problem.shuffledAnswers = toArray("a", "b", "c", "d");
		String playerAnswer = "a";
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrect() {
		problem.shuffledAnswers = toArray("a", "b", "c", "d");
		String playerAnswer = "A";
		assertFalse(judge.judge(problem, playerAnswer));
	}
}
