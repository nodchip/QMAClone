# ValidatorStressTest Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `ValidatorStressTest` を最小差分で JUnit4 から JUnit5 (Jupiter) へ移行し、成功または除外判断を確定する。

**Architecture:** JUnit4 の import/annotation/runner を Jupiter へ置換する。`@Rule`（GuiceBerry）は変更せず、互換性判定対象とする。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter, GuiceBerry

---

### Task 1: ValidatorStressTest の最小差分移行を試行する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorStressTest.java`

**Step 1: JUnit4依存を置換する**
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`
- `@RunWith(JUnit4.class)` と関連 import を削除
- `@Ignore` -> `@Disabled`
- `@Rule` は変更しない

**Step 2: build を実行する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 3: 対象テストを実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorStressTest test`
Expected: 成功または互換性エラーの再現

**Step 4: 判定分岐**
- 成功時: そのまま移行完了としてコミット
- 失敗時: 置換ミスでないことを確認し、除外理由をドキュメント化

### Task 2: 結果を反映してコミットする

**Files:**
- Success path:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorStressTest.java`
- Fallback path:
  - `docs/plans/2026-02-11-validatorstresstest-jupiter-migration-design.md`

**Step 1: 差分確認**
Run: `git status --short`

**Step 2: コミット**
- Success path:
  - `git add src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/validater/ValidatorStressTest.java`
  - `git commit -m "ValidatorStressTestをJUnit5へ移行"`
- Fallback path:
  - `git add docs/plans/2026-02-11-validatorstresstest-jupiter-migration-design.md`
  - `git commit -m "ValidatorStressTestのJupiter除外理由を記録"`

### Task 3: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/validatorstresstest-jupiter-migration-exec
```

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/validatorstresstest-jupiter-migration-exec
git branch -d feature/validatorstresstest-jupiter-migration-exec
```
