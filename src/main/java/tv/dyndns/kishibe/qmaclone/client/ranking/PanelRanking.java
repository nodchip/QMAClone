package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class PanelRanking extends VerticalPanel {

	interface Factory {
		PanelRanking create(String label);
	}

	private final CellTableRanking cellTableRanking;

	@Inject
	public PanelRanking(CellTableRanking.Factory cellTableRankingFactory, @Assisted String label) {
		setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		setWidth("100%");
		addStyleName("rankingSectionCard");
		this.cellTableRanking = cellTableRankingFactory.create(label);
		cellTableRanking.setWidth("100%");

		HTML title = new HTML("<h3 class='rankingSectionTitle'>" + label + "</h3>");
		add(title);
		ScrollPanel scrollPanel = new ScrollPanel(cellTableRanking);
		scrollPanel.setWidth("100%");
		scrollPanel.setHeight("540px");
		scrollPanel.setStyleName("rankingSectionScroll");
		add(scrollPanel);
	}

	public void setRanking(List<PacketRankingData> ranking) {
		cellTableRanking.setRanking(ranking);
	}

}
