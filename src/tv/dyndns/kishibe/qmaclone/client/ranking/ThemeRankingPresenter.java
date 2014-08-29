package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class ThemeRankingPresenter {

	interface View extends IsWidget {
		void setRanking(List<PacketRankingData> ranking);
	}

	private enum DateRange {
		Old, All, Year, YearMonth
	}

	private static final Logger logger = Logger.getLogger(ThemeRankingPresenter.class.toString());
	private final ServiceAsync service;
	private View view;
	private int year;
	private int month;
	private String theme;
	private DateRange dateRange;

	@Inject
	public ThemeRankingPresenter(ServiceAsync service) {
		this.service = Preconditions.checkNotNull(service);
	}

	public void setView(View view) {
		this.view = Preconditions.checkNotNull(view);
	}

	public void onOldSelected() {
		dateRange = DateRange.Old;
		update();
	}

	public void onAllSelected() {
		dateRange = DateRange.All;
		update();
	}

	public void onYearSelected(int year) {
		dateRange = DateRange.Year;
		this.year = year;
		update();
	}

	public void onMonthSelected(int year, int month) {
		dateRange = DateRange.YearMonth;
		this.year = year;
		this.month = month;
		update();
	}

	public void onThemeSelected(String theme) {
		this.theme = theme;
		update();
	}

	private void update() {
		if (theme == null || dateRange == null) {
			return;
		}
		switch (dateRange) {
		case Old:
			service.getThemeRankingOld(theme, callbackGetThemeRanking);
			break;
		case All:
			service.getThemeRankingAll(theme, callbackGetThemeRanking);
			break;
		case Year:
			service.getThemeRanking(theme, year, callbackGetThemeRanking);
			break;
		case YearMonth:
			service.getThemeRanking(theme, year, month, callbackGetThemeRanking);
			break;
		default:
			break;
		}
	}

	@VisibleForTesting
	final AsyncCallback<List<PacketRankingData>> callbackGetThemeRanking = new AsyncCallback<List<PacketRankingData>>() {
		@Override
		public void onSuccess(List<PacketRankingData> result) {
			view.setRanking(result);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマランキングの取得に失敗しました", caught);
		}
	};

}
