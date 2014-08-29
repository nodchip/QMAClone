package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class ValidatorFlashTest extends ValidatorTestBase {
	private ValidatorFlash validator;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorFlash();
		problem.type = ProblemType.Flash;
		problem.answers = toArray("アイウエオ");
	}

	@Test
	public void checkShouldWork() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldWarnIfNoAnswer() {
		problem.answers = toArray();
		assertEquals(Arrays.asList("解答が入力されていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldWarnIfTooLongAnswer() {
		problem.answers = toArray("アイウエオカキクケコ");
		assertEquals(Arrays.asList("1番目の解答の文字数が多すぎます(8文字以下である必要があります)"),
				validator.check(problem).warn);
	}

	@Test
	public void checkShouldWarnIfAnswerIsInvalid() {
		problem.answers = toArray("無念");
		assertEquals(Arrays.asList("1番目の解答に使用不可能な文字が含まれている、又は文字の種類が混在しています"),
				validator.check(problem).warn);
	}

	@Test
	public void checkShouldWarnIfDifferentLetters() {
		problem.answers = toArray("ＡＢＣ", "０１２");
		assertEquals(Arrays.asList("解答に含まれている文字が違います"), validator.check(problem).warn);
	}
}
