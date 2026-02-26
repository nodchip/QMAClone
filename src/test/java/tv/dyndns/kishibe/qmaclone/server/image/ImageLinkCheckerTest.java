package tv.dyndns.kishibe.qmaclone.server.image;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

  @Test
  public void processWaitsBetweenRequestsToSameHost() throws Exception {
    String sameHostUrl1 = "https://same.example.com/a.png";
    String sameHostUrl2 = "https://same.example.com/b.png";
    File file1 = File.createTempFile("QMAClone", ".png");
    File file2 = File.createTempFile("QMAClone", ".png");
    when(imageUtils.getInputCacheFile(sameHostUrl1)).thenReturn(file1);
    when(imageUtils.getInputCacheFile(sameHostUrl2)).thenReturn(file2);
    when(imageUtils.isImage(file1)).thenReturn(true);
    when(imageUtils.isImage(file2)).thenReturn(true);

    List<Long> startedAtNanos = new CopyOnWriteArrayList<>();
    org.mockito.Mockito.doAnswer(invocation -> {
      startedAtNanos.add(System.nanoTime());
      return null;
    }).when(downloader).downloadToFile(any(URL.class), any(File.class));

    ImageLinkChecker imageLinkChecker = new ImageLinkChecker(imageUtils, downloader, 120, 2);
    imageLinkChecker.process(createProblem(sameHostUrl1));
    imageLinkChecker.process(createProblem(sameHostUrl2));
    imageLinkChecker.getImageLinks();

    assertThat(startedAtNanos).hasSize(2);
    Collections.sort(startedAtNanos);
    long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(startedAtNanos.get(1) - startedAtNanos.get(0));
    assertThat(elapsedMillis).isAtLeast(100L);
  }

  @Test
  public void processAllowsParallelRequestsToDifferentHosts() throws Exception {
    String url1 = "https://host-a.example.com/a.png";
    String url2 = "https://host-b.example.com/b.png";
    File file1 = File.createTempFile("QMAClone", ".png");
    File file2 = File.createTempFile("QMAClone", ".png");
    when(imageUtils.getInputCacheFile(url1)).thenReturn(file1);
    when(imageUtils.getInputCacheFile(url2)).thenReturn(file2);
    when(imageUtils.isImage(file1)).thenReturn(true);
    when(imageUtils.isImage(file2)).thenReturn(true);

    AtomicInteger activeDownloads = new AtomicInteger();
    AtomicInteger maxActiveDownloads = new AtomicInteger();
    org.mockito.Mockito.doAnswer(invocation -> {
      int current = activeDownloads.incrementAndGet();
      maxActiveDownloads.updateAndGet(value -> Math.max(value, current));
      try {
        Thread.sleep(120L);
      } finally {
        activeDownloads.decrementAndGet();
      }
      return null;
    }).when(downloader).downloadToFile(any(URL.class), any(File.class));

    ImageLinkChecker imageLinkChecker = new ImageLinkChecker(imageUtils, downloader, 120, 2);
    imageLinkChecker.process(createProblem(url1));
    imageLinkChecker.process(createProblem(url2));
    imageLinkChecker.getImageLinks();

    assertThat(maxActiveDownloads.get()).isAtLeast(2);
  }

  private static PacketProblem createProblem(String url) {
    PacketProblem problem = new PacketProblem();
    problem.id = PROBLEM_ID;
    problem.type = ProblemType.Click;
    problem.choices = new String[] { url };
    return problem;
  }
}
