package tv.dyndns.kishibe.qmaclone.client.setting;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingImageLink extends VerticalPanel {
	public PanelSettingImageLink() {
		setStyleName("settingImageLinkRoot");

		final CellTableImageLink cellTableImageLink = new CellTableImageLink();
		cellTableImageLink.addStyleName("settingImageLinkTable");
		final SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		final SimplePager simplePager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		simplePager.setStyleName("settingImageLinkPager");
		simplePager.setDisplay(cellTableImageLink);

		HTML introCard = new HTML("<h3 class='settingThemeModeTitle'>画像リンク切れ</h3>"
				+ "<p class='settingThemeModeLead settingImageLinkLead'>"
				+ "リンク切れや応答異常の画像URLを確認し、問題編集画面から修正できます。"
				+ "</p>");
		introCard.setStyleName("settingImageLinkIntroCard");
		add(introCard);

		FlowPanel tableCard = new FlowPanel();
		tableCard.setStyleName("settingImageLinkTableCard");
		tableCard.add(new HTML(
				"<p class='settingImageLinkHint'>問題番号を押すと該当問題の編集画面へ移動します。ステータスコードが4xx/5xxの項目を優先して確認してください。</p>"));
		tableCard.add(simplePager);

		SimplePanel tableScroll = new SimplePanel(cellTableImageLink);
		tableScroll.setStyleName("settingImageLinkTableScroll");
		tableCard.add(tableScroll);
		add(tableCard);
	}
}
