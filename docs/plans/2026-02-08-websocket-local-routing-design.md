# ローカル起動時のWebSocket接続先固定 設計

## 背景
Eclipse DevMode (`http://127.0.0.1:8888/QMAClone.html`) とローカル Tomcat9 (`http://localhost:8080/QMAClone-1.0-SNAPSHOT/`) の両方で、WebSocket セッションをローカルサーバーへ接続したい。

現行実装はポート番号 (`:8888` / `:8080`) で判定しているため、将来的な起動ポート変更に弱く、要件の意図（ローカルホストならローカル接続）を直接表現できていない。

## 目的
- ローカル起動時は常に `ws://localhost:60080/QMAClone/websocket/` を使う。
- 非ローカル起動時は既存の外部接続先を維持する。

## 対象
- `src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java`

## 設計方針
1. WebSocket 接続先の判定をポート依存からホスト依存へ変更する。
2. `Location.getHostName()` を利用し、`localhost` または `127.0.0.1` の場合にローカル接続先を返す。
3. それ以外は既存の外部接続先 (`ws://kishibe.dyndns.tv/QMAClone/websocket/`) を返す。
4. URL 文字列は定数として保持し、分岐ロジックを簡潔にする。

## 仕様
- `GWT.isClient() == true` かつ `hostName in {"localhost", "127.0.0.1"}`:
  - `ws://localhost:60080/QMAClone/websocket/`
- それ以外:
  - `ws://kishibe.dyndns.tv/QMAClone/websocket/`

## 非対象
- `http://<PC名>:8080/...` でのローカル判定対応（今回不要）。
- サーバー側 WebSocket ポートの変更。

## 影響範囲
- クライアントの WebSocket 接続先決定ロジックのみ。
- 既存の通信プロトコルやサーバー実装には影響しない。

## 検証観点
1. DevMode: `http://127.0.0.1:8888/QMAClone.html` で `ws://localhost:60080/...` に接続される。
2. ローカル Tomcat: `http://localhost:8080/QMAClone-1.0-SNAPSHOT/` で `ws://localhost:60080/...` に接続される。
3. 非ローカル環境では `ws://kishibe.dyndns.tv/...` を維持する。

## リスク
- ローカル判定を厳密一致にしているため、`localhost` / `127.0.0.1` 以外のローカルアクセス（PC名など）は外部接続扱いになる。
- これは要件で許容されている。
