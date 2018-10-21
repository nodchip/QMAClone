package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderException;
import tv.dyndns.kishibe.qmaclone.server.util.Normalizer;

public class WikipediaAllTitlesDictionary implements Dictionary {

  private static final Logger logger = Logger
      .getLogger(WikipediaAllTitlesDictionary.class.getName());
  private static final String WIKIPEDIA_ALL_TITLE_URL = "https://dumps.wikimedia.org/jawiki/latest/jawiki-latest-all-titles-in-ns0.gz";
  private static final File WIKIPEDIA_ALL_TITLE_FILE = new File(
      Constant.FILE_PATH_BASE + "qmaclone/jawiki-latest-all-titles-in-ns0.gz");
  private final Downloader downloader;

  @Inject
  public WikipediaAllTitlesDictionary(Downloader downloader) {
    this.downloader = Preconditions.checkNotNull(downloader);
  }

  @Override
  public List<String> getWords() {
    try {
      ensureFile();
      return readFile();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Wikipediaのタイトル一覧の取得に失敗しました", e);
      return Lists.newArrayList();
    }
  }

  private void ensureFile() throws IOException {
    if (WIKIPEDIA_ALL_TITLE_FILE.isFile() && System
        .currentTimeMillis() < WIKIPEDIA_ALL_TITLE_FILE.lastModified() + 7L * 24 * 60 * 60 * 1000) {
      return;
    }

    try {
      downloader.downloadToFile(new URL(WIKIPEDIA_ALL_TITLE_URL), WIKIPEDIA_ALL_TITLE_FILE);
    } catch (DownloaderException e) {
      logger.log(Level.SEVERE, "Wikipedia全記事タイトル一覧のダウンロードに失敗しました", e);
      throw Throwables.propagate(e);
    }
  }

  private List<String> readFile() throws IOException {
    List<String> words = Lists.newArrayList();

    try (
        Scanner scanner = new Scanner(
            new BufferedInputStream(new GZIPInputStream(
                new BufferedInputStream(new FileInputStream(WIKIPEDIA_ALL_TITLE_FILE)))),
        "utf-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
          continue;
        }

        if (line.contains("(")) {
          line = line.substring(0, line.indexOf("("));
        }

        line = line.replaceAll("_", "");

        words.add(Normalizer.normalize(line));

        if (words.size() % 100000 == 0) {
          logger.log(Level.INFO, "Wikipedia: " + words.size());
        }
      }
    }

    return words;
  }

}
