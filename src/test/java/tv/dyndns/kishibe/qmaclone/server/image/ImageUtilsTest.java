package tv.dyndns.kishibe.qmaclone.server.image;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.image.ImageUtils.Parameter;
import tv.dyndns.kishibe.qmaclone.server.testing.GuiceInjectionExtension;

import com.google.common.io.Files;
import com.google.inject.Inject;

@ExtendWith(MockitoExtension.class)
@ExtendWith(GuiceInjectionExtension.class)
public class ImageUtilsTest {
  @Inject
  private ImageUtils imageUtils;

  @Disabled
  @Test
  public void testImageManagerTest() {
    assertTrue(new File(Constant.FILE_PATH_BASE + "image").isDirectory());
    assertTrue(new File(Constant.FILE_PATH_BASE + "image/input").isDirectory());
    assertTrue(new File(Constant.FILE_PATH_BASE + "image/output").isDirectory());
    assertEquals(new File(Constant.FILE_PATH_BASE + "image"), ImageIO.getCacheDirectory());
    assertTrue(ImageIO.getUseCache());
  }

  @Test
  public void testGetInputCacheFile() {
    assertEquals(new File(Constant.FILE_PATH_BASE + "image/input/66/ab7ab659551ee960af518f169c688f319a6574"),
        imageUtils.getInputCacheFile("QMAClone"));
  }

  @Test
  public void testGetOutputCacheFile() {
    assertEquals(new File(Constant.FILE_PATH_BASE + "image/output/a9/fd962cc05723ab44db1bbb75a33ba655b3d51f"),
        imageUtils.getOutputCacheFile(new Parameter("QMAClone", 512, 384, true)));
  }

  @Test
  public void testResizeImage() throws IOException {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
      imageUtils.resizeImage(new File("testdata/1394387_2204778689.jpg"), 32, 16, true, stream);
    }
    BufferedImage image = ImageIO.read(file);
    assertEquals(32, image.getWidth());
    assertEquals(16, image.getHeight());
  }

  @Test
  public void resizeImageShouldFillTransparencyWithWhiteForGif() throws IOException {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
      imageUtils.resizeImage(new File("testdata/img378.gif"), 512, 384, true, stream);
    }
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
    try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
      imageUtils.resizeImage(new File("testdata/160px-Japanese_Map_symbol_(Police_station).svg.png"), 512, 384, true,
          stream);
    }
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
    try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
      imageUtils.resizeImage(new File("testdata/1394387_2204778689.jpg"), 32, 16, false, stream);
    }
    BufferedImage image = ImageIO.read(file);
    int rgb = image.getRGB(0, 0);
    int r = rgb & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = (rgb >> 16) & 0xff;
    assertNotEquals(255, r);
    assertNotEquals(255, g);
    assertNotEquals(255, b);
  }

  @Test
  public void testResizeImageThrowsExceptionOnFileNotFound() throws IOException {
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
      assertThrows(IOException.class,
          () -> imageUtils.resizeImage(new File("testdata/hogehoge.fugafuga"), 32, 16, true, stream));
    }
  }

  @Test
  public void testToHashString() {
    assertEquals("66ab7ab659551ee960af518f169c688f319a6574", ImageUtils.toHashString("QMAClone"));
  }

  @Test
  public void testGetLastModified() throws IOException {
    File file = new File(Constant.FILE_PATH_BASE + "image/output/a9/fd962cc05723ab44db1bbb75a33ba655b3d51f");
    Files.createParentDirs(file);
    Files.write("test", file, Charset.forName("utf-8"));
    file.deleteOnExit();

    assertEquals(file.lastModified(), imageUtils.getLastModified(new Parameter("QMAClone", 512, 384, true)));
  }
}

