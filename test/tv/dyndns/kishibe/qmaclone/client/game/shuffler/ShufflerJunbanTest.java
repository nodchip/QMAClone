package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ShufflerJunbanTest extends ShufflerTestBase {

	private ShufflerJunban shuffler;

	@Before
	public void setUp() {
		super.setUp();
		shuffler = new ShufflerJunban();
		problem.numberOfDisplayedChoices = 4;
	}

	@Test
	public void shuffleShouldWorkWithThreeAnswers() {
		problem.answers = toArray("a", "b", "c");
		shuffler.shuffle(problem, toArray(1, 2, 0), toArray(1, 2, 0));
		assertEquals(asList("a", "b", "c"), problem.getShuffledAnswerList());
		assertEquals(asList("b", "c", "a"), problem.getShuffledChoiceList());
	}

	@Test
	public void shuffleShouldWorkWithFourAnswers() {
		problem.answers = toArray("a", "b", "c", "d");
		shuffler.shuffle(problem, toArray(1, 2, 3, 0), toArray(1, 2, 3, 0));
		assertEquals(asList("a", "b", "c", "d"), problem.getShuffledAnswerList());
		assertEquals(asList("b", "c", "d", "a"), problem.getShuffledChoiceList());
	}

	@Test
	public void shuffleShouldWorkWithEightAnswers() {
		problem.answers = toArray("q", "w", "e", "r", "t", "y", "u", "i");
		shuffler.shuffle(problem, toArray(1, 3, 2, 4, 5, 6, 7, 0), toArray(1, 3, 2, 4, 5, 6, 7, 0));
		assertEquals(asList("w", "e", "r", "t"), problem.getShuffledAnswerList());
		assertEquals(asList("w", "r", "e", "t"), problem.getShuffledChoiceList());
	}

	@Test
	public void shuffleShouldWorkWithNumberOfDisplayedChoices() {
		problem.answers = toArray("q", "w", "e", "r", "t", "y", "u", "i");
		problem.numberOfDisplayedChoices = 3;
		shuffler.shuffle(problem, toArray(1, 3, 2, 4, 5, 6, 7, 0), toArray(1, 3, 2, 4, 5, 6, 7, 0));
		assertEquals(asList("w", "e", "r"), problem.getShuffledAnswerList());
		assertEquals(asList("w", "r", "e"), problem.getShuffledChoiceList());
	}

}
