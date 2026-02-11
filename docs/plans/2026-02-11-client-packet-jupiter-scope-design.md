# client/packet Jupiter移行スコープ整理 Design

## 背景

`client/packet` 配下のテストを確認した結果、以下の構成である。

- `PacketProblemTest`: JUnit Jupiter 移行済み
- `PacketRoomKeyTest`: JUnit Jupiter 移行済み
- `PacketPlayerSummaryTest`: `QMACloneGWTTestCaseBase` 継承の GWT テスト

今回の目的は、Jupiter 移行作業を安定して前進させることであり、GWT 依存テストの実行基盤調整まで同時に含めない。

## 方針

### 採用案（A案）

- `client/packet` の通常テストは「Jupiter 移行済み」として完了扱いにする。
- `PacketPlayerSummaryTest` は「GWT legacy テスト」として明示的に除外管理する。

### 不採用案

- B案: `PacketPlayerSummaryTest` を疑似ユニットテスト化して Jupiter へ寄せる。
- C案: GWT テストランナー統合まで含めて一括移行する。

B/C 案は、今回スコープに対して設計・実装コストと不確実性が高い。

## アーキテクチャとデータフロー

### 実行レーン分離

- Jupiter レーン: Surefire で通常テストを実行する。
- GWT legacy レーン: GWT 基盤テストとして別管理する。

### 実行順序（直列）

1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=... test`

今回の `-Dtest` には `PacketPlayerSummaryTest` を含めない。

## エラーハンドリング

### Build failure

- import 不整合、エンコーディング異常、API 差異を優先確認する。

### Surefire failure

- テスト対象フィルタ、クラス名、検出ルールを確認する。

### GWT failure混入

- 実行対象に GWT テストが混入していないかを最優先で確認する。

## 完了条件

- `client/packet` の通常テストが Jupiter 前提で成功すること。
- `PacketPlayerSummaryTest` の除外理由と再着手条件が文書化されること。
- `build -> test` の実行コマンドと結果を残すこと。

## 再着手条件（GWT側）

`PacketPlayerSummaryTest` は次の条件で再着手する。

- GWT テスト基盤を刷新する計画が承認されたとき。
- GWT 依存テストの移行方針（Jupiter 化または別ランナー維持）が確定したとき。

## 影響範囲

- 本設計では本番コード変更なし。
- 変更対象は設計文書のみ。
