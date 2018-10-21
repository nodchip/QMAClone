package tv.dyndns.kishibe.qmaclone.server.sns;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.FacebookType;

public class FacebookClient implements SnsClient {
  private static final Logger logger = Logger.getLogger(FacebookClient.class.getName());
  private final Database database;
  private final Downloader downloader;

  @Inject
  public FacebookClient(Database database, Downloader downloader) {
    this.database = Preconditions.checkNotNull(database);
    this.downloader = Preconditions.checkNotNull(downloader);
  }

  @Override
  public void postProblem(PacketProblem problem) {
    String problemReportSentence = problem.getProblemReportSentence();
    String message = "問題番号" + problem.id + ":" + problemReportSentence
        + " http://kishibe.dyndns.tv/qmaclone/";
    post(message);
  }

  @Override
  public void postThemeModeUpdate(String theme) {
    String message = "テーマモード 「" + theme + "」 が更新されました" + " http://kishibe.dyndns.tv/qmaclone/";
    post(message);
  }

  private void post(String message) {
    String accessToken = getPageAccessToken();
    com.restfb.FacebookClient facebookClient = new DefaultFacebookClient(accessToken,
        Version.UNVERSIONED);
    facebookClient.publish("me/feed", FacebookType.class, Parameter.with("message", message));
  }

  @Override
  public void followBack() {
    throw new UnsupportedOperationException();
  }

  @VisibleForTesting
  String getPageAccessToken() {
    String accessToken;
    try {
      accessToken = database.getPassword("facebook_access_token");
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "Failed to get the access token from the database.", e);
      return null;
    }

    URL url;
    try {
      url = new URL("https://graph.facebook.com/1060467615/accounts?access_token=" + accessToken);
    } catch (MalformedURLException e) {
      logger.log(Level.WARNING, "URLオブジェクトの構築に失敗しました", e);
      return null;
    }

    String json;
    try {
      json = downloader.downloadAsString(url);
    } catch (DownloaderException e) {
      logger.log(Level.WARNING, "ページアクセストークンの取得に失敗しました", e);
      return null;
    }

    List<String> elements = ImmutableList.copyOf(json.replaceAll("\\s", "").split("\""));
    int accessTokenIndex = elements.indexOf("access_token");
    if (accessTokenIndex == -1) {
      return null;
    }
    return elements.get(accessTokenIndex + 2);
  }
}
