package tv.dyndns.kishibe.qmaclone.server.image;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.server.Injectors;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

public class BrokenImageLinkDetector implements Runnable {
  private static final Logger logger = Logger.getLogger(BrokenImageLinkDetector.class.toString());
  private final ImageLinkChecker.Factory imageLinkCheckerFactory;
  private final Database database;
  private volatile List<PacketImageLink> brokenImageLinks = Lists.newArrayList();
  public static boolean SKIP_HOST_CHECK_FOR_TESTING = false;

  @Inject
  public BrokenImageLinkDetector(ImageLinkChecker.Factory imageLinkCheckerFactory,
      Database database) {
    this.imageLinkCheckerFactory = Preconditions.checkNotNull(imageLinkCheckerFactory);
    this.database = Preconditions.checkNotNull(database);
  }

  @Override
  public void run() {
    String hostName = null;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      logger.log(Level.WARNING, "ホスト名の取得に失敗しました");
    }

    if (!SKIP_HOST_CHECK_FOR_TESTING && "nighthawk".equals(hostName)) {
      logger.log(Level.INFO, "デバッグ実行のためリンク切れ画像の検出を行いませんでした");
      return;
    }

    try {
      runInternal();
    } catch (Exception e) {
      logger.log(Level.WARNING, "画像リンク切れ検出中に予期せぬエラーが発生しました", e);
    }
  }

  private void runInternal() {
    logger.info("リンク切れ画像の検出を開始しました");

    ImageLinkChecker imageLinkChecker = imageLinkCheckerFactory.create();
    try {
      database.processProblems(imageLinkChecker);
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "リンク切れ画像の検出に失敗しました", e);
      return;
    }

    logger.info("データベースの精査が終わりました");

    List<PacketImageLink> imageLinks = imageLinkChecker.getImageLinks();

    logger.info("リンク切れ画像リストを取得しました");

    Collections.sort(imageLinks);

    logger.info("リンク切れ画像リストをソートしました");

    this.brokenImageLinks = imageLinks;

    logger.info("リンク切れ画像の検出を終了しました: |imageLinks|=" + imageLinks.size());
  }

  public List<PacketImageLink> getBrokenImageLinks() {
    return brokenImageLinks;
  }

  public static void main(String[] args) throws DatabaseException {
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        logger.log(Level.SEVERE, "Handled uncaught exception: " + t, e);
        System.exit(-1);
      }
    });

    BrokenImageLinkDetector brokenImageLinkDetector = Injectors.get()
        .getInstance(BrokenImageLinkDetector.class);
    brokenImageLinkDetector.run();
    System.exit(0);
  }

}
