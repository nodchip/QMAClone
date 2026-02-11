package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WordSegmenterTest {

	private static final String FAKE_STRING = "fake string";
	@Mock
	private TrieCache mockTrieCache;
	@Mock
	private Trie mockTrie;
	private WordSegmenter wordSegmenter;

	@BeforeEach
	public void setUp() throws Exception {
		wordSegmenter = new WordSegmenter(mockTrieCache);
	}

	@Test
	public void parseShouldDelegateToTrie() {
		when(mockTrieCache.get()).thenReturn(mockTrie);

		wordSegmenter.parse(FAKE_STRING, null, null, null);

		verify(mockTrie).parse(FAKE_STRING, null, null, null);
	}

}
