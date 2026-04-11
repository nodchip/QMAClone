package tv.dyndns.kishibe.qmaclone.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class SecuritySourcePolicyTest {
  @Test
  public void externalLinksOpenedInNewTabShouldDeclareNoopenerAndNoreferrer() throws IOException {
    assertFileContains("src/main/java/tv/dyndns/kishibe/qmaclone/client/link/WidgetLinkData.java",
        "target='_blank' rel='noopener noreferrer'");
    assertFileContains(
        "src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsRatingHistory.java",
        "target='_blank' rel='noopener noreferrer'");
    assertFileContains(
        "src/main/java/tv/dyndns/kishibe/qmaclone/client/statistics/PanelStatisticsPrefectureRatingRanking.java",
        "target='_blank' rel='noopener noreferrer'");
  }

  @Test
  public void liveNginxSnapshotsShouldContainClickjackingDefenseHeaders() throws IOException {
    assertFileContains("ops/config/live/nginx/nginx.conf",
        "add_header X-Frame-Options \"DENY\" always;");
    assertFileContains("ops/config/live/nginx/nginx.conf",
        "add_header Content-Security-Policy \"frame-ancestors 'none'\" always;");
    assertFileContains("ops/config/live/nginx/sites-enabled/default",
        "add_header X-Frame-Options \"DENY\" always;");
    assertFileContains("ops/config/live/nginx/sites-enabled/default",
        "add_header Content-Security-Policy \"frame-ancestors 'none'\" always;");
  }

  private void assertFileContains(String relativePath, String expected) throws IOException {
    String content = Files.readString(Path.of(relativePath), StandardCharsets.UTF_8);
    assertTrue(content.contains(expected),
        () -> relativePath + " should contain: " + expected);
  }
}
