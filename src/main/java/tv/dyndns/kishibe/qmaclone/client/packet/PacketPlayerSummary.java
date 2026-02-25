package tv.dyndns.kishibe.qmaclone.client.packet;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

import com.google.common.base.Objects;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketPlayerSummary implements IsSerializable {
	public String level;
	public String name;
	public String prefecture;
	public String imageFileName;
	public int rating;
	public transient int userCode;
	public String recentMode;
	public String recentState;
	private transient SafeHtml html;
	private transient SafeHtml resultHtml;
	private transient SafeHtml gameHtml;
	private static PacketPlayerSummary DEFAULT_PLAYER_SUMMARY = newDefaultPlayerSummary();

	public SafeHtml asSafeHtml() {
		if (html == null) {
			StringBuilder sb = new StringBuilder().append(level).append(' ').append(name)
					.append('\n').append(prefecture).append(' ').append(rating);
			return html = new SafeHtmlBuilder().appendEscapedLines(sb.toString()).toSafeHtml();
		}

		return html;
	}

	public SafeHtml asResultSafeHtml() {
		if (resultHtml == null) {
			StringBuilder sb = new StringBuilder().append(level).append(' ').append(name);
			resultHtml = SafeHtmlUtils.fromString(sb.toString());
		}

		return resultHtml;
	}

	public SafeHtml asGameSafeHtml() {
		if (gameHtml == null) {
			gameHtml = new SafeHtmlBuilder()
					.appendHtmlConstant("<span class='gamePlayerNameLevel'>")
					.appendEscaped(level)
					.appendHtmlConstant("</span><br><span class='gamePlayerNameMain'>")
					.appendEscaped(name)
					.appendHtmlConstant("</span>").toSafeHtml();
		}

		return gameHtml;
	}

	private static PacketPlayerSummary newDefaultPlayerSummary() {
		PacketPlayerSummary summary = new PacketPlayerSummary();
		summary.level = "(COM)";
		summary.name = "未初期化です";
		summary.prefecture = "東京";
		summary.imageFileName = Constant.ICON_NO_IMAGE;
		summary.rating = 1300;
		return summary;
	}

	public static PacketPlayerSummary getDefaultPlayerSummary() {
		return DEFAULT_PLAYER_SUMMARY;
	}

	/**
	 * インスタンスを複製する。
	 */
	public PacketPlayerSummary copy() {
		PacketPlayerSummary copy = new PacketPlayerSummary();
		copy.level = level;
		copy.name = name;
		copy.prefecture = prefecture;
		copy.imageFileName = imageFileName;
		copy.rating = rating;
		copy.userCode = userCode;
		copy.recentMode = recentMode;
		copy.recentState = recentState;
		return copy;
	}

	public static PacketPlayerSummary fromJsonObject(JSONObject object) {
		PacketPlayerSummary summary = new PacketPlayerSummary();
		summary.level = PacketJsonParser.getString(object, "level");
		summary.name = PacketJsonParser.getString(object, "name");
		summary.prefecture = PacketJsonParser.getString(object, "prefecture");
		summary.imageFileName = PacketJsonParser.getString(object, "imageFileName");
		summary.rating = PacketJsonParser.getInt(object, "rating");
		summary.recentMode = PacketJsonParser.getString(object, "recentMode");
		summary.recentState = PacketJsonParser.getString(object, "recentState");
		return summary;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PacketPlayerSummary)) {
			return false;
		}
		PacketPlayerSummary rh = (PacketPlayerSummary) obj;
		return Objects.equal(level, rh.level)
				&& Objects.equal(name, rh.name)
				&& Objects.equal(prefecture, rh.prefecture)
				&& Objects.equal(imageFileName, rh.imageFileName)
				&& rating == rh.rating
				&& userCode == rh.userCode
				&& Objects.equal(recentMode, rh.recentMode)
				&& Objects.equal(recentState, rh.recentState);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(level, name, prefecture, imageFileName, rating, userCode, recentMode,
				recentState);
	}

	@Override
	public String toString() {
		return asSafeHtml().asString();
	}
}
