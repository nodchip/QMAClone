package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;

@RunWith(JUnit4.class)
public class ValidatorTest extends ValidatorTestBase {
	private Validator validator;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorNull();
		problem.genre = ProblemGenre.Random;
	}

	@Test
	public void testCheckTypingAnswer() {
		assertFalse(validator.checkTypingAnswer(null));
		assertFalse(validator.checkTypingAnswer(""));
		assertTrue(validator.checkTypingAnswer("０１２ＡＢＣ"));
		assertTrue(validator.checkTypingAnswer("あいうえお"));
		assertTrue(validator.checkTypingAnswer("アイウエオ"));
		assertFalse(validator.checkTypingAnswer("０あ"));
		assertFalse(validator.checkTypingAnswer("０ア"));
		assertFalse(validator.checkTypingAnswer("あア"));
		assertFalse(validator.checkTypingAnswer("残念ながら"));
	}

	@Test
	public void testToFull() {
		assertEquals("０１２３ＡＢＣＤ一", validator.toFull("０１23ＡＢCD一"));
	}

	@Test
	public void testCheck() {
		problem.genre = ProblemGenre.Anige;
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}

	@Test
	public void testCheckGenre() {
		problem.genre = ProblemGenre.Random;
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckType() {
		problem.type = ProblemType.Random;
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckSentence() {
		problem.sentence = "";
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckCreator() {
		problem.creator = "";
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckDefaultCreator() {
		problem.creator = "未初期化です";
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckImageAnswer() {
		problem.imageAnswer = true;
		problem.answers = new String[] { "hoge", null, null, null, null, null, null, null };
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckImageChoice() {
		problem.imageChoice = true;
		problem.choices = new String[] { "hoge", null, null, null, null, null, null, null };
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckRandomFlag() {
		problem.randomFlag = RandomFlag.Random5;
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckImageUrl() {
		problem.imageUrl = "hoge";
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckMovieUrl() {
		problem.movieUrl = "hoge";
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckDuplicatedAnswer() {
		problem.answers = new String[] { "a", "a", "b", "c", null, null, null, null };
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testCheckDuplicatedChoice() {
		problem.choices = new String[] { "A", "A", "B", "C", null, null, null, null };
		assertTrue(validator.check(problem).hasWarning());
	}

	@Test
	public void testIsUrl() {
		assertTrue(validator.isUrl("http://www.google.com/"));
		assertFalse(validator.isUrl("hoge"));
	}

	@Test
	public void testConsistsOfTheSameLetters() {
		assertTrue(validator.consistsOfTheSameLetters("a", "a"));
		assertTrue(validator.consistsOfTheSameLetters("abc", "abc"));
		assertTrue(validator.consistsOfTheSameLetters("cba", "acb"));
		assertFalse(validator.consistsOfTheSameLetters("a", "aa"));
		assertFalse(validator.consistsOfTheSameLetters("a", "b"));
	}

	@Test
	public void testRightNowWords() {
		problem.sentence = "現在のモーニング娘。のメンバー数は？";
		assertFalse(validator.check(problem).info.isEmpty());
	}
}
