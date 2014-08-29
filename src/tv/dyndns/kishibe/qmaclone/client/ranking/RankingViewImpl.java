package tv.dyndns.kishibe.qmaclone.client.ranking;

import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.inject.Inject;

public class RankingViewImpl extends DecoratedTabPanel implements RankingPresenter.View {

	@Inject
	public RankingViewImpl(RankingPresenter rankingPresenter,
			GeneralRankingPresenter.View generalRankingPresenter,
			ThemeRankingPresenter.View themeRankingPresenter) {
		rankingPresenter.setView(this);

		setAnimationEnabled(true);
		add(generalRankingPresenter, "総合ランキング");
		add(themeRankingPresenter, "テーマランキング");
		selectTab(0);
	}

}
