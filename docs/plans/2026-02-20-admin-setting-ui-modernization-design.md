# 管理者用設定UI モダン化デザイン（テーブル優先）

## 1. 目的
- 対象は `設定 > 管理者用` の2タブ（`テーマモード編集権限` / `制限ユーザー`）に限定する。
- 既存トーン（QMAClone設定画面の配色・余白・操作感）を維持しながら、情報の視認性と操作効率を改善する。
- 一覧表示は「テーブル優先」とし、カードは補助（説明・サマリー・入力）として使う。

## 2. スコープ
### 2.1 対象
- `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingAdministrator.java`
- `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingThemeModeEditor.java`
- `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingRestrictedUserView.ui.xml`
- `src/main/java/tv/dyndns/kishibe/qmaclone/client/setting/PanelSettingRestrictedUserView.java`
- `src/main/webapp/QMAClone.css`

### 2.2 非対象
- 管理者機能の権限モデル変更（認証・認可の新規導入）
- `設定` 全体の再設計
- サーバーAPI仕様の変更

## 3. UX方針
### 3.1 レイアウト共通骨格
各タブは以下の4ブロックで統一する。
1. ヘッダー（タイトル + 補足説明）
2. 操作バー（更新、タイプ切替、主要操作）
3. テーブル領域（一覧 + 行内操作）
4. 補助表示（空状態、読み込み中、エラー）

### 3.2 主要原則
- 主要操作は上部に集約し、視線移動を短縮する。
- 行内操作は対象行に閉じて表示し、誤操作を減らす。
- 破壊的操作は `Danger` スタイルで明示する。
- 既存設定画面と同じ色トークンと余白リズムを使う。

## 4. 画面別デザイン
### 4.1 テーマモード編集権限
現状の `Grid` + ラジオ3列を `CellTable` ベースに置き換える。

#### 列構成（案）
- ユーザーコード
- プレイヤー名
- 現在状態（申請中 / 承認 / 却下）
- 操作（申請中へ戻す / 承認 / 却下）

#### 変更意図
- ラジオ選択より行アクションの方が意図が明確で、誤変更しづらい。
- 状態はバッジ表示にして、視認性を上げる。
- 「申請リスト更新」ボタンはヘッダー右に配置し、テーブル直上で再読込できるようにする。

### 4.2 制限ユーザー
`FlowPanel` のボタン羅列をやめ、2つのテーブルに分割する。

#### ブロック構成（案）
- 制限タイプセレクタ（上部）
- ユーザーコード管理カード
  - 入力欄 + 追加/削除/クリア
  - ユーザーコード一覧テーブル
- リモートアドレス管理カード
  - 入力欄 + 追加/削除/クリア
  - リモートアドレス一覧テーブル

#### 変更意図
- 件数増加時でも探索しやすくする。
- 入力と対象一覧を同じカードに閉じ、作業コンテキストを固定する。

## 5. コンポーネント設計
### 5.1 既存再利用
- `TabPanel` は継続利用（`PanelSettingAdministrator`）。
- `RpcAsyncCallback` と `SettingSaveToast` は現行のまま使う。
- 既存の `setting*Card` / `setting*Table` 系CSSパターンを優先再利用する。

### 5.2 追加候補
- `CellTableThemeModeEditor`（新規）
- `CellTableRestrictedUserCode`（新規）
- `CellTableRestrictedRemoteAddress`（新規）

※ 新規クラスは責務分離のため。複雑度が低ければ既存クラス内実装も許容する。

## 6. 状態遷移とフィードバック
- 初期ロード中: テーブル下に読み込み表示
- 正常表示: 一覧と操作を表示
- 更新中: 対象操作を一時無効化
- 空状態: 空メッセージを表示
- エラー: 既存ロギング + トースト/画面内メッセージ

## 7. スタイル指針
- `QMAClone.css` のトークン（色、角丸、余白）を使い、直書き色を増やさない。
- 新規スタイルは `settingAdmin*` プレフィックスで追加する。
- ボタンは `Primary / Secondary / Danger` を明確に使い分ける。
- `hover / active / disabled / focus-visible` を全操作要素で定義する。
- テーブルは横幅不足時に横スクロールで逃がし、列の意味を維持する。

## 8. レスポンシブ方針
- ブレークポイントは既存（1200 / 1100 / 960 / 720）を踏襲する。
- 狭幅では操作バーを折り返す。
- テーブルは列削除せず、スクロール優先で情報欠落を防ぐ。

## 9. 実装順序（直列）
1. `PanelSettingThemeModeEditor` の `CellTable` 化
2. `PanelSettingRestrictedUserView` のUI再構成（UiBinder + Java）
3. 管理者用CSS追加・既存スタイルへの統合
4. 目視確認（通常幅 / 中間幅 / モバイル幅）
5. GWTコンパイル
6. テスト

## 10. 検証計画
### 10.1 コマンド
- `mvn "-Dgwt.skipCompilation=false" gwt:compile`
- `mvn "-Dsurefire.skip=false" test`

### 10.2 目視確認
- 通常幅・中間幅・モバイル幅で崩れがない
- 行内操作の押下状態が視認できる
- 空状態 / 読み込み中 / エラー時の表示が崩れない
- 既存設定画面と配色・余白の違和感がない

## 11. 完了条件
- 2タブともテーブル優先UIへ移行済み
- 主要操作が上部に集約され、導線が短縮されている
- 既存設定画面トーンとの不整合がない
- `gwt:compile` 成功

