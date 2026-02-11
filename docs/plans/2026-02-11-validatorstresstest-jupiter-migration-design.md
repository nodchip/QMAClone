# ValidatorStressTest Jupiter移行 Design

## 背景

`ValidatorStressTest` は JUnit4 依存に加え、`@Rule`（GuiceBerry）を使用する特殊テストである。

- 対象: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorStressTest.java`

今回の目的は、最小差分で Jupiter 移行を試み、成功または除外判断を確定させること。

## 方針

### 採用案（A案）

- JUnit4 -> Jupiter の最小差分置換を実施する。
- `@Rule`（GuiceBerry）は今回は変更しない。
- テストロジック、データ、期待値は変更しない。

### 不採用案

- B案: GuiceBerry を Jupiter Extension へ同時移行する。
- C案: 先に実行確認のみ行い実装を保留する。

B は設計スコープが大きく、C は進捗が固定化しないため採用しない。

## アーキテクチャとデータフロー

### 変更対象

- `ValidatorStressTest` 1ファイルのみ
- 本番コード変更なし

### 置換ルール

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `@RunWith(JUnit4.class)` と関連 import を削除
- `@Ignore` -> `@Disabled`（該当時）
- `@Rule` は変更しない（判定対象）

### 実行順序（直列）

1. 1ファイル置換
2. `mvn -DskipTests package`
3. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=...ValidatorStressTest test`
4. 成否判定（成功 or 除外判断）

## エラーハンドリング

### 置換ミス

- import 漏れ、annotation 置換漏れ、assert import 不整合を確認する。

### フレームワーク互換性

- `@Rule` 非対応、GuiceBerry 依存、Java 25 環境依存を切り分ける。

### 判定基準

- 置換ミスは修正して再実行する。
- 互換性要因が支配的なら、Jupiter 直移行対象外として legacy 管理へ切替える。

## 完了条件

- 成功パス:
  - `mvn -DskipTests package` 成功
  - `ValidatorStressTest` 実行成功（failures/errors 0）
  - 対象1ファイルのみ差分
  - 1目的コミットで `master` へ反映

- 除外確定パス:
  - 互換性エラーを再現
  - 置換ミスでないことを確認
  - 除外理由と再着手条件を文書化
  - 文書コミットで `master` へ反映

## 影響範囲

- 本番コードへの影響なし
- 変更対象はテスト1ファイルまたは計画ドキュメント
