package tv.dyndns.kishibe.qmaclone.client.sound;

import com.google.common.base.Preconditions;

/**
 * 効果音再生時のゲイン計算と再生を担当する。
 */
public class AudioEngine {
  private final SoundManager soundManager;

  /**
   * AudioEngineを構築する。
   */
  public AudioEngine(SoundManager soundManager) {
    this.soundManager = Preconditions.checkNotNull(soundManager);
  }

  /**
   * 最終ゲインを計算する。
   */
  public static double computeFinalGain(double masterGain, double categoryGain, double assetGain) {
    return clamp(masterGain * categoryGain * assetGain);
  }

  /**
   * URLを指定して効果音を再生する。
   */
  public void playUrl(String url, double masterGain, double categoryGain, double assetGain) {
    double finalGain = computeFinalGain(masterGain, categoryGain, assetGain);
    if (finalGain <= 0) {
      return;
    }
    soundManager.play(url, finalGain);
  }

  private static double clamp(double value) {
    if (value < 0) {
      return 0;
    }
    if (1 < value) {
      return 1;
    }
    return value;
  }
}
