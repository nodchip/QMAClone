package tv.dyndns.kishibe.qmaclone.client.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * AudioEngineのテスト。
 */
public class AudioEngineTest {

  /**
   * マスター・カテゴリ・アセットのゲインを乗算した値を返す。
   */
  @Test
  public void shouldCalculateFinalGainFromMasterCategoryAndAsset() {
    double gain = AudioEngine.computeFinalGain(0.8, 0.5, 0.5);
    assertEquals(0.2, gain, 0.0001);
  }
}
