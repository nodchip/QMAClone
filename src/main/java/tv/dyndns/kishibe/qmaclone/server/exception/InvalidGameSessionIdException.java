package tv.dyndns.kishibe.qmaclone.server.exception;

/**
 * WebSocketのアップグレードリクエストに含まれるゲームセッションIDが不正だった場合に送出される例外
 * 
 * @author nodchip
 */
@SuppressWarnings("serial")
public class InvalidGameSessionIdException extends Exception {
  public InvalidGameSessionIdException() {
  }

  public InvalidGameSessionIdException(String message) {
    super(message);
  }

  public InvalidGameSessionIdException(Throwable cause) {
    super(cause);
  }

  public InvalidGameSessionIdException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidGameSessionIdException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
