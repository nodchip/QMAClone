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
