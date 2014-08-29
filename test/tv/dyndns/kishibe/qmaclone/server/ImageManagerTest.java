package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.ImageManager.ImageLinkProcessor;
import tv.dyndns.kishibe.qmaclone.server.ImageManager.Parameter;
import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.gwt.thirdparty.guava.common.collect.Maps;
import com.google.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class ImageManagerTest {

  private static final String FAKE_IMAGE_URL_OK = "https://www.google.co.jp/images/nav_logo170_hr.png";
  private static final String FAKE_IMAGE_URL_NOT_FOUND = "https://www.google.co.jp/logo.png";
  private static final String FAKE_IMAGE_URL_MALFORMED = "this is a malformed url";
  private static final String FAKE_IMAGE_URL_UNKNOWN_HOST = "http://www.okayamamokei.com/logo.png";
  private static final int FAKE_PROBLEM_ID = -111111;

  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
  @Inject
  private ImageManager imageManager;
  @Inject
  private Downloader downloader;

  @Ignore
  @Test
  public void testImageManagerTest() {
    assertTrue(new File("/tmp/qmaclone/image").isDirectory());
    assertTrue(new File("/tmp/qmaclone/image/input").isDirectory());
    assertTrue(new File("/tmp/qmaclone/image/output").isDirectory());
    assertEquals(new File("/tmp/qmaclone/image"), ImageIO.getCacheDirectory());
    assertTrue(ImageIO.getUseCache());
  }

  @Test
  public void testGetInputCacheFile() {
    assertEquals(new File("/tmp/qmaclone/image/input/66/ab7ab659551ee960af518f169c688f319a6574"),
        imageManager.getInputCacheFile("QMAClone"));
  }

  @Test
  public void testGetOutputCacheFile() {
    assertEquals(new File("/tmp/qmaclone/image/output/a9/fd962cc05723ab44db1bbb75a33ba655b3d51f"),
        imageManager.getOutputCacheFile(new Parameter("QMAClone", 512, 384, true)));
  }

  @Test
  public void testResizeImage() throws IOException {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    imageManager.resizeImage(new File("testdata/1394387_2204778689.jpg"), 32, 16, true, file);
    BufferedImage image = ImageIO.read(file);
    assertEquals(32, image.getWidth());
    assertEquals(16, image.getHeight());
  }

  @Test
  public void resizeImageShouldFillTransparencyWithWhiteForGif() throws IOException {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    imageManager.resizeImage(new File("testdata/img378.gif"), 512, 384, true, file);
    BufferedImage image = ImageIO.read(file);
    int rgb = image.getRGB(0, 0);
    int r = rgb & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = (rgb >> 16) & 0xff;
    int a = (rgb >>> 24) & 0xff;
    assertEquals(255, r);
    assertEquals(255, g);
    assertEquals(255, b);
    assertEquals(255, a);
  }

  @Test
  public void resizeImageShouldFillTransparencyWithWhiteForPng() throws IOException {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    imageManager.resizeImage(
        new File("testdata/160px-Japanese_Map_symbol_(Police_station).svg.png"), 512, 384, true,
        file);
    BufferedImage image = ImageIO.read(file);
    int rgb = image.getRGB(0, 0);
    int r = rgb & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = (rgb >> 16) & 0xff;
    int a = (rgb >>> 24) & 0xff;
    assertEquals(255, r);
    assertEquals(255, g);
    assertEquals(255, b);
    assertEquals(255, a);
  }

  @Test
  public void testResizeImageShouldFillCanvasIfKeepAspectRatioIsFalse() throws IOException {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    imageManager.resizeImage(new File("testdata/1394387_2204778689.jpg"), 32, 16, false, file);
    BufferedImage image = ImageIO.read(file);
    int rgb = image.getRGB(0, 0);
    int r = rgb & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = (rgb >> 16) & 0xff;
    assertNotEquals(255, r);
    assertNotEquals(255, g);
    assertNotEquals(255, b);
  }

  @Test(expected = IOException.class)
  public void testResizeImageThrowsExceptionOnFileNotFound() throws IOException {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    imageManager.resizeImage(new File("testdata/hogehoge.fugafuga"), 32, 16, true, file);
  }

  @Test
  public void testToHashString() {
    assertEquals("66ab7ab659551ee960af518f169c688f319a6574", ImageManager.toHashString("QMAClone"));
  }

  @Test
  public void testGetLastModified() throws IOException {
    File file = new File("/tmp/qmaclone/image/output/a9/fd962cc05723ab44db1bbb75a33ba655b3d51f");
    Files.createParentDirs(file);
    Files.write("test", file, Charset.forName("utf-8"));
    file.deleteOnExit();

    assertEquals(file.lastModified(),
        imageManager.getLastModified(new Parameter("QMAClone", 512, 384, true)));
  }

  @Test
  public void imageLinkProcessor_process_ok() throws Exception {
    List<PacketImageLink> imageLinks = Lists.newArrayList();
    Map<String, Integer> urlToStatusCode = Maps.newHashMap();
    ImageLinkProcessor processor = new ImageLinkProcessor(imageManager, downloader, imageLinks,
        urlToStatusCode);

    processor.process(createProblemWithImageUrl(FAKE_IMAGE_URL_OK));

    assertEquals(ImmutableMap.of(FAKE_IMAGE_URL_OK, 200), urlToStatusCode);
    assertEquals(ImmutableList.of(), imageLinks);
  }

  @Test
  public void imageLinkProcessor_process_notFound() throws Exception {
    List<PacketImageLink> imageLinks = Lists.newArrayList();
    Map<String, Integer> urlToStatusCode = Maps.newHashMap();
    ImageLinkProcessor processor = new ImageLinkProcessor(imageManager, downloader, imageLinks,
        urlToStatusCode);

    processor.process(createProblemWithImageUrl(FAKE_IMAGE_URL_NOT_FOUND));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = FAKE_PROBLEM_ID;
    expectedImageLink.url = FAKE_IMAGE_URL_NOT_FOUND;
    expectedImageLink.statusCode = 404;

    assertEquals(ImmutableMap.of(FAKE_IMAGE_URL_NOT_FOUND, 404), urlToStatusCode);
    assertEquals(ImmutableList.of(expectedImageLink), imageLinks);
  }

  @Test
  public void imageLinkProcessor_process_malformedUrl() throws Exception {
    List<PacketImageLink> imageLinks = Lists.newArrayList();
    Map<String, Integer> urlToStatusCode = Maps.newHashMap();
    ImageLinkProcessor processor = new ImageLinkProcessor(imageManager, downloader, imageLinks,
        urlToStatusCode);

    processor.process(createProblemWithImageUrl(FAKE_IMAGE_URL_MALFORMED));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = FAKE_PROBLEM_ID;
    expectedImageLink.url = FAKE_IMAGE_URL_MALFORMED;
    expectedImageLink.statusCode = ImageManager.STATUS_CODE_MALFORMED_URL_EXCEPTION;

    assertEquals(
        ImmutableMap.of(FAKE_IMAGE_URL_MALFORMED, ImageManager.STATUS_CODE_MALFORMED_URL_EXCEPTION),
        urlToStatusCode);
    assertEquals(ImmutableList.of(expectedImageLink), imageLinks);
  }

  @Test
  public void imageLinkProcessor_process_unknownHost() throws Exception {
    List<PacketImageLink> imageLinks = Lists.newArrayList();
    Map<String, Integer> urlToStatusCode = Maps.newHashMap();
    ImageLinkProcessor processor = new ImageLinkProcessor(imageManager, downloader, imageLinks,
        urlToStatusCode);

    processor.process(createProblemWithImageUrl(FAKE_IMAGE_URL_UNKNOWN_HOST));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = FAKE_PROBLEM_ID;
    expectedImageLink.url = FAKE_IMAGE_URL_UNKNOWN_HOST;
    expectedImageLink.statusCode = ImageManager.STATUS_CODE_DOWNLOAD_FAILURE;

    assertEquals(
        ImmutableMap.of(FAKE_IMAGE_URL_UNKNOWN_HOST, ImageManager.STATUS_CODE_DOWNLOAD_FAILURE),
        urlToStatusCode);
    assertEquals(ImmutableList.of(expectedImageLink), imageLinks);
  }

  private PacketProblem createProblemWithImageUrl(String imageUrl) {
    PacketProblem problem = new PacketProblem();
    problem.id = FAKE_PROBLEM_ID;
    problem.choices = new String[0];
    problem.answers = new String[0];
    problem.imageUrl = imageUrl;
    return problem;
  }
}
