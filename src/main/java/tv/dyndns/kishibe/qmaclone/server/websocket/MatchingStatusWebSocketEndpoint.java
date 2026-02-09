package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import tv.dyndns.kishibe.qmaclone.server.Game;
import tv.dyndns.kishibe.qmaclone.server.GameManager;
import tv.dyndns.kishibe.qmaclone.server.Injectors;
import tv.dyndns.kishibe.qmaclone.server.exception.GameNotFoundException;
import tv.dyndns.kishibe.qmaclone.server.exception.InvalidGameSessionIdException;

/**
 * マッチング状態配信用の WebSocket エンドポイント
 */
@ServerEndpoint("/websocket/tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingStatus")
public class MatchingStatusWebSocketEndpoint {
  private static final Logger logger = Logger.getLogger(MatchingStatusWebSocketEndpoint.class.getName());

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

    game.getMatchingStatusMessageSender().join(session);
  }

  @OnClose
  public void onClose() {
    Game game = getGameIfExists();
    if (game != null && session != null) {
      game.getMatchingStatusMessageSender().bye(session);
    }
    session = null;
  }

  @OnError
  public void onError(Session session, Throwable cause) {
    logger.log(Level.WARNING,
        "マッチング状態 WebSocket セッションでエラーが発生しました。remoteAddress="
            + (session == null || session.getRequestURI() == null ? "unknown"
                : session.getRequestURI().toString()),
        cause);

    Game game = getGameIfExists();
    if (game != null && session != null) {
      game.getMatchingStatusMessageSender().bye(session);
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