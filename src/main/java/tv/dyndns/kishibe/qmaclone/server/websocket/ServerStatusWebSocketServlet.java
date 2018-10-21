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

import tv.dyndns.kishibe.qmaclone.server.ServerStatusManager;

/**
 * サーバー状況を返すWebSocketの接続リクエストを処理する
 * 
 * @author nodchip
 */
@SuppressWarnings("serial")
public class ServerStatusWebSocketServlet extends WebSocketServlet {
  @WebSocket
  public static class ServerStatusWebSocket {
    private Session session;

    public ServerStatusWebSocket() {
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
      this.session = session;
      serverStatusManager.getServerStatusMessageSender().join(session);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
      serverStatusManager.getServerStatusMessageSender().bye(session);
      this.session = null;
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
      logger.log(Level.WARNING, "チャットメッセージWebSocketセッションでエラーが起こりました。 remoteAddress="
          + session.getRemoteAddress().toString(), cause);
      serverStatusManager.getServerStatusMessageSender().bye(session);
      this.session = null;
    }
  }

  private static final Logger logger = Logger
      .getLogger(ServerStatusWebSocketServlet.class.toString());
  private static ServerStatusManager serverStatusManager;

  @Inject
  public ServerStatusWebSocketServlet(ServerStatusManager serverStatusManager) {
    ServerStatusWebSocketServlet.serverStatusManager = Preconditions
        .checkNotNull(serverStatusManager);
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.register(ServerStatusWebSocket.class);
  }
}
