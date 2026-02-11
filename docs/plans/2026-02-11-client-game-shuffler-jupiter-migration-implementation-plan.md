# client/game/shuffler Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `client/game/shuffler` の6テストを JUnit4 から JUnit5 (Jupiter) へ移行し、既存のシャッフル検証を維持する。

**Architecture:** JUnit4 の import/annotation/runner を Jupiter へ最小差分で置換する。テストロジックは変更しない。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter

---

### Task 1: shuffler 6テストを Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerDefaultTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerJunbanTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerMojiPanelTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerSenmusubiTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerTatoTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerYontakuTest.java`

**Step 1: JUnit4依存を置換する**
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `@RunWith(JUnit4.class)` と関連 import を削除

**Step 2: テストロジックを維持する**
- 入力、期待値、判定検証を変更しない。

**Step 3: build を実行する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象6件を実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerDefaultTest,tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerJunbanTest,tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerMojiPanelTest,tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerSenmusubiTest,tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerTatoTest,tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerYontakuTest test`
Expected: failures 0

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: 対象6ファイルのみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerDefaultTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerJunbanTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerMojiPanelTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerSenmusubiTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerTatoTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/shuffler/ShufflerYontakuTest.java
git commit -m "client/game/shufflerテストをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/client-game-shuffler-jupiter-migration-exec
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/client-game-shuffler-jupiter-migration-exec
git branch -d feature/client-game-shuffler-jupiter-migration-exec
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン
