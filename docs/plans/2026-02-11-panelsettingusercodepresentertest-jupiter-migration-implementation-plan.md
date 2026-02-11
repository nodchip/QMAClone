# PanelSettingUserCodePresenterTest Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** PanelSettingUserCodePresenterTest を JUnit4 から JUnit5 (Jupiter) に最小差分で移行し、既存の検証意図を維持する。

**Architecture:** テスト本体の検証ロジックは変更せず、JUnit4 ランナー依存を除去して Jupiter + MockitoAnnotations.openMocks(this) に置換する。`build -> test` を直列実行して回帰を確認する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter, Mockito

---

### Task 1: PanelSettingUserCodePresenterTest を Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenterTest.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenterTest.java`

**Step 1: 置換対象を確定する**
- `@RunWith(MockitoJUnitRunner.class)` を削除
- `org.junit.Before` を `org.junit.jupiter.api.BeforeEach` へ置換
- `org.junit.Test` を `org.junit.jupiter.api.Test` へ置換
- `MockitoAnnotations.openMocks(this)` を `setUp()` に追加
- `AutoCloseable` を保持して `@AfterEach` で close

**Step 2: テストコードを最小差分で編集する**
- import / annotation / runner / mock 初期化のみ変更する。
- `when` / `verify` / `never` の期待値・呼び出し順は変更しない。

**Step 3: build を実行して確認する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象テストのみ実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -DargLine=-Dnet.bytebuddy.experimental=true -Dtest=tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingUserCodePresenterTest test`
Expected: 対象テストが失敗 0 で完了

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: `PanelSettingUserCodePresenterTest.java` のみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingUserCodePresenterTest.java
git commit -m "PanelSettingUserCodePresenterTestをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/panelsettingusercodepresentertest-jupiter-migration
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/panelsettingusercodepresentertest-jupiter-migration
git branch -d feature/panelsettingusercodepresentertest-jupiter-migration
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン