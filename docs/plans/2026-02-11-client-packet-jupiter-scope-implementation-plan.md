# client/packet Jupiter移行スコープ整理 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `client/packet` の通常テストが Jupiter 実行対象として維持されることを確認し、`PacketPlayerSummaryTest` を GWT legacy 除外対象として文書化した状態で完了させる。

**Architecture:** Jupiter 実行レーンと GWT legacy レーンを分離する。今回の実装は「検証と記録」を対象とし、テストコード本体は変更しない。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter, GWT TestCase

---

### Task 1: client/packet の Jupiter 対象を検証する

**Files:**
- No file changes

**Step 1: build を実行する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 2: packet の Jupiter 対象のみ実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemTest,tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKeyTest test`
Expected: failures 0

**Step 3: 除外対象の現状を確認する**
Run: `rg -n "extends QMACloneGWTTestCaseBase" src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketPlayerSummaryTest.java`
Expected: `QMACloneGWTTestCaseBase` 継承であることを確認

### Task 2: スコープ整理結果を反映してコミットする

**Files:**
- Modify: `docs/plans/2026-02-11-client-packet-jupiter-scope-design.md`

**Step 1: 設計書に検証結果を追記する**
- 実行コマンドと結果（build success / packet 2テスト success）を追記する。
- `PacketPlayerSummaryTest` を GWT legacy として除外中である旨を再明記する。

**Step 2: 差分確認とコミット**
Run: `git status --short`
Expected: 設計書のみ変更

Commit:
```bash
git add docs/plans/2026-02-11-client-packet-jupiter-scope-design.md
git commit -m "client/packetのJupiter移行スコープ検証結果を反映"
```

### Task 3: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/client-packet-jupiter-scope-exec
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/client-packet-jupiter-scope-exec
git branch -d feature/client-packet-jupiter-scope-exec
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン
