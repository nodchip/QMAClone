package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

public class NicoVideoDicImeDictionaryTest {

	@Rule
	public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
	@Inject
	private NicoVideoDicImeDictionary nicoVideoDicImeDictionary;

	@Test
	public void getWords() {
		assertThat(nicoVideoDicImeDictionary.getWords().size(), greaterThan(140000));
	}

}
