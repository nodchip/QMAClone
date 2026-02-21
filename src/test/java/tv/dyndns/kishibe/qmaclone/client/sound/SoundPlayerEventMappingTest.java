package tv.dyndns.kishibe.qmaclone.client.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.SoundPlayer;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

/**
 * SoundPlayerのURL->イベント変換テスト。
 */
public class SoundPlayerEventMappingTest {

  /**
   * ボタン押下URLがBUTTON_PUSHイベントに変換される。
   */
  @Test
  public void legacyButtonUrlShouldMapToButtonEvent() {
    assertEquals(SoundEvent.BUTTON_PUSH, SoundPlayer.toEvent(Constant.SOUND_URL_BUTTON_PUSH));
  }

  /**
   * 未定義URLは変換されない。
   */
  @Test
  public void unknownUrlShouldNotMapToAnyEvent() {
    assertNull(SoundPlayer.toEvent("https://example.com/unknown.wav"));
  }
}
