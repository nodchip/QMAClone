package tv.dyndns.kishibe.qmaclone.client.game.accuracyrate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;

public class AccuracyRateNormalizerDefaultTest {

	private AccuracyRateNormalizerDefault accuracyRateNormalizer;

	@Before
	public void setUp() throws Exception {
		accuracyRateNormalizer = new AccuracyRateNormalizerDefault();
	}

	@Test
	public void normalizeShouldReturnNegativeForNewProblem() {
		PacketProblemMinimum problem = new PacketProblemMinimum();
		problem.good = 0;
		problem.bad = 0;

		assertEquals(-1, accuracyRateNormalizer.normalize(problem), 1e-8);
	}

	@Test
	public void normalizeShouldReturnPositiveForNormalProblems() {
		PacketProblemMinimum problem = new PacketProblemMinimum();
		problem.good = 10;
		problem.bad = 10;

		assertEquals(0.5, accuracyRateNormalizer.normalize(problem), 1e-8);
	}

}
