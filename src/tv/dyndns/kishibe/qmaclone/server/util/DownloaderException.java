package tv.dyndns.kishibe.qmaclone.server.util;

/**
 * {@link Downloader} から投げられる {@link Exception}.
 * 
 * @author nodchip
 */
@SuppressWarnings("serial")
public class DownloaderException extends Exception {
  public DownloaderException() {
  }

  public DownloaderException(String message) {
    super(message);
  }

  public DownloaderException(Throwable cause) {
    super(cause);
  }

  public DownloaderException(String message, Throwable cause) {
    super(message, cause);
  }
}
