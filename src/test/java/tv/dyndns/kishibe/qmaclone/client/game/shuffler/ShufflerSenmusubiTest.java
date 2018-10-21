package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ShufflerSenmusubiTest extends ShufflerTestBase {
	private ShufflerSenmusubi shuffler;

	@Before
	public void setUp() {
		super.setUp();
		shuffler = new ShufflerSenmusubi(4);
		problem.numberOfDisplayedChoices = 4;
	}

	@Test
	public void shuffleShouldWorkWithThreeChoicesAndAnswers() {
		problem.answers = toArray("a", "b", "c");
		problem.choices = toArray("A", "B", "C");
		shuffler.shuffle(problem, toArray(1, 2, 0), toArray(1, 2, 0));
		assertEquals(Arrays.asList("c", "a", "b"), problem.getShuffledAnswerList());
		assertEquals(Arrays.asList("C", "A", "B"), problem.getShuffledChoiceList());
	}

	@Test
	public void shuffleShouldWorkWithFourChoicesAndAnswers() {
		problem.answers = toArray("a", "b", "c", "d");
		problem.choices = toArray("A", "B", "C", "D");
		shuffler.shuffle(problem, toArray(1, 2, 3, 0), toArray(1, 2, 3, 0));
		assertEquals(Arrays.asList("c", "d", "a", "b"), problem.getShuffledAnswerList());
		assertEquals(Arrays.asList("C", "D", "A", "B"), problem.getShuffledChoiceList());
	}

	@Test
	public void shuffleShouldWorkWithEightChoicesAndAnswers() {
		problem.answers = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		problem.choices = toArray("A", "B", "C", "D", "E", "F", "G", "H");
		shuffler.shuffle(problem, toArray(1, 2, 3, 4, 5, 6, 7, 0), toArray(1, 2, 3, 4, 5, 6, 7, 0));
		assertEquals(Arrays.asList("c", "d", "e", "b"), problem.getShuffledAnswerList());
		assertEquals(Arrays.asList("C", "D", "E", "B"), problem.getShuffledChoiceList());
	}

	@Test
	public void shuffleShouldWorkWithEightChoicesAndAnswersWith3Display() {
		problem.numberOfDisplayedChoices = 3;
		problem.answers = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		problem.choices = toArray("A", "B", "C", "D", "E", "F", "G", "H");
		shuffler.shuffle(problem, toArray(1, 2, 3, 4, 5, 6, 7, 0), toArray(1, 2, 3, 4, 5, 6, 7, 0));
		assertEquals(Arrays.asList("c", "d", "b"), problem.getShuffledAnswerList());
		assertEquals(Arrays.asList("C", "D", "B"), problem.getShuffledChoiceList());
	}

}
