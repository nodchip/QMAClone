# Client Ranking Jupiter Migration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `client/ranking` 配下の 3 テストを JUnit4 から JUnit5（Jupiter）へ最小差分で移行する。

**Architecture:** 対象を `DateRangeSelectorPresenterTest` / `ThemeSelectorPresenterTest` / `GeneralRankingPresenterTest` に限定し、JUnit import / annotation を Jupiter 化する。既存の JUnit5 併用基盤を利用し、`build -> test -> 対象明示実行` を直列で検証する。

**Tech Stack:** Maven, JUnit4, JUnit5 (Jupiter), Maven Surefire, Mockito

---

### Task 1: 事前確認

**Files:**
- Modify: なし

**Step 1: 作業状態確認**

Run: `git status --short`  
Expected: 想定外差分なし

**Step 2: 対象3クラス確認**

Run:
```bash
Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/DateRangeSelectorPresenterTest.java
Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/ThemeSelectorPresenterTest.java
Get-Content src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/GeneralRankingPresenterTest.java
```
Expected: JUnit4 依存箇所を把握できる

### Task 2: `DateRangeSelectorPresenterTest` を Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/DateRangeSelectorPresenterTest.java`

**Step 1: JUnit API 置換**

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions`
- 必要に応じて `@RunWith` / `@Rule` 対応

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/DateRangeSelectorPresenterTest.java`

### Task 3: `ThemeSelectorPresenterTest` を Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/ThemeSelectorPresenterTest.java`

**Step 1: JUnit API 置換**

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions`
- 必要に応じて `@RunWith` / `@Rule` 対応

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/ThemeSelectorPresenterTest.java`

### Task 4: `GeneralRankingPresenterTest` を Jupiter 化

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/GeneralRankingPresenterTest.java`

**Step 1: JUnit API 置換**

- `org.junit.Test` -> `org.junit.jupiter.api.Test`
- 必要に応じて `Assert` -> `Assertions`
- 必要に応じて `@RunWith` / `@Rule` 対応

**Step 2: 差分確認**

Run: `git diff -- src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/GeneralRankingPresenterTest.java`

### Task 5: 逐次検証

**Files:**
- Modify: 必要時のみ対象3ファイル

**Step 1: build**

Run: `mvn -DskipTests package`  
Expected: `BUILD SUCCESS`

**Step 2: 既定 test**

Run: `mvn test`  
Expected: `BUILD SUCCESS`

**Step 3: 対象3クラス明示実行**

Run:
```bash
mvn --% -Dsurefire.skip=false -Dtest=tv.dyndns.kishibe.qmaclone.client.ranking.DateRangeSelectorPresenterTest,tv.dyndns.kishibe.qmaclone.client.ranking.ThemeSelectorPresenterTest,tv.dyndns.kishibe.qmaclone.client.ranking.GeneralRankingPresenterTest -DfailIfNoTests=false test
```
Expected: 対象3クラス成功（Failures/Errors 0）

**Step 4: 必要時の再実行**

Run:
```bash
mvn --% -Dsurefire.skip=false -DargLine=-Dnet.bytebuddy.experimental=true -Dtest=tv.dyndns.kishibe.qmaclone.client.ranking.DateRangeSelectorPresenterTest,tv.dyndns.kishibe.qmaclone.client.ranking.ThemeSelectorPresenterTest,tv.dyndns.kishibe.qmaclone.client.ranking.GeneralRankingPresenterTest -DfailIfNoTests=false test
```
Expected: Java 25 + Mockito inline 制約を回避して再検証できる

### Task 6: コミット

**Files:**
- Modify:
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/DateRangeSelectorPresenterTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/ThemeSelectorPresenterTest.java`
  - `src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/GeneralRankingPresenterTest.java`

**Step 1: 最終差分確認**

Run: `git diff --stat`

**Step 2: コミット**

```bash
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/DateRangeSelectorPresenterTest.java
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/ThemeSelectorPresenterTest.java
git add src/test/java/tv/dyndns/kishibe/qmaclone/client/ranking/GeneralRankingPresenterTest.java
git commit -m "client/rankingテストをJUnit5へ移行"
```

### Task 7: 完了報告

**Step 1: 報告整理**

1. 変更ファイル
2. 実行コマンド
3. 成功/失敗
4. 既知制約と回避方法
