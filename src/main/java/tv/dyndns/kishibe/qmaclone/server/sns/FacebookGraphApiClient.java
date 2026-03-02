package tv.dyndns.kishibe.qmaclone.server.sns;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.server.util.Downloader;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderException;

/**
 * Graph APIアクセスを集約するクライアント。
 */
public class FacebookGraphApiClient {
  private final Downloader downloader;

  @Inject
  public FacebookGraphApiClient(Downloader downloader) {
    this.downloader = Preconditions.checkNotNull(downloader);
  }

  public String exchangeCodeToShortLivedUserToken(String appId, String appSecret, String redirectUri, String code)
      throws DownloaderException {
    String url = "https://graph.facebook.com/v22.0/oauth/access_token?client_id=" + encode(appId)
        + "&client_secret=" + encode(appSecret) + "&redirect_uri=" + encode(redirectUri) + "&code=" + encode(code);
    return extractJsonValue(downloadAsString(url), "access_token");
  }

  public String exchangeToLongLivedUserToken(String appId, String appSecret, String shortLivedToken)
      throws DownloaderException {
    String url = "https://graph.facebook.com/v22.0/oauth/access_token?grant_type=fb_exchange_token&client_id="
        + encode(appId) + "&client_secret=" + encode(appSecret) + "&fb_exchange_token=" + encode(shortLivedToken);
    return extractJsonValue(downloadAsString(url), "access_token");
  }

  public String extractExpiresIn(String accessTokenResponseJson) {
    return extractJsonValue(accessTokenResponseJson, "expires_in");
  }

  public String fetchPageAccessToken(String userAccessToken, String pageId) throws DownloaderException {
    String url = "https://graph.facebook.com/v22.0/me/accounts?access_token=" + encode(userAccessToken);
    String json = downloadAsString(url);
    if (Strings.isNullOrEmpty(pageId)) {
      return extractJsonValue(json, "access_token");
    }
    return extractPageAccessTokenById(json, pageId);
  }

  private String extractPageAccessTokenById(String json, String pageId) {
    List<String> elements = ImmutableList.copyOf(json.replaceAll("\\s", "").split("\""));
    for (int i = 0; i < elements.size() - 2; i++) {
      if (!"id".equals(elements.get(i))) {
        continue;
      }
      String idValue = elements.get(i + 2);
      if (!pageId.equals(idValue)) {
        continue;
      }
      for (int j = i; j < elements.size() - 2; j++) {
        if ("id".equals(elements.get(j)) && j != i) {
          break;
        }
        if ("access_token".equals(elements.get(j))) {
          return elements.get(j + 2);
        }
      }
    }
    return null;
  }

  String extractJsonValue(String json, String key) {
    if (Strings.isNullOrEmpty(json)) {
      return null;
    }

    List<String> elements = ImmutableList.copyOf(json.replaceAll("\\s", "").split("\""));
    int index = elements.indexOf(key);
    if (index == -1 || index + 2 >= elements.size()) {
      return null;
    }
    return elements.get(index + 2);
  }

  private String downloadAsString(String urlString) throws DownloaderException {
    try {
      return downloader.downloadAsString(new URL(urlString));
    } catch (MalformedURLException e) {
      throw new DownloaderException(e);
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
