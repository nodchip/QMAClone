package tv.dyndns.kishibe.qmaclone.server.sns;

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.FacebookType;

public class FacebookClient implements SnsClient {
  private static final Logger logger = Logger.getLogger(FacebookClient.class.getName());
  private final FacebookAuthService authService;

  @Inject
  public FacebookClient(FacebookAuthService authService) {
    this.authService = Preconditions.checkNotNull(authService);
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
    if (Strings.isNullOrEmpty(accessToken)) {
      logger.warning("Facebookページトークンが取得できなかったため投稿をスキップしました。");
      return;
    }

    if (tryPublish(accessToken, message)) {
      return;
    }

    String refreshedAccessToken = authService.refreshPageAccessTokenFromUserToken();
    if (!Strings.isNullOrEmpty(refreshedAccessToken)) {
      tryPublish(refreshedAccessToken, message);
    }
  }

  @Override
  public void followBack() {
    throw new UnsupportedOperationException();
  }

  @VisibleForTesting
  String getPageAccessToken() {
    return authService.getValidPageAccessToken();
  }

  private boolean tryPublish(String accessToken, String message) {
    try {
      publishWithAccessToken(accessToken, message);
      return true;
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "Facebook投稿に失敗しました。", e);
      return false;
    }
  }

  @VisibleForTesting
  void publishWithAccessToken(String accessToken, String message) {
    com.restfb.FacebookClient facebookClient = new DefaultFacebookClient(accessToken, Version.UNVERSIONED);
    facebookClient.publish("me/feed", FacebookType.class, Parameter.with("message", message));
  }
}
