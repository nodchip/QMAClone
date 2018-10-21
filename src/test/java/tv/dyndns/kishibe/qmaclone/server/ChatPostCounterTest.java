package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ChatPostCounterTest {

	private static final int FAKE_USER_CODE = 12345678;
	private static final String FAKE_REMOTE_ADDRESS = "1.2.3.4";
	private ChatPostCounter counter;

	@Before
	public void setUp() throws Exception {
		counter = new ChatPostCounter();
	}

	@Test
	public void addAndIsAbleToPostShouldCheckUserCode() {
		for (int i = 0; i < ChatPostCounter.LIMIT_PER_MINUTE; ++i) {
			counter.add(FAKE_USER_CODE, String.valueOf(i));
			assertTrue(counter.isAbleToPost(FAKE_USER_CODE, String.valueOf(i)));
		}
		counter.add(FAKE_USER_CODE, FAKE_REMOTE_ADDRESS);
		assertFalse(counter.isAbleToPost(FAKE_USER_CODE, FAKE_REMOTE_ADDRESS));
	}

	@Test
	public void addAndIsAbleToPostShouldCheckRemoteAddress() {
		for (int i = 0; i < ChatPostCounter.LIMIT_PER_MINUTE; ++i) {
			counter.add(i, FAKE_REMOTE_ADDRESS);
			assertTrue(counter.isAbleToPost(FAKE_USER_CODE, FAKE_REMOTE_ADDRESS));
		}
		counter.add(FAKE_USER_CODE, FAKE_REMOTE_ADDRESS);
		assertFalse(counter.isAbleToPost(FAKE_USER_CODE, FAKE_REMOTE_ADDRESS));
	}

	@Test
	public void addAndIsAbleToPostShouldIgnoreNullRemoteAddress() {
		for (int i = 0; i < ChatPostCounter.LIMIT_PER_MINUTE; ++i) {
			counter.add(i, null);
			assertTrue(counter.isAbleToPost(FAKE_USER_CODE, null));
		}
		counter.add(FAKE_USER_CODE, null);
		assertTrue(counter.isAbleToPost(FAKE_USER_CODE, null));
	}

	@Test
	public void addAndIsAbleToPostShouldIgnoreLocalHost() {
		for (int i = 0; i < ChatPostCounter.LIMIT_PER_MINUTE; ++i) {
			counter.add(i, ChatPostCounter.LOCAL_HOST);
			assertTrue(counter.isAbleToPost(FAKE_USER_CODE, ChatPostCounter.LOCAL_HOST));
		}
		counter.add(FAKE_USER_CODE, ChatPostCounter.LOCAL_HOST);
		assertTrue(counter.isAbleToPost(FAKE_USER_CODE, ChatPostCounter.LOCAL_HOST));
	}

	@Test
	public void runShouldClearUserCodesAndRemoteAddresses() {
		for (int i = 0; i < ChatPostCounter.LIMIT_PER_MINUTE + 1; ++i) {
			counter.add(FAKE_USER_CODE, FAKE_REMOTE_ADDRESS);
		}
		counter.run();
		assertTrue(counter.isAbleToPost(FAKE_USER_CODE, FAKE_REMOTE_ADDRESS));
	}

}
