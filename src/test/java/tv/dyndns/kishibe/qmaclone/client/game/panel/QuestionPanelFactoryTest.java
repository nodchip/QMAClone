package tv.dyndns.kishibe.qmaclone.client.game.panel;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.WidgetTimeProgressBar;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;

/**
 * Test case for {@link QuestionPanelFactory}.
 * 
 * @author nodchip
 */
public class QuestionPanelFactoryTest extends QMACloneGWTTestCaseBase {

	private WidgetTimeProgressBar bar;
	private SessionData sessionData;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		bar = new WidgetTimeProgressBar();
		sessionData = TestDataProvider.getSessionData();
	}

	@Test
	public void testCreate() {
		for (ProblemType type : ProblemType.valuesWithoutRandom) {
			if (type == ProblemType.Random) {
				continue;
			}

			PacketProblem problem = TestDataProvider.getProblem();
			problem.type = type;
			assertNotNull(QuestionPanelFactory.create(problem, bar, sessionData));
		}
	}
}
