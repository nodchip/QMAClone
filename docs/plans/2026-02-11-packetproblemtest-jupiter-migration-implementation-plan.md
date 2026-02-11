# PacketProblemTest Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** PacketProblemTest を JUnit4 から JUnit5 (Jupiter) へ最小差分で移行し、既存検証を維持する。

**Architecture:** JUnit4 の import/annotation と JUnit4 `assert*` だけを Jupiter へ置換する。Truth/Hamcrest のアサーションは維持し、テストロジックは変更しない。`build -> test` を直列実行して確認する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter, Truth, Hamcrest

---

### Task 1: PacketProblemTest を Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketProblemTest.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketProblemTest.java`

**Step 1: JUnit4依存を置換する**
- `@RunWith(JUnit4.class)` を削除
- `@Before` -> `@BeforeEach`
- `@Test` -> `org.junit.jupiter.api.Test`
- `org.junit.Assert.assertEquals/assertTrue` を Jupiter Assertions へ置換

**Step 2: 既存アサーション流儀は維持する**
- Truth (`assertThat`) と Hamcrest (`containsInAnyOrder`) は変更しない
- 文字列リテラルや既存データは変更しない

**Step 3: build を実行して確認する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象テストのみ実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemTest test`
Expected: 対象テストが失敗 0 で完了

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: `PacketProblemTest.java` のみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketProblemTest.java
git commit -m "PacketProblemTestをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/packetproblemtest-jupiter-migration
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/packetproblemtest-jupiter-migration
git branch -d feature/packetproblemtest-jupiter-migration
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン