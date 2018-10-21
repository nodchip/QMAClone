package tv.dyndns.kishibe.qmaclone.client.game.sentence;

import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.collect.Lists;

public class WidgetProblemSentenceRensouTest extends QMACloneGWTTestCaseBase {
	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
	}

	private static List<String> toStringList(StringBuilder[] sbs) {
		List<String> list = Lists.newArrayList();
		for (StringBuilder sb : sbs) {
			list.add(sb.toString());
		}
		return list;
	}

	@Test
	public void testUpdateShouldProcessNormalSentence() {
		PacketProblem problem = new PacketProblem();
		problem.sentence = "a \nb\nc\nd\n";

		WidgetProblemSentenceRensou ui = new WidgetProblemSentenceRensou(problem);

		assertEquals(asList("『ヒント1』", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "『ヒント4』"), toStringList(ui.lines));
		assertFalse(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "d"), toStringList(ui.lines));
	}

	@Test
	public void testUpdateShouldProcessWithoutLastReturn() {
		PacketProblem problem = new PacketProblem();
		problem.sentence = "a \nb\nc\nd\n";

		WidgetProblemSentenceRensou ui = new WidgetProblemSentenceRensou(problem);

		assertEquals(asList("『ヒント1』", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "『ヒント4』"), toStringList(ui.lines));
		assertFalse(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "d"), toStringList(ui.lines));
	}

	@Test
	public void testUpdateShouldProcessClear() {
		PacketProblem problem = new PacketProblem();
		problem.sentence = "a \nb\nc%c\nd\n";

		WidgetProblemSentenceRensou ui = new WidgetProblemSentenceRensou(problem);

		assertEquals(asList("『ヒント1』", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("『ヒント1』", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertFalse(ui.updateLetter());
		assertEquals(asList("d", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
	}

	@Test
	public void testUpdateShouldNotClearIfNotPercentC() {
		PacketProblem problem = new PacketProblem();
		problem.sentence = "a \nb\nc%z\nd\n";

		WidgetProblemSentenceRensou ui = new WidgetProblemSentenceRensou(problem);

		assertEquals(asList("『ヒント1』", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c%", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c%z", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c%z", "『ヒント4』"), toStringList(ui.lines));
		assertFalse(ui.updateLetter());
		assertEquals(asList("a", "b", "c%z", "d"), toStringList(ui.lines));
	}

	@Test
	public void testUpdateShouldProcessMoreThan4Lines() {
		PacketProblem problem = new PacketProblem();
		problem.sentence = "a \nb\nc\nd\ne\nf\n";

		WidgetProblemSentenceRensou ui = new WidgetProblemSentenceRensou(problem);

		assertEquals(asList("『ヒント1』", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "『ヒント2』", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "『ヒント3』", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "『ヒント4』"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "d"), toStringList(ui.lines));
		assertTrue(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "d"), toStringList(ui.lines));
		assertFalse(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "d"), toStringList(ui.lines));
	}

	@Test
	public void testUpdateShouldProcessAllShow() {
		PacketProblem problem = new PacketProblem();
		problem.sentence = "!a \nb\nc\nd\n";

		WidgetProblemSentenceRensou ui = new WidgetProblemSentenceRensou(problem);

		assertEquals(asList("a", "b", "c", "d"), toStringList(ui.lines));
		assertFalse(ui.updateLetter());
		assertEquals(asList("a", "b", "c", "d"), toStringList(ui.lines));
	}

	@Test
	public void testUpdateShouldShowHtml() {
		PacketProblem problem = new PacketProblem();
		problem.sentence = "a\nb\nc\nd\n";

		WidgetProblemSentenceRensou ui = new WidgetProblemSentenceRensou(problem);
		ui.update(0, 10000);
		assertEquals("a<br>b<br>c<br>d", ui.htmlSentence.getHTML().replaceAll("</br>", ""));
	}
}
