package tv.dyndns.kishibe.qmaclone.client;

import org.junit.Test;

import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

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

	@Test
	public void testPaginationButtonsExist() {
		PanelSearchProblem panel = new PanelSearchProblem();
		assertNotNull(findButton(panel, "前へ"));
		assertNotNull(findButton(panel, "次へ"));
	}

	private Button findButton(Widget root, String text) {
		if (root instanceof Button) {
			Button button = (Button) root;
			if (text.equals(button.getText())) {
				return button;
			}
		}
		if (root instanceof ComplexPanel) {
			ComplexPanel panel = (ComplexPanel) root;
			for (int i = 0; i < panel.getWidgetCount(); i++) {
				Button found = findButton(panel.getWidget(i), text);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}
