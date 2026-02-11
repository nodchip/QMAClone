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
public class PatternMatchingAutomatonTest {

	@Mock
	private Dictionary mockDictionary;
	private PatternMatchingAutomaton patternMatchingAutomaton;

	@BeforeEach
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
