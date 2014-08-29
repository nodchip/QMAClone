package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WordSegmenterTest {

	private static final String FAKE_STRING = "fake string";
	@Mock
	private TrieCache mockTrieCache;
	@Mock
	private Trie mockTrie;
	private WordSegmenter wordSegmenter;

	@Before
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
