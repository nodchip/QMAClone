# AGENTS.md（QMAClone）

## 適用範囲
- このファイルは QMAClone 固有ルールのみを扱う。
- 共通ルールは `C:\Users\nodchip\.codex\AGENTS.md` を参照する。

## 目的
- QMAClone で再発した障害を、環境差分（DevMode / Jetty / Tomcat / GWT / WebSocket）を前提に再発防止する。

## 過去の作業ミス（QMAClone固有）と改善案
- ミス: DevMode と Tomcat の差を考慮せず、クラスローダ起因の `NoClassDefFoundError` / `ClassCastException` を見落とした。
- 改善: Jetty 関連変更時は両例外をセットで確認し、DevMode と Tomcat で個別に起動検証する。
- ミス: GWT 再コンパイル失敗後に古い `cache.js` が配信され、画面不整合が起きた。
- 改善: クライアント変更の完了条件に「GWT 再コンパイル成功」と「配備先で最新成果物が参照されること」を含める。
- ミス: WebSocket URL を環境固定値で扱い、接続先不一致（404/500）を誘発した。
- 改善: URL は `protocol / host / contextPath` から導出し、クライアントログに接続先 URL を必ず含める。
- ミス: WebSocket 障害をサーバーログ確認前に断定し、調査が遠回りになった。
- 改善: `netstat -> HTTP GET -> Upgrade ハンドシェイク -> サーバーログ` の順で切り分ける。
- ミス: Tomcat 再配備時に旧状態が残り、修正結果を誤判定した。
- 改善: 必要時は旧展開物削除とサービス再起動を行い、静的状態を破棄する。

## プロジェクト固有ルール

### GWT / クライアント
- クライアント変更後は GWT 再コンパイル成功を完了条件に含める。
- 基盤クラス（例: `StatusUpdater`）変更時は、`@Override` エラー連鎖を関連画面まで確認する。
- `cache.js` を更新した場合、配備先が最新成果物を参照していることを確認する。

### Jetty / Tomcat / クラスローダ
- DevMode と Tomcat でクラスローダ挙動が異なる前提で検証する。
- Jetty 関連依存の変更時は、`NoClassDefFoundError` と `ClassCastException` の両方を確認する。
- サーバー初期化失敗時は Guice バインド不足（`No implementation for ...`）を優先確認する。

### WebSocket
- WebSocket URL は実行中の `protocol / host / contextPath` から導出し、環境固定値に依存しない。
- 接続障害時は次の順で確認する。
1. 待受確認（`netstat`）
2. HTTP 到達確認（通常 GET）
3. Upgrade ハンドシェイク確認（`curl.exe`）
4. サーバーログの例外確認（500/503、初期化失敗）
- クライアントログには接続先 URL を含め、追跡可能な形で出力する。

### デプロイ / ローカル運用
- Tomcat 再配備時は、必要に応じて旧展開物削除とサービス再起動で静的状態を確実に破棄する。
- Eclipse で不整合が疑われる場合は、`target` と `gwt-unitCache` のクリーンを実施する。

## 禁止事項
- 依存スコープ変更を、影響評価と実機検証なしで反映しない。
- BOM 混入やコメント崩れを残したまま次の修正に進まない。
- WebSocket エラーを、サーバーログ未確認のままクライアント側だけで断定しない。
## 追加の振り返り（2026-02-10, QMAClone）
- ミス: Google連携設定画面で、初期ロード時の一覧表示と「Googleログインして紐づけ済みユーザーコードを表示する」押下後の一覧表示を同一状態として扱い、選択したユーザーコードに切り替える の表示条件が揺れた。
- 改善: 設定画面の一覧表示は「初期ロード由来」と「ユーザー操作由来」で分岐を分け、Presenterのコールバックも分離して実装する。

### 設定画面UX（Google連携）
- onLoad 由来の表示と、showUserCodeList 押下由来の表示は別フローとして扱う。
- 1件表示時の切り替えボタンは、初期ロードでは非表示、ユーザー操作由来では表示を基本とする。