package tv.dyndns.kishibe.qmaclone.client.report;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class GwtButtonCell extends ButtonCell {
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml data, SafeHtmlBuilder sb) {
		sb.appendHtmlConstant("<button type=\"button\" tabindex=\"-1\" class=\"gwt-Button\">");
		if (data != null) {
			sb.append(data);
		}
		sb.appendHtmlConstant("</button>");
	}
}
