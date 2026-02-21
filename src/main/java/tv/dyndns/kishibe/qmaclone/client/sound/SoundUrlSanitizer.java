package tv.dyndns.kishibe.qmaclone.client.sound;

/**
 * 音声URLの安全性を検証する。
 */
public final class SoundUrlSanitizer {

  private SoundUrlSanitizer() {
  }

  /**
   * 再生URLを最小限の安全条件で正規化する。
   */
  public static String sanitizeSoundUrl(String url) {
    if (url == null) {
      return null;
    }
    String trimmed = url.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    String lowerCaseUrl = trimmed.toLowerCase();
    if (!lowerCaseUrl.startsWith("http://") && !lowerCaseUrl.startsWith("https://")) {
      return null;
    }
    if (containsUnsafeCharacter(trimmed)) {
      return null;
    }
    return trimmed;
  }

  /**
   * HTML注入に使える危険文字の有無を返す。
   */
  private static boolean containsUnsafeCharacter(String value) {
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (ch == '"' || ch == '\'' || ch == '<' || ch == '>') {
        return true;
      }
      if (ch <= 0x1F || ch == 0x7F) {
        return true;
      }
    }
    return false;
  }
}
