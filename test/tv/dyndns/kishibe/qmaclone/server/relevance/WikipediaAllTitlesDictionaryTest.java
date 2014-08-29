package tv.dyndns.kishibe.qmaclone.server.relevance;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class WikipediaAllTitlesDictionaryTest {

	@Rule
	public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
	@Inject
	private WikipediaAllTitlesDictionary wikipediaAllTitlesDictionary;

	@Test
	public void getWordsShouldReturnWords() {
		assertThat(wikipediaAllTitlesDictionary.getWords().size(), greaterThan(1000000));
	}

}
