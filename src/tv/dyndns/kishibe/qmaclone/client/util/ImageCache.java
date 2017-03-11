package tv.dyndns.kishibe.qmaclone.client.util;

public class ImageCache {
  private static final String URL_PREFIX = "http://kishibe.dyndns.tv/QMAClone/image";
  private static final char[] HEX = "0123456789abcdef".toCharArray();

  private ImageCache() {
  }

  public static String getUrl(String url, int width, int height) {
    return URL_PREFIX + "/url/" + encode(url) + "/width/" + width + "/height/" + height
        + "/keepAspectRatio/false";
  }

  private static String encode(String s) {
    StringBuilder sb = new StringBuilder();
    for (char ch : s.toCharArray()) {
      int i0 = ch % 16;
      ch /= 16;
      int i1 = ch % 16;
      ch /= 16;
      int i2 = ch % 16;
      ch /= 16;
      int i3 = ch % 16;
      sb.append(HEX[i3]).append(HEX[i2]).append(HEX[i1]).append(HEX[i0]);
    }
    return sb.toString();
  }
}
