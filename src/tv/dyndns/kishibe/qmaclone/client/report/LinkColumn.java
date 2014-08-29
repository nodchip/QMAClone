package tv.dyndns.kishibe.qmaclone.client.report;

import com.google.gwt.user.cellview.client.Column;

public abstract class LinkColumn<T> extends Column<T, String> {
	public LinkColumn() {
		super(new LinkCell());
	}
}
