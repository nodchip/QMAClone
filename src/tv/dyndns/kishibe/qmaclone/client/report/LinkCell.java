package tv.dyndns.kishibe.qmaclone.client.report;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class LinkCell extends ButtonCell {
	public interface LinkCellTemplates extends SafeHtmlTemplates {
		@Template("<a class='linkHeader'>{0}</a>")
		SafeHtml link(SafeHtml data);
	}

	private static final LinkCellTemplates TEMPLATES = GWT.create(LinkCellTemplates.class);

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml data, SafeHtmlBuilder sb) {
		sb.append(TEMPLATES.link(data));
	}
}
