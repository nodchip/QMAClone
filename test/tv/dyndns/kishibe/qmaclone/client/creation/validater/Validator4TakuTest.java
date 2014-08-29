package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class Validator4TakuTest extends ValidatorTestBase {
	private Validator4Taku validator;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		validator = new Validator4Taku(true);
		problem.type = ProblemType.YonTaku;
		problem.choices = toArray("a", "b", "c", "d");
	}

	@Test
	public void checkShouldReturnTrueIfValid() {
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNumberOfChoicesIsLow() {
		problem.choices = toArray("a", "b", "c");
		assertEquals(Arrays.asList("選択肢は4つ必要です"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfWaitIsNotContained() {
		assertEquals(Arrays.asList("３行目までの各行の末尾に%w等で適宜ウェイトを入れて下さい"), validator.check(problem).info);
	}
}
