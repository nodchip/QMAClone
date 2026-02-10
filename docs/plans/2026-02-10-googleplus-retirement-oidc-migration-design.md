# Google+連携廃止とOIDC移行設計（A案）

## 1. 目的と前提
- Google+連携機能を廃止し、Google Identity Services（OIDC）へ移行する。
- 移行方式は「初回ログイン時再連携」とする。
- 対象範囲は全面整理（認証、プロフィール、UI、設定、文言、DB）。
- Google+サービスは終了済みのため、外部照合による一括変換は行わない。

## 2. 全体アーキテクチャ方針
- 置換方針は「Google+固有ID」から「認証プロバイダ共通ID」への抽象化とする。
- 既存の `googlePlusId` は段階的に `provider + subject` へ置換する。
- 主キーとなる認証情報:
  - `AUTH_PROVIDER`（例: `google`）
  - `AUTH_SUB`（OIDC `sub`）
- 互換期間は旧導線と新導線を併存し、最終的に旧導線を削除する。

## 3. データモデル/DB設計
- `player` テーブルへ以下を追加する。
  - `AUTH_PROVIDER`（NULL許容で導入）
  - `AUTH_SUB`（NULL許容で導入）
- 一意制約:
  - `UNIQUE (AUTH_PROVIDER, AUTH_SUB)`
- 既存 `GOOGLE_PLUS_ID` は互換期間中は残す（即時削除しない）。
- 起動時自動DDLを導入する。
  - `INFORMATION_SCHEMA` で列・インデックス存在確認
  - 不足時のみ `ALTER TABLE` / `CREATE INDEX` 実行
  - 冪等に実装し、再起動時の再実行で壊れないことを保証

## 4. サーバー/RPCコンポーネント方針
- `Database` / `DirectDatabase` / `CachedDatabase` のGoogle+専用メソッドを、外部認証汎用メソッドへ移行する。
  - 例: `lookupUserCodeByGooglePlusId` -> `lookupUserDataByExternalAccount(provider, sub)`
  - 例: `disconnectUserCodeFromGooglePlus` -> `disconnectExternalAccount`
- `Service` / `ServiceAsync` / `ServiceServletStub` も同様に名称・意味を更新する。
- `PacketUserData` は `googlePlusId` 依存を整理し、`authProvider` / `authSubject` を保持する。
- 互換期間は旧APIを内部委譲で残し、呼び出し元置換後に削除する。

## 5. 初回再連携データフロー
1. クライアントでGISを使ってIDトークンを取得する。
2. サーバーでトークンの署名、`aud`、`exp` を検証する。
3. 検証済み `provider=google` と `sub` でユーザー検索する。
4. 見つかれば通常ログインする。
5. 見つからない場合は再連携導線に遷移し、既存ユーザー確認を行う。
6. 確認成功時に `AUTH_PROVIDER` / `AUTH_SUB` を保存し、旧連携情報を無効化する。
7. 次回以降は新キーのみでログインする。

## 6. エラーハンドリング方針
- 認証エラー（トークン不正、期限切れ、`aud` 不一致）
  - 連携処理を中断し、再ログインを促す。
- 競合エラー（同一 `provider+sub` の多重紐付け）
  - 更新を拒否し、サポート可能なエラーコードを返す。
- 整合性エラー（ユーザー確認情報不足/不一致）
  - 完了扱いにせず、再試行可能な状態を維持する。
- 基盤エラー（DB/ネットワーク）
  - 更新をロールバックし、未完了で終了する。

## 7. 監査ログ/運用
- 追跡性確保のため、最低限以下のイベントログを出力する。
  - `link_start`
  - `link_success`
  - `link_conflict`
  - `link_failed`
- 障害時はログ起点で切り分ける（推測で断定しない）。

## 8. テスト計画（直列実行）
### 8.1 スキーマ検証
- 列なし状態で起動し、自動追加されることを確認する。
- 2回目起動で再追加されないこと（冪等）を確認する。
- 一意制約により重複登録が拒否されることを確認する。

### 8.2 サーバー単体テスト
- 外部認証キー検索/解除APIが期待通り動作することを確認する。
- 競合時に期待するエラーコードが返ることを確認する。
- 互換期間の旧API委譲が維持されることを確認する。

### 8.3 クライアントテスト
- 未連携/連携済み/競合時の表示分岐を確認する。
- 初回再連携フローの遷移とメッセージを確認する。
- 旧Google+文言の残存がないことを確認する。

### 8.4 回帰確認
- 既存ユーザー再連携後に継続利用できることを確認する。
- 新規ユーザー登録が成立することを確認する。
- DevMode/CodeServer + Tomcat の両方でログイン経路を確認する。

## 9. 完了条件
- 新規ユーザーはGIS/OIDCで登録・ログインできる。
- 既存ユーザーは初回再連携で既存データへ到達できる。
- 旧Google+ API呼び出しが0になる。
- build -> test を直列実行し、結果を記録できる。
