package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * {@link TwitterClient} のテスト。
 */
public class TwitterClientTest {

  /**
   * sdk.properties が存在しない場合は最小構成で作成されることを確認する。
   */
  @Test
  public void ensureTwitterSdkPropertiesFileShouldCreateFileWhenMissing() throws Exception {
    Path tempDir = Files.createTempDirectory("twitter-client-test");
    Path propertiesPath = tempDir.resolve("sdk.properties");
    Files.deleteIfExists(propertiesPath);

    TwitterClient.ensureTwitterSdkPropertiesFile(tempDir);

    assertTrue(Files.exists(propertiesPath));
    String content = new String(Files.readAllBytes(propertiesPath), StandardCharsets.UTF_8);
    assertTrue(content.contains("sdk.exclude.fields="));
  }
}
