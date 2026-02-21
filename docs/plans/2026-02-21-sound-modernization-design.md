# Sound Modernization Design

## 目的
- ゲーム内SEをモダンなミニマル電子音へ刷新する。
- 音量設定をユーザーごとに柔軟化（master + カテゴリ別）する。
- 音源クレジットをゲーム内で確認可能にし、ライセンス順守を仕様化する。

## スコープ
- 対象イベントは全面刷新とする。
  - 正解 / 不正解 / タイムアップ / ボタン押下 / 対戦準備完了
  - 勝敗結果
  - UI遷移音（タブ切替・モーダル開閉）
- 設定画面に `Help/About` タブを新設し、音源クレジットを表示する。
- 音量設定の保存は「localStorage + サーバー」の二層構成とする。

## 全体アーキテクチャ
- 既存の呼び出し点（`SoundPlayer` / `SoundManager`）は段階移行を前提に維持する。
- 再生基盤はイベント駆動へ統一する。
  - `SoundEvent`: 再生トリガの列挙
  - `SoundCatalog`: `SoundEvent -> SoundAsset` の解決
  - `AudioEngine`: 再生・同時発音制御・音量適用
- 最終音量は以下で計算する。
  - `master * categoryGain * assetBaseGain`

## コンポーネント設計
- `SoundEvent`（enum）
- `SoundCategory`（enum: ui / gameplay / result）
- `SoundAsset`（assetId, path, baseGain, licenseId）
- `SoundSettings`（master, ui, gameplay, result, muted, version）
- `SoundCreditsEntry`（assetId, title, author, sourceUrl, licenseName）

### クライアント
- `AudioEngine`
  - 音声解放状態（初回ユーザー操作後）を管理
  - 同時発音上限と連打抑制（最小間隔）を適用
- `SoundCatalog`
  - イベントと音源メタ情報の対応表を保持
  - 旧URL再生からの移行マップを持つ（暫定）
- `Help/About` 画面
  - バージョン情報
  - 音源クレジット一覧

### サーバー
- ユーザー単位で音量設定を保存/取得するRPCを追加する。
- 保存形式は拡張性のためJSONを基本とする。

## 保存戦略（localStorage + サーバー）
- 起動時:
  1. localStorageを読み込み
  2. ログイン時はサーバー設定を取得
  3. サーバー優先でマージ（未設定項目はlocalで補完）
- 変更時:
  1. 画面反映とプレビュー再生
  2. localStorageへ即時保存
  3. ログイン時は遅延バッチでサーバー保存

## エラー処理 / 互換性
- 再生失敗時は「無音で継続」を原則にする（ゲーム進行優先）。
- 同種障害ログはレート制限して集約出力する。
- 音源ロード失敗時はフォールバック音へ退避し、再失敗時はスキップ。
- 既存API互換:
  - 当面は `play(url)` を残し内部で `SoundEvent` へ変換
  - 最終的に `play(SoundEvent)` へ統一

## テスト方針
- 単体テスト
  - イベント→音源解決
  - 音量計算
  - 設定マージ（local + server）
  - 連打抑制・同時発音上限
- 結合テスト
  - ログイン前後の設定同期
  - ミュート / カテゴリ別音量の即時反映
  - `Help/About` 表示内容とカタログ整合
- 手動検証
  - デスクトップ通常幅・中間幅・モバイル幅
  - 低速回線時のロード失敗挙動

## 導入ステップ
1. `SoundEvent` / `SoundCatalog` / `AudioEngine` の基盤追加
2. 既存SEをイベントベース再生へ置換
3. 音量UI（master + カテゴリ別）追加
4. localStorage + サーバー同期導入
5. `Help/About` タブと音源クレジット表示追加
6. 旧URLベース呼び出しの段階削減

## 完了条件
- 主要導線で旧SEが残っていない。
- 音量設定が未ログイン/ログイン双方で保持される。
- 音源クレジットがゲーム内で閲覧できる。
- ビルド・テスト・GWTコンパイルが成功する。
