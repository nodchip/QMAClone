package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.util.List;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.exception.InvalidGameSessionIdException;

/**
 * ゲーム関連のヘルパークラス群
 * 
 * @author nodchip
 */
public class GameUtil {
  /**
   * WebSocketのセッションからゲームセッションIDを抜き出す
   * 
   * @param session
   *          WebSocketのセッション
   * @return ゲームセッションID
   * @throws InvalidGameSessionIdException
   *           ゲームセッションIDが不正だった場合
   */
  public static int extractGameSessionId(Session session) throws InvalidGameSessionIdException {
    Map<String, List<String>> parameterMap = session.getUpgradeRequest().getParameterMap();
    if (!parameterMap.containsKey(Constant.KEY_GAME_SESSION_ID)) {
      throw new InvalidGameSessionIdException("クエリパラメータ-にゲームセッションIDが含まれていません");
    }

    List<String> gameSessionIds = parameterMap.get(Constant.KEY_GAME_SESSION_ID);
    if (gameSessionIds.isEmpty()) {
      throw new InvalidGameSessionIdException("クエリパラメータ-のゲームセッションIDが空です");
    } else if (gameSessionIds.size() > 1) {
      throw new InvalidGameSessionIdException("クエリパラメータ-に複数のゲームセッションIDが含まれています");
    }

    String gameSessionIdString = gameSessionIds.get(0);
    try {
      return Integer.parseInt(gameSessionIdString);
    } catch (NumberFormatException e) {
      throw new InvalidGameSessionIdException("ゲームセッションIDの書式が不正です", e);
    }
  }
}
