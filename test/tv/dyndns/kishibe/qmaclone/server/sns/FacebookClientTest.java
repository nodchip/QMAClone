package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class FacebookClientTest {

	@Rule
	public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
	@Inject
	private FacebookClient client;

	@Test
	public void getPageAccessTokenShouldReturnAccessToken() {
		assertFalse(client.getPageAccessToken().isEmpty());
	}

}
