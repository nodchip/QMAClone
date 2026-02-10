# Test Dependency Scope Hardening Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** テスト専用依存 5 件を `test` スコープへ整理し、実行時クラスパスへの不要混入を防ぐ。

**Architecture:** 変更対象を `pom.xml` の依存定義に限定し、依存バージョンやテストコードは変更しない。検証は `build -> test` を直列実行して、スコープ整理による副作用を早期検知する。差分は 1 目的でコミットし、検証結果を明示的に記録する。

**Tech Stack:** Maven (`pom.xml`), JUnit4, Mockito, Hamcrest, Truth, GuiceBerry

---

### Task 1: 事前状態の確認

**Files:**
- Modify: なし
- Test: なし

**Step 1: 作業ツリーを確認する**

Run: `git status --short`  
Expected: クリーン、または今回対象外差分がないことを確認できる

**Step 2: 対象依存の現状を確認する**

Run: `rg -n "junit|mockito-core|hamcrest-all|truth|guiceberry|<scope>test</scope>" pom.xml`  
Expected: 対象 5 依存の現行スコープを把握できる

**Step 3: コミット**

この Task はコミットなし

### Task 2: `pom.xml` のスコープ整理

**Files:**
- Modify: `pom.xml`
- Test: なし

**Step 1: 最小差分で `scope=test` を追加する**

対象:
1. `junit:junit`
2. `org.mockito:mockito-core`
3. `org.hamcrest:hamcrest-all`
4. `com.google.truth:truth`
5. `com.google.guiceberry:guiceberry`

Expected: 上記 5 依存のみ `scope=test` が付与される

**Step 2: 差分を確認する**

Run: `git diff -- pom.xml`  
Expected: 対象 5 依存への `scope=test` 追加以外が含まれない

**Step 3: コミット**

この Task はコミットなし（検証後に実施）

### Task 3: 逐次検証（build -> test）

**Files:**
- Modify: なし
- Test: 実行のみ

**Step 1: build を実行する**

Run: `mvn -DskipTests package`  
Expected: `BUILD SUCCESS`

**Step 2: test を実行する**

Run: `mvn test`  
Expected: `BUILD SUCCESS`（現行設定で skip の場合もフェーズ正常終了）

**Step 3: 失敗時の対処方針**

`compile` エラーで対象依存不足が出た場合:
1. 原因依存を特定
2. 該当依存のみ `scope` を戻す
3. `mvn -DskipTests package` から再実行

### Task 4: 仕上げ（差分確定とコミット）

**Files:**
- Modify: `pom.xml`
- Test: なし

**Step 1: 変更範囲を最終確認する**

Run: `git diff --stat`  
Expected: `pom.xml` のみ、かつスコープ整理のみ

**Step 2: コミットする**

Run:
```bash
git add pom.xml
git commit -m "テスト依存のスコープをtestに整理"
```

Expected: 1 目的のコミットが作成される

### Task 5: 完了報告テンプレート

**Files:**
- Modify: なし
- Test: なし

**Step 1: 実行ログをまとめる**

報告内容:
1. 変更ファイル
2. 実行コマンド
3. 実行結果（成功/失敗）
4. 失敗があれば原因と対処

**Step 2: コミット**

この Task はコミットなし
