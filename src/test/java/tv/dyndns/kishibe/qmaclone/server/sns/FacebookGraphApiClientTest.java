package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.server.util.Downloader;

/**
 * {@link FacebookGraphApiClient} のテスト。
 */
public class FacebookGraphApiClientTest {
  @Test
  public void createAppSecretProofShouldReturnHmacSha256Hex() {
    FacebookGraphApiClient client = new FacebookGraphApiClient(mock(Downloader.class));

    String proof = client.createAppSecretProof("token", "secret");

    assertEquals("e941110e3d2bfe82621f0e3e1434730d7305d106c5f68c87165d0b27a4611a4a", proof);
  }
}
