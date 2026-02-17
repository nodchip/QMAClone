package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.common.base.Preconditions;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class GeneralRankingViewImpl extends VerticalPanel implements GeneralRankingPresenter.View,
		ChangeHandler {

	private static final String[] LABELS = { "最高得点", "平均得点", "プレイ回数", "レーティング", "平均順位", "クラス" };
	private final PanelRanking.Factory panelRankingFactory;
	private final ListBox metricSelector = new ListBox();
	private final SimplePanel contentPanel = new SimplePanel();
	private List<List<PacketRankingData>> rankings;

	@Inject
	public GeneralRankingViewImpl(GeneralRankingPresenter generalRankingPresenter,
			PanelRanking.Factory panelRankingFactory) {
		generalRankingPresenter.setView(this);
		this.panelRankingFactory = Preconditions.checkNotNull(panelRankingFactory);
		setWidth("100%");
		setSpacing(8);
		addStyleName("rankingGeneralRoot");
		metricSelector.setVisibleItemCount(1);
		metricSelector.addStyleName("rankingMetricSelector");
		for (String label : LABELS) {
			metricSelector.addItem(label);
		}
		metricSelector.addChangeHandler(this);
		add(metricSelector);
		contentPanel.setWidth("100%");
		add(contentPanel);
	}

	@Override
	public void setRanking(List<List<PacketRankingData>> rankings) {
		this.rankings = rankings;
		if (rankings == null || rankings.isEmpty()) {
			contentPanel.clear();
			return;
		}
		int index = metricSelector.getSelectedIndex();
		if (index < 0 || index >= rankings.size()) {
			index = 0;
			metricSelector.setSelectedIndex(0);
		}
		showRanking(index);
	}

	@Override
	public void onChange(ChangeEvent event) {
		if (rankings == null || rankings.isEmpty()) {
			return;
		}
		showRanking(metricSelector.getSelectedIndex());
	}

	private void showRanking(int index) {
		if (index < 0 || index >= rankings.size()) {
			return;
		}
		PanelRanking panelRanking = panelRankingFactory.create(LABELS[index]);
		panelRanking.setRanking(rankings.get(index));
		contentPanel.setWidget(panelRanking);
	}

}
