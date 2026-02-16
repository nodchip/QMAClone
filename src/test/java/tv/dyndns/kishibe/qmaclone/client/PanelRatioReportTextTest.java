package tv.dyndns.kishibe.qmaclone.client;

import org.junit.Test;

import com.google.gwt.user.client.ui.HTML;

public class PanelRatioReportTextTest extends QMACloneGWTTestCaseBase {
	@Test
	public void testLeadTextContainsRegisteredProblemListGuidance() {
		PanelRatioReport panel = new PanelRatioReport();
		HTML lead = (HTML) panel.getWidget(0);
		String html = lead.getHTML();
		assertTrue(html.contains("登録した問題の正答率と状態を確認できます。"));
		assertTrue(html.contains("情報更新"));
		assertTrue(html.contains("一括消去は設定画面"));
	}
}
