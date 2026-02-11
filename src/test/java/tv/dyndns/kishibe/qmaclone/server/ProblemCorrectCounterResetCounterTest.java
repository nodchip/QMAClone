package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProblemCorrectCounterResetCounterTest {

	private static final int FAKE_USER_CODE = 12345678;
	private ProblemCorrectCounterResetCounter correct;

	@BeforeEach
	public void setUp() throws Exception {
		correct = new ProblemCorrectCounterResetCounter();
	}

	@Test
	public void runShouldClearUserCodes() {
		correct.add(FAKE_USER_CODE);
		correct.add(FAKE_USER_CODE);
		correct.add(FAKE_USER_CODE);
		correct.run();
		assertTrue(correct.isAbleToReset(FAKE_USER_CODE));
	}

	@Test
	public void andAndIsAbleToResetIntegrationTest() {
		assertTrue(correct.isAbleToReset(FAKE_USER_CODE));
		correct.add(FAKE_USER_CODE);
		assertTrue(correct.isAbleToReset(FAKE_USER_CODE));
		correct.add(FAKE_USER_CODE);
		assertTrue(correct.isAbleToReset(FAKE_USER_CODE));
		correct.add(FAKE_USER_CODE);
		assertFalse(correct.isAbleToReset(FAKE_USER_CODE));
	}

}
