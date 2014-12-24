package tv.dyndns.kishibe.qmaclone.server.relevance;

import org.apache.lucene.util.Version;

public class LuceneVersion {
  public static Version get() {
    return Version.LUCENE_4_10_3;
  }
}
