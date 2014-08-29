package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class ThemeRankingViewImpl extends HorizontalPanel implements ThemeRankingPresenter.View {

	static final int LEFT_WIDTH = 250;
	private final PanelRanking panelRanking;

	@Inject
	public ThemeRankingViewImpl(ThemeRankingPresenter presenter,
			DateRangeSelectorPresenter.View dateRangeSelector,
			ThemeSelectorPresenter.View themeSelector, PanelRanking.Factory panelRankingFactory) {
		presenter.setView(this);

		VerticalPanel verticalPanel = new VerticalPanel();
		{
			ScrollPanel scrollPanel = new ScrollPanel(dateRangeSelector.asWidget());
			scrollPanel.setPixelSize(LEFT_WIDTH, 200);
			verticalPanel.add(scrollPanel);
		}
		{
			ScrollPanel scrollPanel = new ScrollPanel(themeSelector.asWidget());
			scrollPanel.setPixelSize(LEFT_WIDTH, 400);
			verticalPanel.add(scrollPanel);
		}
		add(verticalPanel);

		this.panelRanking = panelRankingFactory.create("最高得点");
		add(panelRanking);
	}

	@Override
	public void setRanking(List<PacketRankingData> ranking) {
		panelRanking.setRanking(ranking);
	}

}
