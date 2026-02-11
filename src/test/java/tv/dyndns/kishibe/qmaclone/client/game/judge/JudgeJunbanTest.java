package tv.dyndns.kishibe.qmaclone.client.game.judge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

import com.google.common.base.Joiner;

public class JudgeJunbanTest extends JudgeTestBase {

	private JudgeJunban judge;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		judge = new JudgeJunban();
		problem.numberOfDisplayedChoices = 4;
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
		String playerAnswer = Joiner.on(Constant.DELIMITER_GENERAL).join("a", "b", "c", "d");
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrect() {
		problem.shuffledAnswers = toArray("a", "b", "c", "d");
		String playerAnswer = Joiner.on(Constant.DELIMITER_GENERAL).join("a", "c", "b", "d");
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnTrueIfCorrectWith3Choices() {
		problem.shuffledAnswers = toArray("a", "b", "c");
		problem.numberOfDisplayedChoices = 3;
		String playerAnswer = Joiner.on(Constant.DELIMITER_GENERAL).join("a", "b", "c");
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrectWith3Choices() {
		problem.shuffledAnswers = toArray("a", "b", "c", "d");
		problem.numberOfDisplayedChoices = 3;
		String playerAnswer = Joiner.on(Constant.DELIMITER_GENERAL).join("a", "c", "b");
		assertFalse(judge.judge(problem, playerAnswer));
	}

}
