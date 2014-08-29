package tv.dyndns.kishibe.qmaclone.client.game.judge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

import com.google.common.base.Joiner;

@RunWith(JUnit4.class)
public class JudgeSenmusubiTest extends JudgeTestBase {

	private JudgeSenmusubi judge;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		judge = new JudgeSenmusubi();
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
		problem.answers = toArray("a", "b", "c", "d");
		problem.choices = toArray("A", "B", "C", "D");
		problem.shuffledAnswers = toArray("a", "b", "c", "d");
		problem.shuffledChoices = toArray("A", "B", "C", "D");
		String p = Constant.DELIMITER_KUMIAWASE_PAIR;
		String l = Constant.DELIMITER_GENERAL;
		String playerAnswer = Joiner.on(l).join("A" + p + "a", "B" + p + "b", "C" + p + "c",
				"D" + p + "d");
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfIncorrect() {
		problem.answers = toArray("a", "b", "c", "d");
		problem.choices = toArray("A", "B", "C", "D");
		problem.shuffledAnswers = toArray("a", "b", "c", "d");
		problem.shuffledChoices = toArray("A", "B", "C", "D");
		String p = Constant.DELIMITER_KUMIAWASE_PAIR;
		String l = Constant.DELIMITER_GENERAL;
		String playerAnswer = Joiner.on(l).join("A" + p + "a", "C" + p + "b", "B" + p + "c",
				"D" + p + "d");
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseIfResign() {
		problem.answers = toArray("a", "b", "c", "d");
		problem.choices = toArray("A", "B", "C", "D");
		problem.shuffledAnswers = toArray("a", "b", "c", "d");
		problem.shuffledChoices = toArray("A", "B", "C", "D");
		String p = Constant.DELIMITER_KUMIAWASE_PAIR;
		String l = Constant.DELIMITER_GENERAL;
		String playerAnswer = Joiner.on(l).join("A" + p + "a", "B" + p + "b", "C" + p + "c");
		assertFalse(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnTrueForThreeChoices() {
		problem.answers = toArray("a", "b", "c");
		problem.choices = toArray("A", "B", "C");
		problem.shuffledAnswers = toArray("a", "b", "c");
		problem.shuffledChoices = toArray("A", "B", "C");
		String p = Constant.DELIMITER_KUMIAWASE_PAIR;
		String l = Constant.DELIMITER_GENERAL;
		String playerAnswer = Joiner.on(l).join("B" + p + "b", "C" + p + "c", "A" + p + "a");
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnTrueForEightChoices() {
		problem.answers = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		problem.choices = toArray("A", "B", "C", "D", "E", "F", "G", "H");
		problem.shuffledAnswers = toArray("e", "f", "g", "h");
		problem.shuffledChoices = toArray("E", "F", "G", "H");
		String p = Constant.DELIMITER_KUMIAWASE_PAIR;
		String l = Constant.DELIMITER_GENERAL;
		String playerAnswer = Joiner.on(l).join("E" + p + "e", "F" + p + "f", "G" + p + "g",
				"H" + p + "h");
		assertTrue(judge.judge(problem, playerAnswer));
	}

	@Test
	public void judgeShouldReturnFalseForPrefixCollition() {
		problem.answers = toArray("a", "aa", "aaa", "aaaa", "aaaaa", "aaaaaa", "aaaaaaa",
				"aaaaaaaa");
		problem.choices = toArray("A", "B", "C", "D", "E", "F", "G", "H");
		problem.shuffledAnswers = toArray("a", "aa", "aaa", "aaaa");
		problem.shuffledChoices = toArray("A", "B", "C", "D");
		String p = Constant.DELIMITER_KUMIAWASE_PAIR;
		String l = Constant.DELIMITER_GENERAL;
		String playerAnswer = Joiner.on(l).join("A" + p + "aaaaa", "B" + p + "aaaaa",
				"C" + p + "aaaa", "D" + p + "aaaa");
		assertFalse(judge.judge(problem, playerAnswer));
	}

}
