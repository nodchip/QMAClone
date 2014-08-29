package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingThemeQuery;

import com.google.common.base.Preconditions;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

public class ThemeProvider extends AsyncDataProvider<PacketTheme> {

	private final PanelSettingThemeQuery presenter;
	private HasData<PacketTheme> lastDisplay;

	public ThemeProvider(PanelSettingThemeQuery presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
	}

	@Override
	protected void onRangeChanged(HasData<PacketTheme> display) {
		lastDisplay = display;
		refresh();
	}

	public void refresh() {
		presenter.onThemeRequested(lastDisplay);
	}

}
