package tv.dyndns.kishibe.qmaclone.server.websocket;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.server.ChatManager;
import tv.dyndns.kishibe.qmaclone.server.websocket.ChatMessagesWebSocketServlet.ChatMessagesWebSocket;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ChatMessagesWebSocketServletTest {
  @Mock
  private ChatManager mockChatManager;
  @Mock
  private Session mockSession;
  @Mock
  private MessageSender<PacketChatMessages> mockMessageSender;
  @Mock
  private WebSocketServletFactory mockWebSocketServletFactory;
  private ChatMessagesWebSocketServlet servlet;
  private ChatMessagesWebSocket webSocket;

  @BeforeEach
  public void setUp() throws Exception {
    servlet = new ChatMessagesWebSocketServlet(mockChatManager);
    webSocket = new ChatMessagesWebSocket();
  }

  @Test
  public void onConnectJoinsSessionToChatMessageMessageSender() {
    when(mockChatManager.getChatMessagesMessageSender()).thenReturn(mockMessageSender);

    webSocket.onConnect(mockSession);

    verify(mockMessageSender).join(mockSession);
  }

  @Test
  public void onCloseByesSessionFromChatMessageMessageSender() {
    when(mockChatManager.getChatMessagesMessageSender()).thenReturn(mockMessageSender);

    webSocket.onConnect(mockSession);
    webSocket.onClose(0, "");

    verify(mockMessageSender).bye(mockSession);
  }

  @Test
  public void onErrorByesSessionFromChatMessageMessageSender() {
    when(mockChatManager.getChatMessagesMessageSender()).thenReturn(mockMessageSender);
    when(mockSession.getRemoteAddress()).thenReturn(new InetSocketAddress(0));

    webSocket.onConnect(mockSession);
    webSocket.onError(new Exception());

    verify(mockMessageSender).bye(mockSession);
  }

  @Test
  public void configureRegistersChatMessagesWebSocket() {
    servlet.configure(mockWebSocketServletFactory);

    verify(mockWebSocketServletFactory).register(ChatMessagesWebSocket.class);
  }
}
