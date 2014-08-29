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
public class PatternMatchingAutomatonTest {

	@Mock
	private Dictionary mockDictionary;
	private PatternMatchingAutomaton patternMatchingAutomaton;

	@Before
	public void setUp() throws Exception {
		patternMatchingAutomaton = new PatternMatchingAutomaton(ImmutableSet.of(mockDictionary));
	}

	@Test
	public void segmentShouldExtractWords() {
		when(mockDictionary.getWords()).thenReturn(
				ImmutableList.of("ab", "bc", "bab", "d", "abcde"));

		assertEquals(ImmutableList.of("bab", "ab", "bc", "d", "abcde"),
				patternMatchingAutomaton.segment("xbabcdex"));

	}

}
