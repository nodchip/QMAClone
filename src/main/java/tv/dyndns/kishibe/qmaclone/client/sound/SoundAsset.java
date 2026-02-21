package tv.dyndns.kishibe.qmaclone.client.sound;

import com.google.common.base.Preconditions;

/**
 * 効果音アセット定義。
 */
public class SoundAsset {
  private final String assetId;
  private final String url;
  private final SoundCategory category;
  private final double baseGain;

  /**
   * 効果音アセットを構築する。
   */
  public SoundAsset(String assetId, String url, SoundCategory category, double baseGain) {
    this.assetId = Preconditions.checkNotNull(assetId);
    this.url = Preconditions.checkNotNull(url);
    this.category = Preconditions.checkNotNull(category);
    this.baseGain = baseGain;
  }

  /**
   * アセット識別子を返す。
   */
  public String getAssetId() {
    return assetId;
  }

  /**
   * 音源URLを返す。
   */
  public String getUrl() {
    return url;
  }

  /**
   * 音源カテゴリを返す。
   */
  public SoundCategory getCategory() {
    return category;
  }

  /**
   * 基本ゲインを返す。
   */
  public double getBaseGain() {
    return baseGain;
  }
}
