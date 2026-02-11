# PanelSettingUserCodePresenterTest Jupiter移行 設計

## 概要
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenterTest.java` のみとする。
- 目的は、テスト意図を維持したまま JUnit4 依存を JUnit5（Jupiter）へ移行すること。
- 変更は最小差分とし、Mockito を含む既存の検証ロジックは変更しない。

## 背景
- `client/setting/theme` 配下に続き、`client/setting` 系テストの JUnit5 移行を段階的に進める。
- 1ファイル単位で移行し、失敗時の切り分けコストを抑える。

## 対象範囲
- 変更対象:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenterTest.java`
- 非対象:
  - 本番コード
  - 他テスト
  - `pom.xml` 等のビルド設定

## 設計方針
- JUnit4 ランナー依存を除去し、Jupiter アノテーションへ置換する。
- Mockito 初期化は `MockitoAnnotations.openMocks(this)` を採用する。
- `when/verify/never` を含むテストロジック、期待値、分岐条件は一切変更しない。

## 変更方針（詳細）
1. JUnit import / annotation の置換
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `@RunWith(MockitoJUnitRunner.class)` を削除

2. Mockito 初期化方式の置換
- `setUp()` 内で `MockitoAnnotations.openMocks(this)` を実行する。
- `AutoCloseable` フィールドを保持し、`@AfterEach` で close する。

3. テストロジックの不変性維持
- 既存のテストメソッド名、`when` 設定、`verify` 条件、コールバック参照は変更しない。

## データフローと検証観点
- `onLoad`：認証情報の有無で View 表示と RPC 呼び出し分岐を検証。
- `connect/showUserCodeList`：認可後の連携・一覧取得分岐を検証。
- `switch/disconnect`：ユーザーコード切替・連携解除の UI 更新と RPC 呼び出しを検証。
- これらの観点は移行前後で同一。

## リスクと対策
- リスク: Mockito 初期化漏れによる NPE。
- 対策: `openMocks` + `@AfterEach` close を実装し、対象テストを単体実行して確認する。
- リスク: 環境依存で Mockito 実行が不安定。
- 対策: 必要に応じて `-DargLine=-Dnet.bytebuddy.experimental=true` を付与して実行する。

## 完了条件
- 当該テストが Jupiter でコンパイル・実行できる。
- 既存の検証意図（View 更新、RPC 呼び出し、分岐）が維持される。
- 変更ファイルが対象テスト1ファイルに限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingUserCodePresenterTest -DargLine=-Dnet.bytebuddy.experimental=true test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。