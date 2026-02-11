# client/setting/theme Jupiter検証 Design

## 背景

`client/setting/theme` 配下の次の4テストは、すでに Jupiter 形式になっている。

- `ThemeCellTest`
- `ThemeProviderTest`
- `ThemeQueryCellTest`
- `ThemeQueryProviderTest`

今回の目的は実装変更ではなく、4テストを一括検証して「Jupiter 移行完了」の証跡を残すことである。

## 方針

### 採用案（A案）

- コード変更は行わず、`build -> 対象4テスト実行` を直列で実施する。
- 実行コマンドと結果を文書化し、完了状態を明示する。

### 不採用案

- B案: 検証に加えて import 形式統一など軽微整形を同時実施する。
- C案: `theme` 以外の隣接領域も混ぜた横断バッチ検証にする。

B/C は今回の目的（完了証跡化）に対して不要な変更やスコープ拡大を招くため採用しない。

## アーキテクチャとデータフロー

### 対象

- テスト実行フローのみ
- 本番コード変更なし

### 実行順序（直列）

1. `mvn -DskipTests package`
2. `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=... test`（theme の4テストのみ）

## エラーハンドリング

### Build failure

- 依存・コンパイル異常を優先修正する。

### Test failure

- 失敗テスト名と失敗種別を確認し、対象テストを個別再実行して切り分ける。

### 対象漏れ

- `-Dtest` のクラス名指定ミスを確認する。

## 完了条件

- `mvn -DskipTests package` が成功する。
- theme 4テスト明示実行が成功する。
- 実行コマンドと結果要約を文書に追記し、1目的コミットで反映する。

## 影響範囲

- 本番コードへの影響なし。
- 変更対象は設計/計画ドキュメントのみ。
