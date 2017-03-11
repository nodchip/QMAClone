package tv.dyndns.kishibe.qmaclone.server.exception;

/**
 * ゲームがゲームマネージャー内で見つからなかった場合に送出される例外
 * 
 * @author nodchip
 */
@SuppressWarnings("serial")
public class GameNotFoundException extends Exception {
  public GameNotFoundException() {
  }

  public GameNotFoundException(String message) {
    super(message);
  }

  public GameNotFoundException(Throwable cause) {
    super(cause);
  }

  public GameNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public GameNotFoundException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
