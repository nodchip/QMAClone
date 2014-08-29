package tv.dyndns.kishibe.qmaclone.client.game.judge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JudgeSlotTest extends JudgeTestBase {
	private JudgeSlot judge;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		judge = new JudgeSlot();
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
		problem.answers = toArray("abcd", "efgh", "ijkl", "mnop");
		problem.shuffledAnswers = toArray("abcd", "efgh", "ijkl", "mnop");
		String playerAnswer = "abcd";
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrect() {
		problem.answers = toArray("abcd", "efgh", "ijkl", "mnop");
		problem.shuffledAnswers = toArray("abcd", "efgh", "ijkl", "mnop");
		String playerAnswer = "afcd";
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrect1() {
		problem.answers = toArray("abcd", "efgh", "ijkl", "mnop");
		problem.shuffledAnswers = toArray("abcd", "efgh", "ijkl", "mnop");
		String playerAnswer = "efgh";
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrect2() {
		problem.answers = toArray("abcd", "efgh", "ijkl", "mnop");
		problem.shuffledAnswers = toArray("abcd", "efgh", "ijkl", "mnop");
		String playerAnswer = "ijkl";
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrect3() {
		problem.answers = toArray("abcd", "efgh", "ijkl", "mnop");
		problem.shuffledAnswers = toArray("abcd", "efgh", "ijkl", "mnop");
		String playerAnswer = "mnop";
		assertFalse(judge.judge(problem, playerAnswer));
	}
}
