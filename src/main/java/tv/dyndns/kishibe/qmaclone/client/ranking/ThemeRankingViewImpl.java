package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class ThemeRankingViewImpl extends VerticalPanel implements ThemeRankingPresenter.View {

	private final PanelRanking panelRanking;

	@Inject
	public ThemeRankingViewImpl(ThemeRankingPresenter presenter,
			DateRangeSelectorPresenter.View dateRangeSelector,
			ThemeSelectorPresenter.View themeSelector, PanelRanking.Factory panelRankingFactory) {
		presenter.setView(this);
		setWidth("100%");
		setSpacing(8);
		addStyleName("rankingThemeRoot");

		HorizontalPanel selectorRow = new HorizontalPanel();
		selectorRow.setWidth("100%");
		selectorRow.setSpacing(8);
		selectorRow.addStyleName("rankingSelectorRow");
		add(selectorRow);

		VerticalPanel dateCard = createSelectorCard("期間");
		ScrollPanel dateScroll = new ScrollPanel(dateRangeSelector.asWidget());
		dateScroll.addStyleName("rankingSelectorScroll");
		dateCard.add(dateScroll);
		selectorRow.add(dateCard);
		selectorRow.setCellWidth(dateCard, "50%");

		VerticalPanel themeCard = createSelectorCard("テーマ");
		ScrollPanel themeScroll = new ScrollPanel(themeSelector.asWidget());
		themeScroll.addStyleName("rankingSelectorScroll");
		themeCard.add(themeScroll);
		selectorRow.add(themeCard);
		selectorRow.setCellWidth(themeCard, "50%");

		this.panelRanking = panelRankingFactory.create("最高得点");
		add(panelRanking);
	}

	@Override
	public void setRanking(List<PacketRankingData> ranking) {
		panelRanking.setRanking(ranking);
	}

	private VerticalPanel createSelectorCard(String title) {
		VerticalPanel card = new VerticalPanel();
		card.setWidth("100%");
		card.addStyleName("rankingSelectorCard");
		card.add(new HTML("<h4 class='rankingSelectorTitle'>" + title + "</h4>"));
		return card;
	}

}
