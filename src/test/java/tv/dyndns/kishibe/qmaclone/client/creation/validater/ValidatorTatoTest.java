package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

public class ValidatorTatoTest extends ValidatorTestBase {
	private ValidatorTato validator;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorTato();
		problem.type = ProblemType.Tato;
		problem.choices = toArray("a", "b", "c", "d");
		problem.answers = toArray("a", "b");
	}

	@Test
	public void checkShouldReturnTrueIfValid() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfChoicesIsLow() {
		problem.choices = toArray("a", "b");
		assertEquals(Arrays.asList("選択肢は3つ以上必要です"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfAnswersIsLow() {
		problem.answers = toArray();
		assertEquals(Arrays.asList("解答は1つ以上必要です"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfAnswerIsNotContainedInChoices() {
		problem.answers = toArray("A", "b");
		assertEquals(Arrays.asList("1個目の解答が選択肢に含まれていません"), validator.check(problem).warn);
	}
}
