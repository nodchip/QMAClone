# StringUtilsTest Jupiter Migration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `StringUtilsTest` を JUnit4 から JUnit5（Jupiter）へ最小差分で移行する。

**Architecture:** 対象を `StringUtilsTest` 1 クラスに限定し、JUnit の import / annotation を Jupiter へ置換する。既存 JUnit4 テスト資産は維持し、`build -> test -> 対象テスト明示実行` を直列で検証する。

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

Run: `Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/util/StringUtilsTest.java`  
Expected: JUnit4 依存箇所（import / annotation）を把握できる

### Task 2: `StringUtilsTest` の Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/util/StringUtilsTest.java`
- Test: なし

**Step 1: annotation と import を置換**

1. `org.junit.Test` -> `org.junit.jupiter.api.Test`
2. 必要なら `org.junit.Assert.*` -> `org.junit.jupiter.api.Assertions.*`

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/util/StringUtilsTest.java`  
Expected: テスト仕様変更なしで JUnit API 置換のみ

### Task 3: 逐次検証

**Files:**
- Modify: 必要時のみ `StringUtilsTest` の局所修正

**Step 1: build**

Run: `mvn -DskipTests package`  
Expected: `BUILD SUCCESS`

**Step 2: 既定 test**

Run: `mvn test`  
Expected: `BUILD SUCCESS`

**Step 3: 対象テスト明示実行**

Run: `mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.util.StringUtilsTest test`  
Expected: `StringUtilsTest` が成功（Failures/Errors 0）

### Task 4: コミット

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/util/StringUtilsTest.java`

**Step 1: 最終差分確認**

Run: `git diff --stat`  
Expected: 対象クラス中心の最小差分

**Step 2: コミット**

```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/util/StringUtilsTest.java
git commit -m "StringUtilsTestをJUnit5へ移行"
```

### Task 5: 完了報告

**Step 1: 報告内容を整理**

1. 変更ファイル
2. 実行コマンド
3. 実行結果
4. 既知制約（`surefire.skip` 運用）
