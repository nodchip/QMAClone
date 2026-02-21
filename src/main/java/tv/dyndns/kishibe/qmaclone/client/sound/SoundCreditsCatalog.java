package tv.dyndns.kishibe.qmaclone.client.sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * 効果音クレジット情報を管理する。
 */
public final class SoundCreditsCatalog {
  private static final String CREDIT_SOURCE = "ザ・マッチメイカァズ2nd (https://osabisi.sakura.ne.jp/m2/)";
  private static final String CREDIT_LICENSE = "配布サイトの利用条件に従う";

  /**
   * クレジット1件分の情報。
   */
  public static class SoundCreditEntry {
    private final String assetId;
    private final String title;
    private final String source;
    private final String license;

    /**
     * クレジット情報を構築する。
     */
    public SoundCreditEntry(String assetId, String title, String source, String license) {
      this.assetId = Preconditions.checkNotNull(assetId);
      this.title = Preconditions.checkNotNull(title);
      this.source = Preconditions.checkNotNull(source);
      this.license = Preconditions.checkNotNull(license);
    }

    /**
     * アセットIDを返す。
     */
    public String getAssetId() {
      return assetId;
    }

    /**
     * 音源タイトルを返す。
     */
    public String getTitle() {
      return title;
    }

    /**
     * 出典を返す。
     */
    public String getSource() {
      return source;
    }

    /**
     * ライセンスを返す。
     */
    public String getLicense() {
      return license;
    }
  }

  private static final List<SoundCreditEntry> ENTRIES = createEntries();
  private static final Map<String, SoundCreditEntry> ENTRY_BY_ASSET_ID = createMap(ENTRIES);

  private SoundCreditsCatalog() {}

  /**
   * クレジット一覧を返す。
   */
  public static List<SoundCreditEntry> getEntries() {
    return ENTRIES;
  }

  /**
   * すべてのアセットにクレジットが存在するかを返す。
   */
  public static boolean hasAllCreditsFor(Map<SoundEvent, SoundAsset> assets) {
    for (SoundAsset asset : assets.values()) {
      if (!ENTRY_BY_ASSET_ID.containsKey(asset.getAssetId())) {
        return false;
      }
    }
    return true;
  }

  private static List<SoundCreditEntry> createEntries() {
    List<SoundCreditEntry> entries = new ArrayList<SoundCreditEntry>();
    entries.add(new SoundCreditEntry("correct", "Correct", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("incorrect", "Incorrect", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("timeUp", "Time Up", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("buttonPush", "Button Push", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("buttonOk", "Button OK", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("readyForGame", "Ready For Game", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("resultWin", "Result Win", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("resultLose", "Result Lose", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("uiTabSwitch", "UI Tab Switch", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("uiModalOpen", "UI Modal Open", CREDIT_SOURCE, CREDIT_LICENSE));
    entries.add(new SoundCreditEntry("uiModalClose", "UI Modal Close", CREDIT_SOURCE, CREDIT_LICENSE));
    return Collections.unmodifiableList(entries);
  }

  private static Map<String, SoundCreditEntry> createMap(List<SoundCreditEntry> entries) {
    Map<String, SoundCreditEntry> map = new HashMap<String, SoundCreditEntry>();
    for (SoundCreditEntry entry : entries) {
      map.put(entry.getAssetId(), entry);
    }
    return Collections.unmodifiableMap(map);
  }
}
