package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class DictionariesTest {

	private static final String FAKE_WORD_1 = "fake word 1";
	private static final String FAKE_WORD_2 = "fake word 2";
	@Mock
	private Dictionary mockDictionary1;
	@Mock
	private Dictionary mockDictionary2;
	private Dictionaries dictionaries;

	@Before
	public void setUp() throws Exception {
		dictionaries = new Dictionaries(ImmutableSet.of(mockDictionary1, mockDictionary2));
	}

	@Test
	public void getWordsShouldAggregateWords() {
		when(mockDictionary1.getWords()).thenReturn(ImmutableList.of(FAKE_WORD_1));
		when(mockDictionary2.getWords()).thenReturn(ImmutableList.of(FAKE_WORD_2));

		assertEquals(ImmutableSet.of(FAKE_WORD_1, FAKE_WORD_2), dictionaries.getWords());
	}

}
