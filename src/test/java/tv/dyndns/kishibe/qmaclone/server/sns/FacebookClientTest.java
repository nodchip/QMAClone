package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}

