package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketThemeModeEditLog implements IsSerializable {

	public enum Type implements IsSerializable {
		Add("追加"), Remove("削除");
		private final String value;

		private Type(String value) {
			this.value = Preconditions.checkNotNull(value);
		}

		@Override
		public String toString() {
			return value;
		}
	}

	private int userCode;
	private String userName;
	private long timeMs;
	private Type type;
	private String theme;
	private String query;

	public int getUserCode() {
		return userCode;
	}

	public void setUserCode(int userCode) {
		this.userCode = userCode;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getTimeMs() {
		return timeMs;
	}

	public void setTimeMs(long timeMs) {
		this.timeMs = timeMs;
	}

	public String getType() {
		return type.name();
	}

	public void setType(String type) {
		this.type = Type.valueOf(type);
	}

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

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PacketThemeModeEditLog)) {
			return false;
		}
		PacketThemeModeEditLog rh = (PacketThemeModeEditLog) obj;
		return userCode == rh.userCode && timeMs == rh.timeMs && type == rh.type
				&& Objects.equal(theme, rh.theme) && Objects.equal(query, rh.query);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(userCode, userName, timeMs, type, theme, query);
	}

}
