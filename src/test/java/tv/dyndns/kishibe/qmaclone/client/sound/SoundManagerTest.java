package tv.dyndns.kishibe.qmaclone.client.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * SoundUrlSanitizerのURL検証テスト。
 */
public class SoundManagerTest {

  /**
   * http/httpsのURLは許可される。
   */
  @Test
  public void sanitizeSoundUrlShouldAllowHttpAndHttps() {
    assertEquals("http://example.com/sound.wav",
        SoundUrlSanitizer.sanitizeSoundUrl("http://example.com/sound.wav"));
    assertEquals("https://example.com/sound.wav",
        SoundUrlSanitizer.sanitizeSoundUrl("https://example.com/sound.wav"));
  }

  /**
   * 不正なスキームは拒否される。
   */
  @Test
  public void sanitizeSoundUrlShouldRejectInvalidScheme() {
    assertNull(SoundUrlSanitizer.sanitizeSoundUrl("javascript:alert(1)"));
    assertNull(SoundUrlSanitizer.sanitizeSoundUrl("data:text/plain,abc"));
    assertNull(SoundUrlSanitizer.sanitizeSoundUrl("file:///tmp/a.wav"));
  }

  /**
   * HTML注入に使える文字を含むURLは拒否される。
   */
  @Test
  public void sanitizeSoundUrlShouldRejectHtmlSpecialChars() {
    assertNull(SoundUrlSanitizer.sanitizeSoundUrl("https://example.com/a\" onerror=\"x"));
    assertNull(SoundUrlSanitizer.sanitizeSoundUrl("https://example.com/<script>.wav"));
  }
}
