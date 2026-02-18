package tv.dyndns.kishibe.qmaclone.client;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * 結果画面向けスタイル定義の契約を検証する。
 */
public class ResultScreenStyleContractTest {
  @Test
  public void resultScreenDefinesModernCardStyles() throws Exception {
    String css =
        Files.readString(Paths.get("src/main/webapp/QMAClone.css"), StandardCharsets.UTF_8);
    assertTrue(css.contains(".resultTitle"));
    assertTrue(css.contains(".resultBackLink"));
    assertTrue(css.contains(".resultHero"));
    assertTrue(css.contains(".resultRankingCard"));
    assertTrue(css.contains(".resultRankingCardMine"));
    assertTrue(css.contains(".resultRatingBadgeUp"));
  }
}
