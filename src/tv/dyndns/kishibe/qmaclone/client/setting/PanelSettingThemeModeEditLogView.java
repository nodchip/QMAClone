package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingThemeModeEditLogView extends VerticalPanel implements
		PanelSettingThemeModeEditLog.View {

	private PanelSettingThemeModeEditLog presenter;
	private CellTableThemeModeEditLog cellTable;

	@Override
	public void setPresenter(PanelSettingThemeModeEditLog presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
	}

	@Override
	public void setNumberOfEntries(int numberOfEntries) {
		cellTable = new CellTableThemeModeEditLog(presenter, numberOfEntries);

		add(createPager());
		add(cellTable);
		add(createPager());
	}

	private SimplePager createPager() {
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(cellTable);
		return pager;
	}

	@Override
	public void setLog(int start, List<PacketThemeModeEditLog> log) {
		cellTable.setRowData(start, log);
	}

}
