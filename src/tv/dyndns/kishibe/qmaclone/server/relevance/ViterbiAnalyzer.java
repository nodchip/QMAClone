package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ViterbiAnalyzer extends Analyzer {
	
	public interface Factory{
		ViterbiAnalyzer create();
	}

	private final ViterbiTokenizer.Factory viterbiTokenizerFactory;

	@Inject
	public ViterbiAnalyzer(ViterbiTokenizer.Factory viterbiTokenizerFactory) {
		this.viterbiTokenizerFactory = Preconditions.checkNotNull(viterbiTokenizerFactory);
	}

	@Override
	protected Reader initReader(String fieldName, Reader reader) {
		return ReaderUtil.wrapWithNormalizer(reader);
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		return new TokenStreamComponents(viterbiTokenizerFactory.create(reader));
	}

}
