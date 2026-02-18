package tv.dyndns.kishibe.qmaclone.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * 結果画面のモダンレイアウト構造をソース上で検証する。
 */
public class PanelResultModernLayoutSourceTest {
  /**
   * ヒーロー領域とランキング領域を持ち、旧Grid実装を除去している。
   */
  @Test
  public void panelResultUsesHeroAndRankingContainers() throws Exception {
    String source =
        Files.readString(Paths.get("src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java"),
            StandardCharsets.UTF_8);
    assertTrue(source.contains("resultHero"));
    assertTrue(source.contains("resultRankingList"));
    assertFalse(source.contains("new Grid("));
  }

  /**
   * 自分の順位カードを強調表示するクラスが用意されている。
   */
  @Test
  public void panelResultHighlightsMyRow() throws Exception {
    String source =
        Files.readString(Paths.get("src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java"),
            StandardCharsets.UTF_8);
    assertTrue(source.contains("resultRankingCardMine"));
  }

  /**
   * ランキングカード内は固定カラム用のスタイル名を持つ。
   */
  @Test
  public void panelResultUsesAlignedColumnsForRankingCard() throws Exception {
    String source =
        Files.readString(Paths.get("src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java"),
            StandardCharsets.UTF_8);
    assertTrue(source.contains("resultRankingName"));
    assertTrue(source.contains("resultRankingScore"));
    assertTrue(source.contains("resultRankingRating"));
  }
}
