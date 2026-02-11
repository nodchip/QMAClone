# client/game/shuffler Jupiter移行 Design

## 背景

`client/game/shuffler` 配下には、JUnit4 形式のテストが6件残っている。

- `ShufflerDefaultTest`
- `ShufflerJunbanTest`
- `ShufflerMojiPanelTest`
- `ShufflerSenmusubiTest`
- `ShufflerTatoTest`
- `ShufflerYontakuTest`

いずれも `@RunWith(JUnit4.class)` と `org.junit.*` 依存の同型パターンであり、最小差分で Jupiter へ一括移行できる。

## 方針

### 採用案（A案）

- 6件を同時に JUnit4 から Jupiter へ移行する。
- テストロジック（期待値・入力・判定条件）は変更しない。
- 変更は import / annotation / runner 削除に限定する。

### 不採用案

- B案: 3件ずつ2回に分ける。
- C案: 移行と同時に命名や構造リファクタを行う。

B/C は今回の目的（移行完了）に対して、手数増加またはスコープ拡大を招くため採用しない。

## アーキテクチャとデータフロー

### 変更対象

- `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/` の6テストのみ
- 本番コード変更なし

### 置換ルール

- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `@RunWith(JUnit4.class)` と関連 import を削除

### 実行順序（直列）

1. 6テストを置換
2. `mvn -DskipTests package`
3. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=... test`（対象6件）

## エラーハンドリング

### Build failure

- import 置換漏れ、annotation 残存、静的 import 不整合を確認する。

### Test failure

- 置換差分の副作用を優先確認し、ロジック変更を入れない方針を維持する。

### 想定外の環境要因

- 対象外テスト混入を疑い、`-Dtest` 指定対象を再確認する。

## 完了条件

- `mvn -DskipTests package` 成功
- 対象6テスト実行成功（failures/errors 0）
- 変更差分が対象6ファイルのみ
- 1目的コミット（日本語メッセージ）
- `master` へ fast-forward 反映と作業ブランチ整理

## 影響範囲

- 本番コード影響なし
- 変更対象はテスト6ファイルと計画ドキュメント
