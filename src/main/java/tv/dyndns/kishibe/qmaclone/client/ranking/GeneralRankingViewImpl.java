package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.ui.TwoColumnSelectionPanel;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GeneralRankingViewImpl extends TwoColumnSelectionPanel implements
		GeneralRankingPresenter.View {

	private static final String[] LABELS = { "最高得点", "平均得点", "プレイ回数", "レーティング", "平均順位", "クラス" };
	private final PanelRanking.Factory panelRankingFactory;

	@Inject
	public GeneralRankingViewImpl(GeneralRankingPresenter generalRankingPresenter,
			PanelRanking.Factory panelRankingFactory) {
		super(200);
		generalRankingPresenter.setView(this);
		this.panelRankingFactory = Preconditions.checkNotNull(panelRankingFactory);
		setWidth("800px");
	}

	@Override
	public void setRanking(List<List<PacketRankingData>> rankings) {
		for (int i = 0; i < rankings.size(); ++i) {
			final int finalIndex = i;
			final List<PacketRankingData> ranking = rankings.get(i);
			add(LABELS[i], new LazyPanel() {
				@Override
				protected Widget createWidget() {
					PanelRanking panelRanking = panelRankingFactory.create(LABELS[finalIndex]);
					panelRanking.setRanking(ranking);
					return panelRanking;
				}
			});
		}
	}

}
