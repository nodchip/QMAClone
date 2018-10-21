package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ThemeQueryCell extends AbstractCell<PacketThemeQuery> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, PacketThemeQuery value,
			SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}

		sb.appendEscaped(value.getQuery());
	}

}
