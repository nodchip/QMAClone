package tv.dyndns.kishibe.qmaclone.client.lobby;

import java.util.Collections;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;

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

  public void testUpdateThemeModeShouldSelectLastTheme() {
    List<List<String>> themeLists = ImmutableList.<List<String>> of(
        ImmutableList.of("テーマ1", "テーマ2", "テーマ3"), ImmutableList.<String> of(),
        ImmutableList.<String> of(), ImmutableList.<String> of(), ImmutableList.<String> of(),
        ImmutableList.<String> of());

    UserData.get().setTheme("テーマ2");
    ui.updateThemeMode(themeLists);

    assertEquals("テーマ2", ui.getThemeModeTheme());
  }

  public void testSaveUserDataShouldSaveTheme() {
    List<List<String>> themeLists = ImmutableList.<List<String>> of(
        ImmutableList.of("テーマ1", "テーマ2", "テーマ3"), ImmutableList.<String> of(),
        ImmutableList.<String> of(), ImmutableList.<String> of(), ImmutableList.<String> of(),
        ImmutableList.<String> of());

    UserData.get().setTheme("テーマ2");
    ui.updateThemeMode(themeLists);

    assertEquals("テーマ2", UserData.get().getTheme());
  }

  public void testSetLastestPlayersShouldRenderPlayerHistory() {
    PacketPlayerSummary first = new PacketPlayerSummary();
    first.level = "賢者";
    first.name = "ノドチップ";
    first.imageFileName = "first.png";
    first.recentMode = "全体対戦";
    first.recentState = "ゲーム中";

    PacketPlayerSummary second = new PacketPlayerSummary();
    second.level = "大賢者";
    second.name = "テスト";
    second.imageFileName = "";
    second.recentMode = "-";
    second.recentState = "ゲーム終了";

    ui.setLastestPlayers(ImmutableList.of(first, second));

    String historyHtml = ui.spanPlayerHistory.getInnerHTML();
    assertTrue(historyHtml.contains("<img"));
    assertTrue(historyHtml.contains("<span"));
    assertTrue(historyHtml.contains("ノドチップ"));
    assertTrue(historyHtml.contains("テスト"));
    assertTrue(historyHtml.contains("全体対戦"));
    assertTrue(historyHtml.contains("ゲーム中"));
    assertTrue(historyHtml.contains("-"));
    assertTrue(historyHtml.contains("ゲーム終了"));
    assertTrue(historyHtml.contains(Constant.ICON_URL_PREFIX + "first.png"));
    assertTrue(historyHtml.contains(Constant.ICON_URL_PREFIX + Constant.ICON_NO_IMAGE));
    assertTrue(historyHtml.indexOf("ノドチップ") < historyHtml.indexOf("テスト"));
    assertFalse(historyHtml.contains("<br"));
  }

  public void testSetLastestPlayersShouldShowEmptyMessageWhenNoPlayers() {
    ui.setLastestPlayers(ImmutableList.<PacketPlayerSummary> of());

    assertEquals("最近のプレイヤー表示はまだありません。", ui.spanPlayerHistory.getInnerText());
  }

  public void testSetLastestPlayersShouldShowEmptyMessageWhenInputIsNull() {
    ui.setLastestPlayers(null);

    assertEquals("最近のプレイヤー表示はまだありません。", ui.spanPlayerHistory.getInnerText());
  }

  public void testSetLastestPlayersShouldShowAtMostTenPlayers() {
    List<PacketPlayerSummary> players = Lists.newArrayList();
    for (int i = 1; i <= 12; i++) {
      PacketPlayerSummary player = new PacketPlayerSummary();
      player.level = "賢者";
      player.name = "player_" + i;
      player.imageFileName = "icon" + i + ".png";
      players.add(player);
    }

    ui.setLastestPlayers(players);

    String historyHtml = ui.spanPlayerHistory.getInnerHTML();
    for (int i = 1; i <= 10; i++) {
      assertTrue(historyHtml.contains("player_" + i));
    }
    for (int i = 11; i <= 12; i++) {
      assertFalse(historyHtml.contains("player_" + i));
    }
  }

  public void testSetLastestPlayersShouldNotRecreateIconWhenInputIsUnchanged() {
    PacketPlayerSummary player = new PacketPlayerSummary();
    player.level = "賢者";
    player.name = "ノドチップ";
    player.imageFileName = "first.png";
    player.recentMode = "全体対戦";
    player.recentState = "ゲーム中";

    ui.setLastestPlayers(ImmutableList.of(player));
    Element firstImageElement = ui.spanPlayerHistory.getFirstChildElement().getFirstChildElement();

    PacketPlayerSummary samePlayer = new PacketPlayerSummary();
    samePlayer.level = "賢者";
    samePlayer.name = "ノドチップ";
    samePlayer.imageFileName = "first.png";
    samePlayer.recentMode = "全体対戦";
    samePlayer.recentState = "ゲーム中";
    ui.setLastestPlayers(ImmutableList.of(samePlayer));

    Element secondImageElement = ui.spanPlayerHistory.getFirstChildElement().getFirstChildElement();
    assertSame(firstImageElement, secondImageElement);
  }

  public void testSetLastestPlayersShouldNotRecreateIconWhenOnlyStateChanges() {
    PacketPlayerSummary player = new PacketPlayerSummary();
    player.level = "賢者";
    player.name = "ノドチップ";
    player.imageFileName = "first.png";
    player.recentMode = "全体対戦";
    player.recentState = "マッチング中";

    ui.setLastestPlayers(ImmutableList.of(player));
    Element firstImageElement = ui.spanPlayerHistory.getFirstChildElement().getFirstChildElement();

    PacketPlayerSummary changedState = new PacketPlayerSummary();
    changedState.level = "賢者";
    changedState.name = "ノドチップ";
    changedState.imageFileName = "first.png";
    changedState.recentMode = "全体対戦";
    changedState.recentState = "ゲーム中";
    ui.setLastestPlayers(ImmutableList.of(changedState));

    Element secondImageElement = ui.spanPlayerHistory.getFirstChildElement().getFirstChildElement();
    assertSame(firstImageElement, secondImageElement);
  }

  public void testSetLastestPlayersShouldRenderModeAndStateInSeparateLines() {
    PacketPlayerSummary player = new PacketPlayerSummary();
    player.level = "賢者";
    player.name = "ノドチップ";
    player.imageFileName = "first.png";
    player.recentMode = "イベント対戦";
    player.recentState = "マッチング中";

    ui.setLastestPlayers(ImmutableList.of(player));

    Element playerElement = ui.spanPlayerHistory.getFirstChildElement();
    Element nameElement = playerElement.getFirstChildElement().getNextSiblingElement();
    Element metaElement = nameElement.getNextSiblingElement();
    Element modeElement = metaElement.getFirstChildElement();
    assertNotNull(modeElement);
    Element stateElement = modeElement.getNextSiblingElement();
    assertNotNull(stateElement);
    assertEquals("イベント対戦", modeElement.getInnerText());
    assertEquals("マッチング中", stateElement.getInnerText());
  }

  public void testPlayerHistoryShouldBeDisplayedBetweenGameButtonsAndMainSettings() {
    String renderedHtml = ui.getElement().getInnerHTML();
    assertTrue(renderedHtml.contains("最近のプレイヤー"));
    assertTrue(renderedHtml.contains("COM対戦"));
    assertTrue(renderedHtml.contains("主要設定"));
    assertTrue(renderedHtml.indexOf("COM対戦") < renderedHtml.indexOf("最近のプレイヤー"));
    assertTrue(renderedHtml.indexOf("最近のプレイヤー") < renderedHtml.indexOf("主要設定"));
  }
}
