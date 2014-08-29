package tv.dyndns.kishibe.qmaclone.client.lobby;

import java.util.Collections;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.common.collect.Lists;

public class LobbyUiTest extends QMACloneGWTTestCaseBase {
	private LobbyUi ui;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		UserData.get().setGenres(Collections.<ProblemGenre> emptySet());
		UserData.get().setTypes(Collections.<ProblemType> emptySet());
		ui = new LobbyUi(null);
	}

	public void testGetPlayerNameShouldReturnNormalPlayer() {
		ui.listBoxLevelName.setSelectedIndex(1);
		ui.listBoxLevelNumber.setSelectedIndex(2);
		ui.textBoxPlayerName.setText("プレイヤー");
		ui.listBoxPrefecture.setSelectedIndex(3);
		UserData.get().setRating(1234);

		final PacketPlayerSummary player = ui.getPlayerSummary();
		assertEquals("見習3", player.level);
		assertEquals("プレイヤー", player.name);
		assertEquals("岩手", player.prefecture);
		assertEquals(1234, player.rating);
	}

	public void testUpdateSpecialLevelName() {
		ui.textBoxPlayerName.setText("プレイヤー");
		ui.listBoxPrefecture.setSelectedIndex(3);
		UserData.get().setRating(1234);

		PacketRankingData data = new PacketRankingData();
		final List<PacketRankingData> ranking = Lists.newArrayList();
		for (int i = 0; i < 100; ++i) {
			ranking.add(data);
		}
		final List<List<PacketRankingData>> rankingData = Lists.newArrayList();
		for (int i = 0; i < 7; ++i) {
			rankingData.add(ranking);
		}

		data = new PacketRankingData();
		data.userCode = 123456789;
		UserData.get().setUserCode(123456789);
		rankingData.get(3).set(40, data);

		ui.updateSpecialLevelName(rankingData);

		assertEquals(LobbyUi.LEVEL_NAMES.size() + 1, ui.listBoxLevelName.getItemCount());
		assertEquals("賢将", ui.listBoxLevelName.getItemText(LobbyUi.LEVEL_NAMES.size()));
		assertEquals(LobbyUi.LEVEL_NAMES.size(), ui.listBoxLevelName.getSelectedIndex());
		assertFalse(ui.listBoxLevelNumber.isEnabled());
		assertTrue(ui.specialLevelName);

		PacketPlayerSummary player = ui.getPlayerSummary();
		assertEquals("賢将", player.level);
		assertEquals("プレイヤー", player.name);
		assertEquals("岩手", player.prefecture);
		assertEquals(1234, player.rating);

		// 別の階級を選んだ際に級数が選択できるようになるか？
		ui.listBoxLevelName.setSelectedIndex(0);
		ui.levelNameChangeHandler.onChange(null);
		assertTrue(ui.listBoxLevelNumber.isEnabled());

		player = ui.getPlayerSummary();
		assertEquals("修練1", player.level);
		assertEquals("プレイヤー", player.name);
		assertEquals("岩手", player.prefecture);
		assertEquals(1234, player.rating);
	}
}
