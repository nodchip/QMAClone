package tv.dyndns.kishibe.qmaclone.client.sound;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * SoundCreditsCatalogのテスト。
 */
public class SoundCreditsCatalogTest {

  /**
   * すべての効果音アセットにクレジットが存在する。
   */
  @Test
  public void everySoundAssetShouldHaveCreditsEntry() {
    assertTrue(SoundCreditsCatalog.hasAllCreditsFor(SoundCatalog.getAssets()));
  }
}
