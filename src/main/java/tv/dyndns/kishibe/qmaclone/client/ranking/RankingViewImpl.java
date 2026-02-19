package tv.dyndns.kishibe.qmaclone.client.ranking;

import tv.dyndns.kishibe.qmaclone.client.ui.TwoColumnSelectionPanel;

import com.google.inject.Inject;

public class RankingViewImpl extends TwoColumnSelectionPanel implements RankingPresenter.View {

	@Inject
	public RankingViewImpl(RankingPresenter rankingPresenter,
			GeneralRankingPresenter.View generalRankingPresenter,
			ThemeRankingPresenter.View themeRankingPresenter) {
		super(170);
		rankingPresenter.setView(this);
		addStyleName("rankingRootPanel");
		setCenterPanelWidth(594);
		setForceContentWidgetWidth(true);
		add("総合ランキング", generalRankingPresenter.asWidget());
		add("テーマランキング", themeRankingPresenter.asWidget());
	}

}
