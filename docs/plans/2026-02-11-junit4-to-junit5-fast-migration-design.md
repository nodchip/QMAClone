# JUnit4 から JUnit5 への高速移行設計（A案）

## 背景
- テスト資産は JUnit4 / JUnit5 が混在している。
- GWT 依存テストは `GWTTestCase` 実行基盤の制約があるため、非 GWT テストと同時に完全移行すると失敗時の切り分けコストが高い。
- 速度優先で移行を進めるため、実行レーンを分離する。

## 方針（A案）
- 非 GWT テストを先に JUnit5（Jupiter）へ寄せる。
- GWT 依存テストは当面 `gwt:test` + Vintage 互換で維持する。
- 失敗時の原因領域を以下で明確に分離する。
  - 非 GWT: `maven-surefire-plugin`
  - GWT: `gwt-maven-plugin:test`

## 構成
- レーン1: 非 GWT（Jupiter 主系）
  - `org.junit.Test` / `Assert` / JUnit4 ライフサイクル注釈を JUnit5 API に統一する。
  - 変換は機械的変更を優先し、`@RunWith` / `@Rule` を使うテストは別バッチで扱う。
- レーン2: GWT（暫定維持）
  - `QMACloneGWTTestCaseBase` 継承テストは当面現状運用。
  - Java 25 での実行安定化オプションは `pom.xml` で共通化する。

## データフロー
1. テスト分類（非 GWT / GWT）
2. 非 GWT の一括変換
3. `build -> test` を直列実行して検証
4. 失敗を分類して再実行（機械的変換漏れ / ランナー依存 / GWT 依存）

## エラーハンドリング
- 移行中に失敗したら、対象ファイル群を最小単位で戻せるようにコミットを小分けにする。
- `@RunWith` / `@Rule` を含むケースは無理に同時変換せず、別タスクへ切り出す。
- GWT 実行失敗は非 GWT と切り離して扱い、同一コミットで混在させない。

## 完了条件
- 非 GWT テストで JUnit4 API 使用を段階的に削減できていること。
- `build -> test` の直列実行結果を記録できること。
- GWT レーンを破壊せずに移行進捗を積み上げられること。
