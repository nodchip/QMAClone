# PanelSettingRestrictedUserTest Jupiter移行 設計

## 概要
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingRestrictedUserTest.java` のみ。
- 目的は、既存の検証意図を維持したまま JUnit4 依存を JUnit5（Jupiter）へ移行すること。
- 変更は最小差分に限定し、テストロジックは変更しない。

## 背景
- `client/setting` 配下の JUnit5 移行を段階的に進めている。
- 1ファイル単位で移行し、障害時の切り分けを容易にする。

## 対象範囲
- 変更対象:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingRestrictedUserTest.java`
- 非対象:
  - 本番コード
  - 他テスト
  - `pom.xml` などのビルド設定

## 設計方針
- JUnit4 ランナー依存を除去し、Jupiter アノテーションへ置換する。
- Mockito 初期化は `MockitoAnnotations.openMocks(this)` を採用する。
- `spy`、`when`、`verify` の既存検証内容は変更しない。

## 変更方針（詳細）
1. import / annotation 置換
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.assertSame` -> `org.junit.jupiter.api.Assertions.assertSame`
- `@RunWith(MockitoJUnitRunner.class)` を削除

2. Mockito 初期化
- `AutoCloseable` フィールドを追加
- `setUp()` 先頭で `MockitoAnnotations.openMocks(this)` を実行
- `@AfterEach` で `closeableMocks.close()` を実行

3. テストロジック不変
- `setView` / `onTypeChanged` の `update()` 呼び出し検証を維持
- RPC 呼び出し引数検証を維持
- `callbackRestrictedUser` 後の再更新検証を維持

## データフローと検証観点
- 画面操作 -> RPC 呼び出し -> コールバック -> 再更新、の連鎖を検証。
- `getRestrictedUserCodes` -> `getRestrictedRemoteAddresses` の順序的連鎖を検証。
- 上記観点は移行前後で同一。

## リスクと対策
- リスク: Mockito 初期化漏れで NPE。
- 対策: `openMocks` + `@AfterEach` close を適用し、対象テスト単体実行で確認。
- リスク: Java 25 環境で Mockito 実行時警告。
- 対策: `-DargLine=-Dnet.bytebuddy.experimental=true` を付与して実行。

## 完了条件
- 当該テストが Jupiter でコンパイル・実行できる。
- 既存検証（update連鎖、RPC引数、callback後再更新）が維持される。
- 変更ファイルが対象テスト1ファイルに限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -DargLine=-Dnet.bytebuddy.experimental=true -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingRestrictedUserTest test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。