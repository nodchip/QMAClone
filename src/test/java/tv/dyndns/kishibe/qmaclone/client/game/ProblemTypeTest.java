package tv.dyndns.kishibe.qmaclone.client.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.EnumSet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ProblemTypeTest {
	@Test
	public void testGetInitial() {
		assertEquals("ラ", ProblemType.Random.getInitial());
	}

	@Test
	public void testGetIndex() {
		assertEquals(0, ProblemType.Random.getIndex());
	}

	@Test
	public void testToString() {
		assertEquals("ランダム", ProblemType.Random.toString());
	}

	@Test
	public void testGetDescription() {
		assertNotNull(ProblemType.Marubatsu.getDescription());
	}

	@Test
	public void testGetNumberOfAnswers() {
		assertEquals(0, ProblemType.Random.getNumberOfAnswers());
	}

	@Test
	public void testGetNumberOfChoices() {
		assertEquals(0, ProblemType.Random.getNumberOfChoices());
	}

	@Test
	public void testIsImageAnswer() {
		assertFalse(ProblemType.Random.isImageAnswer());
	}

	@Test
	public void testIsImageChoice() {
		assertFalse(ProblemType.Random.isImageChoice());
	}

	@Test
	public void testIsPolygonCreation() {
		assertFalse(ProblemType.Random.isPolygonCreation());
	}

	@Disabled
	@Test
	public void testValidate() {
		// TODO(nodchip): テストを書く
	}

	@Disabled
	@Test
	public void testJudge() {
		// TODO(nodchip): テストを書く
	}

	@Disabled
	@Test
	public void testShuffleAnswersAndChoices() {
		// TODO(nodchip): テストを書く
	}

	@Test
	public void testToBitFlag() {
		assertEquals(6,
				ProblemType.toBitFlag(EnumSet.of(ProblemType.Marubatsu, ProblemType.YonTaku)));
	}

	@Test
	public void testFromBitFlag() {
		assertEquals(EnumSet.of(ProblemType.Marubatsu, ProblemType.YonTaku),
				ProblemType.fromBitFlag(6));
	}

	@Test
	public void testFromName() {
		assertEquals(ProblemType.Marubatsu, ProblemType.fromName("○×"));
	}
}
