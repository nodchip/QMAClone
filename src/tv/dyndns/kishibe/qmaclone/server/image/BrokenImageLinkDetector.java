package tv.dyndns.kishibe.qmaclone.server.image;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.server.Injectors;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class BrokenImageLinkDetector implements Runnable {

  private static final Logger logger = Logger.getLogger(BrokenImageLinkDetector.class.toString());
  private final ImageLinkChecker.Factory imageLinkCheckerFactory;
  private final Database database;
  private volatile List<PacketImageLink> brokenImageLinks = Lists.newArrayList();

  @Inject
  public BrokenImageLinkDetector(ImageLinkChecker.Factory imageLinkCheckerFactory, Database database) {
    this.imageLinkCheckerFactory = Preconditions.checkNotNull(imageLinkCheckerFactory);
    this.database = Preconditions.checkNotNull(database);
  }

  @Override
  public void run() {
    logger.info("リンク切れ画像の検出を開始しました");

    ImageLinkChecker imageLinkChecker = imageLinkCheckerFactory.create();
    try {
      database.processProblems(imageLinkChecker);
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "リンク切れ画像の検出に失敗しました", e);
    }

    List<PacketImageLink> imageLinks = imageLinkChecker.getImageLinks();
    Collections.sort(imageLinks);
    this.brokenImageLinks = imageLinks;

    for (PacketImageLink imageLink : imageLinks) {
      logger.info("リンク切れ画像を検出しました: " + imageLink);
    }
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

    BrokenImageLinkDetector brokenImageLinkDetector = Injectors.get().getInstance(
        BrokenImageLinkDetector.class);
    brokenImageLinkDetector.run();
    System.exit(0);
  }

}
