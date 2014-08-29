package tv.dyndns.kishibe.qmaclone.server.relevance;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import tv.dyndns.kishibe.qmaclone.server.util.Normalizer;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class ViterbiTokenizer extends Tokenizer {

	public interface Factory {
		ViterbiTokenizer create(Reader input);
	}

	private final WordSegmenter wordSegmenter;
	private final String input;
	private final char[] buffer;
	private CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private List<Integer> offsets;
	private List<Integer> lengths;
	private int wordIndex = 0;

	@Inject
	public ViterbiTokenizer(WordSegmenter wordSegmenter, @Assisted Reader input) {
		super(input);
		this.wordSegmenter = Preconditions.checkNotNull(wordSegmenter);
		try {
			this.input = Normalizer.normalize(CharStreams.toString(input));
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		this.buffer = this.input.toCharArray();
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		clearAttributes();
		offsets = Lists.newArrayList();
		lengths = Lists.newArrayList();
		wordSegmenter.parse(input, null, offsets, lengths);
		wordIndex = 0;
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();

		if (wordIndex == offsets.size()) {
			return false;
		} else {
			int offset = offsets.get(wordIndex);
			int length = lengths.get(wordIndex);
			++wordIndex;
			termAtt.copyBuffer(buffer, offset, length);
			offsetAtt.setOffset(offset, offset + length);
			return true;
		}
	}

	@Override
	public void end() throws IOException {
		super.end();
		offsetAtt.setOffset(buffer.length, buffer.length);
	}

}
