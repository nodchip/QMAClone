package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;
import tv.dyndns.kishibe.qmaclone.server.util.DownloaderException;

public class NicoVideoDicImeDictionary implements Dictionary {

  private static final Logger logger = Logger.getLogger(NicoVideoDicImeDictionary.class.getName());
  private static final String NICO_VIDEO_DIC_IME_URL = "http://public.s3.tkido.com.s3-website-ap-northeast-1.amazonaws.com/nicoime.zip";
  private static final File NICO_VIDEO_DIC_IME_FILE = new File(Constant.FILE_PATH_BASE + "nicoime.zip");
  private final Downloader downloader;

  @Inject
  public NicoVideoDicImeDictionary(Downloader downloader) {
    this.downloader = Preconditions.checkNotNull(downloader);
  }

  @Override
  public List<String> getWords() {
    try {
      ensureFile();
      return readFile();
    } catch (IOException e) {
      logger.log(Level.WARNING, "ニコニコ大百科IME辞書の取得に失敗しました", e);
      return Lists.newArrayList();
    }
  }

  private void ensureFile() throws IOException {
    if (NICO_VIDEO_DIC_IME_FILE.isFile()
        && System.currentTimeMillis() < NICO_VIDEO_DIC_IME_FILE.lastModified() + 7L * 24 * 60 * 60 * 1000) {
      return;
    }

    try {
      downloader.downloadToFile(new URL(NICO_VIDEO_DIC_IME_URL), NICO_VIDEO_DIC_IME_FILE);
    } catch (DownloaderException e) {
      logger.log(Level.SEVERE, "ニコニコ大百科IME辞書のダウンロードに失敗しました");
      throw Throwables.propagate(e);
    }
  }

  private List<String> readFile() throws IOException {
    List<String> words = Lists.newArrayList();

    try (ZipFile zipFile = new ZipFile(NICO_VIDEO_DIC_IME_FILE);
        Scanner scanner = new Scanner(zipFile.getInputStream(zipFile.getEntry("nicoime_msime.txt")), "utf-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] split = line.split("\t");
        if (split.length < 2) {
          continue;
        }

        String word = split[1];

        if (line.contains("(")) {
          line = line.substring(0, line.indexOf("("));
        }

        line = line.replaceAll(" ", "").trim();
        if (line.isEmpty()) {
          continue;
        }

        words.add(word);

        if (words.size() % 10000 == 0) {
          logger.log(Level.INFO, "NicoVideoIme: " + words.size());
        }
      }
    }

    return words;
  }

}
