# client/setting/theme Jupiter検証 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `client/setting/theme` の4テストを一括検証し、Jupiter 移行完了の証跡を設計書へ反映する。

**Architecture:** コード変更は行わず、`build -> 対象4テスト実行` の直列検証を実施して設計文書に結果を追記する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter

---

### Task 1: theme 4テストの検証を実施する

**Files:**
- No file changes

**Step 1: build を実行する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 2: theme 4テストを明示実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeCellTest,tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeProviderTest,tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryCellTest,tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryProviderTest test`
Expected: failures 0

### Task 2: 検証結果を設計書に反映する

**Files:**
- Modify: `docs/plans/2026-02-11-client-setting-theme-jupiter-verification-design.md`

**Step 1: 実行結果を追記する**
- 実行コマンドと結果（tests run / failures / errors）を追記する。

**Step 2: 差分確認とコミット**
Run: `git status --short`
Expected: 設計書のみ変更

Commit:
```bash
git add docs/plans/2026-02-11-client-setting-theme-jupiter-verification-design.md
git commit -m "client/setting/themeのJupiter検証結果を反映"
```

### Task 3: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/client-setting-theme-jupiter-verification-exec
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/client-setting-theme-jupiter-verification-exec
git branch -d feature/client-setting-theme-jupiter-verification-exec
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン
