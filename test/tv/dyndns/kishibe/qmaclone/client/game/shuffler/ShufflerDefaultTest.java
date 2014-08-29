package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ShufflerDefaultTest extends ShufflerTestBase {
	private ShufflerDefault shuffler;

	@Before
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
