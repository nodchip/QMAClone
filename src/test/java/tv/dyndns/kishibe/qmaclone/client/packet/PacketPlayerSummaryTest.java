package tv.dyndns.kishibe.qmaclone.client.packet;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
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
		assertEquals(
				"<span class='gamePlayerNameLevel'>修練10</span><br><span class='gamePlayerNameMain'>プレイヤー</span>",
				html.asString());
	}

  public void testGetDefaultPlayerSummary() {
    player = PacketPlayerSummary.getDefaultPlayerSummary();
    assertEquals("(COM)", player.level);
    assertEquals("未初期化です", player.name);
    assertEquals("東京", player.prefecture);
    assertEquals(Constant.ICON_NO_IMAGE, player.imageFileName);
    assertEquals(1300, player.rating);
  }

  public void testFromJsonObjectShouldReadImageFileName() {
    JSONObject object = new JSONObject();
    object.put("level", new JSONString("中級"));
    object.put("name", new JSONString("テスト"));
    object.put("prefecture", new JSONString("東京"));
    object.put("imageFileName", new JSONString("avatar.png"));

    PacketPlayerSummary summary = PacketPlayerSummary.fromJsonObject(object);

    assertEquals("avatar.png", summary.imageFileName);
  }
}
