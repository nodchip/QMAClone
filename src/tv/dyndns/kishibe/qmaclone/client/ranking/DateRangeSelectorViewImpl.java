package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketMonth;

import com.google.common.base.Preconditions;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;

public class DateRangeSelectorViewImpl extends Tree implements DateRangeSelectorPresenter.View,
		SelectionHandler<TreeItem> {

	private static final String LABEL_ALL = "全範囲";
	private static final String LABEL_OLD = "旧ランキング";
	private static final String YEAR = "年";
	private static final String MONTH = "月";

	private DateRangeSelectorPresenter presenter;

	@Inject
	public DateRangeSelectorViewImpl(DateRangeSelectorPresenter presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
		presenter.setView(this);

		setAnimationEnabled(true);
		addSelectionHandler(this);
		setPixelSize(ThemeRankingViewImpl.LEFT_WIDTH, 200);
	}

	@Override
	public void setDateRange(List<PacketMonth> months) {
		clear();

		int lastYear = 0;
		TreeItem lastYearTreeItem = null;
		for (PacketMonth month : months) {
			if (month.year != lastYear) {
				lastYear = month.year;
				lastYearTreeItem = addTextItem(month.year + "年");
			}

			lastYearTreeItem.addTextItem(month.year + "年" + month.month + "月");
		}

		addTextItem(LABEL_ALL);
		addTextItem(LABEL_OLD);
	}

	@Override
	public void onSelection(SelectionEvent<TreeItem> event) {
		String label = event.getSelectedItem().getText();
		if (label.equals(LABEL_ALL)) {
			presenter.onAllSelected();

		} else if (label.equals(LABEL_OLD)) {
			presenter.onOldSelected();

		} else if (label.endsWith(YEAR)) {
			int year = Integer.valueOf(label.substring(0, label.length() - 1));
			presenter.onYearSelected(year);

		} else {
			int year = Integer.valueOf(label.substring(0, label.indexOf(YEAR)));
			int month = Integer.valueOf(label.substring(label.indexOf(YEAR) + 1,
					label.indexOf(MONTH)));
			presenter.onMonthSelected(year, month);
		}
	}

}
