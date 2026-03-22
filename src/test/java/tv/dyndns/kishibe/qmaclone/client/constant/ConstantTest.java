package tv.dyndns.kishibe.qmaclone.client.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link Constant}.
 */
public class ConstantTest {
  @Test
  public void resolveWebSocketUrlForLocationShouldUseWsForHttp() {
    assertEquals("ws://localhost:8080/QMAClone-1.0-SNAPSHOT/websocket/",
        Constant.resolveWebSocketUrlForLocation(true, "http:", "localhost:8080",
            "/QMAClone-1.0-SNAPSHOT/"));
  }

  @Test
  public void resolveWebSocketUrlForLocationShouldUseWebSocketPathOnRoot() {
    assertEquals("ws://127.0.0.1:8888/websocket/",
        Constant.resolveWebSocketUrlForLocation(true, "http:", "127.0.0.1:8888",
            "/QMAClone.html"));
  }

  @Test
  public void resolveWebSocketUrlForLocationShouldUseWssForHttps() {
    assertEquals("wss://kishibe.dyndns.tv/QMAClone/websocket/",
        Constant.resolveWebSocketUrlForLocation(true, "https:", "kishibe.dyndns.tv",
            "/QMAClone/"));
  }

  @Test
  public void resolveWebSocketUrlForLocationShouldUseRemoteFallbackWhenNotClient() {
    assertEquals("ws://kishibe.dyndns.tv/QMAClone/websocket/",
        Constant.resolveWebSocketUrlForLocation(false, "http:", "localhost:8080",
            "/QMAClone-1.0-SNAPSHOT/"));
  }

  @Test
  public void resolveWebSocketSchemeShouldReturnWsForHttp() {
    assertEquals("ws", Constant.resolveWebSocketScheme("http:"));
  }

  @Test
  public void resolveWebSocketSchemeShouldReturnWssForHttps() {
    assertEquals("wss", Constant.resolveWebSocketScheme("https:"));
  }

  @Test
  public void resolveContextPathShouldReturnContextPath() {
    assertEquals("/QMAClone", Constant.resolveContextPath("/QMAClone/index.html"));
  }

  @Test
  public void resolveContextPathShouldReturnEmptyForRootPath() {
    assertEquals("", Constant.resolveContextPath("/QMAClone.html"));
  }

  @Test
  public void isWebSocketAvailableForLocationShouldBeTrueOnDevModeRoot() {
    assertEquals(true,
        Constant.isWebSocketAvailableForLocation(true, "127.0.0.1:8888", "/QMAClone.html"));
  }

  @Test
  public void isWebSocketAvailableForLocationShouldBeTrueOnTomcatContext() {
    assertEquals(true, Constant.isWebSocketAvailableForLocation(true, "localhost:8080",
        "/QMAClone-1.0-SNAPSHOT/"));
  }

  @Test
  public void buildWebSocketChannelPathShouldUseStableServerStatusPathWithoutQuery() {
    assertEquals("tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus",
        Constant.buildWebSocketChannelPath(Constant.WEB_SOCKET_CHANNEL_SERVER_STATUS, null));
  }

  @Test
  public void buildWebSocketChannelPathShouldAppendGameSessionIdQueryWhenPresent() {
    assertEquals(
        "tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus?game_session_id=121",
        Constant.buildWebSocketChannelPath(Constant.WEB_SOCKET_CHANNEL_GAME_STATUS, 121));
  }
}
