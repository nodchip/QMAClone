# client accuracyrate/geom/constant テスト Jupiter移行 設計

## 概要
- 対象は以下6ファイル。
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/accuracyrate/AccuracyRateNormalizerDefaultTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/accuracyrate/AccuracyRateNormalizerMarubatsuTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/accuracyrate/AccuracyRateNormalizerYontakuTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/geom/PointTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/geom/PolygonTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java`
- 目的は、JUnit4 依存を JUnit5（Jupiter）へ最小差分で移行すること。
- テストロジック・期待値は変更しない。

## 背景
- 作業効率向上のため、依存が薄いロジック系テストを大バッチ化して移行する。
- 切り分けしやすさを維持するため、グループ順に編集・検証する。

## 対象範囲
- 変更対象: 上記6ファイル
- 非対象:
  - 本番コード
  - 他テスト
  - アサーションロジックの意味変更

## 設計方針
- 最小変更を徹底し、JUnit4 import / annotation / Assert を Jupiter に置換する。
- Mockito 利用がある場合のみ `openMocks` 方式へ置換する（本バッチ対象では原則不要想定）。
- テスト本文・データ・期待値は不変。

## 変更方針（詳細）
1. 共通置換
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`（存在する場合）
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`（必要分のみ）
- `@RunWith(...)` を削除（存在する場合）

2. グループ順
- 先に `accuracyrate` 3件
- 次に `geom` 2件
- 最後に `constant` 1件

## データフローと検証観点
- `accuracyrate`: 入力に対する正規化結果を検証。
- `geom`: 幾何計算・座標関連の期待値を検証。
- `constant`: 定数契約の整合を検証。
- いずれも観点は移行前後で同一。

## リスクと対策
- リスク: 大バッチで失敗時の切り分けが難化。
- 対策: グループ単位で段階確認し、差分を最小化。
- リスク: `gwt:test` の 0件実行失敗。
- 対策: 対象テスト実行に `-DfailIfNoTests=false` を付与。

## 完了条件
- 対象6ファイルが Jupiter 化される。
- `build -> test` が成功する。
- 変更が対象範囲に限定される。

## 検証手順
1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizerDefaultTest,tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizerMarubatsuTest,tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizerYontakuTest,tv.dyndns.kishibe.qmaclone.client.geom.PointTest,tv.dyndns.kishibe.qmaclone.client.geom.PolygonTest,tv.dyndns.kishibe.qmaclone.client.constant.ConstantTest test`

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。