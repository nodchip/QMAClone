package tv.dyndns.kishibe.qmaclone.client.report;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;

public abstract class SafeHtmlColumn<T> extends Column<T, SafeHtml> {
	public SafeHtmlColumn() {
		super(new SafeHtmlCell());
	}
}
