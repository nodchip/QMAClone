package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

public class ValidatorMarubatsuTest extends ValidatorTestBase {
	private ValidatorMarubatsu validator;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorMarubatsu();
		problem.type = ProblemType.Marubatsu;
		problem.answers = toArray("○");
		problem.imageChoice = false;
	}

	@Test
	public void checkShouldWork() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNoAnswer() {
		problem.answers = toArray();
		assertEquals(Arrays.asList("解答が入力されていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNotMarubatsu() {
		problem.answers = toArray("丸");
		assertEquals(Arrays.asList("解答が○×になっていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfChoicesIsLow() {
		problem.imageChoice = true;
		problem.answers = toArray("http://www.google.com/");
		problem.choices = toArray("http://www.google.com/");
		assertEquals(Arrays.asList("選択肢の数が足りません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfAnswerIsNotContainedInChoices() {
		problem.imageChoice = true;
		problem.choices = toArray("http://www.google.com/logo0.jpg",
				"http://www.google.com/logo1.jpg");
		assertEquals(Arrays.asList("解答が選択肢に含まれていません"), validator.check(problem).warn);
	}
}
