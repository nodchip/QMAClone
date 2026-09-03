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
  private static final String REDACTED = "<redacted>";
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
      logger.log(Level.WARNING, "Facebook投稿に失敗しました。 detail=" + sanitizeForLog(e));
      return false;
    }
  }

  @VisibleForTesting
  void publishWithAccessToken(String accessToken, String message) {
    String appSecret = authService.getAppSecretForApiCalls();
    if (Strings.isNullOrEmpty(appSecret)) {
      throw new IllegalStateException("Facebook App Secretが未設定です");
    }
    com.restfb.FacebookClient facebookClient = createFacebookClient(accessToken, appSecret);
    facebookClient.publish("me/feed", FacebookType.class, Parameter.with("message", message));
  }

  @VisibleForTesting
  com.restfb.FacebookClient createFacebookClient(String accessToken, String appSecret) {
    return new DefaultFacebookClient(accessToken, appSecret, Version.UNVERSIONED);
  }

  @VisibleForTesting
  static String sanitizeForLog(Throwable throwable) {
    if (throwable == null) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    Throwable current = throwable;
    while (current != null) {
      if (builder.length() > 0) {
        builder.append(" causedBy=");
      }
      builder.append(current.getClass().getName());
      if (!Strings.isNullOrEmpty(current.getMessage())) {
        builder.append(": ").append(sanitizeSensitiveQueryParameters(current.getMessage()));
      }
      current = current.getCause();
    }
    return builder.toString();
  }

  private static String sanitizeSensitiveQueryParameters(String message) {
    return message
        .replaceAll("(access_token=)[^&\\s]+", "$1" + REDACTED)
        .replaceAll("(appsecret_proof=)[^&\\s]+", "$1" + REDACTED)
        .replaceAll("(client_secret=)[^&\\s]+", "$1" + REDACTED)
        .replaceAll("(fb_exchange_token=)[^&\\s]+", "$1" + REDACTED)
        .replaceAll("(refresh_token=)[^&\\s]+", "$1" + REDACTED)
        .replaceAll("(code=)[^&\\s]+", "$1" + REDACTED);
  }
}
