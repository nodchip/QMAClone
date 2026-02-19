# QMAClone UI/UX スタイルガイド

## 1. 目的と適用範囲
- このガイドは、QMAClone の現行コードベース（`src/main/webapp/QMAClone.css`、`src/main/java/.../client`、`*.ui.xml`、`landing-site/landing.css`）から抽出した UI/UX の実装規約を定義する。
- 対象は「ゲーム本体 UI」と「ランディングページ UI」。両者は独立管理だが、ブランドトーンは統一する。
- 本ガイドの目的は、画面追加・改修時に「見た目」「操作感」「実装方式」の一貫性を維持すること。

## 2. デザイン原則
- 可読性優先: 文字コントラスト、情報階層、余白を優先し、過剰装飾を避ける。
- 一貫性優先: ボタン状態、カード構造、フォーム整列、通知位置を全画面で揃える。
- 段階移行: 既存の `gwt-*` スタイルを活かしつつ、`app-*` / 画面別セマンティッククラスへ移行する。
- 操作明快性: 主要操作は `Primary`、補助は `Secondary`、危険操作は `Danger` に統一する。

## 3. デザイントークン
基準トークンは `:root` に定義する（`QMAClone.css` 3923行付近）。

### 3.1 カラー
- 背景: `--color-bg-page`, `--color-bg-page-alt`
- サーフェス: `--color-surface-1`, `--color-surface-2`, `--color-surface-3`
- ボーダー: `--color-border-subtle`, `--color-border-default`, `--color-border-strong`
- 文字: `--color-text-primary`, `--color-text-secondary`, `--color-text-muted`
- アクセント: `--color-accent-primary`, `--color-accent-primary-hover`, `--color-accent-primary-active`
- 状態色: `--color-danger-bg`, `--color-danger-border`, `--color-danger-text`, `--color-success-text`
- フォーカス: `--color-focus-ring`

### 3.2 サイズ・余白
- 角丸: `--radius-sm` (6), `--radius-md` (10)
- 間隔: `--space-xs` (4), `--space-sm` (8), `--space-md` (12), `--space-lg` (16), `--space-xl` (24)

### 3.3 タイポグラフィ
- 基本フォント: `'Noto Sans Japanese'`（本体） / `'Noto Sans JP'`（landing）
- 本文基準: 13-16px
- セクション見出し: 18-20px
- 主要タイトル: 32-60px（画面特性に応じて可変）

## 4. レイアウト規約

### 4.1 ゲーム本体シェル
- 全体構造は `app-shell` / `app-root-panel` / `app-main-panel` を基準とする。
- 左ナビは固定: `.app-main-sidebar`（幅 190px、`position: fixed`）。
- 中央コンテンツ幅は統一: Java側 `MAIN_CONTENT_WIDTH = "800px"`。
- 右チャットは固定ドック: `.app-chat-panel`（デスクトップ固定配置）。

### 4.2 中央パネル
- 主要パネルは `800px` 基準で中央表示。
- 設定・統計など二段構成画面は「左サブメニュー + 右本文」のカード構造を採用。
- `width: 100%` と `box-sizing: border-box` をセットで使い、はみ出しを防止する。

### 4.3 ランディングページ
- 本体と同一トークンで配色を揃える。
- `landing-shell`（最大 1040px）を基準コンテナとする。
- ヒーロー + パネル + フッターの3層カード構成を維持する。

## 5. ナビゲーション規約
- 左ナビボタンは `.app-main-nav-button` を利用し、常に横幅100%とする。
- 選択状態は `.app-main-nav-button-active` を利用し、濃色背景 + 白文字で明確化する。
- hover/active/focus の状態を必ず定義する。
- 上部タブは新規追加しない。メイン遷移は左ナビで統一する。

## 6. コンポーネント規約

### 6.1 ボタン
- Primary: `creationButtonPrimary` / `linkPrimaryButton` など。
- Secondary: `creationButtonSecondary` / `linkSecondaryButton`。
- Danger: `linkDangerButton` / `settingRatioReportDangerButton`。
- すべてのボタンで `hover / active / disabled / focus-visible` を定義する。

