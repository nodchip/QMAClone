# client accuracyrate/geom/constant テスト Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** accuracyrate/geom/constant の6テストを JUnit4 から JUnit5 (Jupiter) へまとめて移行し、既存検証を維持する。

**Architecture:** JUnit4 の import/annotation/assert を Jupiter へ最小差分で置換する。テストロジックは変更しない。対象を限定し、`build -> test` を直列実行する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter

---

### Task 1: accuracyrate/geom/constant テストを Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/accuracyrate/AccuracyRateNormalizerDefaultTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/accuracyrate/AccuracyRateNormalizerMarubatsuTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/game/accuracyrate/AccuracyRateNormalizerYontakuTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/geom/PointTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/geom/PolygonTest.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java`

**Step 1: JUnit4 依存を置換する**
- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Before`/`@RunWith`/`org.junit.Assert.*` があれば Jupiter へ置換

**Step 2: テストロジックは変更しない**
- 期待値、テストデータ、計算ロジック検証は不変とする。

**Step 3: build を実行して確認する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象6テストをまとめて実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizerDefaultTest,tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizerMarubatsuTest,tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizerYontakuTest,tv.dyndns.kishibe.qmaclone.client.geom.PointTest,tv.dyndns.kishibe.qmaclone.client.geom.PolygonTest,tv.dyndns.kishibe.qmaclone.client.constant.ConstantTest test`
Expected: 対象テストが失敗 0 で完了

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: 対象6ファイルのみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/game/accuracyrate/AccuracyRateNormalizerDefaultTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/accuracyrate/AccuracyRateNormalizerMarubatsuTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/game/accuracyrate/AccuracyRateNormalizerYontakuTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/geom/PointTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/geom/PolygonTest.java src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java
git commit -m "clientのaccuracyrate/geom/constantテストをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/client-accuracyrate-geom-constant-jupiter-migration
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/client-accuracyrate-geom-constant-jupiter-migration
git branch -d feature/client-accuracyrate-geom-constant-jupiter-migration
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン