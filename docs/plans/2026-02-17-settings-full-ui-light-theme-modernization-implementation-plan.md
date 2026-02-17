# 設定画面全体UI/UXモダン化（ライト統一） Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 設定画面を起点に全画面の配色・フォーム・ボタン・テーブル・通知をライトテーマ基準で統一し、主要解像度で崩れないモダンUIへ移行する。

**Architecture:** `QMAClone.css` に用途ベースのテーマトークンを追加し、共通コンポーネントから順にトークン参照へ置換する。続いて設定/ロビー/検索/統計/作成/一覧/チャットへ段階適用し、空状態・エラー状態文言も統一する。各タスクで `build -> test` を直列実行し、最終タスクでデプロイ確認まで行う。

**Tech Stack:** GWT (Java), CSS, Maven, PowerShell, Tomcat

---

### Task 1: ライトテーマトークン基盤の追加

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Modify: `docs/plans/2026-02-17-settings-full-ui-light-theme-modernization-implementation-plan.md`

**Step 1: 既存濃紺直書き色の使用箇所を棚卸し**

Run: `rg "#0|#1|#2|#3|rgb\(" src/main/webapp/QMAClone.css`
Expected: 直書き色の代表箇所を把握できる

**Step 2: テーマトークンを定義**

Run: `QMAClone.css` に `--color-*`, `--space-*`, `--radius-*`, `--shadow-*`, `--font-*` を追加
Expected: 用途ベーストークンが定義される

**Step 3: 影響の少ない共通色からトークン参照へ置換**

Run: 背景・文字・ボーダーの基本ルールをトークン参照に変更
Expected: 見た目を大きく壊さずライト基盤が有効化される

**Step 4: ビルド検証**

Run: `mvn -f C:\Users\nodchip\git\QMAClone\pom.xml -DskipTests compile`
Expected: `BUILD SUCCESS`

**Step 5: コミット**

Run:
`git add src/main/webapp/QMAClone.css`
`git commit -m "ライトテーマ用デザイントークン基盤を追加"`
Expected: 1コミット1目的で記録される

