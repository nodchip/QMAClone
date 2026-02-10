# Google+連携廃止とOIDC移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Google+依存を廃止し、GIS/OIDCベースの認証連携へ移行しつつ、`gwt-plus` を依存関係から削除する。

**Architecture:** 既存の `googlePlusId` 中心設計を `authProvider` + `authSubject` に置換する。DBは起動時冪等DDLで列追加し、互換期間は旧導線を内部委譲で維持する。クライアントはGoogle+ API依存を除去し、OIDCトークン連携APIへ切り替える。

**Tech Stack:** Java 25, Maven, GWT 2.12.2, Guice, MySQL, JUnit4, Mockito

---

### Task 1: 依存関係ベースライン確定（`gwt-plus`削除準備）

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/QMAClone.gwt.xml`
- Test: `target/dependency-tree-before-googleplus-migration.txt`（生成物）

**Step 1: 失敗テストの代わりに依存調査を固定化**

Run:
```powershell
mvn -q -DskipTests dependency:tree > target/dependency-tree-before-googleplus-migration.txt
rg -n "gwt-plus|com.google.api.gwt.services.Plus|GOOGLE_PLUS_ID|googlePlusId" pom.xml src/main/java src/test/java -S
```
Expected: `gwt-plus` と Google+ 依存箇所が列挙される。

**Step 2: `gwt-plus` と Plus モジュール参照を削除**

`pom.xml` から以下依存を削除:
```xml
<dependency>
  <groupId>local.legacy</groupId>
  <artifactId>gwt-plus</artifactId>
  <version>1-0.3-alpha</version>
  <scope>system</scope>
  <systemPath>${third.party.dir}/gwt-plus-v1-0.3-alpha.jar</systemPath>
</dependency>
```

`src/main/java/tv/dyndns/kishibe/qmaclone/QMAClone.gwt.xml` から以下を削除:
```xml
<inherits name='com.google.api.gwt.services.Plus' />
```

**Step 3: 依存削除によるコンパイル失敗を確認**

Run:
```powershell
mvn -DskipTests compile
```
Expected: `Plus` 利用クラスでコンパイルエラー（想定どおり）。

**Step 4: コミット**

```powershell
git add pom.xml src/main/java/tv/dyndns/kishibe/qmaclone/QMAClone.gwt.xml target/dependency-tree-before-googleplus-migration.txt
git commit -m "gwt-plus依存とGWT Plusモジュール参照を削除"
```

### Task 2: DB自動マイグレーション（AUTH_PROVIDER/AUTH_SUB）を追加

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseSchemaMigrator.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DirectDatabase.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseSchemaMigratorTest.java`

**Step 1: 失敗テストを書く**

```java
@Test
public void migrateShouldAddAuthColumnsWhenMissing() throws Exception {
  DatabaseSchemaMigrator migrator = new DatabaseSchemaMigrator(runner);
  migrator.migratePlayerAuthColumns();
  assertTrue(hasColumn("player", "AUTH_PROVIDER"));
  assertTrue(hasColumn("player", "AUTH_SUB"));
}
```

**Step 2: 失敗を確認**

Run:
```powershell
mvn -Dtest=DatabaseSchemaMigratorTest test
```
Expected: FAIL（`DatabaseSchemaMigrator` 未実装）。

**Step 3: 最小実装**

`DatabaseSchemaMigrator` に以下を実装:
- `INFORMATION_SCHEMA.COLUMNS` で列存在確認
- 不足時のみ `ALTER TABLE player ADD COLUMN ...`
- `INFORMATION_SCHEMA.STATISTICS` で一意インデックス存在確認
- 不足時のみ `CREATE UNIQUE INDEX ...`

`DirectDatabase` コンストラクタで `migratePlayerAuthColumns()` を1回実行。

**Step 4: テスト再実行**

Run:
```powershell
mvn -Dtest=DatabaseSchemaMigratorTest test
```
Expected: PASS

**Step 5: コミット**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseSchemaMigrator.java src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DirectDatabase.java src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseSchemaMigratorTest.java
git commit -m "起動時DBマイグレーションでAUTH_PROVIDER/AUTH_SUBを自動追加"
```

### Task 3: サーバーDB APIをGoogle+専用から外部認証汎用へ移行

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/Database.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DirectDatabase.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/CachedDatabase.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseTest.java`

**Step 1: 失敗テストを書く**

```java
@Test
public void lookupUserDataByExternalAccountShouldReturnMatchedUser() throws Exception {
  PacketUserData userData = TestDataProvider.getUserData();
  userData.userCode = FAKE_USER_CODE;
  userData.authProvider = "google";
  userData.authSubject = "sub-1";
  database.setUserData(userData);

  assertEquals(ImmutableList.of(userData),
      database.lookupUserDataByExternalAccount("google", "sub-1"));
}
```

**Step 2: 失敗確認**

Run:
```powershell
mvn -Dtest=DatabaseTest#lookupUserDataByExternalAccountShouldReturnMatchedUser test
```
Expected: FAIL（メソッド未定義）。

**Step 3: 最小実装**

- 新APIを `Database` / `DirectDatabase` / `CachedDatabase` に追加
- 旧Google+ APIは互換のため内部委譲で残す（段階削除）

**Step 4: テスト再実行**

Run:
```powershell
mvn -Dtest=DatabaseTest test
```
Expected: PASS

**Step 5: コミット**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/database/Database.java src/main/java/tv/dyndns/kishibe/qmaclone/server/database/DirectDatabase.java src/main/java/tv/dyndns/kishibe/qmaclone/server/database/CachedDatabase.java src/test/java/tv/dyndns/kishibe/qmaclone/server/database/DatabaseTest.java
git commit -m "外部認証キー対応のDB APIを追加しGoogle+ APIを互換委譲化"
```

### Task 4: RPC/DTOを`googlePlusId`から`authProvider/authSubject`へ移行

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketUserData.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/UserData.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/Service.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/ServiceAsync.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStub.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStubTest.java`

**Step 1: 失敗テストを書く**

```java
@Test
public void lookupUserDataByExternalAccountShouldDelegateToDatabase() throws Exception {
  when(mockDatabase.lookupUserDataByExternalAccount("google", "sub-1"))
      .thenReturn(ImmutableList.of(new PacketUserData()));
  service.lookupUserDataByExternalAccount("google", "sub-1");
  verify(mockDatabase).lookupUserDataByExternalAccount("google", "sub-1");
}
```

**Step 2: 失敗確認**

Run:
```powershell
mvn -Dtest=ServiceServletStubTest#lookupUserDataByExternalAccountShouldDelegateToDatabase test
```
Expected: FAIL

**Step 3: 最小実装**

- RPCメソッドを追加
- 旧メソッドは互換維持
- DTOの `equals/hashCode/toString` を新項目へ更新

**Step 4: テスト再実行**

Run:
```powershell
mvn -Dtest=ServiceServletStubTest test
```
Expected: PASS

**Step 5: コミット**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketUserData.java src/main/java/tv/dyndns/kishibe/qmaclone/client/UserData.java src/main/java/tv/dyndns/kishibe/qmaclone/client/Service.java src/main/java/tv/dyndns/kishibe/qmaclone/client/ServiceAsync.java src/main/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStub.java src/test/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStubTest.java
git commit -m "RPCとDTOを外部認証キー対応へ移行"
```

### Task 5: クライアント設定画面のGoogle+ API依存を除去しOIDC連携へ置換

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenter.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/SettingModule.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodeView.ui.xml`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenterTest.java`

**Step 1: 失敗テストを書く**

```java
@Test
public void connectShouldCallLookupByExternalAccount() {
  when(mockUserData.getAuthProvider()).thenReturn("google");
  when(mockUserData.getAuthSubject()).thenReturn("sub-1");
  presenter.onLoad();
  verify(mockService).lookupUserDataByExternalAccount(eq("google"), eq("sub-1"), any());
}
```

**Step 2: 失敗確認**

Run:
```powershell
mvn -Dtest=PanelSettingUserCodePresenterTest test
```
Expected: FAIL

**Step 3: 最小実装**

- `com.google.api.gwt.services.plus.shared.Plus` 依存を除去
- OIDCトークン取得後に `provider/sub` を設定してRPC呼び出し
- UI文言を「Google+連携」から「Googleアカウント連携（OIDC）」へ変更

**Step 4: テスト再実行**

Run:
```powershell
mvn -Dtest=PanelSettingUserCodePresenterTest test
```
Expected: PASS

**Step 5: コミット**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenter.java src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/SettingModule.java src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodeView.ui.xml src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenterTest.java
git commit -m "設定画面のGoogle+依存を除去しOIDC連携フローへ置換"
```

### Task 6: Google+残存文言/導線/PlusOne機能を整理

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/WidgetProblemForm.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemFeedback.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportUi.java`
- Delete: `src/main/java/tv/dyndns/kishibe/qmaclone/client/PlusOne.java`（不要なら）

**Step 1: 失敗確認（静的検査）**

Run:
```powershell
rg -n "Google\\+|plusone\\.js|PlusOne|com.google.api.gwt.services.plus" src/main/java -S
```
Expected: 複数ヒット

**Step 2: 最小実装**

- Google+文言を中立化
- `PlusOne` 依存導線を削除または無効化
- 不要クラスは削除

**Step 3: 検査再実行**

Run:
```powershell
rg -n "Google\\+|plusone\\.js|com.google.api.gwt.services.plus" src/main/java -S
```
Expected: 0件（意図した例外コメントのみ許容）

**Step 4: コミット**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/WidgetProblemForm.java src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemFeedback.java src/main/java/tv/dyndns/kishibe/qmaclone/client/report/ProblemReportUi.java src/main/java/tv/dyndns/kishibe/qmaclone/client/PlusOne.java
git commit -m "Google+文言とPlusOne導線を整理"
```

### Task 7: 総合検証（build -> test -> gwt:compile 直列）

**Files:**
- Modify: `docs/plans/2026-02-10-googleplus-retirement-oidc-migration-design.md`
- Create: `docs/plans/2026-02-10-googleplus-retirement-oidc-migration-verification.md`

**Step 1: ビルド**

Run:
```powershell
mvn -DskipTests compile
```
Expected: PASS

**Step 2: テスト**

Run:
```powershell
mvn test
```
Expected: PASS

**Step 3: GWT再コンパイル**

Run:
```powershell
mvn -Pgwt-compile-java25 "-Dgwt.skipCompilation=false" -DskipTests gwt:compile
```
Expected: PASS（最新成果物生成）

**Step 4: 検証記録を作成**

`docs/plans/2026-02-10-googleplus-retirement-oidc-migration-verification.md` に実行コマンドと結果を記載。

**Step 5: コミット**

```powershell
git add docs/plans/2026-02-10-googleplus-retirement-oidc-migration-design.md docs/plans/2026-02-10-googleplus-retirement-oidc-migration-verification.md
git commit -m "Google+廃止/OIDC移行の検証結果を記録"
```

### Task 8: 互換削除フェーズ（最終クリーンアップ）

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/database/Database.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStub.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/Service.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/ServiceAsync.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStubTest.java`

**Step 1: 失敗テストを書く**

```java
@Test
public void shouldNotExposeLegacyGooglePlusApi() {
  // 旧メソッド呼び出しが存在しないことをコンパイルで担保する
}
```

**Step 2: 旧APIを削除**

- `lookupUserDataByGooglePlusId` / `disconnectUserCodeFromGooglePlus` を削除
- 呼び出し側を新APIへ完全置換

**Step 3: 全テスト実行**

Run:
```powershell
mvn test
```
Expected: PASS

**Step 4: コミット**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/database/Database.java src/main/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStub.java src/main/java/tv/dyndns/kishibe/qmaclone/client/Service.java src/main/java/tv/dyndns/kishibe/qmaclone/client/ServiceAsync.java src/test/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStubTest.java
git commit -m "Google+互換APIを削除し外部認証APIへ統一"
```

### Task 9: 設定画面UXフロー調整（文言・状態遷移・例外時UI）

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodeView.ui.xml`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodeView.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenter.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenterTest.java`

**Step 1: 失敗テストを追加**

追加観点:
- 連携成功後に `紐づける` ボタンが非表示になる。
- 一覧件数 0/1/2+ で `切り替える` と `解除する` の表示が期待どおりに分岐する。
- ログインキャンセル時に押下前状態へ戻る。
- 解除成功時に全ボタン無効化と再読込案内が表示される。

Run:
```powershell
mvn -Dtest=PanelSettingUserCodePresenterTest test
```
Expected: FAIL（新規観点分）

**Step 2: 最小実装**

- ボタン文言を設計書 `11.1` に合わせて更新。
- `Presenter` で状態遷移を整理。
- 解除成功時は全ボタン無効化 + 再読込案内表示を統一。
- 0件時メッセージを `紐づけ済みユーザーコードはありません` に統一。

**Step 3: テスト実行**

Run:
```powershell
mvn -Dtest=PanelSettingUserCodePresenterTest test
```
Expected: PASS

**Step 4: ビルド反映**

Run:
```powershell
mvn -DskipTests compile
mvn -Pgwt-compile-java25 "-Dgwt.skipCompilation=false" -DskipTests gwt:compile
mvn package -DskipTests
powershell -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat9.ps1 -SkipBuild
```
Expected: PASS

**Step 5: コミット**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodeView.ui.xml src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodeView.java src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenter.java src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenterTest.java
git commit -m "設定画面のGoogle連携UXフローを安定性優先で調整"
```
