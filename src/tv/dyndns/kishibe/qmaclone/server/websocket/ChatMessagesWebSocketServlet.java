package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.server.ChatManager;

/**
 * チャットメッセージを返すWebSocketの接続リクエストを処理する
 * 
 * @author nodchip
 */
@SuppressWarnings("serial")
public class ChatMessagesWebSocketServlet extends WebSocketServlet {
  @WebSocket
  public static class ChatMessagesWebSocket {
    private Session session;

    @OnWebSocketConnect
    public void onConnect(Session session) {
      this.session = session;
      chatManager.getChatMessagesMessageSender().join(session);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
      chatManager.getChatMessagesMessageSender().bye(session);
      this.session = null;
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
      logger.log(Level.WARNING, "チャットメッセージWebSocketセッションでエラーが起こりました。 remoteAddress="
          + session.getRemoteAddress().toString(), cause);
      chatManager.getChatMessagesMessageSender().bye(session);
      this.session = null;
    }
  }

  private static final Logger logger = Logger
      .getLogger(ChatMessagesWebSocketServlet.class.toString());
  private static ChatManager chatManager;

  @Inject
  public ChatMessagesWebSocketServlet(ChatManager chatManager) {
    ChatMessagesWebSocketServlet.chatManager = Preconditions.checkNotNull(chatManager);
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.register(ChatMessagesWebSocket.class);
  }
}
