package tv.dyndns.kishibe.qmaclone.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;

public class DownloaderTest {
  @Test
  public void testDownloadToFile200() throws Exception {
    byte[] expected = "OK".getBytes(StandardCharsets.UTF_8);
    Downloader downloader = newDownloader(expected, null);

    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    downloader.downloadToFile(new URL("https://example.com/a.png"), file);
    assertTrue(file.isFile());
    assertEquals(expected.length, file.length());
  }

  @Test
  public void testDownloadToFile403() throws Exception {
    HttpResponseException e =
        new HttpResponseException.Builder(403, "Forbidden", new HttpHeaders()).build();
    Downloader downloader = newDownloader(null, e);

    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    assertThrows(DownloaderException.class,
        () -> downloader.downloadToFile(new URL("https://example.com/forbidden.png"), file));
  }

  @Test
  public void testDownloadToFile404() throws Exception {
    HttpResponseException e =
        new HttpResponseException.Builder(404, "Not Found", new HttpHeaders()).build();
    Downloader downloader = newDownloader(null, e);

    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    assertThrows(DownloaderException.class,
        () -> downloader.downloadToFile(new URL("https://example.com/notfound.png"), file));
  }

  @Test
  public void testDownloadHttps() throws Exception {
    Downloader downloader = newDownloader("HTTPS".getBytes(StandardCharsets.UTF_8), null);

    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    downloader.downloadToFile(new URL("https://example.com/image.jpg"), file);
    assertTrue(file.isFile());
  }

  private static Downloader newDownloader(byte[] body, HttpResponseException exception) throws Exception {
    HttpTransport transport = Mockito.mock(HttpTransport.class);
    HttpRequestFactory factory = Mockito.mock(HttpRequestFactory.class);
    HttpRequest request = Mockito.mock(HttpRequest.class);
    HttpResponse response = Mockito.mock(HttpResponse.class);
    when(transport.createRequestFactory()).thenReturn(factory);
    when(factory.buildGetRequest(any(GenericUrl.class))).thenReturn(request);
    if (exception != null) {
      when(request.execute()).thenThrow(exception);
    } else {
      when(request.execute()).thenReturn(response);
      when(response.getContent()).thenReturn(new ByteArrayInputStream(body));
    }
    return new Downloader(transport);
  }
}
