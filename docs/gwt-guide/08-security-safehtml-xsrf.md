# 08. セキュリティ（SafeHtml / RPC XSRF）

対象:
- SafeHtml: https://www.gwtproject.org/doc/latest/DevGuideSecuritySafeHtml.html
- RPC XSRF: https://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html

## 要点
- HTMLを動的に出す箇所は `SafeHtmlBuilder` / `SafeHtmlTemplates` を使う。
- 生文字列連結によるHTML挿入は避ける。
- RPCのXSRF対策は、トークン運用とサーバ検証を前提に設計する。

## QMAClone適用
- テーブルセルや差分表示は `SafeHtml` で構築する。
- UI改善でHTML構築を追加する際は、エスケープ責務を必ず明示する。
- 既存RPCに認証・権限を絡める改修時は、XSRF前提で仕様化する。

## 関連
- RPC: [07-rpc.md](07-rpc.md)
- CellTable: [05-celltable.md](05-celltable.md)
