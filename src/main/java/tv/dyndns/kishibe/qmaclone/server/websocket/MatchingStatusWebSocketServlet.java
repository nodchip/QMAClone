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

import tv.dyndns.kishibe.qmaclone.server.Game;
import tv.dyndns.kishibe.qmaclone.server.GameManager;
import tv.dyndns.kishibe.qmaclone.server.exception.GameNotFoundException;
import tv.dyndns.kishibe.qmaclone.server.exception.InvalidGameSessionIdException;

/**
 * マッチング状態を返すWebSocketの接続リクエストを処理する
 * 
 * @author nodchip
 */
@SuppressWarnings("serial")
public class MatchingStatusWebSocketServlet extends WebSocketServlet {
  @WebSocket
  public static class MatchingStatusWebSocket {
    private Session session;
    private int gameSessionId;

    @OnWebSocketConnect
    public void onConnect(Session session) {
      this.session = session;
      try {
        gameSessionId = GameUtil.extractGameSessionId(session);
      } catch (InvalidGameSessionIdException e) {
        logger.log(Level.WARNING, "WebSocketリクエストに含まれるゲームセッションIDが不正です", e);
        session.close();
        return;
      }

      Game game;
      try {
        game = gameManager.getSession(gameSessionId);
      } catch (GameNotFoundException e) {
        logger.log(Level.WARNING, "ゲームセッションが見つかりませんでした gameSessionId=" + gameSessionId, e);
        session.close();
        return;
      }

      game.getMatchingStatusMessageSender().join(session);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
      Game game;
      try {
        game = gameManager.getSession(gameSessionId);
      } catch (GameNotFoundException e) {
        logger.log(Level.WARNING, "ゲームセッションが見つかりませんでした gameSessionId=" + gameSessionId, e);
        session.close();
        return;
      }

      game.getMatchingStatusMessageSender().bye(session);
      this.session = null;
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
      logger.log(Level.WARNING, "マッチング状況WebSocketセッションでエラーが起こりました。 remoteAddress="
          + session.getRemoteAddress().toString(), cause);

      Game game;
      try {
        game = gameManager.getSession(gameSessionId);
      } catch (GameNotFoundException e) {
        logger.log(Level.WARNING, "ゲームセッションが見つかりませんでした gameSessionId=" + gameSessionId, e);
        session.close();
        return;
      }

      game.getMatchingStatusMessageSender().bye(session);
      this.session = null;
    }
  }

  private static final Logger logger = Logger
      .getLogger(ChatMessagesWebSocketServlet.class.toString());
  private static GameManager gameManager;

  @Inject
  public MatchingStatusWebSocketServlet(GameManager gameManager) {
    MatchingStatusWebSocketServlet.gameManager = Preconditions.checkNotNull(gameManager);
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.register(MatchingStatusWebSocket.class);
  }
}
