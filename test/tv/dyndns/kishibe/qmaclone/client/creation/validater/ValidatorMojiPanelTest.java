package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class ValidatorMojiPanelTest extends ValidatorTestBase {
	private ValidatorMojiPanel validator;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		validator = new ValidatorMojiPanel();
		problem.type = ProblemType.MojiPanel;
		problem.answers = toArray("0123");
		problem.choices = toArray("0123456789");
	}

	@Test
	public void checkShouldReturnFalseIfNoAnswers() {
		problem.answers = toArray();
		assertEquals(Arrays.asList("解答が入力されていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfTooShort() {
		problem.answers = toArray("01");
		assertEquals(Arrays.asList("1番目の解答が短すぎます(3文字以上必要です)"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfTooLong() {
		problem.answers = toArray("0123456");
		assertEquals(Arrays.asList("1番目の解答が長すぎます(6文字以下でなければなりません)"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfDifferentLength() {
		problem.answers = toArray("012345", "01234");
		assertEquals(Arrays.asList("解答の長さがそろっていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNoChoices() {
		problem.choices = toArray();
		assertEquals(Arrays.asList("選択文字群が入力されていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNotExpectedAnswerLengthFor3() {
		problem.answers = toArray("012");
		problem.choices = toArray("0123");
		assertEquals(Arrays.asList("解答が3文字の場合は選択文字群には6文字又は8文字必要です"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNotExpectedAnswerLengthFor4() {
		problem.answers = toArray("0123");
		problem.choices = toArray("0123");
		assertEquals(Arrays.asList("解答が4～6文字の場合は選択文字群には10文字必要です"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfDuplicated() {
		problem.choices = toArray("0123456780");
		assertEquals(Arrays.asList("選択文字群に同じ文字が含まれています"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnFalseIfNotContained() {
		problem.answers = toArray("abcd");
		assertEquals(Arrays.asList("1番目の解答の文字が選択文字群に含まれていません"), validator.check(problem).warn);
	}

	@Test
	public void checkShouldReturnTrueForBugTrack383() {
		// BugTrack-QMAClone/383 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F383
		problem.answers = toArray("あいうえ");
		problem.choices = toArray("あいうえおかきくけこ");
		assertEquals(Arrays.asList(), validator.check(problem).warn);
	}
}
