# client テスト移行バックログ分類 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `client` 配下のテストバックログを `移行済み / GWT依存 / 特殊互換` に分類し、運用可能な管理状態にする。

**Architecture:** ソースコードは変更せず、分類結果と運用ルールを設計ドキュメントへ反映する。集計はローカルコマンドで再現可能にする。

**Tech Stack:** PowerShell, ripgrep, Maven

---

### Task 1: バックログ分類を再集計する

**Files:**
- No file changes

**Step 1: 対象テスト一覧を取得**
Run: `Get-ChildItem src/test/java/tv/dyndns/kishibe/qmaclone/client -Recurse -Filter *Test.java`
Expected: client 配下のテストファイル一覧が取得できる

**Step 2: 3分類の件数を集計**
- `extends QMACloneGWTTestCaseBase` -> `GWT依存`
- `org.junit(非jupiter) / @RunWith` -> `通常Jupiter化可能`（特殊互換候補を含む）
- それ以外 -> `移行済み`

**Step 3: ディレクトリ別件数を集計**
Expected: 主要ディレクトリごとの件数分布を取得

### Task 2: 設計ドキュメントへ分類結果を反映する

**Files:**
- Modify: `docs/plans/2026-02-11-client-test-migration-backlog-classification-design.md`

**Step 1: 集計結果を追記**
- 総件数と3分類件数を追記
- ディレクトリ別サマリを更新
- `ValidatorStressTest` を `特殊互換` に確定した旨を記録

**Step 2: 運用テンプレートを明記**
- `build -> target test -> 状態更新` の運用テンプレートを手順化

### Task 3: 検証してコミットする

**Files:**
- Modify: `docs/plans/2026-02-11-client-test-migration-backlog-classification-design.md`

**Step 1: build を実行する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 2: 差分確認**
Run: `git status --short`
Expected: 設計ドキュメントのみ変更

**Step 3: コミット**
```bash
git add docs/plans/2026-02-11-client-test-migration-backlog-classification-design.md
git commit -m "clientテスト移行バックログ分類結果を反映"
```

### Task 4: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/client-test-migration-backlog-classification-exec
```

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/client-test-migration-backlog-classification-exec
git branch -d feature/client-test-migration-backlog-classification-exec
```
