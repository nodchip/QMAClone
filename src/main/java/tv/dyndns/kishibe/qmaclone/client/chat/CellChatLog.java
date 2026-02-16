package tv.dyndns.kishibe.qmaclone.client.chat;

import java.util.Date;

import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class CellChatLog extends AbstractCell<PacketChatMessage> {
	private static final String ICON_BASE_URL = "http://kishibe.dyndns.tv/qmaclone/icon/";
	private static final String FALLBACK_ICON_FILE = "noimage.jpg";

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, PacketChatMessage value,
			SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}
		final String trip = Utility.makeTrip(value.userCode, value.remoteAddress);
		final String date = Utility.toDateFormat(new Date(value.date));
		sb.appendHtmlConstant("<div class=\"chatLogEntry\"><img style=\"width: 48px; height: 48px;\" class=\"chatLogAvatar gwt-Image\" onerror=\"this.onerror=null;this.src='");
		sb.appendEscaped(ICON_BASE_URL);
		sb.appendEscaped(FALLBACK_ICON_FILE);
		sb.appendHtmlConstant("'\" src=\"");
		sb.appendEscaped(ICON_BASE_URL);
		sb.appendEscaped(value.imageFileName);
		sb.appendHtmlConstant("\"><div class=\"chatLogContent\"><div class=\"chatLogMeta\">");
		sb.append(value.resId).append(' ').appendEscaped(value.name);
		sb.appendEscaped(trip).append(' ');
		sb.appendHtmlConstant("<span class=\"chatLogDate\">");
		sb.appendEscaped(date);
		sb.appendHtmlConstant("</span>");
		sb.append(' ').append('(').append(value.classLevel).append(')');
		sb.appendHtmlConstant("</div><div class=\"chatLogBody\">");
		sb.appendEscaped(value.body);
		sb.appendHtmlConstant("</div></div></div>");
	}
}
