package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.ImmutableSet;

@RunWith(JUnit4.class)
public class ShufflerTatoTest extends ShufflerTestBase {

	private ShufflerTato shuffler;

	@Before
	public void setUp() {
		super.setUp();
		shuffler = new ShufflerTato();
		problem.numberOfDisplayedChoices = 4;
	}

	@Test
	public void shuffleShouldWorkWith3ChoicesAnd3Answer() {
		problem.answers = toArray("a", "b", "c");
		problem.choices = toArray("a", "b", "c");
		shuffler.shuffle(problem, toArray(1, 2, 0), toArray(1, 2, 0));
		assertEquals(Arrays.asList("c", "a", "b"), problem.getShuffledChoiceList());
		assertEquals(ImmutableSet.of("a", "b", "c"), ImmutableSet.copyOf(problem.shuffledAnswers));
	}

	@Test
	public void shuffleShouldWorkWith3ChoicesAnd1Answer() {
		problem.answers = toArray("a");
		problem.choices = toArray("a", "b", "c");
		shuffler.shuffle(problem, toArray(0), toArray(1, 2, 0));
		assertEquals(Arrays.asList("b", "c", "a"), problem.getShuffledChoiceList());
		assertEquals(ImmutableSet.of("a"), ImmutableSet.copyOf(problem.shuffledAnswers));
	}

	@Test
	public void shuffleShouldWorkWith4ChoicesAnd4Answer() {
		problem.answers = toArray("a", "b", "c", "d");
		problem.choices = toArray("a", "b", "c", "d");
		shuffler.shuffle(problem, toArray(1, 2, 3, 0), toArray(1, 2, 3, 0));
		assertEquals(Arrays.asList("c", "d", "a", "b"), problem.getShuffledChoiceList());
		assertEquals(ImmutableSet.of("a", "b", "c", "d"),
				ImmutableSet.copyOf(problem.shuffledAnswers));
	}

	@Test
	public void shuffleShouldWorkWith4ChoicesAnd1Answer() {
		problem.answers = toArray("a");
		problem.choices = toArray("a", "b", "c", "d");
		shuffler.shuffle(problem, toArray(0), toArray(1, 2, 3, 0));
		assertEquals(Arrays.asList("b", "c", "d", "a"), problem.getShuffledChoiceList());
		assertEquals(ImmutableSet.of("a"), ImmutableSet.copyOf(problem.shuffledAnswers));
	}

	@Test
	public void shuffleShouldWorkWith8ChoicesAnd8Answer() {
		problem.answers = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		problem.choices = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		shuffler.shuffle(problem, toArray(1, 2, 3, 4, 5, 6, 7, 0), toArray(1, 2, 3, 4, 5, 6, 7, 0));
		assertEquals(Arrays.asList("c", "d", "e", "b"), problem.getShuffledChoiceList());
		assertEquals(ImmutableSet.of("b", "c", "d", "e"),
				ImmutableSet.copyOf(problem.shuffledAnswers));
	}

	@Test
	public void shuffleShouldWorkWith8ChoicesAnd1Answer() {
		problem.answers = toArray("a");
		problem.choices = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		shuffler.shuffle(problem, toArray(0), toArray(1, 2, 3, 4, 5, 6, 7, 0));
		assertEquals(Arrays.asList("b", "c", "d", "a"), problem.getShuffledChoiceList());
		assertEquals(ImmutableSet.of("a"), ImmutableSet.copyOf(problem.shuffledAnswers));
	}

	@Test
	public void shuffleShouldWorkWith8ChoicesAnd2Answer() {
		problem.answers = toArray("a", "b");
		problem.choices = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		shuffler.shuffle(problem, toArray(1, 0), toArray(1, 2, 3, 4, 5, 6, 7, 0));
		assertEquals(Arrays.asList("c", "d", "e", "b"), problem.getShuffledChoiceList());
		assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(problem.shuffledAnswers));
	}

	@Test
	public void shuffleShouldWorkWith8ChoicesAnd8AnswerAnd3Display() {
		problem.answers = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		problem.choices = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		problem.numberOfDisplayedChoices = 3;
		shuffler.shuffle(problem, toArray(1, 2, 3, 4, 5, 6, 7, 0), toArray(1, 2, 3, 4, 5, 6, 7, 0));
		assertEquals(Arrays.asList("c", "d", "b"), problem.getShuffledChoiceList());
		assertEquals(ImmutableSet.of("b", "c", "d"), ImmutableSet.copyOf(problem.shuffledAnswers));
	}

}
