package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ShufflerMojiPanel}.
 * 
 * @author nodchip
 */
public class ShufflerMojiPanelTest extends ShufflerTestBase {
	private ShufflerMojiPanel shuffler;

	@BeforeEach
	public void setUp() {
		super.setUp();
		shuffler = new ShufflerMojiPanel();
	}

	@Test
	public void shuffleShouldShuffleChoicesAndAnswers() {
		problem.answers = toArray("a", "b", "c", "d");
		problem.choices = toArray("A", "B", "C", "D");
		shuffler.shuffle(problem, toArray(3, 2, 1, 0), toArray(3, 2, 1, 0));
		assertArrayEquals(toArray("a", "b", "c", "d"), problem.shuffledAnswers);
		assertArrayEquals(toArray("D", "C", "B", "A"), problem.shuffledChoices);
	}
}
