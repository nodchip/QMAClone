package tv.dyndns.kishibe.qmaclone.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class DownloaderTest {

  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
  @Inject
  private Downloader downloader;

  @Test
  public void testDownloadToFile200() throws Exception {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    URL url = new URL(
        "http://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Image_from_Gutza_Wikipedia.jpg/140px-Image_from_Gutza_Wikipedia.jpg");
    downloader.downloadToFile(url, file);
    assertTrue(file.isFile());
    assertEquals(5785, file.length());
  }

  @Test(expected = DownloaderException.class)
  public void testDownloadToFile403() throws Exception {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    URL url = new URL(
        "http://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Image_from_Gutza_Wikipedia.jpg");
    downloader.downloadToFile(url, file);
  }

  @Test(expected = DownloaderException.class)
  public void testDownloadToFile404() throws Exception {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    URL url = new URL("http://www.google.co.jp/images/srpr/nav_logo14.gif");
    downloader.downloadToFile(url, file);
  }

  @Test
  public void testDownloadHttps() throws Exception {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    URL url = new URL(
        "https://lh3.googleusercontent.com/-V9qGHNTN81o/AAAAAAAAAAI/AAAAAAAAAAA/lhhQJ08VnYE/s96-c/photo.jpg");
    downloader.downloadToFile(url, file);
  }
}
