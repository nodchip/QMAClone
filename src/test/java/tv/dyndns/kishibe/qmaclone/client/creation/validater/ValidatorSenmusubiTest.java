package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class ValidatorSenmusubiTest extends ValidatorTestBase {
	private ValidatorSenmusubi validator;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorSenmusubi();
		problem.type = ProblemType.Senmusubi;
		problem.answers = toArray("a", "b", "c", "d");
		problem.choices = toArray("1", "2", "3", "4");
	}

	@Test
	public void checkShouldReturnTrueIfValid() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfChoicesIsLow() {
		problem.choices = toArray("a", "b");
		assertEquals(Arrays.asList("左側選択肢(選択肢欄)は3つ以上必要です", "左右の選択肢の数が違います"),
				validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfAnswersIsLow() {
		problem.answers = toArray("0", "1");
		assertEquals(Arrays.asList("右側選択肢(解答欄)は3つ以上必要です", "左右の選択肢の数が違います"),
				validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfChoicesAndAnswersAreDifferent() {
		problem.answers = toArray("a", "b", "c");
		assertEquals(Arrays.asList("左右の選択肢の数が違います"), validator.check(problem).warn);
	}
}
