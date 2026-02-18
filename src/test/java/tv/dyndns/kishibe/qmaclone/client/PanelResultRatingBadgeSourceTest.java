package tv.dyndns.kishibe.qmaclone.client;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * 結果画面のレーティングバッジ実装をソース上で検証する。
 */
public class PanelResultRatingBadgeSourceTest {
  @Test
  public void panelResultContainsRatingBadgeClasses() throws Exception {
    String source =
        Files.readString(Paths.get("src/main/java/tv/dyndns/kishibe/qmaclone/client/PanelResult.java"),
            StandardCharsets.UTF_8);
    assertTrue(source.contains("resultRatingBadgeUp"));
    assertTrue(source.contains("resultRatingBadgeDown"));
    assertTrue(source.contains("resultRatingBadgeFlat"));
  }
}
