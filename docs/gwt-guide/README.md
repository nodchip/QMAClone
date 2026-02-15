# GWTプログラミングガイド（QMAClone向け索引）

最終更新: 2026-02-15

このディレクトリは、公式GWTドキュメントをQMAClone開発向けに要約した参照集です。
公式全文の複製ではなく、実装判断に必要な観点とプロジェクト内の適用ポイントを整理しています。

## 利用順（推奨）
1. [01-overview.md](01-overview.md)
2. [02-project-structure.md](02-project-structure.md)
3. [03-compile-debug-cli.md](03-compile-debug-cli.md)
4. [04-uibinder.md](04-uibinder.md)
5. [05-celltable.md](05-celltable.md)
6. [06-css-styling.md](06-css-styling.md)
7. [07-rpc.md](07-rpc.md)
8. [08-security-safehtml-xsrf.md](08-security-safehtml-xsrf.md)
9. [09-jsinterop.md](09-jsinterop.md)
10. [10-deferred-binding.md](10-deferred-binding.md)
11. [11-code-splitting.md](11-code-splitting.md)
12. [12-clientbundle.md](12-clientbundle.md)

## 公式ページ対応表
- DevGuide: https://www.gwtproject.org/doc/latest/DevGuide.html
- Organize Projects: https://www.gwtproject.org/doc/latest/DevGuideOrganizingProjects.html
- Compile & Debug: https://www.gwtproject.org/doc/latest/DevGuideCompilingAndDebugging.html
- Command Line Tools: https://www.gwtproject.org/doc/latest/RefCommandLineTools.html
- UiBinder: https://www.gwtproject.org/doc/latest/DevGuideUiBinder.html
- Cell Tables: https://www.gwtproject.org/doc/latest/DevGuideUiCellTable.html
- CSS Styling: https://www.gwtproject.org/doc/latest/DevGuideUiCss.html
- Server Communication: https://www.gwtproject.org/doc/latest/DevGuideServerCommunication.html
- RPC Tutorial: https://www.gwtproject.org/doc/latest/tutorial/RPC.html
- SafeHtml: https://www.gwtproject.org/doc/latest/DevGuideSecuritySafeHtml.html
- RPC XSRF: https://www.gwtproject.org/doc/latest/DevGuideSecurityRpcXsrf.html
- JsInterop: https://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJsInterop.html
- Deferred Binding: https://www.gwtproject.org/doc/latest/DevGuideCodingBasicsDeferred.html
- Code Splitting: https://www.gwtproject.org/doc/latest/DevGuideCodeSplitting.html
- ClientBundle: https://www.gwtproject.org/doc/latest/DevGuideClientBundle.html

## QMACloneの前提
- サーバ通信はGWT RPCが中心（`Service` / `ServiceAsync`）。
- 画面はUiBinder + Javaロジックの混在。
- テーブルUIはCellTableを利用。
- デプロイはTomcat向けWAR生成 + GWTコンパイル成果物同期。

## 補足
- 迷ったら、まず [07-rpc.md](07-rpc.md) と [08-security-safehtml-xsrf.md](08-security-safehtml-xsrf.md) を確認してください。
- UI実装時は [04-uibinder.md](04-uibinder.md)・[05-celltable.md](05-celltable.md)・[06-css-styling.md](06-css-styling.md) をセットで参照してください。
