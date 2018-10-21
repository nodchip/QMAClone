package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class ValidatorNarabekaeTest extends ValidatorTestBase {
	private Validator validator;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorNarabekae();
		problem.type = ProblemType.Narabekae;
		problem.answers = toArray("012345678");
	}

	@Test
	public void checkShouldReturnTrueIfValid() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFaseIfNoAnswers() {
		problem.answers = toArray();
		assertEquals(Arrays.asList("解答が入力されていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFaseIfTooShort() {
		problem.answers = toArray("a");
		assertEquals(Arrays.asList("1番目の解答が短すぎます(3文字以上でなければなりません)"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFaseIfTooLong() {
		problem.answers = toArray("0123456789");
		assertEquals(Arrays.asList("1番目の解答が長すぎます(9文字以下でなければなりません)"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFaseIf() {
		problem.answers = toArray("123", "abc");
		assertEquals(Arrays.asList("解答に含まれている文字が違います"), validator.check(problem).warn);
	}
}
