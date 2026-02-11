package tv.dyndns.kishibe.qmaclone.client.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

public class ProblemGenreTest {
	@Test
	public void testGetInitial() {
		assertEquals("ノ", ProblemGenre.Random.getInitial());
	}

	@Test
	public void testGetColor() {
		assertEquals("gray", ProblemGenre.Random.getColor());
	}

	@Test
	public void testGetIndex() {
		assertEquals(0, ProblemGenre.Random.getIndex());
	}

	@Test
	public void testToString() {
		assertEquals("ノンジャンル", ProblemGenre.Random.toString());
	}

	@Test
	public void testToBitFlag() {
		assertEquals(6, ProblemGenre.toBitFlag(EnumSet.of(ProblemGenre.Anige, ProblemGenre.Sports)));
	}

	@Test
	public void testFromBitFlag() {
		assertEquals(EnumSet.of(ProblemGenre.Anige, ProblemGenre.Sports),
				ProblemGenre.fromBitFlag(6));
	}

	@Test
	public void testFromName() {
		assertEquals(ProblemGenre.Random, ProblemGenre.fromName("ノンジャンル"));
	}
}
