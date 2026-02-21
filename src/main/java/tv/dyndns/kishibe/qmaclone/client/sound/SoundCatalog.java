package tv.dyndns.kishibe.qmaclone.client.sound;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

/**
 * 効果音イベントとアセットの対応を管理する。
 */
public final class SoundCatalog {
  private static final Map<SoundEvent, SoundAsset> ASSETS = createAssets();

  private SoundCatalog() {
  }

  /**
   * 指定イベントの効果音アセットを返す。
   */
  public static SoundAsset getAsset(SoundEvent event) {
    return ASSETS.get(event);
  }

  /**
   * 全イベントの効果音アセットを返す。
   */
  public static Map<SoundEvent, SoundAsset> getAssets() {
    return ASSETS;
  }

  private static Map<SoundEvent, SoundAsset> createAssets() {
    EnumMap<SoundEvent, SoundAsset> assets = new EnumMap<>(SoundEvent.class);
    assets.put(SoundEvent.CORRECT, new SoundAsset("correct", Constant.SOUND_URL_GOOD, SoundCategory.GAMEPLAY, 1.0));
    assets.put(SoundEvent.INCORRECT, new SoundAsset("incorrect", Constant.SOUND_URL_BAD, SoundCategory.GAMEPLAY, 1.0));
    assets.put(SoundEvent.TIME_UP, new SoundAsset("timeUp", Constant.SOUND_URL_TIME_UP, SoundCategory.GAMEPLAY, 1.0));
    assets.put(SoundEvent.BUTTON_PUSH, new SoundAsset("buttonPush", Constant.SOUND_URL_BUTTON_PUSH, SoundCategory.UI, 1.0));
    assets.put(SoundEvent.BUTTON_OK, new SoundAsset("buttonOk", Constant.SOUND_URL_BUTTON_OK, SoundCategory.UI, 1.0));
    assets.put(SoundEvent.READY_FOR_GAME, new SoundAsset("readyForGame", Constant.SOUND_URL_READY_FOR_GAME, SoundCategory.RESULT, 1.0));
    assets.put(SoundEvent.RESULT_WIN, new SoundAsset("resultWin", Constant.SOUND_URL_GOOD, SoundCategory.RESULT, 1.0));
    assets.put(SoundEvent.RESULT_LOSE, new SoundAsset("resultLose", Constant.SOUND_URL_BAD, SoundCategory.RESULT, 1.0));
    assets.put(SoundEvent.UI_TAB_SWITCH, new SoundAsset("uiTabSwitch", Constant.SOUND_URL_BUTTON_PUSH, SoundCategory.UI, 1.0));
    assets.put(SoundEvent.UI_MODAL_OPEN, new SoundAsset("uiModalOpen", Constant.SOUND_URL_BUTTON_PUSH, SoundCategory.UI, 1.0));
    assets.put(SoundEvent.UI_MODAL_CLOSE, new SoundAsset("uiModalClose", Constant.SOUND_URL_BUTTON_PUSH, SoundCategory.UI, 1.0));
    return Collections.unmodifiableMap(assets);
  }
}
