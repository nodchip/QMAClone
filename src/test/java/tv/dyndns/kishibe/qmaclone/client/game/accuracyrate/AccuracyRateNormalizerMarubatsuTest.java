package tv.dyndns.kishibe.qmaclone.client.game.accuracyrate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;

public class AccuracyRateNormalizerMarubatsuTest {

	private AccuracyRateNormalizerMarubatsu accuracyRateNormalizer;

	@BeforeEach
	public void setUp() throws Exception {
		accuracyRateNormalizer = new AccuracyRateNormalizerMarubatsu();
	}

	@Test
	public void normalizeShouldReturnNegativeForNewProblems() {
		PacketProblemMinimum problem = new PacketProblemMinimum();
		problem.good = 0;
		problem.bad = 0;

		assertEquals(-1, accuracyRateNormalizer.normalize(problem), 1e-8);
	}

	@Test
	public void normalizeShouldReturnPositiveForNormalProblemsWithWeight() {
		PacketProblemMinimum problem = new PacketProblemMinimum();
		problem.good = 15;
		problem.bad = 5;

		assertEquals(0.5, accuracyRateNormalizer.normalize(problem), 1e-8);
	}
}
