package tv.dyndns.kishibe.qmaclone.client.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * YouTubeEmbedUrlPolicyのテスト。
 */
public class YouTubeEmbedUrlPolicyTest {
  private static final String VALID_VIDEO_ID = "dQw4w9WgXcQ";

  /**
   * watch URLから動画IDを抽出できる。
   */
  @Test
  public void extractValidVideoIdShouldAcceptWatchUrl() {
    String url = "https://www.youtube.com/watch?v=" + VALID_VIDEO_ID;

    assertEquals(VALID_VIDEO_ID, YouTubeEmbedUrlPolicy.extractValidVideoId(url));
  }

  /**
   * short URLから動画IDを抽出できる。
   */
  @Test
  public void extractValidVideoIdShouldAcceptShortUrl() {
    String url = "https://youtu.be/" + VALID_VIDEO_ID + "?t=10";

    assertEquals(VALID_VIDEO_ID, YouTubeEmbedUrlPolicy.extractValidVideoId(url));
  }

  /**
   * 不正文字を含むIDは拒否する。
   */
  @Test
  public void extractValidVideoIdShouldRejectInvalidCharacters() {
    String url = "https://www.youtube.com/watch?v=dQw4w9WgXc\"";

    assertNull(YouTubeEmbedUrlPolicy.extractValidVideoId(url));
  }

  /**
   * 長さ不正のIDは拒否する。
   */
  @Test
  public void extractValidVideoIdShouldRejectInvalidLength() {
    String url = "https://www.youtube.com/watch?v=short";

    assertNull(YouTubeEmbedUrlPolicy.extractValidVideoId(url));
  }

  /**
   * クエリが壊れているURLは拒否する。
   */
  @Test
  public void extractValidVideoIdShouldRejectBrokenQuery() {
    String url = "https://www.youtube.com/watch?foo=1&v";

    assertNull(YouTubeEmbedUrlPolicy.extractValidVideoId(url));
  }
}
