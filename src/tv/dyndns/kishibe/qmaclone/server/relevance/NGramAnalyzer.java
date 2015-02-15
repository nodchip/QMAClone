package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;

public class NGramAnalyzer extends Analyzer {

  public static final int MAX_NGRAM_WEIGHT = 5;
  private static final int MIN_NGRAM_WEIGHT = 1;

  @Override
  protected Reader initReader(String fieldName, Reader reader) {
    return ReaderUtil.wrapWithNormalizer(reader);
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
    return new TokenStreamComponents(new NGramTokenizer(reader, MIN_NGRAM_WEIGHT, MAX_NGRAM_WEIGHT));
  }
}
