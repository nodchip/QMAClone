package tv.dyndns.kishibe.qmaclone.server.util;

import com.google.api.client.http.HttpResponseException;
import com.google.common.base.Preconditions;

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

  public boolean hasStatusCode() {
    return getCause() instanceof HttpResponseException;
  }

  public int getStatusCode() {
    Preconditions.checkState(hasStatusCode());
    HttpResponseException responseException = (HttpResponseException) getCause();
    return responseException.getStatusCode();
  }
}
