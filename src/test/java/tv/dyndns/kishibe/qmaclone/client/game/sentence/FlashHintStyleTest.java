package tv.dyndns.kishibe.qmaclone.client.game.sentence;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * フラッシュ問題の入力候補文字のスタイル定義を検証する。
 */
public class FlashHintStyleTest {
  /**
   * 候補文字は表示領域内で中央揃えになるスタイルを持つ。
   */
  @Test
  public void cubeHintUsesCenteredStyle() throws Exception {
    String css = Files.readString(Paths.get("src/main/webapp/QMAClone.css"), StandardCharsets.UTF_8);
    Pattern pattern = Pattern.compile(
        "\\.gwt-Label-cubeHint\\s*\\{[^}]*width:\\s*100%\\s*;[^}]*text-align:\\s*center\\s*;",
        Pattern.DOTALL);
    assertTrue("gwt-Label-cubeHint に width:100% と text-align:center が必要です",
        pattern.matcher(css).find());
  }
}
