package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMonth;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class DateRangeSelectorPresenter {

	interface View extends IsWidget {
		void setDateRange(List<PacketMonth> months);
	}

	private static final Logger logger = Logger.getLogger(DateRangeSelectorPresenter.class
			.toString());
	private final ThemeRankingPresenter themeRankingPresenter;
	private View view;

	@Inject
	public DateRangeSelectorPresenter(ServiceAsync service,
			ThemeRankingPresenter themeRankingPresenter) {
		this.themeRankingPresenter = Preconditions.checkNotNull(themeRankingPresenter);

		service.getThemeRankingDateRanges(callbackGetThemeRankingDateRanges);
	}

	@VisibleForTesting
	final AsyncCallback<List<PacketMonth>> callbackGetThemeRankingDateRanges = new AsyncCallback<List<PacketMonth>>() {
		@Override
		public void onSuccess(List<PacketMonth> result) {
			view.setDateRange(result);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマモードランキングの日付範囲の取得に失敗しました", caught);
		}
	};

	public void setView(View view) {
		this.view = Preconditions.checkNotNull(view);
	}

	public void onAllSelected() {
		themeRankingPresenter.onAllSelected();
	}

	public void onOldSelected() {
		themeRankingPresenter.onOldSelected();
	}

	public void onYearSelected(int year) {
		themeRankingPresenter.onYearSelected(year);
	}

	public void onMonthSelected(int year, int month) {
		themeRankingPresenter.onMonthSelected(year, month);
	}

}
