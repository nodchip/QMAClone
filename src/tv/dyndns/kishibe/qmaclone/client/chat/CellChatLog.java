package tv.dyndns.kishibe.qmaclone.client.chat;

import java.util.Date;

import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class CellChatLog extends AbstractCell<PacketChatMessage> {
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, PacketChatMessage value,
			SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}
		final String trip = Utility.makeTrip(value.userCode, value.remoteAddress);
		final String date = Utility.toDateFormat(new Date(value.date));
		sb.appendHtmlConstant("<table cellpadding=\"0\" cellspacing=\"0\"><tbody><tr><td style=\"vertical-align: top;\" align=\"left\"><img style=\"width: 48px; height: 48px;\" class=\"gwt-Image\" src=\"http://kishibe.dyndns.tv/qmaclone_icon/"
				+ value.imageFileName
				+ "\"></td><td style=\"vertical-align: top;\" align=\"left\"><table style=\"width: 100%;\" cellpadding=\"0\" cellspacing=\"0\"><tbody><tr><td style=\"vertical-align: top;\" align=\"left\"><div style=\"width: 680px;\" class=\"gwt-HTML\">");
		sb.append(value.resId).append(' ').appendEscaped(value.name);
		sb.appendEscaped(trip).append(' ').appendEscaped(date);
		sb.append(' ').append('(').append(value.classLevel).append(')');
		sb.appendHtmlConstant("</div></td></tr><tr><td style=\"vertical-align: top;\" align=\"left\"><div style=\"width: 680px;\" class=\"gwt-HTML\">");
		sb.appendEscaped(value.body);
		sb.appendHtmlConstant("</div></td></tr></tbody></table></td></tr></tbody></table>");
	}
}
