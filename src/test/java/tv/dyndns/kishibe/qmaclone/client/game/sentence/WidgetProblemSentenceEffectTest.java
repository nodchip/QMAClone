package tv.dyndns.kishibe.qmaclone.client.game.sentence;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;

public class WidgetProblemSentenceEffectTest extends QMACloneGWTTestCaseBase {
	private WidgetProblemSentenceEffect widget;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();

		PacketProblem problem = TestDataProvider.getProblem();
		problem.choices = new String[] { "abcde" };
		widget = new WidgetProblemSentenceEffect(problem);
	}

	@Test
	public void testWidgetProblemSentenceEffect() {
		assertEquals(1, widget.getWidgetIndex(widget.html));
	}

	@Test
	public void testUpdate() {
		for (int i = 0; i < 200; ++i) {
			widget.update();
			assertTrue(widget.html.getHTML().contains("abcde"));
		}

		assertFalse(widget.html.getHTML().contains("position:absolute;"));
	}
}
