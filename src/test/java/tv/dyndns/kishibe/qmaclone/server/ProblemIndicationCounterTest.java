package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProblemIndicationCounterTest {

	private static final int FAKE_USER_CODE = 12345678;
	private ProblemIndicationCounter counter;

	@BeforeEach
	public void setUp() throws Exception {
		counter = new ProblemIndicationCounter();
	}

	@Test
	public void testRun() {
		counter.add(FAKE_USER_CODE);
		counter.add(FAKE_USER_CODE);
		counter.add(FAKE_USER_CODE);
		counter.run();
		assertTrue(counter.isAbleToIndicate(FAKE_USER_CODE));
	}

	@Test
	public void AddAndIsAbleToIndicateIntegrationTest() {
		assertTrue(counter.isAbleToIndicate(FAKE_USER_CODE));
		counter.add(FAKE_USER_CODE);
		assertTrue(counter.isAbleToIndicate(FAKE_USER_CODE));
		counter.add(FAKE_USER_CODE);
		assertTrue(counter.isAbleToIndicate(FAKE_USER_CODE));
		counter.add(FAKE_USER_CODE);
		assertFalse(counter.isAbleToIndicate(FAKE_USER_CODE));
	}

}
