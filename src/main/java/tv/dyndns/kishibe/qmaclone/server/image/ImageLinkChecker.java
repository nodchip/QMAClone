package tv.dyndns.kishibe.qmaclone.server.image;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.database.ProblemProcessable;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.inject.Inject;

public class ImageLinkChecker implements ProblemProcessable {
  public interface Factory {
    ImageLinkChecker create();
  }

  private static final Logger logger = Logger.getLogger(ImageLinkChecker.class.toString());
  @VisibleForTesting
  static final int DEFAULT_HOST_ACCESS_INTERVAL_MILLIS = 300;
  @VisibleForTesting
  static final int DEFAULT_PARALLEL_DOWNLOADS = 8;
  @VisibleForTesting
  static final int STATUS_CODE_MALFORMED_URL_EXCEPTION = -1;
  @VisibleForTesting
  static final int STATUS_CODE_DOWNLOAD_FAILURE = -2;

  private final ImageUtils imageUtils;
  private final Downloader downloader;
  private final long hostAccessIntervalMillis;
  private final ExecutorService executorService;
  private final HostAccessScheduler hostAccessScheduler = new HostAccessScheduler();
  private final List<PendingImageLink> pendingImageLinks = Collections.synchronizedList(new ArrayList<>());
  private final ConcurrentMap<String, Future<ImageCheckResult>> urlToImageCheckResult =
      new ConcurrentHashMap<>();
  private final List<PacketImageLink> imageLinks = new ArrayList<>();
  private volatile boolean imageLinksResolved;

  @Inject
  public ImageLinkChecker(ImageUtils imageUtils, Downloader downloader) {
    this(imageUtils, downloader, DEFAULT_HOST_ACCESS_INTERVAL_MILLIS, DEFAULT_PARALLEL_DOWNLOADS);
  }

  @VisibleForTesting
  ImageLinkChecker(ImageUtils imageUtils, Downloader downloader, long hostAccessIntervalMillis,
      int parallelDownloads) {
    this.imageUtils = Preconditions.checkNotNull(imageUtils);
    this.downloader = Preconditions.checkNotNull(downloader);
    Preconditions.checkArgument(hostAccessIntervalMillis >= 0,
        "hostAccessIntervalMillis must be >= 0");
    Preconditions.checkArgument(parallelDownloads >= 1, "parallelDownloads must be >= 1");
    this.hostAccessIntervalMillis = hostAccessIntervalMillis;
    this.executorService = Executors.newFixedThreadPool(parallelDownloads);
  }

  @Override
  public void process(PacketProblem problem) {
    for (String url : problem.getImageUrls()) {
      File inputCacheFile = imageUtils.getInputCacheFile(url);
      try {
        Files.createParentDirs(inputCacheFile);
      } catch (IOException e) {
        logger.log(Level.WARNING, "入力画像キャッシュディレクトリの作成に失敗しました。処理を続行します: inputCacheFile="
            + inputCacheFile, e);
        continue;
      }

      // 同一URLは1回だけ検査し、問題ごとの参照だけ保持する。
      urlToImageCheckResult.computeIfAbsent(url, ignored -> executorService.submit(
          () -> downloadAndCheckImage(url, inputCacheFile)));
      pendingImageLinks.add(new PendingImageLink(problem.id, url));
    }
  }

  public List<PacketImageLink> getImageLinks() {
    resolveImageLinksIfNeeded();
    return Preconditions.checkNotNull(imageLinks);
  }

  public void shutdown() {
    executorService.shutdownNow();
  }

  private void resolveImageLinksIfNeeded() {
    if (imageLinksResolved) {
      return;
    }

    synchronized (this) {
      if (imageLinksResolved) {
        return;
      }

      Map<String, ImageCheckResult> resolvedUrlToResult = new HashMap<>();
      for (PendingImageLink pendingImageLink : pendingImageLinks) {
        ImageCheckResult imageCheckResult = resolvedUrlToResult.get(pendingImageLink.url);
        if (imageCheckResult == null) {
          imageCheckResult = getImageCheckResult(pendingImageLink.url);
          resolvedUrlToResult.put(pendingImageLink.url, imageCheckResult);
        }

        if (imageCheckResult.isHealthy()) {
          continue;
        }

        PacketImageLink imageLink = new PacketImageLink();
        imageLink.problemId = pendingImageLink.problemId;
        imageLink.url = pendingImageLink.url;
        imageLink.statusCode = imageCheckResult.statusCode;
        imageLinks.add(imageLink);

        logger.info(String.format("リンク切れ画像を検出しました: |imageLinks|=%s imageLink=%s", imageLinks.size(),
            imageLink));
      }

      executorService.shutdown();
      imageLinksResolved = true;
    }
  }

