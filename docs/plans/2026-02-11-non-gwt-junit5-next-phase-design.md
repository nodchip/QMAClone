# 非GWTサーバーテスト JUnit5移行 次フェーズ設計

- 日付: 2026-02-11
- 対象: `src/test/java/tv/dyndns/kishibe/qmaclone/server` 配下の `@Rule + GuiceBerryRule` 依存テスト
- 方針: Vintage/Jupiter 共存を前提に、移行可能テストのみ段階的に Jupiter 化する

## 目的

非GWTサーバーテストの JUnit5 移行を、実行安定性を維持しながら前進させる。全面移行は目標にしない。`GuiceBerry` 依存が強いテストは Vintage 維持を正式運用とし、Jupiter 化できるテストを継続的に増やす。

## 分類アーキテクチャ

テストを次の3区分で管理する。

1. `移行可 (Jupiter)`
- `@Inject` 中心で GuiceBerry 固有ライフサイクル依存が薄い
- 外部依存（DB/native/ネットワーク）が小さい

2. `保留 (Vintage)`
- 既存挙動を維持すれば安定実行できる
- 直ちに Jupiter 化する合理性が低い

3. `要Extension`
- GuiceBerry が担う前後処理、注入、リソース管理を Extension で再現する必要がある

分類は以下4軸で機械的に判断する。
- 注入方式: `@Inject` のみで足りるか
- 外部依存: DB / native (`zinnia.dll`) / ネットワーク
- 実行安定性: Mockito strictness、乱数、時刻依存
- 保守性: 実行時間、失敗時の切り分け難度

## 実行戦略

実行経路を分離する。

- 第1層: Jupiter 対象
- 第2層: Vintage 維持対象（保留 + 要Extension）

`要Extension` は実装完了まで Vintage 固定とする。保留は先送りではなく運用判断として明示し、Jupiter 移行トリガー（例: Extension 完了、native 非依存化）を定義する。

## 検証フロー

検証は直列で実施する。

1. `compile`
2. `test-compile`
3. Jupiter 対象テスト群
4. Vintage 対象テスト群

完了報告には各段階のコマンドと結果（成功/失敗理由）を記録する。

## 次回実装用タスク分割（チケット粒度）

1. 分類表作成
- 入力: `server` 配下の `@Rule + GuiceBerryRule` テスト一覧
- 出力: `移行可 / 保留 / 要Extension` と理由、次アクション
- Done: 全対象テストが1行ずつ分類されている

2. Jupiter 即時移行セット（上位10件）
- 入力: 分類表の `移行可`
- 出力: 優先度付き候補10件
- Done: 実装順と検証順が確定

3. Extension 要件定義
- 入力: `要Extension` 一覧
- 出力: 必要責務（注入/前後処理/リソース管理）の仕様
- Done: 実装可否判断が可能

4. Vintage 固定運用整備
- 入力: `保留` と `要Extension`
- 出力: 継続条件、移行トリガー、運用ルール
- Done: 共存運用で判断ぶれがない

5. ネイティブ依存切り分け
- 入力: `zinnia.dll` など依存テスト
- 出力: 移行判定から分離した検証手順
- Done: JUnit移行の判定に混入しない
