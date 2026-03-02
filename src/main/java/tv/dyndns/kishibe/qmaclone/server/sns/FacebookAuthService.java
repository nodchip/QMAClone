package tv.dyndns.kishibe.qmaclone.server.sns;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderException;

/**
 * Facebook認証トークンの取得と更新を扱うサービス。
 */
public class FacebookAuthService {
  private static final Logger logger = Logger.getLogger(FacebookAuthService.class.getName());

  private final FacebookTokenRepository repository;
  private final FacebookGraphApiClient graphApiClient;

  @Inject
  public FacebookAuthService(FacebookTokenRepository repository, FacebookGraphApiClient graphApiClient) {
    this.repository = Preconditions.checkNotNull(repository);
    this.graphApiClient = Preconditions.checkNotNull(graphApiClient);
  }

  public String getValidPageAccessToken() {
    try {
      FacebookTokenState state = repository.load();
      if (!Strings.isNullOrEmpty(state.getPageAccessToken())) {
        return state.getPageAccessToken();
      }
      return refreshPageAccessTokenFromUserToken();
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "Facebookトークンの読み込みに失敗しました", e);
      return null;
    }
  }

  public String refreshPageAccessTokenFromUserToken() {
    try {
      FacebookTokenState state = repository.load();
      if (Strings.isNullOrEmpty(state.getUserAccessToken())) {
        return null;
      }
      String pageToken = graphApiClient.fetchPageAccessToken(state.getUserAccessToken(), state.getPageId());
      if (Strings.isNullOrEmpty(pageToken)) {
        return null;
      }
      repository.savePageToken(state.getPageId(), pageToken);
      return pageToken;
    } catch (DatabaseException | DownloaderException e) {
      logger.log(Level.WARNING, "Facebookページトークンの更新に失敗しました", e);
      return null;
    }
  }

  public boolean refreshTokensByAuthorizationCode(String code, String redirectUri) {
    try {
      String appId = repository.loadAppId();
      String appSecret = repository.loadAppSecret();
      if (Strings.isNullOrEmpty(appId) || Strings.isNullOrEmpty(appSecret)) {
        logger.warning("Facebook App設定が不足しています: appId/appSecret を確認してください。");
        return false;
      }

      String shortLivedToken =
          graphApiClient.exchangeCodeToShortLivedUserToken(appId, appSecret, redirectUri, code);
      if (Strings.isNullOrEmpty(shortLivedToken)) {
        logger.warning("Facebook code -> short-lived token 交換に失敗しました。");
        return false;
      }

      String longLivedToken = graphApiClient.exchangeToLongLivedUserToken(appId, appSecret, shortLivedToken);
      if (Strings.isNullOrEmpty(longLivedToken)) {
        logger.warning("Facebook short-lived -> long-lived token 交換に失敗しました。");
        return false;
      }
      // 現行実装では expires_in の厳密管理は行わず、失効時リフレッシュで回復する。
      repository.saveUserToken(longLivedToken, "0");
      String pageAccessToken = refreshPageAccessTokenFromUserToken();
      if (Strings.isNullOrEmpty(pageAccessToken)) {
        logger.warning("Facebookページアクセストークンの取得に失敗しました。ページ権限・ページID設定を確認してください。");
        return false;
      }
      return true;
    } catch (DatabaseException | DownloaderException e) {
      logger.log(Level.WARNING, "Facebook認可コードからのトークン更新に失敗しました", e);
      return false;
    }
  }

  public String buildAuthorizationUrl(String redirectUri, String state) {
    try {
      String appId = repository.loadAppId();
      if (Strings.isNullOrEmpty(appId)) {
        return null;
      }
      return "https://www.facebook.com/v22.0/dialog/oauth?client_id=" + encode(appId) + "&redirect_uri="
          + encode(redirectUri) + "&state=" + encode(state)
          + "&scope=pages_show_list,pages_read_engagement,pages_manage_posts";
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "Facebook App IDの読み込みに失敗しました", e);
      return null;
    }
  }

  private String encode(String value) {
    try {
      return URLEncoder.encode(Strings.nullToEmpty(value), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }
}
