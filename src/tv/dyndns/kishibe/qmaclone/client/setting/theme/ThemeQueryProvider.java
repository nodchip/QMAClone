package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingThemeQuery;

import com.google.common.base.Preconditions;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

public class ThemeQueryProvider extends AsyncDataProvider<PacketThemeQuery> {

	private final PanelSettingThemeQuery presenter;
	private final String theme;
	private HasData<PacketThemeQuery> lastDisplay;

	public ThemeQueryProvider(PanelSettingThemeQuery presenter, String theme) {
		this.presenter = Preconditions.checkNotNull(presenter);
		this.theme = Preconditions.checkNotNull(theme);
	}

	@Override
	protected void onRangeChanged(HasData<PacketThemeQuery> display) {
		lastDisplay = display;
		refresh();
	}

	public void refresh() {
		presenter.onThemeQueryRequested(theme, lastDisplay);
	}

}
