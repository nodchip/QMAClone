# SettingModuleTest Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** SettingModuleTest を JUnit4 から JUnit5 (Jupiter) へ最小差分で移行し、既存検証を維持する。

**Architecture:** JUnit import を Jupiter に置換するのみで、テスト本文は変更しない。`build -> test` を直列実行して回帰がないことを確認する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter

---

### Task 1: SettingModuleTest を Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/SettingModuleTest.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/SettingModuleTest.java`

**Step 1: JUnit import を置換する**
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.assertTrue` -> `org.junit.jupiter.api.Assertions.assertTrue`

**Step 2: テスト本文は変更しない**
- `provideExternalAccountConnectorShouldReturnGoogleConnector` のロジックを維持する。

**Step 3: build を実行して確認する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象テストのみ実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.SettingModuleTest test`
Expected: 対象テストが失敗 0 で完了

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: `SettingModuleTest.java` のみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/SettingModuleTest.java
git commit -m "SettingModuleTestをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/settingmoduletest-jupiter-migration
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/settingmoduletest-jupiter-migration
git branch -d feature/settingmoduletest-jupiter-migration
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン