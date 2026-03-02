# Facebookアクセストークン自動更新 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 管理画面からFacebook再認可を実行し、`Page Access Token` を自動取得・保存・更新できるようにする。

**Architecture:** 認可開始/コールバック用Servletを追加し、`code -> long-lived user token -> page token` の交換をサーバー側で実行する。投稿処理は `FacebookClient` から専用サービスへ委譲し、無効トークン時に1回だけ再取得リトライする。トークンは `Database#setPassword/getPassword` で管理し、ログへ秘匿情報を出さない。

**Tech Stack:** Java 25, Tomcat 10 (jakarta servlet), Guice, RestFB, Gson, JUnit5, Mockito

---

### Task 1: 既存Facebook投稿処理の分離ポイントを固定化

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookClient.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookClientTest.java`

**Step 1: 失敗テストを追加する**

`FacebookClientTest` に「トークン取得失敗時は投稿を実行しない」ケースを追加する。

**Step 2: 失敗を確認する**

Run:
```powershell
mvn "-Dtest=FacebookClientTest" test
```
Expected: FAIL（投稿分岐が未整理）

**Step 3: 最小実装でテストを通す**

`FacebookClient` のトークン取得と投稿処理を小メソッドに分割し、以降の置換に備える。

**Step 4: 再実行する**

Run:
```powershell
mvn "-Dtest=FacebookClientTest" test
```
Expected: PASS

**Step 5: コミットする**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookClient.java src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookClientTest.java
git commit -m "Facebook投稿処理の分離ポイントを整理"
```

### Task 2: Facebookトークン保管・取得サービスを追加する

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookTokenRepository.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookTokenState.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookTokenRepositoryTest.java`

**Step 1: 失敗テストを追加する**

追加観点:
1. user/page token の保存・取得
2. expiresAt の保存・取得
3. 未設定時のnull/emptyハンドリング

**Step 2: 失敗を確認する**

Run:
```powershell
mvn "-Dtest=FacebookTokenRepositoryTest" test
```
Expected: FAIL（未実装）

**Step 3: 最小実装する**

`Database` の password key を使って `facebook_user_access_token` などを読み書きする。

**Step 4: 再実行する**

Run:
```powershell
mvn "-Dtest=FacebookTokenRepositoryTest" test
```
Expected: PASS

**Step 5: コミットする**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookTokenRepository.java src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookTokenState.java src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookTokenRepositoryTest.java
git commit -m "Facebookトークンリポジトリを追加"
```

### Task 3: Graph API交換ロジックをサービス化する

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthService.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookGraphApiClient.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthServiceTest.java`

**Step 1: 失敗テストを追加する**

追加観点:
1. `code` から long-lived user token を取得できる
2. `/me/accounts` から `page_id` の page token を抽出できる
3. 対象 page が無い場合に失敗を返す

**Step 2: 失敗を確認する**

Run:
```powershell
mvn "-Dtest=FacebookAuthServiceTest" test
```
Expected: FAIL（未実装）

**Step 3: 最小実装する**

`Downloader` + Gson で Graph APIレスポンスを安全にparseし、`FacebookTokenRepository` に保存する。

**Step 4: 再実行する**

Run:
```powershell
mvn "-Dtest=FacebookAuthServiceTest" test
```
Expected: PASS

**Step 5: コミットする**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthService.java src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookGraphApiClient.java src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthServiceTest.java
git commit -m "Facebook OAuth交換サービスを実装"
```

### Task 4: 管理者向け認可開始/コールバックServletを追加する

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthStartServlet.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthCallbackServlet.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/AdminSessionUtil.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStub.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthCallbackServletTest.java`

**Step 1: 失敗テストを追加する**

追加観点:
1. 非管理者セッションは `403`
2. `state` 不一致は `400`
3. `code` 正常時に token 保存処理が呼ばれる

**Step 2: 失敗を確認する**

Run:
```powershell
mvn "-Dtest=FacebookAuthCallbackServletTest" test
```
Expected: FAIL（Servlet未実装）

**Step 3: 最小実装する**

- `@WebServlet("/admin/facebook/auth/start")` と `@WebServlet("/admin/facebook/auth/callback")` を追加
- `AdminSessionUtil` で `loginUserCode` セッション属性 + `AdminAccessManager` 判定を共通化
- callback 成功後は設定画面へリダイレクト（成功/失敗クエリ付き）

**Step 4: 再実行する**

Run:
```powershell
mvn "-Dtest=FacebookAuthCallbackServletTest" test
```
Expected: PASS

**Step 5: コミットする**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthStartServlet.java src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthCallbackServlet.java src/main/java/tv/dyndns/kishibe/qmaclone/server/AdminSessionUtil.java src/main/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStub.java src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookAuthCallbackServletTest.java
git commit -m "Facebook再認可用の管理者Servletを追加"
```

