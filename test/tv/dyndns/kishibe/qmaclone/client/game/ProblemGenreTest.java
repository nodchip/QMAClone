package tv.dyndns.kishibe.qmaclone.client.game;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
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
