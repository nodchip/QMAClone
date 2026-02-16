package tv.dyndns.kishibe.qmaclone.client.report;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class ProblemReportUiCompatibilityTest extends QMACloneGWTTestCaseBase {
	@Test
	public void testLegacyConstructorStillWorksForSearchScreen() {
		PacketProblem problem = new PacketProblem();
		ProblemReportUi ui = new ProblemReportUi(Collections.singletonList(problem), false, true, 20);
		assertNotNull(ui);
	}
}
