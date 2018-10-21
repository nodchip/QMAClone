package tv.dyndns.kishibe.qmaclone.client.report;

import com.google.gwt.user.cellview.client.Column;

public abstract class GwtButtonColumn<T> extends Column<T, String> {
	public GwtButtonColumn() {
		super(new GwtButtonCell());
	}
}