### Task 2: 共通フォームとボタンの統一

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/CreationUi.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/search/*`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/*`

**Step 1: フォーム共通クラスの適用範囲を特定**

Run: `rg "setStyleName|addStyleName|TextBox|ListBox|CheckBox" src/main/java/tv/dyndns/kishibe/qmaclone/client`
Expected: ラベル・入力欄の主要適用箇所が洗い出せる

**Step 2: ラベル右寄せ・中央揃え・ギャップを共通化**

Run: 共通フォームセレクタを追加し、個別画面依存のずれを削減
Expected: ラベルと入力欄の縦横整列が統一される

**Step 3: ボタンを Primary/Secondary/Danger に整理**

Run: ボタン種別クラスと状態色（default/hover/active/disabled）を統一
Expected: 画面ごとの浮いた配色が解消される

**Step 4: 検証**

Run: `mvn -f C:\Users\nodchip\git\QMAClone\pom.xml -DskipTests compile`
Expected: `BUILD SUCCESS`

**Step 5: コミット**

Run:
`git add src/main/webapp/QMAClone.css src/main/java/tv/dyndns/kishibe/qmaclone/client`
`git commit -m "入力フォームとボタンの共通スタイルを統一"`
Expected: 共通UI調整のみの差分で記録される

### Task 3: 共通テーブル・通知・ポップアップの統一

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/SettingSaveToast.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/report/*`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/search/*`

**Step 1: テーブル共通ルールを実装**

Run: ヘッダ・行hover・選択行・ゼロ件行のCSSをトークン基準へ統一
Expected: 表示密度と可読性が全画面で揃う

**Step 2: 通知カード（右下固定）の見た目統一**

Run: トーストとエラー通知の枠線・背景・閉じるボタン色を整理
Expected: 通知系UIの違和感が解消される

**Step 3: ポップアップの3層構造（ヘッダー/本文/フッター）統一**

Run: 共通ダイアログの余白と文言ブロックを再整理
Expected: 空状態・ゼロ件状態でも視認性が維持される

**Step 4: 検証**

Run: `mvn -f C:\Users\nodchip\git\QMAClone\pom.xml -DskipTests compile`
Expected: `BUILD SUCCESS`

**Step 5: コミット**

Run:
`git add src/main/webapp/QMAClone.css src/main/java/tv/dyndns/kishibe/qmaclone/client`
`git commit -m "テーブルと通知ポップアップの共通UIを統一"`
Expected: 共通コンポーネント改修として記録される

### Task 4: 設定画面（左メニュー含む）全面適用

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSetting.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingTop.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/*.java`
- Modify: `src/main/webapp/QMAClone.css`

**Step 1: 左メニュー選択状態の視認性を統一**

Run: 選択中/hover/通常のスタイルをトークン基準へ変更
Expected: メニューの状態判別が明確になる

**Step 2: セクション情報設計を統一**

Run: 説明・操作・結果のカード構造と余白ルールを揃える
Expected: 情報混在が解消される

**Step 3: 即時保存通知フローの視覚統一確認**

Run: 保存中/成功/失敗の表示位置・配色・文言を統一
Expected: 設定変更のフィードバックが一貫する

**Step 4: 検証**

Run:
`mvn -f C:\Users\nodchip\git\QMAClone\pom.xml -DskipTests compile`
`mvn -f C:\Users\nodchip\git\QMAClone\pom.xml test`
Expected: `BUILD SUCCESS`（テスト0件なら0件であることを確認）

**Step 5: コミット**

Run:
`git add src/main/java/tv/dyndns/kishibe/qmaclone/client/setting src/main/webapp/QMAClone.css`
`git commit -m "設定画面と左メニューのライトテーマ統一を適用"`
Expected: 設定画面改修のみのコミットになる

### Task 5: ロビー・検索・統計・問題作成・一覧・チャット適用

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/lobby/*`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/search/*`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/*`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/creation/*`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/chat/*`
- Modify: `src/main/webapp/QMAClone.css`

**Step 1: 画面ごとの色・余白・整列の残差分を吸収**

Run: 既存個別スタイルをトークン・共通クラスへ寄せる
Expected: 画面間の見た目の差が解消される

**Step 2: 空状態/エラー/ゼロ件文言の統一**

Run: 重複・曖昧表現を削除し、次アクションが分かる文言へ変更
Expected: 状態表示が一貫し理解しやすくなる

**Step 3: 狭幅時の見切れ・重なり調整**

Run: `min-width`, `overflow`, `flex-wrap` ルールを調整
Expected: 主要解像度で崩れない

**Step 4: 検証**

Run:
`mvn -f C:\Users\nodchip\git\QMAClone\pom.xml -DskipTests compile`
`mvn -f C:\Users\nodchip\git\QMAClone\pom.xml test`
Expected: `BUILD SUCCESS`

**Step 5: コミット**

Run:
`git add src/main/java/tv/dyndns/kishibe/qmaclone/client src/main/webapp/QMAClone.css`
`git commit -m "主要画面へライトテーマ統一を展開"`
Expected: 主要画面反映の差分がまとめられる

### Task 6: 最終検証とデプロイ

**Files:**
- Modify: `docs/plans/2026-02-17-settings-full-ui-light-theme-modernization-implementation-plan.md`

**Step 1: 主要解像度での目視確認**

Run: PC標準・狭幅・タブレットで主要画面を確認
Expected: 崩れ、見切れ、重なりがない

**Step 2: 最終ビルド/テスト**

Run:
`mvn -f C:\Users\nodchip\git\QMAClone\pom.xml -DskipTests compile`
`mvn -f C:\Users\nodchip\git\QMAClone\pom.xml test`
Expected: `BUILD SUCCESS`

**Step 3: デプロイ実行**

Run: `powershell -ExecutionPolicy Bypass -File C:\Users\nodchip\git\QMAClone\deploy_qmaclone_tomcat9.ps1`
Expected: デプロイスクリプトが成功する

**Step 4: 配備後ヘルスチェック**

Run:
`curl.exe -I http://localhost:8080/QMAClone-1.0-SNAPSHOT/`
`curl.exe -I http://localhost:8080/QMAClone-1.0-SNAPSHOT/tv.dyndns.kishibe.qmaclone.QMAClone/service`
Expected: 前者 `HTTP 200`、後者 `HTTP 405`

**Step 5: 最終コミット**

Run:
`git add docs/plans/2026-02-17-settings-full-ui-light-theme-modernization-implementation-plan.md`
`git commit -m "ライトテーマ統一改修の実装計画を更新"`
Expected: 計画書更新のみが記録される
