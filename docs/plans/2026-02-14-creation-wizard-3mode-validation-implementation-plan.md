# 問題作成ウィザード 3モード導線・バリデーション改善 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 問題作成ウィザードで「新規作成 / 既存修正 / コピー新規作成」の3ユースケースを明確化し、Step3でランダムフラグを即時検証して誤操作と再入力を減らす。

**Architecture:** Step1にモード選択カードを追加し、`CreationUi` がモード状態（NEW/EDIT/CLONE）を管理して表示・有効状態を切り替える。既存の取得ボタンを流用しつつ、モードに応じて導線を段階表示する。Step3の既存即時バリデーションにランダムフラグ検証を組み込み、Step4カードへエラー反映を連動させる。

**Tech Stack:** GWT UiBinder, Java, Maven, CSS

---

### Task 1: Step1 に3モードカードUIを追加

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`
- Modify: `src/main/webapp/QMAClone.css`

**Step 1: 既存ビルド確認**

Run: `mvn -q compile`
Expected: PASS

**Step 2: UiBinder に3カードの骨格を追加**
- `新規作成` カード
- `既存を修正` カード
- `コピーして新規作成` カード
- 各カードに開始ボタン、現在モード表示ラベルを追加

**Step 3: モードカード用スタイルを追加**
- カード通常/選択中状態
- ボタンの primary/secondary クラス
- モバイルで縦並び

**Step 4: コンパイル確認**

Run: `mvn -q compile`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml src/main/webapp/QMAClone.css
git commit -m "問題作成Step1に3モード選択カードを追加"
```

### Task 2: モード状態管理と段階表示を実装

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiModeFlowTest.java`

**Step 1: 失敗テストを追加**
- 初期モードが NEW である
- EDIT/CLONE モードでは問題番号入力と取得ボタンが表示される
- NEW モードでは取得系UIが非表示になる

**Step 2: 失敗確認**

Run: `mvn -q "-Dtest=CreationUiModeFlowTest" test`
Expected: FAIL

**Step 3: 最小実装**
- `CreationUi` に `CreationMode` enum（NEW/EDIT/CLONE）を追加
- モード切替ハンドラを追加
- 取得系UIと送信系UIの表示/有効制御をモード連動で実装

**Step 4: テスト成功確認**

Run: `mvn -q "-Dtest=CreationUiModeFlowTest" test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiModeFlowTest.java
git commit -m "問題作成ウィザードの3モード状態管理を実装"
```

### Task 3: Step3 ランダムフラグ即時バリデーションを追加

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/WidgetProblemForm.java`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiStepValidationTest.java`

**Step 1: 失敗テストを追加**
- Step3でランダムフラグ不正時にエラーとなる
- エラー文言が修正指示型で表示される

**Step 2: 失敗確認**

Run: `mvn -q "-Dtest=CreationUiStepValidationTest" test`
Expected: FAIL

**Step 3: 実装**
- Step3検証ロジックへランダムフラグ検証を追加
- `WidgetProblemForm` の該当フィールドにエラー表示を連動
- Step4サマリーカードの解答設定にエラー件数を反映

**Step 4: テスト成功確認**

Run: `mvn -q "-Dtest=CreationUiStepValidationTest,CreationUiStep4SummaryTest" test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/WidgetProblemForm.java src/test/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUiStepValidationTest.java
git commit -m "問題作成Step3にランダムフラグ即時バリデーションを追加"
```

### Task 4: ボタン階層デザインを安全重視へ統一

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`

**Step 1: 失敗基準を決める（目視確認項目）**
- 各状態で primary が1つだけである
- 補助操作は secondary で表示される

**Step 2: スタイル実装**
- primary/secondary クラス適用
- `入力内容をリセットする` は常に secondary
- モード別に主操作を切替表示

**Step 3: コンパイル確認**

Run: `mvn -q compile`
Expected: PASS

**Step 4: GWTテスト再実行**

Run: `mvn -q "-Dtest=CreationUiModeFlowTest,CreationUiStepValidationTest,CreationUiStep4SummaryTest" test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/webapp/QMAClone.css src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml
git commit -m "問題作成ウィザードの主操作ボタン階層を整理"
```

### Task 5: 最終検証とデプロイ

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

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat9.ps1`
Expected: `Done.`

**Step 3: 手動確認**
- 3モードカードの見分けがつく
- モード切替で表示項目が正しく切り替わる
- Step3 ランダムフラグ不正時に即時エラー表示
- Step4カードへエラー反映
- 主操作ボタンが状態ごとに1つに絞られる

**Step 4: 完了報告**
- 実行コマンドと結果
- 未検証項目があれば明記
