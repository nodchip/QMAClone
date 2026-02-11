# JUnit4 から JUnit5 への高速移行実装計画（A案）

## 前提
- 対象は「非 GWT テストの Jupiter 化を先行」。
- GWT 依存テストは `gwt:test` で暫定維持する。
- 検証は必ず `build -> test` を直列実行する。

## 実施ステップ
1. 実行基盤の固定
   - `pom.xml` のテスト実行方針を確認する。
   - 非 GWT は Surefire、GWT は `gwt:test` を利用する運用を明文化する。

2. 非 GWT テストの機械的変換（第一波）
   - 対象条件:
     - `import org.junit.Test;` を使用
     - `@RunWith` / `@Rule` / `ExpectedException` なし
     - `QMACloneGWTTestCaseBase` 非継承
   - 変換内容:
     - `org.junit.Test` -> `org.junit.jupiter.api.Test`
     - `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
     - `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`

3. 検証
   - `mvn -DskipTests package`
   - 変換対象テストのピンポイント実行（Surefire）
   - 必要に応じて `mvn test` で回帰確認

4. 非機械変換テストの別バッチ化
   - `@RunWith` / `@Rule` 使用テストを一覧化し、個別計画へ切り出す。

## 今回の初回実装スコープ
- 第一波として以下を Jupiter 化する。
  - `ProblemCorrectCounterResetCounterTest`
  - `ChatPostCounterTest`
  - `ProblemIndicationCounterTest`
  - `DatabaseSchemaMigratorTest`

## リスクと緩和
- リスク: 断続的に JUnit4 API が残る。
  - 緩和: 変換バッチごとに `rg` で残存 API を可視化する。
- リスク: GWT 側を誤って壊す。
  - 緩和: GWT 依存テストは今回触らない。
