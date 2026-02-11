# client/game/judge テスト Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `client/game/judge` の6テストを JUnit4 から JUnit5 (Jupiter) へ移行し、既存の判定検証を維持する。

**Architecture:** JUnit4 の import/annotation/assert を Jupiter へ最小差分で置換する。テストロジックは変更しない。対象6件をまとめて検証する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter

---

### Task 1: judge 6テストを Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeClickTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeDefaultTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeJunbanTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeSenmusubiTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeSlotTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeTatoTest.java`

**Step 1: JUnit4依存を置換する**
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Before` / `@RunWith` / `org.junit.Assert.*` があれば Jupiter へ置換

**Step 2: テストロジックは変更しない**
- 問題データ、期待値、判定結果検証を維持する。

**Step 3: build を実行する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象6件を実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeClickTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeDefaultTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeJunbanTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeSenmusubiTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeSlotTest,tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeTatoTest test`
Expected: failures 0

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: 対象6ファイルのみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeClickTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeDefaultTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeJunbanTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeSenmusubiTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeSlotTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/judge/JudgeTatoTest.java
git commit -m "client/game/judgeテストをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/client-game-judge-jupiter-migration
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/client-game-judge-jupiter-migration
git branch -d feature/client-game-judge-jupiter-migration
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン