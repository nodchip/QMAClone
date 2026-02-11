# client/game/judge テスト Jupiter移行 設計

## 概要
- 対象は以下6ファイル。
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeClickTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeDefaultTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeJunbanTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeSenmusubiTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeSlotTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeTatoTest.java`
- 目的は、JUnit4 依存を JUnit5（Jupiter）へ中バッチで移行し、既存検証意図を維持すること。

## 背景
- `client/game` の残件を効率よく削減するため、同質な `judge` 群をバッチ化する。
- 判定ロジック系テストは切り分けしやすく、バッチ移行に適している。

## 対象範囲
- 変更対象: 上記6ファイル
- 非対象:
  - 本番コード
  - 他ディレクトリのテスト
  - テストロジック変更

## 設計方針
- 最小変更を徹底し、JUnit4 import/annotation/assert を Jupiter へ置換する。
- テスト本文、期待値、入力データは変更しない。
- Mockito 利用がある場合のみ `openMocks` 方式へ置換する。

## 変更方針（詳細）
1. 共通置換
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`（存在する場合）
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `@RunWith(...)` 削除（存在する場合）

2. ロジック不変
- 各問題形式の判定期待値を維持する。

## データフローと検証観点
- 問題/回答データ準備 -> Judge呼び出し -> 正誤判定検証。
- 観点は移行前後で同一。

## リスクと対策
- リスク: 一括置換で一部テストのみ失敗。
- 対策: 対象6件を限定実行し、影響範囲を固定。
- リスク: `gwt:test` 0件実行失敗。
- 対策: `-DfailIfNoTests=false` を付与。

## 完了条件
- 対象6ファイルが Jupiter 化される。
- `build -> 対象6件テスト実行` が成功する。
- 変更が対象範囲に限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeClickTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeDefaultTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeJunbanTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeSenmusubiTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeSlotTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeTatoTest test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。