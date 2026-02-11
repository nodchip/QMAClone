# PanelSettingThemeQueryTest Jupiter移行 設計

## 概要
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingThemeQueryTest.java` のみ。
- 目的は、既存の検証意図を維持したまま JUnit4 依存を JUnit5（Jupiter）へ移行すること。
- 変更は最小差分に限定し、テストロジックや期待値は変更しない。

## 背景
- `client/setting` 配下の JUnit5 移行を段階的に進めている。
- 1ファイル単位で移行し、問題発生時の切り分けを容易にする。

## 対象範囲
- 変更対象:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingThemeQueryTest.java`
- 非対象:
  - 本番コード
  - 他テスト
  - `pom.xml` などのビルド設定

## 設計方針
- JUnit4 ランナー依存を除去し、Jupiter アノテーションへ置換する。
- Mockito 初期化は `MockitoAnnotations.openMocks(this)` を採用する。
- `UserData` の初期化順・`verify` 条件・コールバック検証は変更しない。

## 変更方針（詳細）
1. import / annotation 置換
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.After` -> `org.junit.jupiter.api.AfterEach`
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `@RunWith(MockitoJUnitRunner.class)` を削除

2. Mockito 初期化
- `AutoCloseable` フィールドを追加
- `setUp()` で `MockitoAnnotations.openMocks(this)` を実行
- `tearDown()` で `closeableMocks.close()` を実行

3. テストロジック不変
- `commandUpdateForm`、`callbackIsThemeModeEditor`、`onAddButtonClicked` など既存メソッドは変更しない。
- `verify(mockService).isThemeModeEditor(...)` を含む後処理検証は維持する。

## データフローと検証観点
- 権限判定フロー: `isThemeModeEditor` / `isApplyingThemeModeEditor` の分岐を検証。
- 編集フロー: 追加・削除・件数取得RPC引数を検証。
- 画面更新フロー: フォーム状態更新と `Scheduler.scheduleFixedDelay` を検証。
- 上記観点は移行前後で同一。

## リスクと対策
- リスク: Mockito 初期化漏れで NPE。
- 対策: `openMocks` + `@AfterEach` close を適用し、対象テスト単体実行で確認。
- リスク: Java 25 環境で Mockito 実行の警告/不安定性。
- 対策: 必要に応じて `-DargLine=-Dnet.bytebuddy.experimental=true` を付与。

## 完了条件
- 当該テストが Jupiter でコンパイル・実行できる。
- 既存検証（権限分岐、RPC呼び出し、UI更新）が維持される。
- 変更ファイルが対象テスト1ファイルに限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -DargLine=-Dnet.bytebuddy.experimental=true -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingThemeQueryTest test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。