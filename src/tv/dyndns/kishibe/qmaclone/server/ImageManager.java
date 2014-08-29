package tv.dyndns.kishibe.qmaclone.server;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.color.CMMException;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.database.ProblemProcessable;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class ImageManager {

  /**
   * URLに含まれるパラメーターを保持する
   */
  public static class Parameter {
    public final String url;
    public final int width;
    public final int height;
    public final boolean keepAspectRatio;

    public Parameter(String url, int width, int height, boolean keepAspectRatio) {
      this.url = url;
      this.width = width;
      this.height = height;
      this.keepAspectRatio = keepAspectRatio;
    }

    public String getHashString() {
      return toHashString(Joiner.on('+').join(url, width, height, keepAspectRatio));
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(url, width, height, keepAspectRatio);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Parameter)) {
        return false;
      }
      Parameter rh = (Parameter) obj;
      return Objects.equal(url, rh.url) && width == rh.width && height == rh.height
          && keepAspectRatio == rh.keepAspectRatio;
    }
  }

  private static Logger logger = Logger.getLogger(ImageManager.class.toString());
  private static final String CACHE_ROOT_PATH = "/tmp/qmaclone/image";
  private static final String CACHE_INPUT_PATH = CACHE_ROOT_PATH + "/input";
  private static final String CACHE_OUTPUT_PATH = CACHE_ROOT_PATH + "/output";
  @VisibleForTesting
  static final int STATUS_CODE_MALFORMED_URL_EXCEPTION = -1;
  @VisibleForTesting
  static final int STATUS_CODE_DOWNLOAD_FAILURE = -2;

  private final Database database;
  private final Downloader downloader;
  private final ImageLinkProcessor.Factory imageLinkProcessorFactory;

  private volatile List<PacketImageLink> errorImageLinks = Lists.newArrayList();
  private final Runnable commandUpdate = new Runnable() {
    @Override
    public void run() {
      try {
        update();
      } catch (Exception e) {
        logger.log(Level.WARNING, "ImageLinkの更新に失敗しました", e);
      }
    }
  };
  private final LoadingCache<Parameter, byte[]> cache = CacheBuilder.newBuilder().softValues()
      .build(new CacheLoader<Parameter, byte[]>() {
        @Override
        public byte[] load(Parameter key) throws Exception {
          return getImage(key);
        }
      });

  @Inject
  public ImageManager(Database database, ThreadPool threadPool, Downloader downloader,
      ImageLinkProcessor.Factory imageLinkProcessorFactory) {
    this.database = Preconditions.checkNotNull(database);
    this.downloader = Preconditions.checkNotNull(downloader);
    this.imageLinkProcessorFactory = Preconditions.checkNotNull(imageLinkProcessorFactory);

    new File(CACHE_ROOT_PATH).mkdirs();
    new File(CACHE_INPUT_PATH).mkdirs();
    new File(CACHE_OUTPUT_PATH).mkdirs();
    ImageIO.setCacheDirectory(new File(CACHE_ROOT_PATH));
    ImageIO.setUseCache(true);

    threadPool.addDailyTask(commandUpdate);
  }

  /**
   * ダウンロードした画像ファイルのキャッシュ格納先ファイルを返す
   * 
   * @param url
   * @return
   */
  @VisibleForTesting
  File getInputCacheFile(String url) {
    String hash = toHashString(url);
    return new File(CACHE_INPUT_PATH + "/" + hash.substring(0, 2) + "/" + hash.substring(2));
  }

  /**
   * リサイズ後の画像ファイルのキャッシュ格納先ファイルを返す
   * 
   * @param parameter
   * @return
   */
  @VisibleForTesting
  File getOutputCacheFile(Parameter parameter) {
    String hash = parameter.getHashString();
    return new File(CACHE_OUTPUT_PATH + "/" + hash.substring(0, 2) + "/" + hash.substring(2));
  }

  /**
   * 画像をリサイズする
   * 
   * @param inputFile
   *          リサイズ元画像ファイル
   * @param canvasWidth
   *          リサイズ後の画像の幅
   * @param canvasHeight
   *          　リサイズ後の画像の高さ
   * @param outputFile
   *          リサイズ後画像ファイル
   * @throws IOException
   */
  @VisibleForTesting
  void resizeImage(File inputFile, int canvasWidth, int canvasHeight, boolean keepAspectRatio,
      File outputFile) throws IOException {
    BufferedImage inputImage = ImageIO.read(inputFile);

    if (inputImage == null) {
      throw new IOException("ダウンロードした画像ファイル形式が判別できませんでした inputFile:" + inputFile);
    }

    int imageWidth = canvasWidth;
    int imageHeight = canvasHeight;
    int offsetX = 0;
    int offsetY = 0;
    if (keepAspectRatio) {
      if (inputImage.getWidth() * 3 <= inputImage.getHeight() * 4) {
        // 縦長
        imageWidth = imageHeight * inputImage.getHeight() / inputImage.getWidth();
        offsetX = (canvasWidth - imageWidth) / 2;
      } else {
        // 横長
        imageHeight = imageWidth * inputImage.getWidth() / inputImage.getHeight();
        offsetY = (canvasHeight - imageHeight) / 2;
      }
    }

    ImageFilter imageFilter = new AreaAveragingScaleFilter(imageWidth, imageHeight);
    Image middleImage = new Canvas().createImage(new FilteredImageSource(inputImage.getSource(),
        imageFilter));
    BufferedImage outputImage = new BufferedImage(canvasWidth, canvasHeight,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = outputImage.createGraphics();

    // 透明色が黒く表示されるバグへの対処
    graphics.setColor(Color.WHITE);
    graphics.fill(new Rectangle(canvasWidth, canvasHeight));

    graphics.drawImage(middleImage, offsetX, offsetY, imageWidth, imageHeight, null);
    ImageIO.write(outputImage, "jpeg", outputFile);

    logger.log(Level.INFO, String.format("%d bytes -> %d bytes (%s->%s)", inputFile.length(),
        outputFile.length(), inputFile.getPath(), outputFile.getPath()));
  }

  /**
   * 文字列のハッシュを返す
   * 
   * @param data
   * @return
   */
  @VisibleForTesting
  static String toHashString(String data) {
    return DigestUtils.shaHex(data);
  }

  public long getLastModified(Parameter parameter) {
    File outputCacheFile = getOutputCacheFile(parameter);
    return outputCacheFile.lastModified();
  }

  public void writeToStream(Parameter parameter, OutputStream outputStream) throws IOException {
    try {
      outputStream.write(cache.get(parameter));
    } catch (ExecutionException e) {
      throw new IOException(e);
    }
  }

  @VisibleForTesting
  byte[] getImage(Parameter parameter) throws IOException {
    String url = parameter.url;
    int width = parameter.width;
    int height = parameter.height;
    boolean keepAspectRatio = parameter.keepAspectRatio;
    File inputCacheFile = getInputCacheFile(url);
    Files.createParentDirs(inputCacheFile);
    File outputCacheFile = getOutputCacheFile(parameter);
    Files.createParentDirs(outputCacheFile);

    // 画像がダウンロードされていなければダウンロードする
    // double-check
    // TODO(nodchip): 実行速度が早くシンプルな方法に変更する
    if (!inputCacheFile.isFile()) {
      // BugTrack-QMAClone/434 - QMAClone wiki
      // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F434#1330156832
      File tempFile = File.createTempFile("ImageManager-download", null);
      int statusCode;
      try {
        statusCode = downloader.downloadToFile(new URL(url), tempFile);
      } catch (DownloaderException e) {
        throw new IOException("ダウンロードに失敗しました: url=" + url, e);
      }

      if (statusCode / 100 != 2) {
        String message = String.format("ダウンロードに失敗しました: url=%s statusCode=%s", url, statusCode);
        throw new IOException(message);
      }
      tempFile.renameTo(inputCacheFile);
    }

    // 画像がリサイズされていなければリサイズする
    if (!outputCacheFile.isFile()) {
      // BugTrack-QMAClone/434 - QMAClone wiki
      // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F434#1330156832
      File tempFile = File.createTempFile("ImageManager-resize", null);
      resizeImage(inputCacheFile, width, height, keepAspectRatio, tempFile);
      tempFile.renameTo(outputCacheFile);
    }

    return Files.toByteArray(outputCacheFile);
  }

  private boolean isImage(File file) {
    try {
      return ImageIO.read(file) != null;

    } catch (IOException e) {
      logger.log(Level.INFO, "画像ファイル読み込み中に入出力エラーが発生しました", e);
      return false;

    } catch (CMMException e) {
      logger.log(Level.INFO, "画像ファイルの読み込みに失敗しました", e);
      return false;
    }
  }

  @VisibleForTesting
  static class ImageLinkProcessor implements ProblemProcessable {
    interface Factory {
      ImageLinkProcessor create(List<PacketImageLink> imageLinks,
          Map<String, Integer> urlToStatusCode);
    }

    private final ImageManager imageManager;
    private final Downloader downloader;
    private final List<PacketImageLink> imageLinks;
    private final Map<String, Integer> urlToStatusCode;

    @Inject
    public ImageLinkProcessor(ImageManager imageManager, Downloader downloader,
        @Assisted List<PacketImageLink> imageLinks, @Assisted Map<String, Integer> urlToStatusCode) {
      this.imageManager = Preconditions.checkNotNull(imageManager);
      this.downloader = Preconditions.checkNotNull(downloader);
      this.imageLinks = Preconditions.checkNotNull(imageLinks);
      this.urlToStatusCode = Preconditions.checkNotNull(urlToStatusCode);
    }

    @Override
    public void process(PacketProblem problem) {
      for (String url : problem.getImageUrls()) {
        File inputCacheFile = imageManager.getInputCacheFile(url);

        if (!urlToStatusCode.containsKey(url)) {
          try {
            Files.createParentDirs(inputCacheFile);
          } catch (IOException e) {
            logger.log(Level.WARNING, "入力画像キャッシュディレクトリの作成に失敗しました。処理を続行します: inputCacheFile="
                + inputCacheFile, e);
            continue;
          }

          try {
            int statusCode = downloader.downloadToFile(new URL(url), inputCacheFile);
            urlToStatusCode.put(url, statusCode);

          } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "不正なURLです: url=" + url, e);
            // URLが不正な場合はダミーステータスコードを表示する
            urlToStatusCode.put(url, STATUS_CODE_MALFORMED_URL_EXCEPTION);

          } catch (DownloaderException e) {
            logger.log(Level.WARNING, "ダウンロードに失敗しました: url=" + url, e);
            // ダウンロードに失敗した場合はダミーステータスコードを表示する
            urlToStatusCode.put(url, STATUS_CODE_DOWNLOAD_FAILURE);
          }
        }

        int statusCode = urlToStatusCode.get(url);

        // 正常取得かつ正常画像の場合はエラー出力をしない
        if (statusCode / 100 == 2 && imageManager.isImage(inputCacheFile)) {
          continue;
        }

        PacketImageLink imageLink = new PacketImageLink();
        imageLink.problemId = problem.id;
        imageLink.url = url;
        imageLink.statusCode = statusCode;
        imageLinks.add(imageLink);
      }
    }
  }

  @VisibleForTesting
  synchronized void update() throws DatabaseException {
    List<PacketImageLink> imageLinks = Lists.newArrayList();
    Map<String, Integer> urlToStatusCode = Maps.newHashMap();
    ImageLinkProcessor imageLinkProcessor = imageLinkProcessorFactory.create(imageLinks,
        urlToStatusCode);
    database.processProblems(imageLinkProcessor);

    Collections.sort(imageLinks);

    this.errorImageLinks = imageLinks;
  }

  public List<PacketImageLink> getErrorImageLinks() {
    return errorImageLinks;
  }

  public static void main(String[] args) throws DatabaseException {
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        logger.log(Level.SEVERE, "Handled uncaught exception: " + t, e);
        System.exit(-1);
      }
    });
    ImageManager imageManager = Injectors.get().getInstance(ImageManager.class);
    imageManager.update();
    System.exit(0);
  }
}
