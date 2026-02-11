package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.Rule;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

public class FacebookClientTest {

  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
  @Inject
  private FacebookClient client;

  @Test
  public void getPageAccessTokenShouldReturnAccessToken() {
    String pageAccessToken = client.getPageAccessToken();
    System.out.println(pageAccessToken);
    assertFalse(pageAccessToken.isEmpty());
  }
}
