package tv.dyndns.kishibe.qmaclone.client.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * SoundSettingsStoreのテスト。
 */
public class SoundSettingsStoreTest {

  /**
   * 音量設定をJSONへ往復変換できる。
   */
  @Test
  public void shouldRoundTripSettingsJson() {
    SoundSettings settings = new SoundSettings(0.8, 0.7, 0.6, 0.9, false, 1);
    assertEquals(settings, SoundSettingsStore.fromJson(SoundSettingsStore.toJson(settings)));
  }
}
