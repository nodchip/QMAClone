package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BadUserDetectorTest {

	private static final int FAKE_USER_CODE_1 = 11111111;
	private static final int FAKE_USER_CODE_2 = 22222222;

	@Mock
	private Database mockDatabase;
	private BadUserDetector manager;

	@BeforeEach
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
		Map<Integer, Set<Integer>> startLogs = Maps.newHashMap();
		Map<Integer, Set<Integer>> finishLogs = Maps.newHashMap();

		manager.extractStartAndFinish(body, startLogs, finishLogs);

		assertThat(startLogs).isEqualTo(ImmutableMap.of(1, ImmutableSet.of(123), 2, ImmutableSet.of(123, 234)));
		assertThat(finishLogs).isEqualTo(
				ImmutableMap.of(1, ImmutableSet.of(123), 2, ImmutableSet.of(123), 2953, ImmutableSet.of(22950866)));
	}

	@Test
	public void detectBadProblemCreatorShouldJudgeWithNumberOfProblems() throws Exception {
		when(mockDatabase.getUserCodeToIndicatedProblems())
				.thenReturn(ImmutableMap.of(FAKE_USER_CODE_1, 100, FAKE_USER_CODE_2, 5));

		manager.detectBadProblemCreator();

		// verify(mockDatabase).clearRestrictedUserCodes(RestrictionType.PROBLEM_SUBMITTION);
		verify(mockDatabase).addRestrictedUserCode(FAKE_USER_CODE_1, RestrictionType.PROBLEM_SUBMITTION);
	}
}
