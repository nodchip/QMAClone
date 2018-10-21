package tv.dyndns.kishibe.qmaclone.client.packet;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

import com.google.common.collect.ImmutableSet;

@RunWith(JUnit4.class)
public class PacketRoomKeyTest {
	private PacketRoomKey keyEvent1;
	private PacketRoomKey keyEvent2;
	private PacketRoomKey keyWhole1;
	private PacketRoomKey keyWhole2;
	private PacketRoomKey keyTheme1;
	private PacketRoomKey keyTheme2;

	@Before
	public void setUp() throws Exception {
		keyEvent1 = new PacketRoomKey(GameMode.EVENT, null, ImmutableSet.of(ProblemGenre.Sports),
				ImmutableSet.of(ProblemType.YonTaku));
		keyEvent2 = new PacketRoomKey(GameMode.EVENT, null, ImmutableSet.of(ProblemGenre.Sports),
				ImmutableSet.of(ProblemType.YonTaku));
		keyWhole1 = new PacketRoomKey(GameMode.WHOLE, null, ImmutableSet.of(ProblemGenre.Geinou),
				ImmutableSet.of(ProblemType.Rensou));
		keyWhole2 = new PacketRoomKey(GameMode.WHOLE, null, ImmutableSet.of(ProblemGenre.Geinou,
				ProblemGenre.Gakumon), ImmutableSet.of(ProblemType.Rensou, ProblemType.Narabekae));
		keyTheme1 = new PacketRoomKey(GameMode.THEME, "THEME", ImmutableSet.of(
				ProblemGenre.Zatsugaku, ProblemGenre.Anige), ImmutableSet.of(ProblemType.MojiPanel));
		keyTheme2 = new PacketRoomKey(GameMode.THEME, "THEME",
				ImmutableSet.of(ProblemGenre.Zatsugaku), ImmutableSet.of(ProblemType.MojiPanel,
						ProblemType.Typing));
	}

	@Test
	public void testHashCode() {
		assertEquals(keyEvent1.hashCode(), keyEvent2.hashCode());
		assertEquals(keyWhole1.hashCode(), keyWhole2.hashCode());
		assertEquals(keyTheme1.hashCode(), keyTheme2.hashCode());
	}

	@Test
	public void testEquals() {
		assertEquals(keyEvent1, keyEvent2);
		assertEquals(keyWhole1, keyWhole2);
		assertEquals(keyTheme1, keyTheme2);
	}

	@Test
	public void testPacketRoomKeyGameModeStringSetOfProblemGenreSetOfProblemType() {
		assertEquals(ImmutableSet.of(ProblemGenre.Sports), keyEvent1.getGenres());
		assertEquals(ImmutableSet.of(ProblemType.YonTaku), keyEvent1.getTypes());
		assertEquals("THEME", keyTheme1.getName());
	}
}
