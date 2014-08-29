package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class ValidatorTypingTest extends ValidatorTestBase {
	private ValidatorTyping validator;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorTyping();
		problem.type = ProblemType.Typing;
		problem.answers = toArray("ABC");
	}

	@Test
	public void checkShouldReturnTrueIfValid() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNoAnswers() {
		problem.answers = toArray();
		assertEquals(Arrays.asList("解答が入力されていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfTooLong() {
		problem.answers = toArray("0123456789");
		assertEquals(Arrays.asList("1個目の解答の文字数が多すぎます(8文字以下にして下さい)"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfInvalidCharacters() {
		problem.answers = toArray("Aあア");
		assertEquals(Arrays.asList("1個目の解答に使用不可能な文字が含まれている、又は文字の種類が混在しています"),
				validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnTrueForHiraganaWithBar() {
		problem.answers = toArray("うろたんだー");
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnTrueForKatakanaWithBar() {
		problem.answers = toArray("ウロタンダー");
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}
}