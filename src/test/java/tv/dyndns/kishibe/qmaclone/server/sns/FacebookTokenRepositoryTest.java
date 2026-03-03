package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.Test;

import com.google.common.cache.CacheLoader.InvalidCacheLoadException;

import tv.dyndns.kishibe.qmaclone.server.database.Database;

/**
 * {@link FacebookTokenRepository} のテスト。
 */
public class FacebookTokenRepositoryTest {
  @Test
  public void loadShouldReturnTokenStateFromDatabase() throws Exception {
    Database database = mock(Database.class);
    when(database.getPassword(FacebookTokenRepository.KEY_USER_ACCESS_TOKEN)).thenReturn("user-token");
    when(database.getPassword(FacebookTokenRepository.KEY_USER_ACCESS_TOKEN_EXPIRES_AT)).thenReturn("111");
    when(database.getPassword(FacebookTokenRepository.KEY_PAGE_ID)).thenReturn("page-id");
    when(database.getPassword(FacebookTokenRepository.KEY_PAGE_ACCESS_TOKEN)).thenReturn("page-token");

    FacebookTokenRepository repository = new FacebookTokenRepository(database);
    FacebookTokenState state = repository.load();

    assertEquals("user-token", state.getUserAccessToken());
    assertEquals("111", state.getUserAccessTokenExpiresAtEpochSecond());
    assertEquals("page-id", state.getPageId());
    assertEquals("page-token", state.getPageAccessToken());
  }

  @Test
  public void loadShouldUseLegacyExpiresKeyWhenPrimaryMissing() throws Exception {
    Database database = mock(Database.class);
    when(database.getPassword(FacebookTokenRepository.KEY_USER_ACCESS_TOKEN)).thenReturn("user-token");
    when(database.getPassword(FacebookTokenRepository.KEY_USER_ACCESS_TOKEN_EXPIRES_AT))
        .thenThrow(new InvalidCacheLoadException("missing"));
    when(database.getPassword(FacebookTokenRepository.LEGACY_KEY_USER_ACCESS_TOKEN_EXPIRES_AT))
        .thenReturn("222");

    FacebookTokenRepository repository = new FacebookTokenRepository(database);
    FacebookTokenState state = repository.load();

    assertEquals("222", state.getUserAccessTokenExpiresAtEpochSecond());
  }

  @Test
  public void saveUserTokenShouldUseShortExpiresKey() throws Exception {
    Database database = mock(Database.class);
    FacebookTokenRepository repository = new FacebookTokenRepository(database);

    repository.saveUserToken("user-token", "333");

    verify(database).setPassword(FacebookTokenRepository.KEY_USER_ACCESS_TOKEN, "user-token");
    verify(database).setPassword(FacebookTokenRepository.KEY_USER_ACCESS_TOKEN_EXPIRES_AT, "333");
    verify(database, never()).setPassword(
        FacebookTokenRepository.LEGACY_KEY_USER_ACCESS_TOKEN_EXPIRES_AT, "333");
  }

  @Test
  public void savePageTokenShouldPersistPageFields() throws Exception {
    Database database = mock(Database.class);
    FacebookTokenRepository repository = new FacebookTokenRepository(database);

    repository.savePageToken("page-id", "page-token");

    verify(database).setPassword(FacebookTokenRepository.KEY_PAGE_ID, "page-id");
    verify(database).setPassword(FacebookTokenRepository.KEY_PAGE_ACCESS_TOKEN, "page-token");
  }

  @Test
  public void savePageTokenShouldPersistEmptyPageIdWhenNull() throws Exception {
    Database database = mock(Database.class);
    FacebookTokenRepository repository = new FacebookTokenRepository(database);

    repository.savePageToken(null, "page-token");

    verify(database).setPassword(FacebookTokenRepository.KEY_PAGE_ID, "");
    verify(database).setPassword(FacebookTokenRepository.KEY_PAGE_ACCESS_TOKEN, "page-token");
  }

  @Test
  public void loadAppSecretShouldReturnNullWhenPasswordIsMissing() throws Exception {
    Database database = mock(Database.class);
    when(database.getPassword(FacebookTokenRepository.KEY_APP_SECRET))
        .thenThrow(new InvalidCacheLoadException("missing"));

    FacebookTokenRepository repository = new FacebookTokenRepository(database);

    assertNull(repository.loadAppSecret());
  }
}
