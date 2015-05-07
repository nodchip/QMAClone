package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class ViterbiTokenizerTest {

  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
  @Inject
  private WordSegmenter wordSegmenter;
  private ViterbiAnalyzer viterbiAnalyzer;
  @Mock
  private ViterbiTokenizer.Factory viterbiTokenizerFactory;

  @Before
  public void setUp() throws Exception {
    viterbiAnalyzer = new ViterbiAnalyzer(viterbiTokenizerFactory);
  }

  @Test
  public final void testVitabiTokenizer() throws Exception {
    String s = "「Google」で2020年完成予定の 人工知能で会話しつつ検索などを行うサービスを 「Google　○○○○○」という？ ＢＲＡＩＮ";
    StringReader reader = new StringReader(s);
    
    when(viterbiTokenizerFactory.create(any(Reader.class))).then(new Answer<ViterbiTokenizer>() {
      @Override
      public ViterbiTokenizer answer(InvocationOnMock invocation) throws Throwable {
        Reader reader = (Reader) invocation.getArguments()[0];
        return new ViterbiTokenizer(wordSegmenter, reader);
      }
    });
    
    try (Analyzer a = viterbiAnalyzer;
        TokenStream ts = a.tokenStream("default", reader)) {
      CharTermAttribute termAttribute = ts.getAttribute(CharTermAttribute.class);
      Set<String> words = new HashSet<String>();
      ts.reset();
      while (ts.incrementToken()) {
        words.add(termAttribute.toString());
      }
      ts.end();
      assertTrue(words.contains("google"));
      assertTrue(words.contains("人工知能"));
      assertTrue(words.contains("検索"));
      assertTrue(words.contains("会話"));
      assertTrue(words.contains("サービス"));
    }
  }

}
