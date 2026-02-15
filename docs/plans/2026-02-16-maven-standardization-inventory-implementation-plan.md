# Maven Standardization Inventory Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `src` 配下の非Java資産を拡張子ベースで棚卸しし、2分類（今すぐ移動可 / 要検証）と推奨移動先を含む移動計画を作成する。

**Architecture:** PowerShell で `src` を走査し、生成物ディレクトリを除外して拡張子ごとに集計する。集計結果から代表パスを抽出し、移動判定ルールを適用して結果ドキュメントにまとめる。成果物は `docs/plans` 配下の結果レポートとして保存し、検証は `mvn compile` で行う。

**Tech Stack:** PowerShell, ripgrep, Maven, Markdown

---

### Task 1: 棚卸しスクリプト相当処理の実行

**Files:**
- Modify: `docs/plans/2026-02-16-maven-standardization-inventory-implementation-plan.md`
- Create: `docs/plans/2026-02-16-maven-standardization-inventory-results.md`

**Step 1: 棚卸し対象を確認**

Run: `Test-Path src`
Expected: `True`

**Step 2: 拡張子別集計を実行**

Run: PowerShell で `src` 配下を再帰走査し、`src/main/gwt-unitCache` を除外して拡張子別に件数を集計
Expected: 拡張子ごとの件数が取得できる

**Step 3: 代表パスを抽出**

Run: 各拡張子ごとに先頭3〜5件の相対パスを収集
Expected: 各拡張子に対応した代表パス一覧が得られる

### Task 2: 移動計画レポート作成

**Files:**
- Create: `docs/plans/2026-02-16-maven-standardization-inventory-results.md`

**Step 1: 判定基準を適用**

Run: 拡張子単位で `今すぐ移動可` / `要検証` を付与
Expected: 全拡張子に判定カテゴリが付与される

**Step 2: 推奨移動先を記載**

Run: `src/main/resources` / `src/test/resources` の推奨先を拡張子単位で付与
Expected: 各カテゴリに移動先候補が明示される

**Step 3: レポート保存**

Run: Markdown として保存
Expected: 棚卸し結果と移動計画が1ファイルで参照可能になる

### Task 3: 検証とコミット

**Files:**
- Modify: `docs/plans/2026-02-16-maven-standardization-inventory-implementation-plan.md`
- Create: `docs/plans/2026-02-16-maven-standardization-inventory-results.md`

**Step 1: ビルド検証**

Run: `mvn compile`
Expected: `BUILD SUCCESS`

**Step 2: 差分確認**

Run: `git status --short` と `git diff -- docs/plans/...`
Expected: 目的ファイルのみ差分が存在

**Step 3: コミット**

Run:
`git add docs/plans/2026-02-16-maven-standardization-inventory-implementation-plan.md docs/plans/2026-02-16-maven-standardization-inventory-results.md`
`git commit -m "Maven標準化向け棚卸し結果と移動計画を追加"`
Expected: 1コミット1目的で記録される
