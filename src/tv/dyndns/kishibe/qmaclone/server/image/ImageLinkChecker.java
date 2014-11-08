package tv.dyndns.kishibe.qmaclone.server.image;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  static final int STATUS_CODE_MALFORMED_URL_EXCEPTION = -1;
  @VisibleForTesting
  static final int STATUS_CODE_DOWNLOAD_FAILURE = -2;

  private final ImageUtils imageUtils;
  private final Downloader downloader;
  private List<PacketImageLink> imageLinks;
  private Map<String, Integer> urlToStatusCode;

  @Inject
  public ImageLinkChecker(ImageUtils imageUtils, Downloader downloader) {
    this.imageUtils = Preconditions.checkNotNull(imageUtils);
    this.downloader = Preconditions.checkNotNull(downloader);
  }

  @Override
  public void process(PacketProblem problem) {
    imageLinks = new ArrayList<>();
    urlToStatusCode = new HashMap<>();

    for (String url : problem.getImageUrls()) {
      File inputCacheFile = imageUtils.getInputCacheFile(url);

      int statusCode;
      if (urlToStatusCode.containsKey(url)) {
        statusCode = urlToStatusCode.get(url);
      } else {
        try {
          Files.createParentDirs(inputCacheFile);
        } catch (IOException e) {
          logger.log(Level.WARNING, "入力画像キャッシュディレクトリの作成に失敗しました。処理を続行します: inputCacheFile="
              + inputCacheFile, e);
          continue;
        }

        try {
          downloader.downloadToFile(new URL(url), inputCacheFile);
          statusCode = 200;

        } catch (MalformedURLException e) {
          logger.log(Level.WARNING, "不正なURLです: url=" + url, e);
          // URLが不正な場合はダミーステータスコードを表示する
          statusCode = STATUS_CODE_MALFORMED_URL_EXCEPTION;

        } catch (DownloaderException e) {
          logger.log(Level.WARNING, "ダウンロードに失敗しました: url=" + url, e);
          // ダウンロードに失敗した場合はステータスコードまたはダミーステータスコードを表示する
          statusCode = e.hasStatusCode() ? e.getStatusCode() : STATUS_CODE_DOWNLOAD_FAILURE;
        }

        urlToStatusCode.put(url, statusCode);
      }

      // 正常取得かつ正常画像の場合はエラー出力をしない
      if (statusCode / 100 == 2 && imageUtils.isImage(inputCacheFile)) {
        continue;
      }

      PacketImageLink imageLink = new PacketImageLink();
      imageLink.problemId = problem.id;
      imageLink.url = url;
      imageLink.statusCode = statusCode;
      imageLinks.add(imageLink);
      logger.info("リンク切れ画像を検出しました: " + imageLink);
    }
  }

  public List<PacketImageLink> getImageLinks() {
    return Preconditions.checkNotNull(imageLinks);
  }
}
