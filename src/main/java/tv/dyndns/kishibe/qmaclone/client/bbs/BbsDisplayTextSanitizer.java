package tv.dyndns.kishibe.qmaclone.client.bbs;

/**
 * 掲示板本文を表示用テキストへ変換するユーティリティ。
 */
final class BbsDisplayTextSanitizer {
  private static final int MAX_BODY_DISPLAY_LENGTH = 4000;

  private BbsDisplayTextSanitizer() {}

  /**
   * HTMLタグを除去しつつ、改行タグは改行文字に復元する。
   *
   * @param body 掲示板本文
   * @return 表示用テキスト
   */
  static String toDisplayText(String body) {
    if (body == null) {
      return "";
    }
    StringBuilder text = new StringBuilder(body.length());
    StringBuilder tag = new StringBuilder();
    boolean inTag = false;
    for (int i = 0; i < body.length(); ++i) {
      char ch = body.charAt(i);
      if (inTag) {
        if (ch == '>') {
          String lowerTag = tag.toString().toLowerCase();
          if (lowerTag.startsWith("br") || lowerTag.startsWith("/div") || lowerTag.startsWith("/p")) {
            text.append('\n');
          }
          inTag = false;
          tag.setLength(0);
        } else {
          tag.append(ch);
        }
      } else if (ch == '<') {
        inTag = true;
      } else if (ch == '\u00A0') {
        text.append(' ');
      } else {
        text.append(ch);
      }
    }
    String normalized = text.toString().replace("&nbsp;", " ").replace("&#160;", " ").trim();
    if (normalized.length() > MAX_BODY_DISPLAY_LENGTH) {
      return normalized.substring(0, MAX_BODY_DISPLAY_LENGTH) + "\n...(長文のため省略)";
    }
    return normalized;
  }
}

