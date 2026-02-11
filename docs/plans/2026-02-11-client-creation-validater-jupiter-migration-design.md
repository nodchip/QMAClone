# client/creation/validater テスト Jupiter移行 設計

## 概要
- 対象は `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater` 配下の未移行19テスト。
- 目的は、JUnit4 依存を JUnit5（Jupiter）へ一括移行し、検証意図を維持すること。
- 効率優先のため一括実装し、検証を段階化して切り分け性を担保する。

## 背景
- 残件削減のため大バッチ方針を採用。
- `validater` 群は同質な入力検証テストが多く、一括移行に適している。

## 対象範囲
- 変更対象: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/*.java` の未移行19件
- 非対象:
  - 本番コード
  - 他ディレクトリのテスト
  - 文字化け修正や振る舞い変更

## 設計方針
- 最小変更を徹底し、JUnit4 import/annotation/assert のみ Jupiter へ置換。
- テスト本文、期待値、入力データは変更しない。
- Mockito 利用がある場合のみ `openMocks` へ置換。

## 変更方針（詳細）
1. 共通置換
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`（存在する場合）
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `@RunWith(...)` を削除（存在する場合）

2. Mockito ありテスト
- `MockitoJUnitRunner` 依存があれば `MockitoAnnotations.openMocks(this)` + `@AfterEach close` に置換

3. 目的外の変更禁止
- ロジック改善や命名変更は行わない

## データフローと検証観点
- 入力作成 -> validator 実行 -> 判定結果検証、の既存フローを維持。
- テスト観点（問題形式ごとの検証ルール）は移行前後で同一。

## リスクと対策
- リスク: 19件一括で失敗時の切り分け負荷が増加。
- 対策: 検証を前半9件 / 後半10件 / 全体の3段階で実行。
- リスク: `gwt:test` 0件失敗。
- 対策: `-DfailIfNoTests=false` を付与。

## 完了条件
- 対象19件が Jupiter 化される。
- `build -> test`（前半/後半/全体）がすべて成功する。
- 変更が対象範囲に限定される。

## 検証手順
1. `mvn -DskipTests package`
2. 前半9件 `-Dtest=...`
3. 後半10件 `-Dtest=...`
4. 19件全体 `-Dtest=...`
（各 test 実行で `-Dsurefire.skip=false -DfailIfNoTests=false` を付与）

## コミット方針
- 1コミット1目的を維持し、設計ドキュメントのみを先にコミットする。