# Sound Modernization Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** ゲーム内SEをモダン化し、`Help/About` で音源クレジットを表示し、音量設定（master + カテゴリ別）を `localStorage + サーバー` で同期する。

**Architecture:** 既存の `SoundPlayer` 呼び出し点を入口として維持し、内部を `SoundEvent` ベースの `AudioEngine` に置き換える。音量は `master * category * assetGain` で計算し、設定はクライアント即時保存 + ログイン時サーバー同期で管理する。音源クレジットは設定画面の `Help/About` タブに統合し、アセット定義と同一IDで紐付ける。

**Tech Stack:** Java (GWT client), GWT UiBinder, RPC (`Service`/`ServiceAsync`), server Java (`ServiceServletStub`, `Database`), Maven Surefire, GWT compile

---

### Task 1: Soundイベント基盤を追加する

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundEvent.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCategory.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundAsset.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCatalog.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCatalogTest.java`

**Step 1: Write the failing test**

```java
@Test
public void shouldResolveDistinctAssetsForCoreEvents() {
  assertNotNull(SoundCatalog.getAsset(SoundEvent.CORRECT));
  assertNotNull(SoundCatalog.getAsset(SoundEvent.INCORRECT));
  assertNotEquals(
      SoundCatalog.getAsset(SoundEvent.CORRECT).getAssetId(),
      SoundCatalog.getAsset(SoundEvent.INCORRECT).getAssetId());
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=SoundCatalogTest" test`  
Expected: `SoundCatalog` / `SoundEvent` 未定義で FAIL

**Step 3: Write minimal implementation**

```java
public enum SoundEvent { CORRECT, INCORRECT, TIME_UP, BUTTON_PUSH, READY, RESULT_WIN, RESULT_LOSE, UI_TAB_SWITCH, UI_MODAL_OPEN, UI_MODAL_CLOSE }
```

```java
public final class SoundCatalog {
  public static SoundAsset getAsset(SoundEvent event) { ... }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=SoundCatalogTest" test`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundEvent.java src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCategory.java src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundAsset.java src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCatalog.java src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCatalogTest.java
git commit -m "SoundEventベースの音源カタログを追加"
```

---

### Task 2: AudioEngine と SoundPlayer 統合を実装する

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/AudioEngine.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundManager.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/SoundPlayer.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/AudioEngineTest.java`

**Step 1: Write the failing test**

```java
@Test
public void shouldCalculateFinalGainFromMasterCategoryAndAsset() {
  double gain = AudioEngine.computeFinalGain(0.8, 0.5, 0.5);
  assertEquals(0.2, gain, 0.0001);
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=AudioEngineTest" test`  
Expected: `AudioEngine` 未定義で FAIL

**Step 3: Write minimal implementation**

```java
public static double computeFinalGain(double master, double category, double asset) {
  return master * category * asset;
}
```

**Step 4: Run test to verify it passes**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=AudioEngineTest" test`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/AudioEngine.java src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundManager.java src/main/java/tv/dyndns/kishibe/qmaclone/client/SoundPlayer.java src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/AudioEngineTest.java
git commit -m "AudioEngineを追加してSoundPlayerに統合"
```

---

### Task 3: 既存URL呼び出しをイベントへ移行する

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/SceneReadyForGame.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/game/SceneGame.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/game/input/InputWidget.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundPlayerEventMappingTest.java`

**Step 1: Write the failing test**

```java
@Test
public void legacyButtonUrlShouldMapToButtonEvent() {
  assertEquals(SoundEvent.BUTTON_PUSH, SoundPlayer.toEvent(Constant.SOUND_URL_BUTTON_PUSH));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=SoundPlayerEventMappingTest" test`  
Expected: `toEvent` 未実装で FAIL

**Step 3: Write minimal implementation**

```java
public static SoundEvent toEvent(String url) { ... }
```

**Step 4: Run test to verify it passes**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=SoundPlayerEventMappingTest" test`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java src/main/java/tv/dyndns/kishibe/qmaclone/client/SceneReadyForGame.java src/main/java/tv/dyndns/kishibe/qmaclone/client/game/SceneGame.java src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java src/main/java/tv/dyndns/kishibe/qmaclone/client/game/input/InputWidget.java src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundPlayerEventMappingTest.java
git commit -m "効果音呼び出しをSoundEventベースへ移行"
```

---

### Task 4: 音量設定モデルと localStorage 保存を追加する

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundSettings.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundSettingsStore.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/OtherUi.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/OtherUi.ui.xml`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundSettingsStoreTest.java`

**Step 1: Write the failing test**

```java
@Test
public void shouldRoundTripSettingsJson() {
  SoundSettings settings = new SoundSettings(0.8, 0.7, 0.6, 0.9, false, 1);
  assertEquals(settings, SoundSettingsStore.fromJson(SoundSettingsStore.toJson(settings)));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=SoundSettingsStoreTest" test`  
Expected: クラス未定義で FAIL

**Step 3: Write minimal implementation**

```java
public final class SoundSettingsStore {
  public static String toJson(SoundSettings settings) { ... }
  public static SoundSettings fromJson(String json) { ... }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=SoundSettingsStoreTest" test`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundSettings.java src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundSettingsStore.java src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/OtherUi.java src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/OtherUi.ui.xml src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundSettingsStoreTest.java
git commit -m "音量設定のlocalStorage保存と設定UIを追加"
```

---

### Task 5: サーバー同期（UserData経由）を追加する

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketUserData.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/UserData.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseSchemaMigrator.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DirectDatabase.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/CachedDatabase.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DirectDatabaseSoundSettingsTest.java`

**Step 1: Write the failing test**

```java
@Test
public void shouldPersistSoundSettingsInUserData() {
  // loadUserData -> saveUserData -> loadUserData で音量項目が保持されることを検証
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=DirectDatabaseSoundSettingsTest" test`  
Expected: DBカラム/マッピング不足で FAIL

**Step 3: Write minimal implementation**

```java
// PacketUserData に soundMasterVolume, soundUiVolume, soundGameplayVolume, soundResultVolume, soundMuted を追加
// DatabaseSchemaMigrator で必要カラムを追加
```

**Step 4: Run test to verify it passes**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=DirectDatabaseSoundSettingsTest" test`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketUserData.java src/main/java/tv/dyndns/kishibe/qmaclone/client/UserData.java src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseSchemaMigrator.java src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DirectDatabase.java src/main/java/tv/dyndns/kishibe/qmaclone/server/database/CachedDatabase.java src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DirectDatabaseSoundSettingsTest.java
git commit -m "音量設定のサーバー永続化を追加"
```

---

### Task 6: Help/Aboutタブと音源クレジット表示を追加する

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingAbout.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingAbout.ui.xml`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCreditsCatalog.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSetting.java`
- Create: `docs/credits/sound-assets.md`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCreditsCatalogTest.java`

**Step 1: Write the failing test**

```java
@Test
public void everySoundAssetShouldHaveCreditsEntry() {
  assertTrue(SoundCreditsCatalog.hasAllCreditsFor(SoundCatalog.getAllAssets()));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=SoundCreditsCatalogTest" test`  
Expected: カタログ未実装で FAIL

**Step 3: Write minimal implementation**

```java
public final class SoundCreditsCatalog {
  public static List<SoundCreditsEntry> getEntries() { ... }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=SoundCreditsCatalogTest" test`  
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingAbout.java src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingAbout.ui.xml src/main/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCreditsCatalog.java src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSetting.java docs/credits/sound-assets.md src/test/java/tv/dyndns/kishibe/qmaclone/client/sound/SoundCreditsCatalogTest.java
git commit -m "Help/Aboutタブと音源クレジット表示を追加"
```

---

### Task 7: 総合検証とデプロイ

**Files:**
- Modify: 必要な不具合修正のみ（スコープ外修正禁止）
- Verify: `deploy_qmaclone_tomcat9.ps1`

**Step 1: Run focused tests**

Run:  
`mvn "-Dsurefire.skip=false" "-DfailIfNoTests=false" "-Dtest=SoundCatalogTest,AudioEngineTest,SoundPlayerEventMappingTest,SoundSettingsStoreTest,DirectDatabaseSoundSettingsTest,SoundCreditsCatalogTest" test`  
Expected: PASS

**Step 2: Run GWT compile**

Run: `mvn "-Dgwt.skipCompilation=false" gwt:compile`  
Expected: BUILD SUCCESS

**Step 3: Deploy**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat9.ps1`  
Expected:
- `/QMAClone-1.0-SNAPSHOT/` が `HTTP 200`
- `/tv.dyndns.kishibe.qmaclone.QMAClone/service` が `HTTP 405`
- `/tv.dyndns.kishibe.qmaclone.QMAClone/service?warmup=1` が `HTTP 200`

**Step 4: Commit**

```bash
git add -A
git commit -m "SEモダン化を適用し音量同期とAboutクレジットを実装"
```

---

## 実装ルール（必須）
- @test-driven-development を各Taskで厳守する（Red -> Green -> Refactor）。
- 1コミット1目的を維持する（Task単位）。
- 依存する検証は直列で実行する（build -> test -> gwt:compile -> deploy）。
- `security.txt` はコミット対象に含めない。
