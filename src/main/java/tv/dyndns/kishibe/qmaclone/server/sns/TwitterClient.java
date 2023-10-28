package tv.dyndns.kishibe.qmaclone.server.sns;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.twitter.clientlib.ApiClientCallback;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsOAuth2;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.TweetCreateRequest;
import com.twitter.clientlib.model.TweetCreateResponse;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.QMACloneModule;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

public class TwitterClient implements SnsClient {
  private static final Logger logger = Logger.getLogger(TwitterClient.class.getName());
  private static final String TWITTER_OAUTH2_CLIENT_ID = "twitter_oauth2_client_id";
  private static final String TWITTER_OAUTH2_CLIENT_SECRET = "twitter_oauth2_client_secret";
  private static final String TWITTER_OAUTH2_ACCESS_TOKEN = "twitter_oauth2_access_token";
  private static final String TWITTER_OAUTH2_REFRESH_TOKEN = "twitter_oauth2_refresh_token";
  private final TwitterApi twitterApi;

  @Inject
  public TwitterClient(Database database) {
    String clientIid;
    String clientSecret;
    String accessToken;
    String refreshToken;
    try {
      clientIid = database.getPassword(TWITTER_OAUTH2_CLIENT_ID);
      clientSecret = database.getPassword(TWITTER_OAUTH2_CLIENT_SECRET);
      accessToken = database.getPassword(TWITTER_OAUTH2_ACCESS_TOKEN);
      refreshToken = database.getPassword(TWITTER_OAUTH2_REFRESH_TOKEN);
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "Failed to get tokens for Twitter.", e);
      twitterApi = null;
      return;
    }

    TwitterCredentialsOAuth2 credentials = new TwitterCredentialsOAuth2(clientIid, clientSecret, accessToken,
        refreshToken, true);
    twitterApi = new TwitterApi(credentials);
    twitterApi.addCallback(new RefreshTokenCallback(database));
  }

  @Override
  public void postProblem(PacketProblem problem) {
    String problemReportSentence = problem.getProblemReportSentence();
    if (problemReportSentence.length() > 115) {
      problemReportSentence = problemReportSentence.substring(0, 115);
    }
    String status = "問題番号" + problem.id + ":" + problemReportSentence;
    post(status);
  }

  @Override
  public void postThemeModeUpdate(String theme) {
    String status = "テーマモード 「" + theme + "」 が更新されました";
    post(status);
  }

  @Override
  public void followBack() {
    throw new UnsupportedOperationException();
  }

  private void post(String status) {
    Preconditions.checkNotNull(twitterApi);

    TweetCreateRequest tweetCreateRequest = new TweetCreateRequest().text(status);
    try {
      logger.log(Level.INFO, "Twitterへ投稿中です。 status=" + status);
      TweetCreateResponse result = twitterApi.tweets().createTweet(tweetCreateRequest).execute();
      logger.log(Level.INFO, "Twitterへ投稿しました。 result=" + result);
    } catch (ApiException e) {
      logger.log(Level.WARNING, "Twitterへの投稿に失敗しました。", e);
    }
  }

  public static void main(String[] args) {
    TwitterClient twitterClient = Guice.createInjector(new QMACloneModule()).getInstance(TwitterClient.class);
    twitterClient.post("このメッセージは開発者によるテスト投稿です。");
  }

  class RefreshTokenCallback implements ApiClientCallback {
    private final Database database;

    public RefreshTokenCallback(Database database) {
      this.database = database;
    }

    @Override
    public void onAfterRefreshToken(OAuth2AccessToken accessToken) {
      try {
        database.setPassword(TWITTER_OAUTH2_ACCESS_TOKEN, accessToken.getAccessToken());
        database.setPassword(TWITTER_OAUTH2_REFRESH_TOKEN, accessToken.getRefreshToken());
      } catch (DatabaseException e) {
        logger.log(Level.WARNING, "Failed to set tokens for Twitter.", e);
        return;
      }
    }
  }
}
