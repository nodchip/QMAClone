package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerYontaku;

public class ShufflerYontakuTest extends ShufflerTestBase {
	private ShufflerYontaku shuffler;

	@BeforeEach
	public void setUp() {
		super.setUp();
		shuffler = new ShufflerYontaku();
	}

	@Test
	public void shuffleShouldWorkWithFourAnswers() {
		problem.answers = toArray("a");
		problem.choices = toArray("a", "b", "c", "d");
		shuffler.shuffle(problem, toArray(0), toArray(1, 2, 3, 0));
		assertEquals(Arrays.asList("b", "c", "d", "a"), problem.getShuffledChoiceList());
		assertEquals(Arrays.asList("a"), problem.getShuffledAnswerList());
	}

	@Test
	public void shuffleShouldWorkWithEightAnswers() {
		problem.answers = toArray("a");
		problem.choices = toArray("a", "b", "c", "d", "e", "f", "g", "h");
		shuffler.shuffle(problem, toArray(0), toArray(1, 2, 3, 4, 5, 6, 7, 0));
		assertEquals(Arrays.asList("b", "c", "d", "a"), problem.getShuffledChoiceList());
		assertEquals(Arrays.asList("a"), problem.getShuffledAnswerList());
	}
}