### 6.2 フォーム
- グリッドフォームは `gridFrame` / `gridNoFrame` を基底にする。
- ラベルセルは右寄せ + 中央揃え（`td:has(+ td input...)` ルール）を維持する。
- 入力欄は境界線・背景・focusリングをトークンで統一する。

### 6.3 カード
- 新規UIは「カード」単位で構成する。
- 典型値: `border: 1px solid var(--color-border-default)`, `border-radius: 8-12px`, `background: var(--color-surface-1/2)`。
- セクション見出し + 本文 + 操作の順で情報を置く。

### 6.4 テーブル/一覧
- CellTable はページ別ラッパークラスを必ず付ける（例: `settingThemeModeEditLogTable`, `settingImageLinkTable`, `rankingTableWidget`）。
- 行背景・ヘッダ背景・hover状態を統一し、意味色はバッジで補助する。

### 6.5 通知
- 保存通知: 右下トースト (`app-setting-save-toast`)。
- エラー通知: 右下スタック (`app-error-panel`)。
- 通知はチャット開閉状態と重ならないようオフセットクラスで調整する。

## 7. ゲーム画面固有規約
- `gameRoot` / `gameSidebar` / `gameQuestionPane` / `questionPanelRoot` を基本構成とする。
- 問題文、解答表示、入力UIは「中央寄せ」を基準とする。
- 解答入力ウィジット（`gwt-Button-*` 群）は文字を上下左右中央に配置する。
- 残り時間ゲージは `gameTimerWidget` / `timerMeter` / `timerFill` を使用し、中央配置で表示する。
- プレイヤーカードは横方向の情報列（アイコン・名前・所属・レート・挨拶・状態）の整列を維持する。

## 8. モーション規約
- 基本トランジション時間:
  - 90ms: 押下時の微小移動
  - 140ms: 色/境界/影の変化
  - 180ms: 主要ボタンやカードの強調遷移
- `transform` は控えめに使い、レイアウトシフトを起こさない。
- `prefers-reduced-motion` を尊重する（landing側で実装済み）。

## 9. レスポンシブ規約
- 主要ブレークポイント: `1200`, `1100`, `960`, `720`。
- PC優先で設計し、狭幅時は以下を適用する。
- 左右2カラムは1カラムへフォールバック。
- 固定高さ領域は `overflow` と `max-height` を明示。
- 固定UI（ナビ・チャット・通知）は重なり検証を必須化。

## 10. 命名・実装規約
- クラス命名は「画面/機能プレフィックス + 役割」で統一する。
- 例: `app-*`, `setting*`, `statistics*`, `ranking*`, `bbs*`, `link*`, `creation*`, `searchProblem*`。
- Java側は `addStyleName()` でセマンティッククラスを付与する。
- GWT難読化クラス（例: `GG-*`）に依存した恒久スタイルは作らない。
- 既存の `gwt-Button-*` は互換層として維持し、新規画面はセマンティッククラスを優先する。

## 11. アクセシビリティ規約
- `focus-visible` のアウトラインを必ず定義する。
- 色のみで状態を表現しない（文字・バッジ・ラベルを併用）。
- クリック可能要素は `cursor: pointer` を付与する。
- テキストコントラストは `--color-text-primary/secondary` 基準で確保する。

## 12. 実装チェックリスト
- 新規色の直書きを避け、トークン参照している。
- ボタン3系統（Primary/Secondary/Danger）に準拠している。
- hover/active/focus/disabled の状態差がある。
- 800px中央パネル基準を崩していない。
- 左ナビ・チャット・通知の固定位置が競合していない。
- `width: 100%` 要素に `box-sizing: border-box` を併用している。
- CellTableは画面専用ラッパークラス配下で装飾している。
- 狭幅（1200/1100/960/720）で崩れがない。

## 13. 運用ルール
- UI変更時は `QMAClone.css` のトークン層との整合を最優先とする。
- 画面別の一時回避スタイル（`!important`）は最小限にし、恒久化時に撤去する。
- DOM実測で競合を確認してから修正する（推測で上書きしない）。

