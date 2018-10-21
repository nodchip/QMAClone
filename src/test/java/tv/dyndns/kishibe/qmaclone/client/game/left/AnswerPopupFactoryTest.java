package tv.dyndns.kishibe.qmaclone.client.game.left;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * Test for {@link AnswerPopupFactory}.
 * 
 * @author nodchip
 */
public class AnswerPopupFactoryTest extends QMACloneGWTTestCaseBase {
	private AbsolutePanel absolutePanel;
	private AnswerPopupFactory factory;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		absolutePanel = new AbsolutePanel();
		factory = new AnswerPopupFactory(absolutePanel);
	}

	@Test
	public void testGet() {
		for (ProblemType type : ProblemType.valuesWithoutRandom) {
			if (type == ProblemType.Random) {
				continue;
			}

			PacketProblem problem = new PacketProblem();
			problem.type = type;
			factory.get(problem);
		}
	}
}
