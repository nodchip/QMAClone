package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import tv.dyndns.kishibe.qmaclone.server.ChatManager;
import tv.dyndns.kishibe.qmaclone.server.Injectors;

/**
 * チャットメッセージ配信用の WebSocket エンドポイント
 */
@ServerEndpoint("/websocket/tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages")
public class ChatMessagesWebSocketEndpoint {
  private static final Logger logger = Logger.getLogger(ChatMessagesWebSocketEndpoint.class.getName());

  private Session session;

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    chatManager().getChatMessagesMessageSender().join(session);
  }

  @OnClose
  public void onClose() {
    if (session != null) {
      chatManager().getChatMessagesMessageSender().bye(session);
    }
    session = null;
  }

  @OnError
  public void onError(Session session, Throwable cause) {
    logger.log(Level.WARNING,
        "チャットメッセージ WebSocket セッションでエラーが発生しました。remoteAddress="
            + (session == null || session.getRequestURI() == null ? "unknown"
                : session.getRequestURI().toString()),
        cause);
    if (session != null) {
      chatManager().getChatMessagesMessageSender().bye(session);
    }
    this.session = null;
  }

  private ChatManager chatManager() {
    return Injectors.get().getInstance(ChatManager.class);
  }
}