### Task 5: 管理画面にFacebook再認可導線を追加する

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingAdministrator.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingThemeModeEditor.java`
- Modify: `src/main/webapp/QMAClone.css`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingAdministratorTest.java`

**Step 1: 失敗テストを追加する**

追加観点:
1. 管理者モードで再認可ボタンが表示される
2. クリックで `/admin/facebook/auth/start` を開く

**Step 2: 失敗を確認する**

Run:
```powershell
mvn "-Dtest=PanelSettingAdministratorTest" test
```
Expected: FAIL（導線未追加）

**Step 3: 最小実装する**

- 「Facebook連携を更新」ボタンを追加
- 新規ウィンドウ/同一タブ遷移のいずれかに統一
- 完了メッセージ表示領域を追加

**Step 4: 再実行する**

Run:
```powershell
mvn "-Dtest=PanelSettingAdministratorTest" test
```
Expected: PASS

**Step 5: コミットする**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingAdministrator.java src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingThemeModeEditor.java src/main/webapp/QMAClone.css src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingAdministratorTest.java
git commit -m "管理画面にFacebook再認可導線を追加"
```

### Task 6: 投稿処理を自動更新対応へ切り替える

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookClient.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/SnsClients.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookClientTest.java`

**Step 1: 失敗テストを追加する**

追加観点:
1. token無効時に1回だけ再取得して再投稿
2. 再取得不能なら例外で全体停止せずWARNで終了

**Step 2: 失敗を確認する**

Run:
```powershell
mvn "-Dtest=FacebookClientTest" test
```
Expected: FAIL（再取得リトライ未実装）

**Step 3: 最小実装する**

`FacebookAuthService#getValidPageAccessToken()` を導入し、`FacebookClient#post` から使用する。

**Step 4: 再実行する**

Run:
```powershell
mvn "-Dtest=FacebookClientTest" test
```
Expected: PASS

**Step 5: コミットする**

```powershell
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookClient.java src/main/java/tv/dyndns/kishibe/qmaclone/server/sns/SnsClients.java src/test/java/tv/dyndns/kishibe/qmaclone/server/sns/FacebookClientTest.java
git commit -m "Facebook投稿を自動トークン更新対応へ切替"
```

### Task 7: 監視・運用ログ・設定例を整備する

**Files:**
- Modify: `ops/config/live/tomcat10/qmaclone-admin.properties`
- Modify: `docs/plans/2026-03-02-facebook-access-token-auto-refresh-design.md`
- Create: `ops/notes/2026-03-02-facebook-token-operations.md`

**Step 1: 追記内容を作成する**

設定キーの説明、再認可手順、障害時確認コマンドを明記する。

**Step 2: ローカル検証を実行する**

Run:
```powershell
rg --line-number -S "facebook_" ops/config/live/tomcat10/qmaclone-admin.properties
```
Expected: 必須キーが揃っている

**Step 3: コミットする**

```powershell
git add ops/config/live/tomcat10/qmaclone-admin.properties docs/plans/2026-03-02-facebook-access-token-auto-refresh-design.md ops/notes/2026-03-02-facebook-token-operations.md
git commit -m "Facebookトークン自動更新の運用手順を整備"
```

### Task 8: 総合検証（build -> test -> gwt:compile -> ローカル配備）

**Files:**
- Create: `ops/log/2026-03-02-facebook-token-auto-refresh-verification.log`

**Step 1: ビルド**

Run:
```powershell
mvn -DskipTests compile
```
Expected: PASS

**Step 2: テスト**

Run:
```powershell
mvn "-Dsurefire.skip=false" test
```
Expected: PASS（既知除外以外）

**Step 3: GWTコンパイル**

Run:
```powershell
mvn "-Dgwt.skipCompilation=false" gwt:compile
```
Expected: PASS

**Step 4: ローカル配備**

Run:
```powershell
powershell -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat10.ps1
curl.exe -I http://localhost:8080/QMAClone/
curl.exe -I http://localhost:8080/QMAClone-1.0-SNAPSHOT/
```
Expected: `/QMAClone/` は 200、`/QMAClone-1.0-SNAPSHOT/` は 404

**Step 5: 検証ログ保存とコミット**

```powershell
git add ops/log/2026-03-02-facebook-token-auto-refresh-verification.log
git commit -m "Facebookトークン自動更新の総合検証ログを追加"
```
