# ProblemGenre / ProblemType Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `ProblemGenreTest` と `ProblemTypeTest` を JUnit4 から JUnit5 (Jupiter) へ移行し、既存の判定ロジック検証を維持する。

**Architecture:** JUnit4 の import/annotation/runner を Jupiter へ最小差分で置換する。`ProblemTypeTest` の `@Ignore` は `@Disabled` へ置換する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter

---

### Task 1: 2テストを Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/ProblemGenreTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/ProblemTypeTest.java`

**Step 1: JUnit4依存を置換する**
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `@RunWith(JUnit4.class)` と関連 import を削除
- `@Ignore` -> `@Disabled`

**Step 2: テストロジックを維持する**
- 問題データ、期待値、判定ロジックを変更しない。

**Step 3: build を実行する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象2件を実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.game.ProblemGenreTest,tv.dyndns.kishibe.qmaclone.client.game.ProblemTypeTest test`
Expected: failures 0

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: 対象2ファイルのみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/game/ProblemGenreTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/ProblemTypeTest.java
git commit -m "ProblemGenreとProblemTypeテストをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/problemgenre-problemtype-jupiter-migration-exec
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/problemgenre-problemtype-jupiter-migration-exec
git branch -d feature/problemgenre-problemtype-jupiter-migration-exec
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン
