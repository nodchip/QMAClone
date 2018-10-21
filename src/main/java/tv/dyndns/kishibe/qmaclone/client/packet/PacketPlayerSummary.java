package tv.dyndns.kishibe.qmaclone.client.packet;

import name.pehl.piriti.json.client.JsonReader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketPlayerSummary implements IsSerializable {
	public static class Json {
		public interface PacketPlayerSummaryReader extends JsonReader<PacketPlayerSummary> {
		}

		public static final PacketPlayerSummaryReader READER = GWT
				.create(PacketPlayerSummaryReader.class);
	}

	public String level;
	public String name;
	public String prefecture;
	public int rating;
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
			StringBuilder sb = new StringBuilder().append(level).append('\n').append(name);
			gameHtml = new SafeHtmlBuilder().appendEscapedLines(sb.toString()).toSafeHtml();
		}

		return gameHtml;
	}

	private static PacketPlayerSummary newDefaultPlayerSummary() {
		PacketPlayerSummary summary = new PacketPlayerSummary();
		summary.level = "(COM)";
		summary.name = "未初期化です";
		summary.prefecture = "東京";
		summary.rating = 1300;
		return summary;
	}

	public static PacketPlayerSummary getDefaultPlayerSummary() {
		return DEFAULT_PLAYER_SUMMARY;
	}

	@Override
	public String toString() {
		return asSafeHtml().asString();
	}
}
