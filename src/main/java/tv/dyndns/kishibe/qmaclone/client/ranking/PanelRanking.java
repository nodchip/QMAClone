package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

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
		setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		this.cellTableRanking = cellTableRankingFactory.create(label);

		ScrollPanel scrollPanel = new ScrollPanel(cellTableRanking);
		scrollPanel.setPixelSize(600, 600);
		add(scrollPanel);
	}

	public void setRanking(List<PacketRankingData> ranking) {
		cellTableRanking.setRanking(ranking);
	}

}
