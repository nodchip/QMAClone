package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.database.Database;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VoteManagerTest {

	private static final int FAKE_PROBLEM_ID = 12345;

	@Mock
	private Database mockDatabase;
	@Mock
	private ThreadPool mockThreadPool;
	@Mock
	private RestrictedUserUtils mockRestrictedUserUtils;
	@Captor
	private ArgumentCaptor<PacketProblem> problemCaptor;
	private VoteManager voteManager;

	@BeforeEach
	public void setUp() throws Exception {
		voteManager = new VoteManager(mockDatabase, mockThreadPool, mockRestrictedUserUtils);
	}

	@Test
	public void resetShouldSetVoteGoodBadToZero() throws Exception {
		PacketProblem fakePacketProblem = new PacketProblem();
		fakePacketProblem.voteGood = 10;
		fakePacketProblem.voteBad = 20;

		when(mockDatabase.getProblem(ImmutableList.of(FAKE_PROBLEM_ID))).thenReturn(
				ImmutableList.of(fakePacketProblem));

		voteManager.reset(FAKE_PROBLEM_ID);

		verify(mockDatabase).updateProblem(problemCaptor.capture());
		assertEquals(0, problemCaptor.getValue().voteGood);
		assertEquals(0, problemCaptor.getValue().voteBad);
	}

}
