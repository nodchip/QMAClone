package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.annotation.WebServlet;

import org.junit.jupiter.api.Test;

public class RemoteLoggingServletTest {

  @Test
  public void webServletAnnotation_hasLegacyAndModuleRelativePaths() {
    WebServlet annotation = RemoteLoggingServlet.class.getAnnotation(WebServlet.class);
    assertNotNull(annotation);
    assertArrayEquals(
        new String[] { "/tv.dyndns.kishibe.qmaclone.QMAClone/remote_logging", "/remote_logging" },
        annotation.urlPatterns());
  }
}
