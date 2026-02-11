package tv.dyndns.kishibe.qmaclone.client.game.accuracyrate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;

public class AccuracyRateNormalizerYontakuTest {

	private AccuracyRateNormalizerYontaku accuracyRateNormalizer;

	@BeforeEach
	public void setUp() throws Exception {
		accuracyRateNormalizer = new AccuracyRateNormalizerYontaku();
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

		assertEquals(1.0 / 3.0, accuracyRateNormalizer.normalize(problem), 1e-8);
	}

}
