package tv.dyndns.kishibe.qmaclone.client.util;

public class ImageUrl {
  public static String normalize(String url) {
    url = url.replaceAll("http://upload.wikimedia.org/", "https://upload.wikimedia.org/");
    return url;
  }
}
