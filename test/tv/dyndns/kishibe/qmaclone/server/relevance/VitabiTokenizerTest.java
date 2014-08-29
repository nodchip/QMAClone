package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class VitabiTokenizerTest {

	@Rule
	public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
	@Inject
	private ViterbiAnalyzer viterbiAnalyzer;

	@Test
	public final void testVitabiTokenizer() throws Exception {
		String s = "「Google」で2020年完成予定の 人工知能で会話しつつ検索などを行うサービスを 「Google　○○○○○」という？ ＢＲＡＩＮ";
		try (Analyzer a = viterbiAnalyzer;
				TokenStream ts = a.tokenStream("default", new StringReader(s))) {
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
