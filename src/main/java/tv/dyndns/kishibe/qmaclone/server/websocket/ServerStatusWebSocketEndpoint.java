package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import tv.dyndns.kishibe.qmaclone.server.Injectors;
import tv.dyndns.kishibe.qmaclone.server.ServerStatusManager;

/**
 * サーバー状態配信用の WebSocket エンドポイント
 */
@ServerEndpoint("/websocket/tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus")
public class ServerStatusWebSocketEndpoint {
  private static final Logger logger = Logger.getLogger(ServerStatusWebSocketEndpoint.class.getName());

  private Session session;

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    serverStatusManager().getServerStatusMessageSender().join(session);
  }

  @OnClose
  public void onClose() {
    if (session != null) {
      serverStatusManager().getServerStatusMessageSender().bye(session);
    }
    session = null;
  }

  @OnError
  public void onError(Session session, Throwable cause) {
    logger.log(Level.WARNING,
        "サーバー状態 WebSocket セッションでエラーが発生しました。remoteAddress="
            + (session == null || session.getRequestURI() == null ? "unknown"
                : session.getRequestURI().toString()),
        cause);
    if (session != null) {
      serverStatusManager().getServerStatusMessageSender().bye(session);
    }
    this.session = null;
  }

  private ServerStatusManager serverStatusManager() {
    return Injectors.get().getInstance(ServerStatusManager.class);
  }
}