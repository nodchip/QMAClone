package tv.dyndns.kishibe.qmaclone.client.creation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tv.dyndns.kishibe.qmaclone.client.constant.Constant.DELIMITER_GENERAL;
import static tv.dyndns.kishibe.qmaclone.client.constant.Constant.DELIMITER_KUMIAWASE_PAIR;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class WrongAnswerPresenterTest {

	private static final String FAKE_ANSWER = "aaa";
	private static final int FAKE_COUNT = 123;
	@Mock
	private WrongAnswerView mockView;
	private WrongAnswerPresenter presenter;
	private PacketProblem problem;

	@Before
	public void setUp() throws Exception {
		presenter = new WrongAnswerPresenter(mockView);
		problem = TestDataProvider.getProblem();
		problem.type = ProblemType.YonTaku;
	}

	@Test
	public void setWrongAnswersShouldShowString() {
		presenter = spy(presenter);

		List<PacketWrongAnswer> wrongAnswers = ImmutableList.of(new PacketWrongAnswer().setAnswer(
				FAKE_ANSWER).setCount(FAKE_COUNT));

		when(presenter.normalize(wrongAnswers, problem)).thenReturn(wrongAnswers);

		presenter.setWrongAnswers(wrongAnswers, problem);

		verify(mockView).setAnswer(wrongAnswers);
	}

	@Test
	public void asStringShouldWorkWithSingleAnswer() {
		List<PacketWrongAnswer> actual = presenter.normalize(
				ImmutableList.of(new PacketWrongAnswer().setAnswer("aaa").setCount(123)), problem);
		assertEquals(ImmutableList.of(new PacketWrongAnswer().setAnswer("aaa").setCount(123)),
				actual);
	}

	@Test
	public void asStringShouldMergeSimilarPoint() {
		problem.type = ProblemType.Click;
		String p1 = "100 100";
		String p2 = "105 105";
		List<PacketWrongAnswer> actual = presenter.normalize(ImmutableList.of(
				new PacketWrongAnswer().setAnswer(p1).setCount(10), new PacketWrongAnswer()
						.setAnswer(p2).setCount(20)), problem);
		assertEquals(ImmutableList.of(new PacketWrongAnswer().setAnswer(p1).setCount(30)), actual);
	}

	@Test
	public void asStringShouldNormalizeSingleUrl() {
		List<PacketWrongAnswer> actual = presenter.normalize(ImmutableList
				.of(new PacketWrongAnswer().setAnswer("http://example.com/a.jpg").setCount(10)),
				problem);
		assertEquals(ImmutableList.of(new PacketWrongAnswer().setAnswer("a.jpg").setCount(10)),
				actual);
	}

	@Test
	public void asStringShouldNormalizeMultipleUrl() {
		String a1 = "http://example.com/a.jpg";
		String a2 = "http://example.com/b.jpg";
		String a3 = "http://example.com/c.jpg";
		List<PacketWrongAnswer> actual = presenter.normalize(
				ImmutableList.of(new PacketWrongAnswer().setAnswer(
						Joiner.on(DELIMITER_GENERAL).join(a1, a2, a3)).setCount(10)), problem);
		assertEquals(
				ImmutableList.of(new PacketWrongAnswer().setAnswer(
						Joiner.on(DELIMITER_GENERAL).join("a.jpg", "b.jpg", "c.jpg")).setCount(10)),
				actual);
	}

	@Test
	public void asStringShouldNormalizeMultipleUrlPair() {
		String a1 = "http://example.com/a.jpg" + DELIMITER_KUMIAWASE_PAIR
				+ "http://example.com/A.jpg";
		String a2 = "http://example.com/b.jpg" + DELIMITER_KUMIAWASE_PAIR
				+ "http://example.com/B.jpg";
		String a3 = "http://example.com/c.jpg" + DELIMITER_KUMIAWASE_PAIR
				+ "http://example.com/C.jpg";
		List<PacketWrongAnswer> actual = presenter.normalize(
				ImmutableList.of(new PacketWrongAnswer().setAnswer(
						Joiner.on(DELIMITER_GENERAL).join(a1, a2, a3)).setCount(10)), problem);
		assertEquals(
				ImmutableList.of(new PacketWrongAnswer().setAnswer(
						Joiner.on(DELIMITER_GENERAL).join(
								"a.jpg" + DELIMITER_KUMIAWASE_PAIR + "A.jpg",
								"b.jpg" + DELIMITER_KUMIAWASE_PAIR + "B.jpg",
								"c.jpg" + DELIMITER_KUMIAWASE_PAIR + "C.jpg")).setCount(10)),
				actual);
	}

	@Test
	public void asStringShouldSortMultipleAnswers() {
		String a1 = "aaa";
		String a2 = "bbb";
		String a3 = "ccc";
		List<PacketWrongAnswer> actual = presenter.normalize(
				ImmutableList.of(new PacketWrongAnswer().setAnswer(
						Joiner.on(DELIMITER_GENERAL).join(a3, a2, a1)).setCount(10)), problem);
		assertEquals(
				ImmutableList.of(new PacketWrongAnswer().setAnswer(
						Joiner.on(DELIMITER_GENERAL).join(a1, a2, a3)).setCount(10)), actual);
	}

	@Test
	public void asStringShouldSortAnswersByCounterThenLexicograph() {
		String a1 = "aaa";
		String a2 = "bbb";
		String a3 = "ccc";
		List<PacketWrongAnswer> actual = presenter.normalize(ImmutableList.of(
				new PacketWrongAnswer().setAnswer(a2).setCount(10), new PacketWrongAnswer()
						.setAnswer(a3).setCount(20), new PacketWrongAnswer().setAnswer(a1)
						.setCount(10)), problem);
		assertEquals(ImmutableList.of(new PacketWrongAnswer().setAnswer(a3).setCount(20),
				new PacketWrongAnswer().setAnswer(a1).setCount(10), new PacketWrongAnswer()
						.setAnswer(a2).setCount(10)), actual);
	}

	@Test
	public void asStringShouldNotSortTatoStrings() {
		problem.type = ProblemType.Junban;
		String a1 = "ccc" + DELIMITER_GENERAL + "aaa" + DELIMITER_GENERAL + "bbb";
		String a2 = "bbb" + DELIMITER_GENERAL + "aaa" + DELIMITER_GENERAL + "ccc";
		String a3 = "aaa" + DELIMITER_GENERAL + "bbb" + DELIMITER_GENERAL + "ccc";
		List<PacketWrongAnswer> actual = presenter.normalize(ImmutableList.of(
				new PacketWrongAnswer().setAnswer(a1).setCount(20), new PacketWrongAnswer()
						.setAnswer(a2).setCount(10), new PacketWrongAnswer().setAnswer(a3)
						.setCount(10)), problem);
		assertEquals(ImmutableList.of(new PacketWrongAnswer().setAnswer(a1).setCount(20),
				new PacketWrongAnswer().setAnswer(a3).setCount(10), new PacketWrongAnswer()
						.setAnswer(a2).setCount(10)), actual);
	}

}
