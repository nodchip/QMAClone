# PacketRoomKeyTest Jupiter移行 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** PacketRoomKeyTest を JUnit4 から JUnit5 (Jupiter) へ最小差分で移行し、既存検証を維持する。

**Architecture:** JUnit4 の import/annotation を Jupiter へ置換する。テストロジック（setUp, hashCode/equals/getter 検証）は変更しない。`build -> test` を直列実行して回帰を確認する。

**Tech Stack:** Java 25, Maven Surefire 3.2.5, JUnit Jupiter

---

### Task 1: PacketRoomKeyTest を Jupiter 化する

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketRoomKeyTest.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketRoomKeyTest.java`

**Step 1: JUnit4依存を置換する**
- `@RunWith(JUnit4.class)` を削除
- `@Before` -> `@BeforeEach`
- `@Test` -> `org.junit.jupiter.api.Test`
- `assertEquals` を `org.junit.jupiter.api.Assertions.assertEquals` に置換

**Step 2: テスト本文は変更しない**
- `setUp` のデータ生成と 3 テストメソッドの期待値を維持する。

**Step 3: build を実行して確認する**
Run: `mvn -DskipTests package`
Expected: `BUILD SUCCESS`

**Step 4: 対象テストのみ実行する**
Run: `mvn --% -Dsurefire.skip=false -DfailIfNoTests=false -Dtest=tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKeyTest test`
Expected: 対象テストが失敗 0 で完了

**Step 5: 差分確認とコミット**
Run: `git status --short`
Expected: `PacketRoomKeyTest.java` のみ変更

Commit:
```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/packet/PacketRoomKeyTest.java
git commit -m "PacketRoomKeyTestをJUnit5へ移行"
```

### Task 2: master へ反映する

**Files:**
- No file changes

**Step 1: master ワークツリーへ fast-forward merge**
Run:
```bash
git checkout master
git merge --ff-only feature/packetroomkeytest-jupiter-migration
```
Expected: fast-forward 成功

**Step 2: worktree / ブランチを整理**
Run:
```bash
git worktree remove .worktrees/packetroomkeytest-jupiter-migration
git branch -d feature/packetroomkeytest-jupiter-migration
```
Expected: 作業ブランチが整理され、ワークツリーがクリーン