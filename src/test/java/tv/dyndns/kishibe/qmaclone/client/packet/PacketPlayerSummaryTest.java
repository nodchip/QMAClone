package tv.dyndns.kishibe.qmaclone.client.packet;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;

import com.google.gwt.safehtml.shared.SafeHtml;

public class PacketPlayerSummaryTest extends QMACloneGWTTestCaseBase {
	private PacketPlayerSummary player;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		player = new PacketPlayerSummary();
		player.level = "修練10";
		player.name = "プレイヤー";
		player.prefecture = "青森";
		player.rating = 1234;
	}

	public void testAsSafeHtml() {
		final SafeHtml html = player.asSafeHtml();
		assertEquals("修練10 プレイヤー<br>青森 1234", html.asString());
	}

	public void testAsResultSafeHtml() {
		final SafeHtml html = player.asResultSafeHtml();
		assertEquals("修練10 プレイヤー", html.asString());
	}

	public void testAsGameSafeHtml() {
		final SafeHtml html = player.asGameSafeHtml();
		assertEquals("修練10<br>プレイヤー", html.asString());
	}

	public void testGetDefaultPlayerSummary() {
		player = PacketPlayerSummary.getDefaultPlayerSummary();
		assertEquals("(COM)", player.level);
		assertEquals("未初期化です", player.name);
		assertEquals("東京", player.prefecture);
		assertEquals(1300, player.rating);
	}
}
