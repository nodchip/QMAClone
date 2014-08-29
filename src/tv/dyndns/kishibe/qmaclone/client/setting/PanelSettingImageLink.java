package tv.dyndns.kishibe.qmaclone.client.setting;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingImageLink extends VerticalPanel {
	public PanelSettingImageLink() {
		setHorizontalAlignment(ALIGN_CENTER);

		final CellTableImageLink cellTableImageLink = new CellTableImageLink();
		final SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		final SimplePager simplePager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		simplePager.setDisplay(cellTableImageLink);

		add(simplePager);
		add(cellTableImageLink);
	}
}
