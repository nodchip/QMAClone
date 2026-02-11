# WrongAnswerPresenterTest Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `WrongAnswerPresenterTest` を JUnit4 から JUnit5 (Jupiter) へ移行し、既存のテストロジックを維持する。

**Architecture:** JUnit4 の import/annotation/runner を Jupiter へ最小差分で置換する。Mockito 設定方式の変更は行わない。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter, Mockito

---

### Task 1: WrongAnswerPresenterTest を Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/WrongAnswerPresenterTest.java`

**Step 1: JUnit4依存を置換する**
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Before` -> `org.junit.jupiter.api.BeforeEach`
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `@RunWith(MockitoJUnitRunner.class)` と関連 import を削除

**Step 2: テストロジックを維持する**
- モック対象、期待値、検証ロジックは変更しない。

**Step 3: build を実行する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象テストを実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.creation.WrongAnswerPresenterTest test`
Expected: failures 0

**Step 5: 互換性エラー時の再検証**
Run: `mvn --% -Dnet.bytebuddy.experimental=true -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.creation.WrongAnswerPresenterTest test`
Expected: Java 25 + Mockito/ByteBuddy の互換性要因を切り分け

**Step 6: 差分確認とコミット**
Run: `git status --short`
Expected: 対象1ファイルのみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/WrongAnswerPresenterTest.java
git commit -m "WrongAnswerPresenterTestをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/wronganswerpresenter-jupiter-migration-exec
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/wronganswerpresenter-jupiter-migration-exec
git branch -d feature/wronganswerpresenter-jupiter-migration-exec
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン
