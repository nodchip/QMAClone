# Facebookアクセストークン自動取得・自動更新 設計

## 背景と目的
- 現在の `FacebookClient` は `facebook_access_token` を読み、`/1060467615/accounts` から都度ページトークンを取得して投稿している。
- 実運用では Facebook OAuth 失効により `OAuthException` が発生し、定期投稿が失敗する。
- 手動でのトークン差し替えを廃止し、管理画面からの再認可とサーバー側自動更新で運用負荷と停止時間を下げる。

## 採用方針（推奨案）
- 採用: `Facebook Login` による管理画面再認可 + サーバー側トークン交換・保存
- 非採用:
1. 失効検知のみ自動化（再取得は手動）: 要件の「自動取得」を満たさない
2. 完全無人で永続更新: Facebook仕様上、再認可が必要になるケースを避けられない

## アーキテクチャ
- 追加コンポーネント
1. `FacebookAuthController`
  - 認可開始エンドポイント（state生成）
  - callback エンドポイント（code受領）
2. `FacebookAuthService`
  - code -> short-lived user token
  - short-lived -> long-lived user token
  - `/me/accounts` から `page_id` の page token 抽出
  - トークン有効性チェック
3. `FacebookTokenRepository`
  - DB保管・更新・失効判定
4. `FacebookPostingService`（既存 `FacebookClient` の責務整理）
  - 投稿時に有効な page token を取得
  - 失敗時の1回リトライ（再取得）

- 既存変更点
1. `FacebookClient#getPageAccessToken` の JSON文字列分解ロジックを廃止
2. `FacebookClient#post` は `FacebookAuthService` 経由で token を取得
3. ログを構造化（token値は絶対に出力しない）

## データモデル（Database password keys）
- `facebook_app_id`
- `facebook_app_secret`
- `facebook_page_id`
- `facebook_user_access_token`
- `facebook_user_access_token_expires_at`（epoch秒）
- `facebook_page_access_token`
- `facebook_last_refresh_at`
- `facebook_auth_state_secret`（state検証用）

## 認可・更新フロー
1. 管理者が管理画面で「Facebook連携を更新」を実行
2. サーバーが `state` を生成し、Facebook認可URLへリダイレクト
3. callback で `code` と `state` を受信し、`state` を検証
4. Graph APIで user token を交換（長期化）
5. `/me/accounts` から対象 `page_id` の page token を取得
6. DBへ保存し、連携状態を `ACTIVE` に更新
7. 投稿処理は `facebook_page_access_token` を優先使用
8. 投稿時に `OAuthException`（無効token）なら1回だけ `FacebookAuthService` で再取得を試行

## エラーハンドリングと運用
- 管理画面再認可時
1. state不一致: 即失敗、監査ログ記録
2. token交換失敗: UIに理由表示（権限不足/期限切れ）
- 投稿時
1. token無効: 再取得リトライ1回
2. 再取得失敗: 投稿をスキップし、管理画面に「再認可必要」フラグを出す
- 定期監視
1. 1日1回、期限・有効性チェックジョブを実行
2. 期限閾値（例7日）未満で警告ログ

## セキュリティ要件
- callback URLは固定・HTTPS必須
- `state` と nonce 検証必須
- App Secret / token はマスクして保存・表示
- 管理画面の操作は既存管理者権限に限定
- 監査ログに `who/when/result` を残す（token文字列は残さない）

## テスト戦略
- 単体テスト
1. token交換成功/失敗
2. `/me/accounts` から page token 抽出
3. state検証OK/NG
4. 投稿時の無効tokenリトライ
- 結合テスト（モックGraph API）
1. 認可開始 -> callback -> DB保存
2. 失効tokenからの回復
- 回帰テスト
1. 既存の `FacebookClientTest` を `FacebookAuthService` 中心に再編
2. SNS投稿（Twitter含む）への副作用がないことを確認

## 段階的リリース計画
1. Phase 1: `FacebookAuthService` + `FacebookTokenRepository` 実装（投稿ロジック切替なし）
2. Phase 2: 管理画面の認可導線追加（手動運用と併用）
3. Phase 3: `FacebookClient` を新方式へ切替
4. Phase 4: 旧 `facebook_access_token` 参照を削除し、運用手順を更新

## 完了条件
- 管理画面から再認可できる
- page token 自動更新後に投稿が成功する
- token失効時、再取得または再認可要求に遷移できる
- token値がログ・画面・例外メッセージに漏れない
