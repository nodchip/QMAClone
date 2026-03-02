package tv.dyndns.kishibe.qmaclone.server.sns;

/**
 * Facebook連携で利用するトークン状態。
 */
public class FacebookTokenState {
  private final String userAccessToken;
  private final String userAccessTokenExpiresAtEpochSecond;
  private final String pageId;
  private final String pageAccessToken;

  public FacebookTokenState(String userAccessToken, String userAccessTokenExpiresAtEpochSecond,
      String pageId, String pageAccessToken) {
    this.userAccessToken = userAccessToken;
    this.userAccessTokenExpiresAtEpochSecond = userAccessTokenExpiresAtEpochSecond;
    this.pageId = pageId;
    this.pageAccessToken = pageAccessToken;
  }

  public String getUserAccessToken() {
    return userAccessToken;
  }

  public String getUserAccessTokenExpiresAtEpochSecond() {
    return userAccessTokenExpiresAtEpochSecond;
  }

  public String getPageId() {
    return pageId;
  }

  public String getPageAccessToken() {
    return pageAccessToken;
  }
}

