package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.List;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.exception.InvalidGameSessionIdException;

/**
 * ゲーム関連のヘルパークラス
 */
public class GameUtil {
  /**
   * Jetty WebSocket セッションからゲームセッションIDを抽出する。
   */
  public static int extractGameSessionId(Session session) throws InvalidGameSessionIdException {
    return extractGameSessionId(session.getUpgradeRequest().getParameterMap());
  }

  /**
   * JSR-356 WebSocket セッションからゲームセッションIDを抽出する。
   */
  public static int extractGameSessionId(javax.websocket.Session session)
      throws InvalidGameSessionIdException {
    return extractGameSessionId(session.getRequestParameterMap());
  }

  private static int extractGameSessionId(Map<String, List<String>> parameterMap)
      throws InvalidGameSessionIdException {
    if (!parameterMap.containsKey(Constant.KEY_GAME_SESSION_ID)) {
      throw new InvalidGameSessionIdException("クエリパラメータにゲームセッションIDが含まれていません");
    }

    List<String> gameSessionIds = parameterMap.get(Constant.KEY_GAME_SESSION_ID);
    if (gameSessionIds.isEmpty()) {
      throw new InvalidGameSessionIdException("クエリパラメータのゲームセッションIDが空です");
    }
    if (gameSessionIds.size() > 1) {
      throw new InvalidGameSessionIdException("クエリパラメータに複数のゲームセッションIDが含まれています");
    }

    String gameSessionIdString = gameSessionIds.get(0);
    try {
      return Integer.parseInt(gameSessionIdString);
    } catch (NumberFormatException e) {
      throw new InvalidGameSessionIdException("ゲームセッションIDの形式が不正です", e);
    }
  }
}