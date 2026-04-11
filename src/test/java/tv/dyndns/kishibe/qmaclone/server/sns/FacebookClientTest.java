package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

/**
 * {@link FacebookClient} のテスト。
 */
public class FacebookClientTest {
  /**
   * ページアクセストークンがレスポンスに含まれる場合、正しく抽出できることを確認する。
   */
  @Test
  public void getPageAccessTokenShouldReturnAccessToken() throws Exception {
    FacebookAuthService authService = mock(FacebookAuthService.class);
    when(authService.getValidPageAccessToken()).thenReturn("page-token");

    FacebookClient client = new FacebookClient(authService);

    assertEquals("page-token", client.getPageAccessToken());
  }

  /**
   * レスポンスにアクセストークンが含まれない場合は null を返すことを確認する。
   */
  @Test
  public void getPageAccessTokenShouldReturnNullWhenTokenDoesNotExist() throws Exception {
    FacebookAuthService authService = mock(FacebookAuthService.class);
    when(authService.getValidPageAccessToken()).thenReturn(null);

    FacebookClient client = new FacebookClient(authService);

    assertNull(client.getPageAccessToken());
  }

  /**
   * 投稿時に初回失敗した場合はページトークン再取得を試みることを確認する。
   */
  @Test
  public void postThemeModeUpdateShouldRetryWithRefreshedTokenWhenFirstPostFails() {
    FacebookAuthService authService = mock(FacebookAuthService.class);
    when(authService.getValidPageAccessToken()).thenReturn("first-token");
    when(authService.refreshPageAccessTokenFromUserToken()).thenReturn("second-token");

    FacebookClient client = new FacebookClient(authService) {
      private int publishCount = 0;

      @Override
      void publishWithAccessToken(String accessToken, String message) {
        publishCount++;
        if (publishCount == 1) {
          throw new RuntimeException("first publish failed");
        }
      }
    };

    client.postThemeModeUpdate("test");
    verify(authService).getValidPageAccessToken();
    verify(authService).refreshPageAccessTokenFromUserToken();
  }

  @Test
  public void postThemeModeUpdateShouldRedactSensitiveTokenFromFailureLog() {
    FacebookAuthService authService = mock(FacebookAuthService.class);
    when(authService.getValidPageAccessToken()).thenReturn("page-token");
    when(authService.refreshPageAccessTokenFromUserToken()).thenReturn(null);

    FacebookClient client = new FacebookClient(authService) {
      @Override
      void publishWithAccessToken(String accessToken, String message) {
        throw new RuntimeException(
            "POST failed: https://graph.facebook.com/me/feed?access_token=secret-token&appsecret_proof=secret-proof");
      }
    };

    Logger logger = Logger.getLogger(FacebookClient.class.getName());
    CapturingHandler handler = new CapturingHandler();
    Level previousLevel = logger.getLevel();
    boolean previousUseParentHandlers = logger.getUseParentHandlers();
    logger.addHandler(handler);
    logger.setUseParentHandlers(false);
    logger.setLevel(Level.WARNING);
    try {
      client.postThemeModeUpdate("test");
    } finally {
      logger.removeHandler(handler);
      logger.setLevel(previousLevel);
      logger.setUseParentHandlers(previousUseParentHandlers);
    }

    assertFalse(handler.messages.isEmpty());
    String message = handler.messages.get(0);
    assertTrue(message.contains("access_token=<redacted>"));
    assertTrue(message.contains("appsecret_proof=<redacted>"));
    assertFalse(message.contains("secret-token"));
    assertFalse(message.contains("secret-proof"));
    assertNull(handler.throwables.get(0));
  }

  private static final class CapturingHandler extends Handler {
    private final List<String> messages = new ArrayList<>();
    private final List<Throwable> throwables = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
      messages.add(record.getMessage());
      throwables.add(record.getThrown());
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
  }
}

