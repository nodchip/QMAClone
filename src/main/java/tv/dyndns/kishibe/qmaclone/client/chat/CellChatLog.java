package tv.dyndns.kishibe.qmaclone.client.chat;

import java.util.Date;

import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

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
		final String fallbackIconUrl = SafeHtmlUtils.htmlEscape(ICON_BASE_URL + FALLBACK_ICON_FILE);
		final String iconUrl = SafeHtmlUtils.htmlEscape(ICON_BASE_URL + value.imageFileName);
		sb.appendHtmlConstant("<div class=\"chatLogEntry\">");
		sb.appendHtmlConstant("<img style=\"width: 48px; height: 48px;\" class=\"chatLogAvatar gwt-Image\" onerror=\"this.onerror=null;this.src='"
				+ fallbackIconUrl + "'\" src=\"" + iconUrl + "\">");
		sb.appendHtmlConstant("<div class=\"chatLogContent\"><div class=\"chatLogMeta\">");
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
