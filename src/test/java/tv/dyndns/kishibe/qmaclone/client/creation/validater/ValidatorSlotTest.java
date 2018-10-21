package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class ValidatorSlotTest extends ValidatorTestBase {
	private ValidatorSlot validator;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorSlot();
		problem.type = ProblemType.Slot;
		problem.answers = toArray("abcd", "ABCD", "0123", "あいうえ");
	}

	@Test
	public void checkShouldReturnTrueIfValid() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfAnswerIsLow() {
		problem.answers = toArray("abcd", "ABCD");
		assertEquals(Arrays.asList("解答は4つ必要です"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfLengthsAreDifferent() {
		problem.answers = toArray("a", "AB", "012", "あいうえ");
		assertEquals(Arrays.asList("解答の長さがそろっていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfTooShort() {
		problem.answers = toArray("a", "A", "0", "あ");
		assertEquals(Arrays.asList("解答は2文字以でなければなりません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfTooLong() {
		problem.answers = toArray("0123456", "ABCDEFG", "abcdefg", "あいうえおかき");
		assertEquals(Arrays.asList("解答は6文字以下でなければなりません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIf() {
		problem.answers = toArray("Abcd", "ABCD", "0123", "あいうえ");
		assertEquals(Arrays.asList("1文字目の縦列に重複している文字があります"), validator.check(problem).warn);
	}
}
