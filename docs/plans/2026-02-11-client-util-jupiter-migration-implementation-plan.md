# Client Util Jupiter Migration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `client/util` 配下の 3 テストを JUnit4 から JUnit5（Jupiter）へ最小差分で移行する。

**Architecture:** 対象を `CommandRunnerTest` / `ImageCacheTest` / `ImageUrlTest` に限定し、JUnit import と annotation を Jupiter 化する。既存基盤（Jupiter+Vintage+Surefire）を利用し、`build -> test -> 対象明示実行` を直列で検証する。

**Tech Stack:** Maven, JUnit4, JUnit5 (Jupiter), Maven Surefire

---

### Task 1: 事前確認

**Files:**
- Modify: なし
- Test: なし

**Step 1: 作業状態確認**

Run: `git status --short`  
Expected: 想定外差分なし

**Step 2: 対象クラス確認**

Run:
```bash
Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/util/CommandRunnerTest.java
Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageCacheTest.java
Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageUrlTest.java
```
Expected: JUnit4 依存箇所（import / annotation）を把握できる

### Task 2: `CommandRunnerTest` を Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/util/CommandRunnerTest.java`

**Step 1: JUnit API 置換**

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions`
- `@RunWith(JUnit4.class)` があれば削除

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/util/CommandRunnerTest.java`  
Expected: テスト仕様変更なし

### Task 3: `ImageCacheTest` を Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageCacheTest.java`

**Step 1: JUnit API 置換**

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions`
- `@RunWith(JUnit4.class)` があれば削除

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageCacheTest.java`  
Expected: テスト仕様変更なし

### Task 4: `ImageUrlTest` を Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageUrlTest.java`

**Step 1: JUnit API 置換**

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions`
- `@RunWith(JUnit4.class)` があれば削除

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageUrlTest.java`  
Expected: テスト仕様変更なし

### Task 5: 逐次検証

**Files:**
- Modify: 必要時のみ対象3クラスの局所修正

**Step 1: build**

Run: `mvn -DskipTests package`  
Expected: `BUILD SUCCESS`

**Step 2: 既定 test**

Run: `mvn test`  
Expected: `BUILD SUCCESS`

**Step 3: 対象3クラス明示実行**

Run:
```bash
mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.util.CommandRunnerTest,tv.dyndns.kishibe.qmaclone.client.util.ImageCacheTest,tv.dyndns.kishibe.qmaclone.client.util.ImageUrlTest -DfailIfNoTests=false test
```
Expected: 対象3クラスが成功（Failures/Errors 0）

### Task 6: コミット

**Files:**
- Modify:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/util/CommandRunnerTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageCacheTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageUrlTest.java`

**Step 1: 最終差分確認**

Run: `git diff --stat`  
Expected: 対象3ファイル中心の最小差分

**Step 2: コミット**

```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/util/CommandRunnerTest.java
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageCacheTest.java
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/util/ImageUrlTest.java
git commit -m "client/utilテストをJUnit5へ移行"
```

### Task 7: 完了報告

**Step 1: 報告内容を整理**

1. 変更ファイル
2. 実行コマンド
3. 成功/失敗
4. 既知制約（`failIfNoTests=false` 利用理由）
