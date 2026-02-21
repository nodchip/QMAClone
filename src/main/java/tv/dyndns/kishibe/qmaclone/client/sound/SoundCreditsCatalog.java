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
    entries.add(new SoundCreditEntry("correct", "Correct", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("incorrect", "Incorrect", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("timeUp", "Time Up", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("buttonPush", "Button Push", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("buttonOk", "Button OK", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("readyForGame", "Ready For Game", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("resultWin", "Result Win", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("resultLose", "Result Lose", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("uiTabSwitch", "UI Tab Switch", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("uiModalOpen", "UI Modal Open", "QMAClone Legacy Sound", "Project Internal"));
    entries.add(new SoundCreditEntry("uiModalClose", "UI Modal Close", "QMAClone Legacy Sound", "Project Internal"));
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
