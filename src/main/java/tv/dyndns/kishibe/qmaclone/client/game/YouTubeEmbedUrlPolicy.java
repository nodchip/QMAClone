package tv.dyndns.kishibe.qmaclone.client.game;

/**
 * YouTube埋め込みURLの安全性ポリシー。
 */
final class YouTubeEmbedUrlPolicy {
  private YouTubeEmbedUrlPolicy() {
  }

  /**
   * URLから安全な動画IDを抽出する。
   */
  static String extractValidVideoId(String url) {
    String candidateVideoId = extractCandidateVideoId(url);
    if (!isValidYouTubeVideoId(candidateVideoId)) {
      return null;
    }
    return candidateVideoId;
  }

  /**
   * URLから動画ID候補を抽出する。
   */
  private static String extractCandidateVideoId(String url) {
    String idFromQuery = extractVideoIdFromQuery(url);
    if (idFromQuery != null) {
      return idFromQuery;
    }
    return extractVideoIdFromPath(url);
  }

  private static String extractVideoIdFromQuery(String url) {
    int queryIndex = url.indexOf('?');
    if (queryIndex == -1 || queryIndex + 1 >= url.length()) {
      return null;
    }
    String query = url.substring(queryIndex + 1);
    for (String pair : query.split("&")) {
      int equalsIndex = pair.indexOf('=');
      if (equalsIndex <= 0 || equalsIndex + 1 >= pair.length()) {
        continue;
      }
      String key = pair.substring(0, equalsIndex);
      if ("v".equals(key)) {
        return pair.substring(equalsIndex + 1);
      }
    }
    return null;
  }

  private static String extractVideoIdFromPath(String url) {
    int pathSeparatorIndex = url.lastIndexOf('/');
    if (pathSeparatorIndex == -1 || pathSeparatorIndex + 1 >= url.length()) {
      return null;
    }
    String id = url.substring(pathSeparatorIndex + 1);
    int queryOrParameterIndex = id.indexOf('?');
    if (queryOrParameterIndex != -1) {
      id = id.substring(0, queryOrParameterIndex);
    }
    int extraParameterIndex = id.indexOf('&');
    if (extraParameterIndex != -1) {
      id = id.substring(0, extraParameterIndex);
    }
    int fragmentIndex = id.indexOf('#');
    if (fragmentIndex != -1) {
      id = id.substring(0, fragmentIndex);
    }
    return id;
  }

  /**
   * YouTube動画IDとして妥当か検証する。
   */
  private static boolean isValidYouTubeVideoId(String videoId) {
    if (videoId == null || videoId.length() != 11) {
      return false;
    }
    for (int i = 0; i < videoId.length(); i++) {
      char character = videoId.charAt(i);
      if (character >= 'A' && character <= 'Z') {
        continue;
      }
      if (character >= 'a' && character <= 'z') {
        continue;
      }
      if (character >= '0' && character <= '9') {
        continue;
      }
      if (character == '_' || character == '-') {
        continue;
      }
      return false;
    }
    return true;
  }
}
