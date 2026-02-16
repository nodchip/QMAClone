# Chat UI Modernization Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** チャットを右下固定フローティング前提のモダンUIへ刷新し、可読性と操作フィードバックを改善する。

**Architecture:** 既存の `Controller` が持つチャット開閉状態管理を維持しつつ、`PanelChat` と `QMAClone.css` の `app-chat-*` 系スタイルを再構成する。機能ロジックは変更せず、レイアウトとスタイル、最小限のクラス付与に限定して改善する。

**Tech Stack:** GWT (UiBinder/Widget), Java 25, Maven, CSS

---

### Task 1: 現行チャットDOMとスタイル適用点の固定

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/chat/PanelChat.java`
- Test: `mvn compile`

**Step 1: 変更前レイアウト確認ポイントを明文化する**
- 右チャットの現行構造（ヘッダー/本文/再表示ボタン）をコメントと計画に照合する。

**Step 2: チャットDOMに不足クラスを付与する（必要最小限）**
- `PanelChat` 側にタブ領域・本文領域へ `app-chat-*` プレフィックスクラスを付与する。
- `Controller` 側でチャットヘッダー/本文ラッパーのクラス責務を整理する。

**Step 3: コンパイル確認**
- Run: `mvn compile`
- Expected: BUILD SUCCESS

**Step 4: Commit**
- `git add src/main/java/tv/dyndns/kishibe/qmaclone/client/Controller.java src/main/java/tv/dyndns/kishibe/qmaclone/client/chat/PanelChat.java`
- `git commit -m "チャットUI刷新向けにDOMクラス構造を整理"`

### Task 2: 右下固定フローティングレイアウトへ刷新

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Test: `mvn compile`

**Step 1: 既存 `app-chat-*` スタイルを棚卸しする**
- 重複定義（旧3カラム向け）を把握し、最終的に有効化する定義を一箇所へ集約する。

**Step 2: 右下固定レイアウトを実装する**
- `.app-chat-panel` を `position: fixed` + `right/bottom` 基準へ変更する。
- 展開時サイズを `width: 360px; height: min(56vh, 520px)` ベースに定義する。
- `@media` で狭幅時 `calc(100vw - 24px)` に切り替える。

**Step 3: 折りたたみ時トリガの位置を固定する**
- `.app-chat-reopen` を右下基準で常時視認可能にする。
- 開閉で位置ジャンプしないよう遷移先のアンカーを統一する。

**Step 4: コンパイル確認**
- Run: `mvn compile`
- Expected: BUILD SUCCESS

**Step 5: Commit**
- `git add src/main/webapp/QMAClone.css`
- `git commit -m "チャットを右下固定フローティングレイアウトへ更新"`

### Task 3: フラット上質トーンへのスタイル刷新

**Files:**
- Modify: `src/main/webapp/QMAClone.css`
- Test: `mvn compile`

**Step 1: チャット用トークンを導入する**
- `chat-*` 系CSS変数（背景・境界・文字・影）を追加する。

**Step 2: 3層カードの見た目を実装する**
- ヘッダー/メッセージ領域/入力領域の背景トーンを分離する。
- 角丸、境界、影を統一する。

**Step 3: ボタン状態差を明確化する**
- `.app-chat-toggle` と `.app-chat-reopen` の `hover/active/disabled/focus-visible` を調整する。
- 送信ボタン（PanelRealtime側）にも同系統スタイルを適用する。

**Step 4: コンパイル確認**
- Run: `mvn compile`
- Expected: BUILD SUCCESS

**Step 5: Commit**
- `git add src/main/webapp/QMAClone.css`
- `git commit -m "チャット配色と操作フィードバックをモダン化"`

### Task 4: 検証・デプロイ・完了確認

**Files:**
- Modify: `docs/plans/2026-02-16-chat-modernization-design.md`（必要なら実績追記のみ）
- Test: `mvn test`
- Test: `deploy_qmaclone_tomcat9.ps1`

**Step 1: ビルドとテストを直列実行する**
- Run: `mvn compile`
- Run: `mvn test`
- Expected: BUILD SUCCESS（既存設定により `Tests are skipped.` を明記）

**Step 2: デプロイを実行する**
- Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\deploy_qmaclone_tomcat9.ps1`
- Expected:
  - `/QMAClone-1.0-SNAPSHOT/` が HTTP 200
  - `/tv.dyndns.kishibe.qmaclone.QMAClone/service` が HTTP 405

**Step 3: 画面検証チェックリスト**
- デスクトップ: 右下固定・主要UI非干渉
- 折りたたみ: 常時トリガ表示・再展開1クリック
- 狭幅: はみ出しなし
- 視覚差: 送信/開閉ボタンの `hover/active/disabled`

**Step 4: Commit**
- `git add <実変更ファイル>`
- `git commit -m "チャットUIモダン化を適用"`

## 補足
- `.settings/org.eclipse.core.resources.prefs` は依頼対象外のためコミット対象に含めない。
- 本計画は UI 変更中心のため、回帰確認はスクリーンショット比較を必須とする。
