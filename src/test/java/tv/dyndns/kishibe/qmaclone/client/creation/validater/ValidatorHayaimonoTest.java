package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class ValidatorHayaimonoTest extends ValidatorTestBase {
	private ValidatorHayaimono validator;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorHayaimono();
		problem.type = ProblemType.Hayaimono;
		problem.answers = toArray("a", "b", "c");
		problem.choices = toArray("a", "b", "c", "d", "e", "f", "g", "h");
	}

	@Test
	public void checkShouldReturnTrueIfValid() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfChoicesIsNotValid() {
		problem.choices = toArray("a", "b", "c");
		assertEquals(Arrays.asList("選択肢は6つ又は8つ必要です"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfAnswerIsNotValidFor6Choices() {
		problem.answers = toArray("a", "b", "c");
		problem.choices = toArray("a", "b", "c", "d", "e", "f");
		assertEquals(Arrays.asList("選択肢が6つの場合は解答は2つ必要です"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfAnswerIsNotValidFor8Choices() {
		problem.answers = toArray("a", "b");
		problem.choices = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		assertEquals(Arrays.asList("選択肢が8つの場合は解答は3つ又は4つ必要です"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfAnswerIsNotContainedInChoices() {
		problem.answers = toArray("A", "b", "c");
		assertEquals(Arrays.asList("1個目の解答が選択肢に含まれていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfAnswersAreDuplicated() {
		problem.answers = toArray("a", "a", "a");
		assertEquals(Arrays.asList("解答が重複しています"), validator.check(problem).warn);
	}
}
