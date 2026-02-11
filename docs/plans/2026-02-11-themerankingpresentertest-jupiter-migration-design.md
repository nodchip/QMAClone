# ThemeRankingPresenterTest Jupiter移行 設計

## 概要
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/ThemeRankingPresenterTest.java` のみ。
- 目的は、既存の検証意図を維持したまま JUnit4 依存を JUnit5（Jupiter）へ移行すること。
- 変更は最小差分に限定する。

## 背景
- `client/ranking` では主要テストの Jupiter 移行を進めており、残件として `ThemeRankingPresenterTest` を処理する。
- 1ファイル単位で移行し、失敗時の切り分けを容易にする。

## 対象範囲
- 変更対象:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/ThemeRankingPresenterTest.java`
- 非対象:
  - 本番コード
  - 他テスト
  - `pom.xml` などビルド設定

## 設計方針
- JUnit4 ランナー依存を除去し、Jupiter アノテーションへ置換する。
- Mockito 初期化は `MockitoAnnotations.openMocks(this)` を採用する。
- RPC 呼び出し検証・View 反映検証のロジックは変更しない。

## 変更方針（詳細）
1. import / annotation 置換
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `@RunWith(MockitoJUnitRunner.class)` を削除

2. Mockito 初期化
- `AutoCloseable` フィールドを追加
- `setUp()` で `MockitoAnnotations.openMocks(this)` を実行
- `@AfterEach` で `closeableMocks.close()` を実行

3. ロジック不変
- `onOldSelected` / `onAllSelected` / `onYearSelected` / `onMonthSelected` の RPC 引数検証を維持
- `callbackGetThemeRanking` の `setRanking` 検証を維持

## データフローと検証観点
- `onThemeSelected` 後の期間選択メソッド呼び出しに対する RPC 呼び出し分岐を検証。
- コールバック成功時の View 反映を検証。
- 観点は移行前後で同一。

## リスクと対策
- リスク: Mockito 初期化漏れによる NPE。
- 対策: `openMocks` + `@AfterEach` close を実装し、対象テスト単体実行で確認。
- リスク: Java 25 環境で Mockito 実行警告。
- 対策: 必要に応じて `-DargLine=-Dnet.bytebuddy.experimental=true` を付与して実行。

## 完了条件
- 当該テストが Jupiter でコンパイル・実行できる。
- 既存検証（RPC呼び出し・View反映）が維持される。
- 変更ファイルが対象テスト1ファイルに限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -DargLine=-Dnet.bytebuddy.experimental=true -Dtest=tv.dyndns.kishibe.qmaclone.client.ranking.ThemeRankingPresenterTest test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。