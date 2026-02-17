package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingThemeModeEditLogView extends VerticalPanel implements
		PanelSettingThemeModeEditLog.View {

	private PanelSettingThemeModeEditLog presenter;
	private CellTableThemeModeEditLog cellTable;
	private final FlowPanel tableCardBody = new FlowPanel();

	public PanelSettingThemeModeEditLogView() {
		setStyleName("settingThemeModeEditLogRoot");

		HTML introCard = new HTML("<h3 class='settingThemeModeTitle'>テーマモード編集ログ</h3>"
				+ "<p class='settingThemeModeLead settingThemeModeEditLogLead'>"
				+ "テーマモード編集の履歴を時系列で確認できます。最新100件を表示し、ページ移動で過去ログへ遡れます。"
				+ "</p>");
		introCard.setStyleName("settingThemeModeEditLogIntroCard");
		add(introCard);

		FlowPanel tableCard = new FlowPanel();
		tableCard.setStyleName("settingThemeModeEditLogTableCard");
		tableCard.add(new HTML(
				"<p class='settingThemeModeEditLogHint'>新しい履歴が下側に表示されます。<br>テーマ名とクエリを確認して更新内容を追跡してください。</p>"));
		tableCardBody.setStyleName("settingThemeModeEditLogTableBody");
		tableCard.add(tableCardBody);
		add(tableCard);
	}

	@Override
	public void setPresenter(PanelSettingThemeModeEditLog presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
	}

	@Override
	public void setNumberOfEntries(int numberOfEntries) {
		cellTable = new CellTableThemeModeEditLog(presenter, numberOfEntries);
		cellTable.setWidth("100%");
		cellTable.addStyleName("settingThemeModeEditLogTable");

		tableCardBody.clear();
		tableCardBody.add(createPager());

		SimplePanel tableScroll = new SimplePanel(cellTable);
		tableScroll.setStyleName("settingThemeModeEditLogTableScroll");
		tableCardBody.add(tableScroll);
		tableCardBody.add(createPager());
	}

	private SimplePager createPager() {
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.addStyleName("settingThemeModeEditLogPager");
		pager.setDisplay(cellTable);
		return pager;
	}

	@Override
	public void setLog(int start, List<PacketThemeModeEditLog> log) {
		cellTable.setRowData(start, log);
	}

}