  private ImageCheckResult getImageCheckResult(String url) {
    Future<ImageCheckResult> future = urlToImageCheckResult.get(url);
    if (future == null) {
      return new ImageCheckResult(STATUS_CODE_DOWNLOAD_FAILURE, false);
    }

    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.log(Level.WARNING, "画像リンク検査の待機中に割り込みが発生しました: url=" + url, e);
      return new ImageCheckResult(STATUS_CODE_DOWNLOAD_FAILURE, false);
    } catch (ExecutionException e) {
      logger.log(Level.WARNING, "画像リンク検査中に予期せぬエラーが発生しました: url=" + url, e);
      return new ImageCheckResult(STATUS_CODE_DOWNLOAD_FAILURE, false);
    }
  }

  private ImageCheckResult downloadAndCheckImage(String url, File inputCacheFile) {
    int statusCode;
    try {
      URL imageUrl = new URL(url);
      waitForHostAccess(imageUrl);
      downloader.downloadToFile(imageUrl, inputCacheFile);
      statusCode = 200;
    } catch (MalformedURLException e) {
      logger.log(Level.WARNING, "不正なURLです: url=" + url, e);
      statusCode = STATUS_CODE_MALFORMED_URL_EXCEPTION;
    } catch (DownloaderException e) {
      logger.log(Level.WARNING, "ダウンロードに失敗しました: url=" + url, e);
      statusCode = e.hasStatusCode() ? e.getStatusCode() : STATUS_CODE_DOWNLOAD_FAILURE;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.log(Level.WARNING, "画像リンク検査の待機中に割り込みが発生しました: url=" + url, e);
      statusCode = STATUS_CODE_DOWNLOAD_FAILURE;
    }

    boolean healthyImage = statusCode / 100 == 2 && imageUtils.isImage(inputCacheFile);
    return new ImageCheckResult(statusCode, healthyImage);
  }

  private void waitForHostAccess(URL imageUrl) throws InterruptedException {
    if (hostAccessIntervalMillis <= 0) {
      return;
    }

    String host = imageUrl.getHost();
    if (host == null || host.isEmpty()) {
      return;
    }

    long reservedAtNanos = hostAccessScheduler.reserveAccessAtNanos(
        host.toLowerCase(Locale.ROOT), TimeUnit.MILLISECONDS.toNanos(hostAccessIntervalMillis));
    long waitNanos = reservedAtNanos - System.nanoTime();
    if (waitNanos > 0) {
      TimeUnit.NANOSECONDS.sleep(waitNanos);
    }
  }

  private static class PendingImageLink {
    private final int problemId;
    private final String url;

    private PendingImageLink(int problemId, String url) {
      this.problemId = problemId;
      this.url = Preconditions.checkNotNull(url);
    }
  }

  private static class ImageCheckResult {
    private final int statusCode;
    private final boolean healthy;

    private ImageCheckResult(int statusCode, boolean healthy) {
      this.statusCode = statusCode;
      this.healthy = healthy;
    }

    private boolean isHealthy() {
      return healthy;
    }
  }

  /**
   * 同一ホストのアクセス開始時刻を順番に予約し、バーストアクセスを抑制する。
   */
  private static class HostAccessScheduler {
    private final ConcurrentMap<String, HostSchedule> hostToSchedule = new ConcurrentHashMap<>();

    private long reserveAccessAtNanos(String host, long intervalNanos) {
      HostSchedule schedule = hostToSchedule.computeIfAbsent(host, key -> new HostSchedule());
      return schedule.reserveAccessAtNanos(intervalNanos);
    }
  }

  private static class HostSchedule {
    private long nextAccessAtNanos;

    private synchronized long reserveAccessAtNanos(long intervalNanos) {
      long now = System.nanoTime();
      long reservedAtNanos = Math.max(now, nextAccessAtNanos);
      nextAccessAtNanos = reservedAtNanos + intervalNanos;
      return reservedAtNanos;
    }
  }
}
