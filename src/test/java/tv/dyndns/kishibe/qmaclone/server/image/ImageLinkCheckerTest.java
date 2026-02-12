package tv.dyndns.kishibe.qmaclone.server.image;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.common.collect.ImmutableList;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ImageLinkCheckerTest {
  private static final int PROBLEM_ID = -111111;
  private static final String IMAGE_URL_OK = "https://example.com/ok.png";
  private static final String IMAGE_URL_NOT_FOUND = "https://example.com/notfound.png";
  private static final String IMAGE_URL_MALFORMED = "this is a malformed url";
  private static final String IMAGE_URL_UNKNOWN_HOST = "https://example.com/unknown.png";

  @Mock
  private ImageUtils imageUtils;
  @Mock
  private Downloader downloader;

  @Test
  public void processWorksWithStatusCodeOk() throws Exception {
    File file = File.createTempFile("QMAClone", ".png");
    when(imageUtils.getInputCacheFile(IMAGE_URL_OK)).thenReturn(file);
    when(imageUtils.isImage(file)).thenReturn(true);

    ImageLinkChecker imageLinkChecker = new ImageLinkChecker(imageUtils, downloader);
    imageLinkChecker.process(createProblem(IMAGE_URL_OK));

    assertThat(imageLinkChecker.getImageLinks()).isEmpty();
  }

  @Test
  public void processWorksWithStatusCodeNotFound() throws Exception {
    File file = File.createTempFile("QMAClone", ".png");
    when(imageUtils.getInputCacheFile(IMAGE_URL_NOT_FOUND)).thenReturn(file);
    when(imageUtils.isImage(file)).thenReturn(false);
    doThrow(new DownloaderException("404")).when(downloader).downloadToFile(any(URL.class), any(File.class));

    ImageLinkChecker imageLinkChecker = new ImageLinkChecker(imageUtils, downloader);
    imageLinkChecker.process(createProblem(IMAGE_URL_NOT_FOUND));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = PROBLEM_ID;
    expectedImageLink.url = IMAGE_URL_NOT_FOUND;
    expectedImageLink.statusCode = ImageLinkChecker.STATUS_CODE_DOWNLOAD_FAILURE;
    assertThat(imageLinkChecker.getImageLinks()).isEqualTo(ImmutableList.of(expectedImageLink));
  }

  @Test
  public void processWorksWithMalformedUrl() throws Exception {
    File file = File.createTempFile("QMAClone", ".png");
    when(imageUtils.getInputCacheFile(IMAGE_URL_MALFORMED)).thenReturn(file);

    ImageLinkChecker imageLinkChecker = new ImageLinkChecker(imageUtils, downloader);
    imageLinkChecker.process(createProblem(IMAGE_URL_MALFORMED));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = PROBLEM_ID;
    expectedImageLink.url = IMAGE_URL_MALFORMED;
    expectedImageLink.statusCode = ImageLinkChecker.STATUS_CODE_MALFORMED_URL_EXCEPTION;
    assertThat(imageLinkChecker.getImageLinks()).isEqualTo(ImmutableList.of(expectedImageLink));
  }

  @Test
  public void processWorksWithUnknownHost() throws Exception {
    File file = File.createTempFile("QMAClone", ".png");
    when(imageUtils.getInputCacheFile(IMAGE_URL_UNKNOWN_HOST)).thenReturn(file);
    doThrow(new DownloaderException("unknown")).when(downloader).downloadToFile(any(URL.class), any(File.class));

    ImageLinkChecker imageLinkChecker = new ImageLinkChecker(imageUtils, downloader);
    imageLinkChecker.process(createProblem(IMAGE_URL_UNKNOWN_HOST));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = PROBLEM_ID;
    expectedImageLink.url = IMAGE_URL_UNKNOWN_HOST;
    expectedImageLink.statusCode = ImageLinkChecker.STATUS_CODE_DOWNLOAD_FAILURE;
    assertThat(imageLinkChecker.getImageLinks()).isEqualTo(ImmutableList.of(expectedImageLink));
  }

  @Test
  public void processReturnsMultipleImageLinks() throws Exception {
    File file = File.createTempFile("QMAClone", ".png");
    when(imageUtils.getInputCacheFile(IMAGE_URL_NOT_FOUND)).thenReturn(file);
    when(imageUtils.isImage(file)).thenReturn(false);
    doThrow(new DownloaderException("404")).when(downloader).downloadToFile(any(URL.class), any(File.class));

    ImageLinkChecker imageLinkChecker = new ImageLinkChecker(imageUtils, downloader);
    imageLinkChecker.process(createProblem(IMAGE_URL_NOT_FOUND));
    imageLinkChecker.process(createProblem(IMAGE_URL_NOT_FOUND));

    PacketImageLink expectedImageLink = new PacketImageLink();
    expectedImageLink.problemId = PROBLEM_ID;
    expectedImageLink.url = IMAGE_URL_NOT_FOUND;
    expectedImageLink.statusCode = ImageLinkChecker.STATUS_CODE_DOWNLOAD_FAILURE;
    assertThat(imageLinkChecker.getImageLinks()).isEqualTo(
        ImmutableList.of(expectedImageLink, expectedImageLink));
  }

  private static PacketProblem createProblem(String url) {
    PacketProblem problem = new PacketProblem();
    problem.id = PROBLEM_ID;
    problem.type = ProblemType.Click;
    problem.choices = new String[] { url };
    return problem;
  }
}
