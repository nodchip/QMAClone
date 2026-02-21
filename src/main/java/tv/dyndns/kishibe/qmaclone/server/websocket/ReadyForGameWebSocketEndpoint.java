package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import tv.dyndns.kishibe.qmaclone.server.Game;
import tv.dyndns.kishibe.qmaclone.server.GameManager;
import tv.dyndns.kishibe.qmaclone.server.Injectors;
import tv.dyndns.kishibe.qmaclone.server.exception.GameNotFoundException;
import tv.dyndns.kishibe.qmaclone.server.exception.InvalidGameSessionIdException;

/**
 * ゲーム開始準備状態配信用の WebSocket エンドポイント
 */
@ServerEndpoint("/websocket/tv.dyndns.kishibe.qmaclone.client.packet.PacketReadyForGame")
public class ReadyForGameWebSocketEndpoint {
  private static final Logger logger = Logger.getLogger(ReadyForGameWebSocketEndpoint.class.getName());

  private Session session;
  private int gameSessionId;

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    try {
      gameSessionId = GameUtil.extractGameSessionId(session);
    } catch (InvalidGameSessionIdException e) {
      logger.log(Level.WARNING, "WebSocket リクエストに含まれるゲームセッションIDが不正です", e);
      closeSession(session);
      return;
    }

    Game game;
    try {
      game = gameManager().getSession(gameSessionId);
    } catch (GameNotFoundException e) {
      logger.log(Level.WARNING, "ゲームセッションが見つかりません gameSessionId=" + gameSessionId, e);
      closeSession(session);
      return;
    }

    game.getReadyForGameMessageSender().join(session);
  }

  @OnClose
  public void onClose() {
    Game game = getGameIfExists();
    if (game != null && session != null) {
      game.getReadyForGameMessageSender().bye(session);
    }
    session = null;
  }

  @OnError
  public void onError(Session session, Throwable cause) {
    logger.log(Level.WARNING,
        "準備状態 WebSocket セッションでエラーが発生しました。remoteAddress="
            + (session == null || session.getRequestURI() == null ? "unknown"
                : session.getRequestURI().toString()),
        cause);

    Game game = getGameIfExists();
    if (game != null && session != null) {
      game.getReadyForGameMessageSender().bye(session);
    }
    this.session = null;
  }

  private Game getGameIfExists() {
    try {
      return gameManager().getSession(gameSessionId);
    } catch (GameNotFoundException e) {
      logger.log(Level.WARNING, "ゲームセッションが見つかりません gameSessionId=" + gameSessionId, e);
      return null;
    }
  }

  private GameManager gameManager() {
    return Injectors.get().getInstance(GameManager.class);
  }

  private void closeSession(Session session) {
    try {
      session.close();
    } catch (Exception e) {
    }
  }
}