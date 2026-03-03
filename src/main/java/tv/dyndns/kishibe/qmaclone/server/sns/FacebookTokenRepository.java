package tv.dyndns.kishibe.qmaclone.server.sns;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

/**
 * Facebook連携トークンを永続化するリポジトリ。
 */
public class FacebookTokenRepository {
  static final String KEY_USER_ACCESS_TOKEN = "facebook_user_access_token";
  static final String KEY_USER_ACCESS_TOKEN_EXPIRES_AT = "facebook_user_token_expires_at";
  static final String LEGACY_KEY_USER_ACCESS_TOKEN_EXPIRES_AT =
      "facebook_user_access_token_expires_at";
  static final String KEY_PAGE_ID = "facebook_page_id";
  static final String KEY_PAGE_ACCESS_TOKEN = "facebook_page_access_token";
  static final String KEY_APP_ID = "facebook_app_id";
  static final String KEY_APP_SECRET = "facebook_app_secret";

  private final Database database;

  @Inject
  public FacebookTokenRepository(Database database) {
    this.database = Preconditions.checkNotNull(database);
  }

  public FacebookTokenState load() throws DatabaseException {
    return new FacebookTokenState(loadOptionalPassword(KEY_USER_ACCESS_TOKEN),
        loadOptionalPasswordWithFallback(KEY_USER_ACCESS_TOKEN_EXPIRES_AT,
            LEGACY_KEY_USER_ACCESS_TOKEN_EXPIRES_AT),
        loadOptionalPassword(KEY_PAGE_ID),
        loadOptionalPassword(KEY_PAGE_ACCESS_TOKEN));
  }

  public void saveUserToken(String userAccessToken, String expiresAtEpochSecond) throws DatabaseException {
    database.setPassword(KEY_USER_ACCESS_TOKEN, userAccessToken);
    database.setPassword(KEY_USER_ACCESS_TOKEN_EXPIRES_AT, expiresAtEpochSecond);
  }

  public void savePageToken(String pageId, String pageAccessToken) throws DatabaseException {
    database.setPassword(KEY_PAGE_ID, Strings.nullToEmpty(pageId));
    database.setPassword(KEY_PAGE_ACCESS_TOKEN, pageAccessToken);
  }

  public String loadAppId() throws DatabaseException {
    return loadOptionalPassword(KEY_APP_ID);
  }

  public String loadAppSecret() throws DatabaseException {
    return loadOptionalPassword(KEY_APP_SECRET);
  }

  private String loadOptionalPassword(String key) throws DatabaseException {
    try {
      return database.getPassword(key);
    } catch (InvalidCacheLoadException e) {
      return null;
    }
  }

  private String loadOptionalPasswordWithFallback(String primaryKey, String fallbackKey)
      throws DatabaseException {
    String value = loadOptionalPassword(primaryKey);
    return value != null ? value : loadOptionalPassword(fallbackKey);
  }
}
