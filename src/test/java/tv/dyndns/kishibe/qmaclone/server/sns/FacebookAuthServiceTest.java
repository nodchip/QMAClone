package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

/**
 * {@link FacebookAuthService} のテスト。
 */
public class FacebookAuthServiceTest {
  @Test
  public void getValidPageAccessTokenShouldReturnStoredTokenIfExists() throws Exception {
    FacebookTokenRepository repository = mock(FacebookTokenRepository.class);
    FacebookGraphApiClient graphApiClient = mock(FacebookGraphApiClient.class);
    when(repository.load()).thenReturn(new FacebookTokenState("user", "0", "page-id", "page-token"));

    FacebookAuthService service = new FacebookAuthService(repository, graphApiClient);

    assertEquals("page-token", service.getValidPageAccessToken());
  }

  @Test
  public void refreshPageAccessTokenFromUserTokenShouldPersistNewPageToken() throws Exception {
    FacebookTokenRepository repository = mock(FacebookTokenRepository.class);
    FacebookGraphApiClient graphApiClient = mock(FacebookGraphApiClient.class);
    when(repository.load()).thenReturn(new FacebookTokenState("user-token", "0", "page-id", null));
    when(repository.loadAppSecret()).thenReturn("app-secret");
    when(graphApiClient.fetchPageAccessToken("user-token", "page-id", "app-secret"))
        .thenReturn("new-page-token");

    FacebookAuthService service = new FacebookAuthService(repository, graphApiClient);
    String token = service.refreshPageAccessTokenFromUserToken();

    assertEquals("new-page-token", token);
    verify(repository).savePageToken("page-id", "new-page-token");
  }

  @Test
  public void refreshTokensByAuthorizationCodeShouldUpdateTokens() throws Exception {
    FacebookTokenRepository repository = mock(FacebookTokenRepository.class);
    FacebookGraphApiClient graphApiClient = mock(FacebookGraphApiClient.class);
    when(repository.loadAppId()).thenReturn("app-id");
    when(repository.loadAppSecret()).thenReturn("app-secret");
    when(graphApiClient.exchangeCodeToShortLivedUserToken("app-id", "app-secret", "https://callback", "code"))
        .thenReturn("short");
    when(graphApiClient.exchangeToLongLivedUserToken("app-id", "app-secret", "short")).thenReturn("long");
    when(repository.load()).thenReturn(new FacebookTokenState("long", "0", "page-id", null));
    when(graphApiClient.fetchPageAccessToken("long", "page-id", "app-secret")).thenReturn("page-token");

    FacebookAuthService service = new FacebookAuthService(repository, graphApiClient);
    boolean result = service.refreshTokensByAuthorizationCode("code", "https://callback");

    assertTrue(result);
    verify(repository).saveUserToken("long", "0");
    verify(repository).savePageToken("page-id", "page-token");
  }
}
