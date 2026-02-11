package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DictionariesTest {

	private static final String FAKE_WORD_1 = "fake word 1";
	private static final String FAKE_WORD_2 = "fake word 2";
	@Mock
	private Dictionary mockDictionary1;
	@Mock
	private Dictionary mockDictionary2;
	private Dictionaries dictionaries;

	@BeforeEach
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
