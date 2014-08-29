package tv.dyndns.kishibe.qmaclone.server.sns;

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.QMACloneModule;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Inject;

public class TwitterClient implements SnsClient {
  private static final Logger logger = Logger.getLogger(TwitterClient.class.getName());
  private final TwitterFactory twitterFactory;

  @Inject
  public TwitterClient(Database database) {
    String consumerKey;
    String consumerSecret;
    String token;
    String tokenSecret;
    try {
      consumerKey = database.getPassword("twitter_consumer_key");
      consumerSecret = database.getPassword("twitter_consumer_secret");
      token = database.getPassword("twitter_access_token");
      tokenSecret = database.getPassword("twitter_access_token_secret");
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "Failed to get tokens for Twitter.", e);
      twitterFactory = null;
      return;
    }

    ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
    configurationBuilder.setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
        .setOAuthAccessToken(token).setOAuthAccessTokenSecret(tokenSecret);
    twitterFactory = new TwitterFactory(configurationBuilder.build());
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
    Preconditions.checkNotNull(twitterFactory);

    Twitter twitter = null;
    try {
      twitter = twitterFactory.getInstance();
      twitter.updateStatus(status);

    } catch (Exception e) {
      logger.log(Level.WARNING, "Twitterへの投稿に失敗しました。", e);

    }
  }

  public static void main(String[] args) {
    TwitterClient twitterClient = Guice.createInjector(new QMACloneModule()).getInstance(
        TwitterClient.class);
    twitterClient.post("このメッセージは開発者によるテスト投稿です。");
  }
}
