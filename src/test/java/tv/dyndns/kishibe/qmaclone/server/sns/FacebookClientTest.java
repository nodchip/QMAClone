package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tv.dyndns.kishibe.qmaclone.server.testing.GuiceInjectionExtension;

import com.google.inject.Inject;

@ExtendWith(GuiceInjectionExtension.class)
public class FacebookClientTest {
  @Inject
  private FacebookClient client;

  @Test
  public void getPageAccessTokenShouldReturnAccessToken() {
    String pageAccessToken = client.getPageAccessToken();
    System.out.println(pageAccessToken);
    assertFalse(pageAccessToken.isEmpty());
  }
}

