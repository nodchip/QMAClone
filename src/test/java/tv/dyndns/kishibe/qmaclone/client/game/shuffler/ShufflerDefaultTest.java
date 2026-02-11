package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShufflerDefaultTest extends ShufflerTestBase {
	private ShufflerDefault shuffler;

	@BeforeEach
	public void setUp() {
		super.setUp();
		shuffler = new ShufflerDefault();
	}

	@Test
	public void shuffleShouldShuffleChoicesAndAnswers() {
		problem.answers = toArray("a", "b", "c", "d");
		problem.choices = toArray("A", "B", "C", "D");
		shuffler.shuffle(problem, toArray(3, 2, 1, 0), toArray(3, 2, 1, 0));
		assertArrayEquals(toArray("d", "c", "b", "a"), problem.shuffledAnswers);
		assertArrayEquals(toArray("D", "C", "B", "A"), problem.shuffledChoices);
	}
}
