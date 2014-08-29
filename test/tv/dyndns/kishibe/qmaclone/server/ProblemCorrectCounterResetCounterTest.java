package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ProblemCorrectCounterResetCounterTest {

	private static final int FAKE_USER_CODE = 12345678;
	private ProblemCorrectCounterResetCounter correct;

	@Before
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
