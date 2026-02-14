# 問題作成ウィザード 5ステップ再編（モード分離）設計

## 目的
3モード選択（新規作成 / 既存修正 / コピー新規作成）と基本情報入力が同一画面で競合している状態を解消し、ユーザーの意思決定と入力作業を段階分離する。

## スコープ
- 問題作成ウィザードを 4 ステップから 5 ステップへ再編する。
- Step5（確認）でカード単位の編集戻り導線を維持する。
- ランダムフラグの検証責務を Step2（基本情報）に集約する。

## 新しいステップ構成
1. モード選択
2. 基本情報（ジャンル / 出題形式 / ランダムフラグ / 問題作成者）
3. 問題文
4. 解答設定
5. 確認

## UI 方針
- Step1 はモード選択専用画面にする。
- Step1 では問題フォーム（`panelWizardFormHost`）を非表示にする。
- Step2〜Step4 では問題フォームを表示し、`WidgetProblemForm#setWizardStep()` で入力項目を段階表示する。
- Step5 は既存のサマリーカード表示を活用し、戻り導線を `Step2 / Step3 / Step4` に割り当てる。

## バリデーション責務
- Step1: モード選択済みであること。
- Step2: ジャンル必須、出題形式必須、ランダムフラグ 1〜4、問題作成者必須。
- Step3: 問題文必須（空白のみ禁止）。
- Step4: 解答1必須 + 既存 `validateProblem()` の深い検証。
- Step5: 送信直前チェック（入力そのものは行わない）。

## Step5 の戻り導線
- 基本情報カード -> Step2
- 問題文カード -> Step3
- 解答設定カード -> Step4

## エラー表示方針
- 画面上部: `入力エラーがN件あります` を表示。
- カード単位: 該当カードに `creationSummaryError` を付与し、件数表示する。
- Step2/3/4 の `StepValidationResult` を個別に評価して、Step5 で合成表示する。

## 実装対象
- `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`
- `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.ui.xml`
- `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/WidgetProblemForm.java`
- 必要に応じて `src/main/webapp/QMAClone.css`

## テスト対象
- `CreationUiModeFlowTest`: Step1 でフォーム非表示、Step2以降で表示。
- `CreationUiStepValidationTest`: Step2 ランダムフラグ検証、Step4 解答検証。
- `CreationUiStep4SummaryTest`（名称は必要に応じて見直し）: Step5 カードの戻り導線とエラー反映。

## 完了条件
- モード選択と基本情報入力が画面上で分離される。
- ランダムフラグ不正値が Step2 で即時に検出される。
- Step5 から Step2/3/4 へカード単位で直接戻れる。
