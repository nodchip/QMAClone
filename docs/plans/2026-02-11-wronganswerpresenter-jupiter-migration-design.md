# WrongAnswerPresenterTest Jupiter移行 Design

## 背景

`WrongAnswerPresenterTest` は JUnit4 形式で記述されており、通常の Jupiter 移行対象である。

- 対象: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/WrongAnswerPresenterTest.java`

今回の目的は最小差分で JUnit4 依存を排除し、既存のテストロジックを維持したまま Jupiter へ移行すること。

## 方針

### 採用案（A案）

- JUnit4 -> Jupiter 置換のみ実施する。
- Mockito の運用方式（extension 化など）は変更しない。
- テストロジック、期待値、検証内容は変更しない。

### 不採用案

- B案: 移行と同時に Mockito extension 化まで行う。
- C案: まず検証だけ行い、実装は保留する。

B は1目的コミットを崩し、C は今回のスコープに対して前進が小さいため採用しない。

## アーキテクチャとデータフロー

### 変更対象

- `WrongAnswerPresenterTest` 1ファイルのみ
- 本番コード変更なし

### 置換ルール

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `@RunWith(MockitoJUnitRunner.class)` と関連 import を削除

### 実行順序（直列）

1. テスト1ファイルを置換
2. `mvn -DskipTests package`
3. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=...WrongAnswerPresenterTest test`

## エラーハンドリング

### Build failure

- import 漏れ、annotation 置換漏れを確認する。

### Test failure

- 置換差分（Assert import / lifecycle annotation）を優先確認する。

### 環境互換性 failure

- Java 25 + Mockito/ByteBuddy 起因の可能性を確認し、必要時は `-Dnet.bytebuddy.experimental=true` を用いた再検証結果を記録する。

## 完了条件

- `mvn -DskipTests package` 成功
- `WrongAnswerPresenterTest` 実行成功（failures/errors 0）
- 差分が対象1ファイルのみ
- 1目的コミット（日本語メッセージ）
- `master` へ fast-forward 反映と作業ブランチ整理

## 影響範囲

- 本番コードへの影響なし
- 変更対象はテスト1ファイルと計画ドキュメント
