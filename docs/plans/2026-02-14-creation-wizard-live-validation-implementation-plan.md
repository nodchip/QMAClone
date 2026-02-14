# 問題作成ウィザード即時バリデーション Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 問題作成ウィザードでステップごとの即時バリデーションと具体的エラーメッセージを実装し、入力ミスを早期に検知できるようにする。

**Architecture:** `CreationUi` にステップ遷移制御とエラー件数集約を持たせ、`WidgetProblemForm` には項目単位の検証結果反映を持たせる。既存の `validateProblem()` は Step3 の整合チェックに再利用し、Step1/Step2 は軽量な必須チェックを追加する。遷移時は `validate -> focus -> block` の順で処理し、誤入力時の復帰導線を明確にする。

**Tech Stack:** GWT UiBinder, Java, Maven, JUnit

---

### Task 1: バリデーション結果モデルを導入

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/StepValidationResult.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/StepValidationResultTest.java`

**Step 1: 失敗テストを作成**

```java
@Test
public void hasErrors_returnsTrue_whenFieldErrorsExist() {
  StepValidationResult result = new StepValidationResult();
  result.addError("genre", "ジャンルを選択してください");
  assertTrue(result.hasErrors());
}
```

**Step 2: テスト失敗確認**

Run: `mvn -q -Dtest=StepValidationResultTest test`
Expected: FAIL（クラス未定義）

**Step 3: 最小実装**
- `Map<String, String> fieldErrors`
- `addError`, `clear`, `hasErrors`, `firstErrorFieldId`

**Step 4: テスト成功確認**

Run: `mvn -q -Dtest=StepValidationResultTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/StepValidationResult.java src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/StepValidationResultTest.java
git commit -m "問題作成バリデーション結果モデルを追加"
```

### Task 2: WidgetProblemFormに項目別エラー表示を追加

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/WidgetProblemForm.java`
- Modify: `src/main/webapp/QMAClone.css`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/WidgetProblemFormTest.java`

**Step 1: 失敗テスト作成（表示反映API）**
- `applyStepErrors(Map<String, String>)` でエラー表示/解除されることを検証

**Step 2: 失敗確認**

Run: `mvn -q -Dtest=WidgetProblemFormTest test`
Expected: FAIL（メソッド未定義）

**Step 3: 最小実装**
- 対象項目（ジャンル、出題形式、問題文、解答1）のエラーラベルを追加
- `applyStepErrors` と `clearStepErrors` を実装
- エラー時のラベル強調CSSを追加

**Step 4: 成功確認**

Run: `mvn -q -Dtest=WidgetProblemFormTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/WidgetProblemForm.java src/main/webapp/QMAClone.css src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/WidgetProblemFormTest.java
git commit -m "問題作成フォームに項目別エラー表示を実装"
```

### Task 3: CreationUiに即時バリデーションと遷移時ブロックを実装

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiStepValidationTest.java`

**Step 1: 失敗テスト作成**
- Step1未入力で `next` しても遷移しない
- Step2問題文空で遷移しない
- Step3で既存整合NGなら遷移しない

**Step 2: 失敗確認**

Run: `mvn -q -Dtest=CreationUiStepValidationTest test`
Expected: FAIL

**Step 3: 最小実装**
- `validateCurrentStepLive()` を追加
- `validateStepForTransition(int)` を追加
- `onButtonNextStep` で失敗時に遷移中止 + 最初のエラー項目へフォーカス
- Step上部にエラー件数表示

**Step 4: 成功確認**

Run: `mvn -q -Dtest=CreationUiStepValidationTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiStepValidationTest.java
git commit -m "問題作成ウィザードに即時バリデーションを実装"
```

### Task 4: 統合検証とデプロイ

**Files:**
- Use: `deploy_qmaclone_tomcat9.ps1`

**Step 1: 直列検証**

Run: `mvn -q compile`
Expected: PASS

Run: `mvn -q test`
Expected: PASS

Run: `mvn -q "-Dgwt.skipCompilation=false" gwt:compile`
Expected: PASS

**Step 2: デプロイ**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\\deploy_qmaclone_tomcat9.ps1`
Expected: `Done.`

**Step 3: 手動確認**
- Step1未入力時にエラー表示
- Step2問題文空で遷移不可
- Step3必須解答欠落で遷移不可
- 修正後に即時でエラー消去
- Step4まで正常遷移

**Step 4: 完了記録**
- 実行コマンドと結果を完了報告に記載
- 未検証項目があれば明記
