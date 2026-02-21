package tv.dyndns.kishibe.qmaclone.client.sound;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * SoundCatalogのテスト。
 */
public class SoundCatalogTest {

  /**
   * 主要イベントの音源が取得できる。
   */
  @Test
  public void shouldResolveDistinctAssetsForCoreEvents() {
    assertNotNull(SoundCatalog.getAsset(SoundEvent.CORRECT));
    assertNotNull(SoundCatalog.getAsset(SoundEvent.INCORRECT));
    assertNotEquals(
        SoundCatalog.getAsset(SoundEvent.CORRECT).getAssetId(),
        SoundCatalog.getAsset(SoundEvent.INCORRECT).getAssetId());
  }
}
