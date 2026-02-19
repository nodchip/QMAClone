package tv.dyndns.kishibe.qmaclone.client.game.sentence;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.user.client.ui.Grid;

public class WidgetProblemSentenceCubeTest extends QMACloneGWTTestCaseBase {
	@Test
	public void testSentenceWidthShouldBeFullWidth() {
		PacketProblem problem = new PacketProblem();
		problem.sentence = "テスト";
		problem.shuffledAnswers = new String[] { "サンプル" };

		WidgetProblemSentenceCube widget = new WidgetProblemSentenceCube(problem);
		Grid grid = (Grid) widget.getWidget(1);
		assertEquals("100%", grid.getElement().getStyle().getWidth());
	}
}
