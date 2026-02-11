package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

public class ValidatorJunbanTest extends ValidatorTestBase {
	private ValidatorJunban validator;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorJunban();
		problem.type = ProblemType.Junban;
	}

	@Test
	public void checkShouldWork() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFailIfNumberOfAnswersIsLow() {
		problem.answers = toArray("a");
		assertEquals(Arrays.asList("解答の数が足りません"), validator.check(problem).warn);
	}
}
