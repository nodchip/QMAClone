# WebSocket接続先ローカル固定 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** ローカル起動時のWebSocket接続先をホスト名判定で `ws://localhost:60080/QMAClone/websocket/` に固定し、非ローカル時は既存の外部接続先を維持する。

**Architecture:** `Constant` にローカル/リモートURL定数を追加し、`Location.getHost()` ベースの判定を `Location.getHostName()` ベースへ置き換える。判定ロジックを `resolveWebSocketUrlForHostName` に分離して単体テスト可能にする。`getWebSocketUrl` は `GWT.isClient` と `Location` 取得の例外を安全に扱う。

**Tech Stack:** Java 8, GWT, JUnit4, Maven

---

### Task 1: WebSocket接続先判定の失敗テストを先に追加する

**Files:**
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java`

**Step 1: 失敗するテストを書く**

```java
package tv.dyndns.kishibe.qmaclone.client.constant;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test for {@link Constant}.
 */
@RunWith(JUnit4.class)
public class ConstantTest {
  @Test
  public void resolveWebSocketUrlForHostNameShouldUseLocalForLocalhost() {
    assertEquals("ws://localhost:60080/QMAClone/websocket/",
        Constant.resolveWebSocketUrlForHostName(true, "localhost"));
  }

  @Test
  public void resolveWebSocketUrlForHostNameShouldUseLocalForLoopbackIp() {
    assertEquals("ws://localhost:60080/QMAClone/websocket/",
        Constant.resolveWebSocketUrlForHostName(true, "127.0.0.1"));
  }

  @Test
  public void resolveWebSocketUrlForHostNameShouldUseRemoteForNonLocal() {
    assertEquals("ws://kishibe.dyndns.tv/QMAClone/websocket/",
        Constant.resolveWebSocketUrlForHostName(true, "kishibe.dyndns.tv"));
  }
}
```

**Step 2: テストを実行して失敗を確認する**

Run: `C:\Users\nodchip\tools\apache-maven-3.9.6\bin\mvn.cmd -Dtest=tv.dyndns.kishibe.qmaclone.client.constant.ConstantTest test`  
Expected: `Constant.resolveWebSocketUrlForHostName` が未定義で失敗

### Task 2: ホスト名判定ロジックを実装してテストを通す

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java`

**Step 1: URL定数と判定メソッドを追加する**

```java
private static final String WEB_SOCKET_URL_LOCAL = "ws://localhost:" + WEB_SOCKET_PORT
    + "/QMAClone/websocket/";
private static final String WEB_SOCKET_URL_REMOTE = "ws://kishibe.dyndns.tv/QMAClone/websocket/";

private static String getWebSocketUrl() {
  if (!GWT.isClient()) {
    return WEB_SOCKET_URL_REMOTE;
  }

  try {
    return resolveWebSocketUrlForHostName(true, Location.getHostName());
  } catch (Throwable e) {
    return WEB_SOCKET_URL_REMOTE;
  }
}

static String resolveWebSocketUrlForHostName(boolean isClient, String hostName) {
  if (isClient && ("localhost".equals(hostName) || "127.0.0.1".equals(hostName))) {
    return WEB_SOCKET_URL_LOCAL;
  }
  return WEB_SOCKET_URL_REMOTE;
}
```

**Step 2: テストを再実行して成功を確認する**

Run: `C:\Users\nodchip\tools\apache-maven-3.9.6\bin\mvn.cmd -Dtest=tv.dyndns.kishibe.qmaclone.client.constant.ConstantTest test`  
Expected: PASS

### Task 3: ビルドと全体テストを直列で検証する

**Files:**
- Modify: `src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java`
- Create: `src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java`

**Step 1: ビルド実行**

Run: `C:\Users\nodchip\tools\apache-maven-3.9.6\bin\mvn.cmd -DskipTests build-helper:parse-version package`  
Expected: BUILD SUCCESS

**Step 2: テスト実行**

Run: `C:\Users\nodchip\tools\apache-maven-3.9.6\bin\mvn.cmd test -DskipITs`  
Expected: 既存テスト含め成功、少なくとも `ConstantTest` は PASS

**Step 3: コミット**

```bash
git add src/main/java/tv/dyndns/kishibe/qmaclone/client/constant/Constant.java src/test/java/tv/dyndns/kishibe/qmaclone/client/constant/ConstantTest.java docs/plans/2026-02-08-websocket-local-routing-implementation.md
git commit -m "WebSocket接続先のローカル判定をホスト名ベースへ変更"
```
