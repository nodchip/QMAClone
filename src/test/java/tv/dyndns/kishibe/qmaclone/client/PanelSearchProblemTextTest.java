package tv.dyndns.kishibe.qmaclone.client;

import org.junit.Test;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;

public class PanelSearchProblemTextTest extends QMACloneGWTTestCaseBase {
	@Test
	public void testLeadTextContainsSearchGuidance() {
		PanelSearchProblem panel = new PanelSearchProblem();
		HTML lead = (HTML) panel.getWidget(0);
		String html = lead.getHTML();
		assertTrue(html.contains("問題文や作問者名で、登録済みの問題を検索できます。"));
		assertTrue(html.contains("検索する"));
	}

	@Test
	public void testSearchButtonLabelIsUserFriendly() {
		PanelSearchProblem panel = new PanelSearchProblem();
		Button searchButton = (Button) panel.getWidget(panel.getWidgetCount() - 2);
		assertEquals("検索する", searchButton.getText());
	}
}
