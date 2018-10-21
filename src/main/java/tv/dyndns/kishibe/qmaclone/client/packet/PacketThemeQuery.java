package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketThemeQuery implements IsSerializable {

	public String theme;
	public String query;

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
