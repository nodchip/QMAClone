package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class ValidatorJunbanTest extends ValidatorTestBase {
	private ValidatorJunban validator;

	@Before
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
