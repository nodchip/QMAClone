package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

public class Validator4TakuTest extends ValidatorTestBase {
	private Validator4Taku validator;

	@BeforeEach
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
