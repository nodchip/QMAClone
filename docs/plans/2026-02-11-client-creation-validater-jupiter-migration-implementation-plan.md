# client/creation/validater テスト Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `client/creation/validater` 配下の未移行16テストを JUnit4 から JUnit5 (Jupiter) へ一括移行し、既存検証を維持する。

**Architecture:** JUnit4 の import/annotation/assert を Jupiter へ最小差分で置換する。テストロジックは変更しない。実装は一括、検証は前半8件/後半8件/全体の3段階で実行する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter

---

### Task 1: validater 16テストを Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/Validator4TakuTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorClickTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorEffectTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorFlashTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorHayaimonoTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorJunbanTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorMarubatsuTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorMojiPanelTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorNarabekaeTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorSenmusubiTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorSlotTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorStressTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorTatoTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorTegakiTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorTypingTest.java`

**Step 1: JUnit4 依存を置換する**
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Before` / `@RunWith` / `org.junit.Assert.*` があれば Jupiter へ置換

**Step 2: テストロジックを維持する**
- 期待値・入力データ・メソッド本体は変更しない。

**Step 3: build 実行**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 段階検証（前半8件）**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.creation.validater.Validator4TakuTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorClickTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorEffectTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorFlashTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorHayaimonoTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorJunbanTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorMarubatsuTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorMojiPanelTest test`
Expected: failures 0

**Step 5: 段階検証（後半8件）**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorNarabekaeTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorSenmusubiTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorSlotTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorStressTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorTatoTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorTegakiTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorTest,tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorTypingTest test`
Expected: failures 0

**Step 6: 全体検証（16件）**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.creation.validater.* test`
Expected: failures 0

**Step 7: 差分確認とコミット**
Run: `git status --short`
Expected: 対象16ファイルのみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/*.java
git commit -m "client/creation/validaterテストをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/client-creation-validater-jupiter-migration
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/client-creation-validater-jupiter-migration
git branch -d feature/client-creation-validater-jupiter-migration
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン