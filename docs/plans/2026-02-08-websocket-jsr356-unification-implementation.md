# WebSocket JSR356 統一 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** DevMode / Windows Tomcat 9 / Ubuntu Tomcat 9 の3環境で、同一WAR・同一オリジンURLで WebSocket 通信を安定稼働させる。

**Architecture:** Jetty 固有の `WebSocketServlet` 実装を `javax.websocket` (`@ServerEndpoint`) に移行し、サーバー実装依存を除去する。`MessageSender<T>` は JSR356 `Session` を扱う共通送信基盤として維持し、業務層 (`ChatManager` / `ServerStatusManager` / `Game`) は既存の `send()` 呼び出しを継続する。クライアント接続先は `http->ws` / `https->wss` の同一オリジンに統一する。

**Tech Stack:** Java 8, javax.websocket (JSR356), GWT, Tomcat 9, Jetty DevMode, Maven, JUnit4

---

### Task 1: 接続先解決ロジックの仕様固定（テスト先行）

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java`

**Step 1: Write the failing test**

`ConstantTest` に以下の期待を追加する。
- `localhost` -> `ws://localhost/QMAClone/websocket/`
- `127.0.0.1` -> `ws://127.0.0.1:8888/QMAClone/websocket/`（`Location.getHost()` 利用を想定）
- 非ローカル -> 既存の外部 URL

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=tv.dyndns.kishibe.qmaclone.client.constant.ConstantTest test`
Expected: FAIL（旧ロジックが `:60080` 固定のため）

**Step 3: Write minimal implementation**

`Constant.getWebSocketUrl()` を `Location.getHost()` ベースへ変更し、`https` 時は `wss://` になるよう変換する補助関数を追加する。

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=tv.dyndns.kishibe.qmaclone.client.constant.ConstantTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java
git commit -m "WebSocket接続先を同一オリジンに統一"
```

### Task 2: MessageSender を JSR356 Session へ移行（テスト先行）

**Files:**
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/MessageSenderTest.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/MessageSender.java`

**Step 1: Write the failing test**

`MessageSenderTest` を `javax.websocket.Session` / `RemoteEndpoint.Async` モック前提に更新し、`join` / `bye` / `send` の挙動を検証する。

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=tv.dyndns.kishibe.qmaclone.server.websocket.MessageSenderTest test`
Expected: FAIL（Jetty API 依存差異）

**Step 3: Write minimal implementation**

`MessageSender` の `Session` 型を JSR356 に差し替え、`getAsyncRemote().sendText(...)` を使用する。送信失敗時はセッション除去を維持する。

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=tv.dyndns.kishibe.qmaclone.server.websocket.MessageSenderTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/MessageSender.java src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket/MessageSenderTest.java
git commit -m "MessageSenderをJSR356 Session対応へ移行"
```

### Task 3: 5系統の WebSocket エンドポイントを @ServerEndpoint 化

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/ChatMessagesWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/ServerStatusWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/MatchingStatusWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/ReadyForGameWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/GameStatusWebSocketServlet.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket/GameUtil.java`

**Step 1: Write the failing test**

各 Servlet テストを `@OnOpen/@OnClose/@OnError` 呼び出しを前提としたテストへ更新し、`join/bye` と `gameSessionId` 抽出を検証する。

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=*WebSocketServletTest test`
Expected: FAIL（API 不一致）

**Step 3: Write minimal implementation**

各クラスを `WebSocketServlet` 継承から `@ServerEndpoint("/websocket/...")` 方式へ移行し、`Session` と `EndpointConfig` を使う。`GameUtil` は JSR356 用 query/path 解析に合わせる。

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=*WebSocketServletTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/websocket src/test/java/tv/dyndns/kishibe/qmaclone/server/websocket
git commit -m "WebSocketエンドポイントをJSR356へ移行"
```

### Task 4: Guice 構成から Jetty 固有モジュールを撤去

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/QMACloneModule.java`
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStub.java`
- Modify: `src/test/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStubTest.java`

**Step 1: Write the failing test**

`ServiceServletStubTest` でコンストラクタ依存（Jetty `Server`）除去後の生成が通ることを検証する。

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=tv.dyndns.kishibe.qmaclone.server.ServiceServletStubTest test`
Expected: FAIL（コンストラクタ差分）

**Step 3: Write minimal implementation**

`QMACloneModule` から `WebSocketModule` 依存を除外し、`MessageSender<T>` を `@Provides` でバインドする。`ServiceServletStub` の Jetty `Server` 参照は削除する。

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=tv.dyndns.kishibe.qmaclone.server.ServiceServletStubTest test`
Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/server/QMACloneModule.java src/main/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStub.java src/test/java/tv/dyndns/kishibe/qmaclone/server/ServiceServletStubTest.java
git commit -m "Guice構成からJetty固有依存を分離"
```

### Task 5: E2E 検証（3環境）

**Files:**
- Modify: `docs/plans/2026-02-08-websocket-jsr356-unification-implementation.md`

**Step 1: Build and basic verify**

Run: `mvn -DskipTests compile`
Expected: BUILD SUCCESS

**Step 2: DevMode 検証**

- Eclipse DevMode 起動
- ブラウザで `/QMAClone.html` を開く
- Network タブで `ws://127.0.0.1:8888/QMAClone/websocket/...` が `101` になること

**Step 3: Windows Tomcat 9 検証**

- `http://localhost:8080/...` で接続
- `ws://localhost:8080/QMAClone/websocket/...` が `101`

**Step 4: Ubuntu Tomcat 9 検証**

- `https://...` 配下で接続
- `wss://<host>/QMAClone/websocket/...` が `101`

**Step 5: Commit**

```bash
git add docs/plans/2026-02-08-websocket-jsr356-unification-implementation.md
git commit -m "WebSocket JSR356移行の検証結果を記録"
```
