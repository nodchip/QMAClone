package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingThemeQuery;

import com.google.common.base.Preconditions;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public class ThemeQueryTreeViewModel implements TreeViewModel {

	private final PanelSettingThemeQuery presenter;
	private ThemeProvider themeProvider;
	private ThemeQueryProvider themeQueryProvider;

	public ThemeQueryTreeViewModel(PanelSettingThemeQuery presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
	}

	@Override
	public <T> NodeInfo<?> getNodeInfo(T value) {
		if (value == null) {
			return new DefaultNodeInfo<PacketTheme>(themeProvider = new ThemeProvider(presenter),
					new ThemeCell(), new SingleSelectionModel<PacketTheme>(),
					themeCellPreviewEventHandler, null);

		} else if (value instanceof PacketTheme) {
			PacketTheme theme = (PacketTheme) value;
			return new DefaultNodeInfo<PacketThemeQuery>(
					themeQueryProvider = new ThemeQueryProvider(presenter, theme.getName()),
					new ThemeQueryCell(), new SingleSelectionModel<PacketThemeQuery>(),
					themeQueryCellPreviewEventHandler, null);

		} else {
			throw new IllegalArgumentException("Unsupported object type: "
					+ value.getClass().getName());
		}
	}

	private final CellPreviewEvent.Handler<PacketTheme> themeCellPreviewEventHandler = new CellPreviewEvent.Handler<PacketTheme>() {
		@Override
		public void onCellPreview(CellPreviewEvent<PacketTheme> event) {
			PacketTheme value = event.getValue();
			presenter.onThemeQuerySelected(value.getName(), "");
		}
	};
	private final CellPreviewEvent.Handler<PacketThemeQuery> themeQueryCellPreviewEventHandler = new CellPreviewEvent.Handler<PacketThemeQuery>() {
		@Override
		public void onCellPreview(CellPreviewEvent<PacketThemeQuery> event) {
			PacketThemeQuery value = event.getValue();
			presenter.onThemeQuerySelected(value.getTheme(), value.getQuery());
		}
	};

	@Override
	public boolean isLeaf(Object value) {
		return value instanceof PacketThemeQuery;
	}

	public void refresh(boolean refreshTheme) {
		themeQueryProvider.refresh();
		if (refreshTheme) {
			themeProvider.refresh();
		}
	}

}
