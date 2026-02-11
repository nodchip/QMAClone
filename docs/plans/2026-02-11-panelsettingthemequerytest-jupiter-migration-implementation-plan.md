# PanelSettingThemeQueryTest Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** PanelSettingThemeQueryTest を JUnit4 から JUnit5 (Jupiter) へ最小差分で移行し、既存の検証意図を維持する。

**Architecture:** ランナー依存を削除し、Jupiter アノテーションと `MockitoAnnotations.openMocks(this)` へ置換する。テストロジックは変更せず、`build -> test` を直列実行して回帰を確認する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter, Mockito

---

### Task 1: PanelSettingThemeQueryTest を Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingThemeQueryTest.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingThemeQueryTest.java`

**Step 1: JUnit4依存を置換する**
- `@RunWith(MockitoJUnitRunner.class)` を削除
- `@Before` -> `@BeforeEach`
- `@After` -> `@AfterEach`
- `@Test` -> `org.junit.jupiter.api.Test`

**Step 2: Mockito 初期化を openMocks へ置換する**
- `AutoCloseable closeableMocks` フィールドを追加
- `setUp()` 冒頭で `closeableMocks = MockitoAnnotations.openMocks(this)`
- `tearDown()` で `closeableMocks.close()`

**Step 3: build を実行して確認する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象テストのみ実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -DargLine=-Dnet.bytebuddy.experimental=true -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingThemeQueryTest test`
Expected: 対象テストが失敗 0 で完了

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: `PanelSettingThemeQueryTest.java` のみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingThemeQueryTest.java
git commit -m "PanelSettingThemeQueryTestをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/panelsettingthemequerytest-jupiter-migration
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/panelsettingthemequerytest-jupiter-migration
git branch -d feature/panelsettingthemequerytest-jupiter-migration
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン