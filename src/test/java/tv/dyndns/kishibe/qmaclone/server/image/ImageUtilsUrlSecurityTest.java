package tv.dyndns.kishibe.qmaclone.server.image;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tv.dyndns.kishibe.qmaclone.server.ThreadPool;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;

/**
 * 画像取得URLのセキュリティ検証テスト。
 */
public class ImageUtilsUrlSecurityTest {

  /**
   * ループバック宛URLを拒否する。
   */
  @Test
  public void validateDownloadTargetUrlShouldRejectLoopbackAddress() throws Exception {
    ImageUtils imageUtils = new ImageUtils(Mockito.mock(ThreadPool.class), Mockito.mock(Downloader.class));

    assertThrows(IOException.class,
        () -> imageUtils.validateDownloadTargetUrl(new URL("http://127.0.0.1/private.png")));
  }

  /**
   * 非HTTPスキームを拒否する。
   */
  @Test
  public void validateDownloadTargetUrlShouldRejectNonHttpScheme() throws Exception {
    ImageUtils imageUtils = new ImageUtils(Mockito.mock(ThreadPool.class), Mockito.mock(Downloader.class));

    assertThrows(IOException.class,
        () -> imageUtils.validateDownloadTargetUrl(new URL("file:///etc/passwd")));
  }

  /**
   * 公開アドレスを許可する。
   */
  @Test
  public void validateDownloadTargetUrlShouldAllowPublicAddress() throws Exception {
    ImageUtils imageUtils = new ImageUtils(Mockito.mock(ThreadPool.class), Mockito.mock(Downloader.class));

    assertDoesNotThrow(
        () -> imageUtils.validateDownloadTargetUrl(new URL("https://203.0.113.10/image.png")));
  }
}
