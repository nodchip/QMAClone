# WebSocket Transport Unification Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** DevMode (Jetty) と Tomcat/本番 (JSR-356) の WebSocket 実装を、接続管理・ルーティング・監視ログの3層で統一し、環境差分による再発障害を防止する。

**Architecture:** 既存の二系統実装（Servlet / Endpoint）は維持し、共通処理を `WebSocketTransportFacade` に集約する。セッション差分は `WebSocketSessionAdapter` で吸収し、`WebSocketRouteRegistry` を単一ソースとしてサーバ登録とクライアントURL導出の整合を保証する。障害時は標準化ログキーとエラーコードで追跡可能性を確保する。

**Tech Stack:** Java 8, GWT 2.9, Jetty WebSocketServlet, JSR-356 (`javax.websocket`), Guice, JUnit4/Mockito, Maven

---

### Task 1: ベースライン固定（現行挙動をテストで可視化）

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java`
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketRouteCompatibilityTest.java`

**Step 1: Write the failing test**
- `Constant` の URL 導出に対して、以下を明示するテストを追加する。
  - DevMode root: `ws://127.0.0.1:8888/devmode-websocket/`
  - Tomcat local: `ws://localhost:8080/<context>/websocket/`
  - HTTPS: `wss://.../websocket/`
- 新規 `WebSocketRouteCompatibilityTest` で、現在の packet 別パス定義の期待値を固定する（文字列比較）。

**Step 2: Run test to verify it fails**
- Run: `mvn -DskipTests=false "-Dtest=tv.dyndns.kishibe.qmaclone.client.constant.ConstantTest,tv.dyndns.kishibe.qmaclone.server.websocket.WebSocketRouteCompatibilityTest" surefire:test`
- Expected: 新規互換テストが FAIL（まだルートレジストリ未導入のため）。

**Step 3: Write minimal implementation**
- このタスクでは実装は行わず、次タスクで使う失敗テストを確定する。

**Step 4: Run test to verify baseline is captured**
- Run: 同上
- Expected: 意図した失敗だけが残る。

**Step 5: Commit**
- `git add src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketRouteCompatibilityTest.java`
- `git commit -m "WebSocket URLとルート互換性のベースラインテストを追加"`

### Task 2: ルート定義の単一化（WebSocketRouteRegistry）

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketRoute.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketRouteRegistry.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java`
- Modify: `src/main/webapp/WEB-INF/web.xml`
- Test: `src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketRouteCompatibilityTest.java`

**Step 1: Write the failing test**
- `WebSocketRouteRegistry` が packet ごとに `websocket` / `devmode-websocket` 両パスを返すテストを追加。

**Step 2: Run test to verify it fails**
- Run: `mvn -DskipTests=false "-Dtest=tv.dyndns.kishibe.qmaclone.server.websocket.WebSocketRouteCompatibilityTest" surefire:test`
- Expected: `WebSocketRouteRegistry` 未実装で FAIL。

**Step 3: Write minimal implementation**
- `WebSocketRoute` を immutable な値オブジェクトとして追加。
- `WebSocketRouteRegistry` を static 定義で作成（既存 packet 5種）。
- `Constant` の URL 導出を registry ベースに寄せる（最低限 API を経由）。
- `web.xml` の devmode servlet mapping は registry 定義と一致させる。

**Step 4: Run test to verify it passes**
- Run: 上記 surefire + `mvn compile`
- Expected: PASS / BUILD SUCCESS

**Step 5: Commit**
- `git add src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketRoute.java src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketRouteRegistry.java src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java src/main/webapp/WEB-INF/web.xml src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketRouteCompatibilityTest.java`
- `git commit -m "WebSocketルート定義をレジストリに集約"`

### Task 3: セッション抽象化（WebSocketSessionAdapter）

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketSessionAdapter.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/JettySessionAdapter.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/Jsr356SessionAdapter.java`
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketSessionAdapterTest.java`

**Step 1: Write the failing test**
- 2実装が同じ契約（id/address/send/close）で振る舞うテストを作成。

**Step 2: Run test to verify it fails**
- Run: `mvn -DskipTests=false "-Dtest=tv.dyndns.kishibe.qmaclone.server.websocket.WebSocketSessionAdapterTest" surefire:test`
- Expected: クラス未存在で FAIL。

**Step 3: Write minimal implementation**
- Adapter interface と最小実装を追加。
- null 安全と例外伝播方針を明示。

**Step 4: Run test to verify it passes**
- Run: 上記 surefire + `mvn compile`
- Expected: PASS / BUILD SUCCESS

**Step 5: Commit**
- `git add src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketSessionAdapter.java src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/JettySessionAdapter.java src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/Jsr356SessionAdapter.java src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketSessionAdapterTest.java`
- `git commit -m "WebSocketセッション抽象化を導入"`

### Task 4: 共通接続管理（WebSocketTransportFacade）

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketTransportFacade.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketErrorCode.java`
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketTransportFacadeTest.java`

**Step 1: Write the failing test**
- `handleOpen` で sender.join が呼ばれる。
- `handleClose` で sender.bye が呼ばれる。
- `handleError` で標準エラーコードが返る。

**Step 2: Run test to verify it fails**
- Run: `mvn -DskipTests=false "-Dtest=tv.dyndns.kishibe.qmaclone.server.websocket.WebSocketTransportFacadeTest" surefire:test`
- Expected: 未実装で FAIL。

**Step 3: Write minimal implementation**
- facade と error code enum を実装。
- route から sender 解決し join/bye 実行。

**Step 4: Run test to verify it passes**
- Run: 上記 surefire + `mvn compile`
- Expected: PASS / BUILD SUCCESS

**Step 5: Commit**
- `git add src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketTransportFacade.java src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketErrorCode.java src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketTransportFacadeTest.java`
- `git commit -m "WebSocket接続管理をFacadeへ集約"`

### Task 5: Servlet/Endpoint を薄いアダプタへ変更

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/ServerStatusWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/ChatMessagesWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/MatchingStatusWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/ReadyForGameWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/GameStatusWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/*WebSocketEndpoint.java` (5 files)
- Test: 既存 servlet/endpoint テスト群

**Step 1: Write the failing test**
- 既存テストを、直接 sender を触る期待から facade 呼び出し期待へ更新。

**Step 2: Run test to verify it fails**
- Run: `mvn -DskipTests=false "-Dtest=tv.dyndns.kishibe.qmaclone.server.websocket.*WebSocket*Test" surefire:test`
- Expected: 旧実装前提のため FAIL。

**Step 3: Write minimal implementation**
- 各 servlet/endpoint を facade 呼び出し中心に縮小。
- GameSessionId 抽出は `GameUtil` / 抽出ヘルパを経由。

**Step 4: Run test to verify it passes**
- Run: 上記 surefire + `mvn compile`
- Expected: PASS / BUILD SUCCESS

**Step 5: Commit**
- 変更対象をまとめて add/commit。
- `git commit -m "WebSocket Servlet/Endpointを共通Facadeへ委譲"`

### Task 6: ログ標準化

**Files:**
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketEventLogger.java`
- Create: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketLogContext.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/StatusUpdater.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/*` (必要箇所)
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/WebSocketEventLoggerTest.java`

**Step 1: Write the failing test**
- error/open/close で必須キー（route, packetType, resolvedUrl, runtimeMode, errorCode）が出ることをテスト。

**Step 2: Run test to verify it fails**
- Run: `mvn -DskipTests=false "-Dtest=tv.dyndns.kishibe.qmaclone.server.websocket.WebSocketEventLoggerTest" surefire:test`
- Expected: logger 未実装で FAIL。

**Step 3: Write minimal implementation**
- 標準ログ出力ユーティリティを実装。
- クライアント警告ログにも `resolvedUrl` を含める。

**Step 4: Run test to verify it passes**
- Run: 上記 surefire + `mvn compile`
- Expected: PASS / BUILD SUCCESS

**Step 5: Commit**
- `git commit -m "WebSocketイベントログを標準化"`

### Task 7: 結合検証と運用手順更新

**Files:**
- Modify: `deploy_qmaclone_tomcat9.ps1` (必要時)
- Modify: `AGENTS.md` (必要時のみ、手順追加)
- Create: `docs/plans/2026-02-09-websocket-transport-unification-verification.md`

**Step 1: Write verification checklist**
- DevMode/Tomcat local/prod の確認項目をチェックリスト化。

**Step 2: Execute verification commands**
- Run (順番厳守):
  1. `mvn compile`
  2. `mvn test`
  3. DevMode 実機確認（`127.0.0.1:8888`）
  4. Tomcat 実機確認（`localhost:8080`）
- Expected: handshake 成功、標準ログキー一致、RPC fallback 正常。

**Step 3: Capture evidence**
- 成功ログ/失敗ログの実例を検証メモへ保存。

**Step 4: Commit docs/scripts**
- `git commit -m "WebSocket統一実装の検証結果と手順を追加"`

---

## Final integration steps
1. `git log --oneline -n 10` でコミット粒度を確認。
2. 必要なら `docs/plans/2026-02-09-websocket-transport-unification-design.md` へ実装差分を反映。
3. 最終確認:
   - `mvn compile` success
   - `mvn test` success
   - DevMode/Tomcat で WebSocket 接続成功

## Notes
- すべてのタスクは TDD の Red -> Green -> Refactor を遵守する。
- 依存関係のある実行は必ず逐次実行する。
- 1コミット1目的を徹底する。