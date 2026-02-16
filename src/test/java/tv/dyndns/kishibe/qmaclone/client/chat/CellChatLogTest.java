package tv.dyndns.kishibe.qmaclone.client.chat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;

/**
 * {@link CellChatLog} の描画テスト。
 * 
 * @author nodchip
 */
public class CellChatLogTest {

	/**
	 * メッセージ描画で必要なCSSクラスとエスケープが適用されることを確認する。
	 */
	@Test
	public void renderShouldUseModernClassesAndEscapeUserInput() {
		CellChatLog cell = new CellChatLog();
		PacketChatMessage message = new PacketChatMessage();
		message.resId = 123;
		message.userCode = 1;
		message.name = "<b>name</b>";
		message.remoteAddress = "127.0.0.1";
		message.date = 0L;
		message.classLevel = 1;
		message.body = "<script>alert(1)</script>";
		message.imageFileName = "noimage.jpg";

		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		cell.render(null, message, sb);
		String html = sb.toSafeHtml().asString();

		assertThat(html, containsString("chatLogEntry"));
		assertThat(html, containsString("chatLogMeta"));
		assertThat(html, containsString("chatLogBody"));
		assertThat(html, containsString("&lt;b&gt;name&lt;/b&gt;"));
		assertThat(html, containsString("&lt;script&gt;alert(1)&lt;/script&gt;"));
		assertThat(html, not(containsString("<script>alert(1)</script>")));
	}
}
