# ThemeQueryCellTest Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** ThemeQueryCellTest を JUnit4 から JUnit5 (Jupiter) へ最小差分で移行し、既存のテスト意図を維持する。

**Architecture:** テスト本体のロジックは変更せず、JUnit4 依存の注釈・ランナー・import だけを Jupiter へ置換する。アサーションは Hamcrest を維持し、意味的差分を避ける。検証は `build -> test` を直列で実行する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter, Hamcrest

---

### Task 1: ThemeQueryCellTest を Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryCellTest.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryCellTest.java`

**Step 1: 失敗しない最小置換方針を確定する**
- 置換対象を限定する。
  - `@RunWith(JUnit4.class)` を削除
  - `@Before` を `@BeforeEach` に置換
  - `org.junit.Test` を `org.junit.jupiter.api.Test` に置換
  - `org.junit.Assert.assertThat` を `org.hamcrest.MatcherAssert.assertThat` に置換

**Step 2: テストコードを最小変更で編集する**
- 上記の import / annotation / runner を置換する。
- テストメソッド名、期待値、`ThemeQueryCell#render` 呼び出しは変更しない。

**Step 3: build を実行してコンパイル確認する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象テストのみ実行して動作確認する**
Run: `mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryCellTest test`
Expected: `Tests run` が 1 件以上で失敗 0

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: `ThemeQueryCellTest.java` のみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/theme/ThemeQueryCellTest.java
git commit -m "ThemeQueryCellTestをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ移動し fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/themequerycelltest-jupiter-migration
```
Expected: fast-forward 成功

**Step 2: worktree を削除してブランチ整理する**
Run:
```bash
git worktree remove .worktrees/themequerycelltest-jupiter-migration
git branch -d feature/themequerycelltest-jupiter-migration
```
Expected: 作業ディレクトリと作業ブランチがクリーンに整理される