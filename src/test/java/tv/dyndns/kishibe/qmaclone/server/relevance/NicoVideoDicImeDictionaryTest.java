package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tv.dyndns.kishibe.qmaclone.server.testing.GuiceInjectionExtension;

import com.google.inject.Inject;

@ExtendWith(GuiceInjectionExtension.class)
public class NicoVideoDicImeDictionaryTest {
	@Inject
	private NicoVideoDicImeDictionary nicoVideoDicImeDictionary;

	@Test
	public void getWords() {
		assertThat(nicoVideoDicImeDictionary.getWords().size(), greaterThan(140000));
	}

}

