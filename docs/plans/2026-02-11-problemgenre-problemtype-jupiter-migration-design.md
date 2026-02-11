# ProblemGenre / ProblemType Jupiter移行 Design

## 背景

`client/game` 配下で通常 JUnit4 形式のテストとして、次の2件が残っている。

- `ProblemGenreTest`
- `ProblemTypeTest`

`ProblemTypeTest` には `@Ignore` が含まれており、Jupiter では `@Disabled` へ置換が必要である。

## 方針

### 採用案（A案）

- 2件を同一バッチで JUnit4 から Jupiter へ移行する。
- 変更は import / annotation / runner 除去に限定する。
- テストロジック（期待値、比較対象、データ）は変更しない。

### 不採用案

- B案: 1件ずつ分割移行する。
- C案: 移行と同時にテスト構造のリファクタを実施する。

B は手数が増え、C はスコープ逸脱となるため採用しない。

## アーキテクチャとデータフロー

### 変更対象

- `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/ProblemGenreTest.java`
- `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/ProblemTypeTest.java`

### 置換ルール

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `@RunWith(JUnit4.class)` と関連 import を削除
- `@Ignore` -> `@Disabled`

### 実行順序（直列）

1. 2テストを置換
2. `mvn -DskipTests package`
3. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=... test`（対象2件）

## エラーハンドリング

### Build failure

- import 漏れ、runner 削除漏れ、`@Disabled` import 漏れを確認する。

### Test failure

- 置換差分（static import / annotation）を優先確認する。

### 想定外要因

- `-Dtest` の対象漏れ・クラス名 typo を確認する。

## 完了条件

- `mvn -DskipTests package` 成功
- 対象2テスト実行成功（failures/errors 0）
- 差分が対象2ファイルのみ
- 1目的コミット（日本語メッセージ）
- `master` へ fast-forward 反映と作業ブランチ整理

## 影響範囲

- 本番コードへの影響なし
- 変更対象はテスト2ファイルと計画ドキュメント
