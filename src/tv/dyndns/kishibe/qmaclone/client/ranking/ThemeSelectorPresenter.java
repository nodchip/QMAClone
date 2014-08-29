package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class ThemeSelectorPresenter {

	interface View extends IsWidget {
		void setTheme(List<List<String>> themes);
	}

	private static final Logger logger = Logger.getLogger(ThemeSelectorPresenter.class.toString());
	private final ThemeRankingPresenter themeRankingPresenter;
	private View view;

	@Inject
	public ThemeSelectorPresenter(ServiceAsync service, ThemeRankingPresenter themeRankingPresenter) {
		this.themeRankingPresenter = Preconditions.checkNotNull(themeRankingPresenter);

		service.getThemeModeThemes(callbackGetThemeModeThemes);
	}

	public void setView(View view) {
		this.view = Preconditions.checkNotNull(view);
	}

	@VisibleForTesting
	final AsyncCallback<List<List<String>>> callbackGetThemeModeThemes = new AsyncCallback<List<List<String>>>() {
		@Override
		public void onSuccess(List<List<String>> result) {
			view.setTheme(result);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマモードの取得に失敗しました", caught);
		}
	};

	public void onThemeSelected(String theme) {
		themeRankingPresenter.onThemeSelected(theme);
	}

}
