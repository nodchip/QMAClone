package tv.dyndns.kishibe.qmaclone.client.report;

import com.google.gwt.user.cellview.client.Header;

public class LinkHeader extends Header<String> {
	private final String text;

	public LinkHeader(String text) {
		super(new LinkCell());
		this.text = text;
	}

	@Override
	public String getValue() {
		return text;
	}
}
