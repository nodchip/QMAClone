package tv.dyndns.kishibe.qmaclone.server.image;

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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.ThreadPool;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderException;

public class ImageUtils {

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

  private static Logger logger = Logger.getLogger(ImageUtils.class.toString());
  private static final String CACHE_ROOT_PATH = Constant.FILE_PATH_BASE + "image/";
  private static final String CACHE_INPUT_PATH = CACHE_ROOT_PATH + "input/";
  private static final String CACHE_OUTPUT_PATH = CACHE_ROOT_PATH + "output/";

  private final Downloader downloader;

  private final LoadingCache<Parameter, byte[]> cache = CacheBuilder.newBuilder().softValues()
      .build(new CacheLoader<Parameter, byte[]>() {
        @Override
        public byte[] load(Parameter key) {
          try {
            return getImage(key);
          } catch (Exception e) {
            logger.log(Level.WARNING, "画像の取得に失敗しました。 key=" + key, e);
            return null;
          }
        }
      });

  @Inject
  public ImageUtils(ThreadPool threadPool, Downloader downloader) {
    this.downloader = Preconditions.checkNotNull(downloader);

    EnsureDirectories();
    ImageIO.setCacheDirectory(new File(CACHE_ROOT_PATH));
    ImageIO.setUseCache(true);
  }

  private void EnsureDirectories() {
    new File(CACHE_ROOT_PATH).mkdirs();
    new File(CACHE_INPUT_PATH).mkdirs();
    new File(CACHE_OUTPUT_PATH).mkdirs();
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
   * @param inputFile    リサイズ元画像ファイル
   * @param canvasWidth  リサイズ後の画像の幅
   * @param canvasHeight リサイズ後の画像の高さ
   * @param outputFile   リサイズ後画像ファイル
   * @throws IOException
   */
  @VisibleForTesting
  void resizeImage(File inputFile, int canvasWidth, int canvasHeight, boolean keepAspectRatio,
      OutputStream outputStream) throws IOException {
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
    Image middleImage = new Canvas().createImage(new FilteredImageSource(inputImage.getSource(), imageFilter));
    BufferedImage outputImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = outputImage.createGraphics();

    // 透明色が黒く表示されるバグへの対処
    graphics.setColor(Color.WHITE);
    graphics.fill(new Rectangle(canvasWidth, canvasHeight));

    graphics.drawImage(middleImage, offsetX, offsetY, imageWidth, imageHeight, null);
    try {
      ImageIO.write(outputImage, "jpeg", outputStream);
    } catch (IOException e) {
      logger.log(Level.WARNING, "画像の出力に失敗しました。", e);
      throw new IOException(e);
    }
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
    EnsureDirectories();
    File outputCacheFile = getOutputCacheFile(parameter);
    return outputCacheFile.lastModified();
  }

  public void writeToStream(Parameter parameter, OutputStream outputStream) throws IOException {
    EnsureDirectories();
    try {
      outputStream.write(cache.get(parameter));
    } catch (ExecutionException e) {
      throw new IOException(e);
    }
  }

  @VisibleForTesting
  byte[] getImage(Parameter parameter) throws IOException {
    URL url;
    try {
      url = new URL(parameter.url);
    } catch (MalformedURLException e) {
      throw new IOException("不正なURLです: url=" + parameter.url, e);
    }
    int width = parameter.width;
    int height = parameter.height;
    boolean keepAspectRatio = parameter.keepAspectRatio;
    File inputCacheFile = getInputCacheFile(parameter.url);
    Files.createParentDirs(inputCacheFile);
    File outputCacheFile = getOutputCacheFile(parameter);
    Files.createParentDirs(outputCacheFile);

    // 画像がダウンロードされていなければダウンロードする
    // double-check
    // TODO(nodchip): 実行速度が早くシンプルな方法に変更する
    if (!inputCacheFile.isFile()) {
      // BugTrack-QMAClone/434 - QMAClone wiki
      // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F434#1330156832
      File tempFile = File.createTempFile("ImageUtils-download", null);
      try {
        downloader.downloadToFile(url, tempFile);
      } catch (DownloaderException e) {
        throw new IOException("ダウンロードに失敗しました: url=" + url, e);
      }

      logger.info(String.format("画像ファイルのダウンロードに成功しました。 tempFile=%s", tempFile));

      java.nio.file.Files.copy(tempFile.toPath(), inputCacheFile.toPath());

      logger.info(String.format("画像ファイルをコピーしました。 tempFile=%s inputCacheFile=%s", tempFile, inputCacheFile));

      if (!inputCacheFile.isFile()) {
        throw new IOException(String.format("画像ファイルが見つかりませんでした。 inputCacheFile=%s", inputCacheFile));
      }
    }

    // 画像がリサイズされていなければリサイズする
    // BugTrack-QMAClone/434 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F434#1330156832
    // リサイズ後の画像をファイルとしてストレージに書き込もうとすると失敗する。
    // 回避策として、リサイズ後の画像を直接出力する。
    // アイコンの画像変更が出来ない · Issue #1079 · nodchip/QMAClone
    // https://github.com/nodchip/QMAClone/issues/1079
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    resizeImage(inputCacheFile, width, height, keepAspectRatio, outputStream);
    return outputStream.toByteArray();
  }

  public boolean isImage(File file) {
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
}
