# 07. サーバ通信（GWT RPC中心）

対象:
- Server Communication: https://www.gwtproject.org/doc/latest/DevGuideServerCommunication.html
- RPC Tutorial: https://www.gwtproject.org/doc/latest/tutorial/RPC.html

## 要点
- GWT RPCは `Service` / `ServiceAsync` / DTO（Serializable）で構成する。
- 非同期コールバックの失敗ハンドリングを統一しないと、画面ごとの挙動差が生じる。
- クライアント・サーバで共有する型の互換性を維持することが重要。

## QMAClone適用
- RPCコールバックは `RpcAsyncCallback` を使用し、共通失敗処理に寄せる。
- 旧GWTキャッシュ起因の通信不整合は `StaleRpcFailureDetector` を利用して切り分ける。
- DTO変更時は client/server 両方のコンパイルと画面動作を確認する。

## 関連
- SafeHtml/XSRF: [08-security-safehtml-xsrf.md](08-security-safehtml-xsrf.md)
- コンパイル: [03-compile-debug-cli.md](03-compile-debug-cli.md)
