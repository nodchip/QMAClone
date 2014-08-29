package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.BadUserDetector.SessionIdAndUserCode;
import tv.dyndns.kishibe.qmaclone.server.database.Database;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class BadUserDetectorTest {

	private static final int FAKE_USER_CODE_1 = 11111111;
	private static final int FAKE_USER_CODE_2 = 22222222;

	@Mock
	private Database mockDatabase;
	private BadUserDetector manager;

	@Before
	public void setUp() throws Exception {
		manager = new BadUserDetector(mockDatabase);
	}

	@Test
	public void extractStartAndGoalShouldParseLogs() {
		String body = "情報: Game{method=transitFromMachingToReady, sessionId=1, userCode=123}\n"
				+ "情報: Game{method=transitFromMachingToReady, sessionId=2, userCode=123}\n"
				+ "情報: Game{method=transitFromMachingToReady, sessionId=2, userCode=234}\n"
				+ "8 12, 2012 9:02:51 午後 tv.dyndns.kishibe.server.GameLogger write\n"
				+ "情報: ServiceServletStub{method=notifyGameFinished, userCode=123, sessionId=1, oldRating=123, newRating=1274, remoteAddress=127.0.0.1}\n"
				+ "8 12, 2012 9:02:52 午後 tv.dyndns.kishibe.server.GameLogger write\n"
				+ "情報: ServiceServletStub{method=notifyGameFinished, userCode=123, sessionId=2, oldRating=123, newRating=1423, remoteAddress=127.0.0.1}\n"
				+ "情報: ServiceServletStub{method=notifyGameFinished, userCode=22950866, sessionId=2953, oldRating=2019, newRating=1876, remoteAddress=203.136.117.99}";
		Set<Integer> userCodes = Sets.newHashSet();
		Set<SessionIdAndUserCode> startLogs = Sets.newHashSet();
		Set<SessionIdAndUserCode> finishLogs = Sets.newHashSet();
		Map<Integer, List<Double>> userCodeToResponseTimes = Maps.newHashMap();
		Map<SessionIdAndUserCode, Double> sessionIdAndUserCodeToTimeUpCount = Maps.newHashMap();

		manager.extractStartAndFinish(body, startLogs, finishLogs, userCodeToResponseTimes,
				sessionIdAndUserCodeToTimeUpCount, userCodes);

		assertEquals(ImmutableSet.of(new SessionIdAndUserCode(1, 123), new SessionIdAndUserCode(2,
				123), new SessionIdAndUserCode(2, 234)), startLogs);
		assertEquals(ImmutableSet.of(new SessionIdAndUserCode(1, 123), new SessionIdAndUserCode(2,
				123), new SessionIdAndUserCode(2953, 22950866)), finishLogs);
	}

	@Test
	public void sessionIdAndUserCodeShouldWorkWithHashSet() {
		SessionIdAndUserCode code0 = new SessionIdAndUserCode(123, 12345678);
		SessionIdAndUserCode code1 = new SessionIdAndUserCode(234, 23456789);

		Set<SessionIdAndUserCode> codes = Sets.newHashSet();
		codes.add(code0);
		codes.add(code1);

		assertTrue(codes.contains(new SessionIdAndUserCode(123, 12345678)));
		assertFalse(codes.contains(new SessionIdAndUserCode(999, 99999999)));
	}

	@Test
	public void detectBadProblemCreatorShouldJudgeWithNumberOfProblems() throws Exception {
		when(mockDatabase.getUserCodeToIndicatedProblems()).thenReturn(
				ImmutableMap.of(FAKE_USER_CODE_1, 100, FAKE_USER_CODE_2, 5));

		manager.detectBadProblemCreator();

		// verify(mockDatabase).clearRestrictedUserCodes(RestrictionType.PROBLEM_SUBMITTION);
		verify(mockDatabase).addRestrictedUserCode(FAKE_USER_CODE_1,
				RestrictionType.PROBLEM_SUBMITTION);
	}
}
