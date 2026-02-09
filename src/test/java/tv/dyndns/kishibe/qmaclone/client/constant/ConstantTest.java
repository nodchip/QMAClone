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
  public void resolveWebSocketUrlForLocationShouldUseWsForHttp() {
    assertEquals("ws://localhost:8080/QMAClone-1.0-SNAPSHOT/websocket/",
        Constant.resolveWebSocketUrlForLocation(true, "http:", "localhost:8080",
            "/QMAClone-1.0-SNAPSHOT/"));
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
}
