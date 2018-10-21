package tv.dyndns.kishibe.qmaclone.server.websocket;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.server.ServerStatusManager;
import tv.dyndns.kishibe.qmaclone.server.websocket.ServerStatusWebSocketServlet.ServerStatusWebSocket;

@RunWith(MockitoJUnitRunner.class)
public class ServerStatusWebSocketServletTest {
  @Mock
  private ServerStatusManager mockServerStatusManager;
  @Mock
  private Session mockSession;
  @Mock
  private MessageSender<PacketServerStatus> mockMessageSender;
  @Mock
  private WebSocketServletFactory mockWebSocketServletFactory;
  private ServerStatusWebSocketServlet servlet;
  private ServerStatusWebSocket webSocket;

  @Before
  public void setUp() throws Exception {
    servlet = new ServerStatusWebSocketServlet(mockServerStatusManager);
    webSocket = new ServerStatusWebSocket();
  }

  @Test
  public void onConnectJoinsSessionToServerStatusMessageSender() {
    when(mockServerStatusManager.getServerStatusMessageSender()).thenReturn(mockMessageSender);

    webSocket.onConnect(mockSession);

    verify(mockMessageSender).join(mockSession);
  }

  @Test
  public void onCloseByesSessionFromServerStatusMessageSender() {
    when(mockServerStatusManager.getServerStatusMessageSender()).thenReturn(mockMessageSender);

    webSocket.onConnect(mockSession);
    webSocket.onClose(0, "");

    verify(mockMessageSender).bye(mockSession);
  }

  @Test
  public void onErrorByesSessionFromServerStatusMessageSender() {
    when(mockServerStatusManager.getServerStatusMessageSender()).thenReturn(mockMessageSender);
    when(mockSession.getRemoteAddress()).thenReturn(new InetSocketAddress(0));

    webSocket.onConnect(mockSession);
    webSocket.onError(new Exception());

    verify(mockMessageSender).bye(mockSession);
  }

  @Test
  public void configureRegistersServerStatusWebSocket() {
    servlet.configure(mockWebSocketServletFactory);

    verify(mockWebSocketServletFactory).register(ServerStatusWebSocket.class);
  }
}
