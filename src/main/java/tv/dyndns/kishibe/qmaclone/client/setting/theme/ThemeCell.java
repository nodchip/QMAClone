package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ThemeCell extends AbstractCell<PacketTheme> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, PacketTheme value,
			SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}

		sb.appendEscaped(value.getName() + " (" + value.getNumberOfProblems() + ")");
	}

}
