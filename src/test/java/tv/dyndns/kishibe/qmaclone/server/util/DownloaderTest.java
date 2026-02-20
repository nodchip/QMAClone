package tv.dyndns.kishibe.qmaclone.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

  @Test
  public void downloadToFileShouldValidateRedirectDestination() throws Exception {
    HttpTransport transport = Mockito.mock(HttpTransport.class);
    HttpRequestFactory factory = Mockito.mock(HttpRequestFactory.class);
    HttpRequest request = Mockito.mock(HttpRequest.class);
    HttpResponse redirectResponse = Mockito.mock(HttpResponse.class);
    HttpResponse finalResponse = Mockito.mock(HttpResponse.class);
    HttpHeaders redirectHeaders = new HttpHeaders();
    redirectHeaders.setLocation("http://127.0.0.1/private.png");

    when(transport.createRequestFactory()).thenReturn(factory);
    when(factory.buildGetRequest(any(GenericUrl.class))).thenReturn(request);
    when(request.execute()).thenReturn(redirectResponse, finalResponse);
    when(redirectResponse.getStatusCode()).thenReturn(302);
    when(redirectResponse.getHeaders()).thenReturn(redirectHeaders);
    when(finalResponse.getStatusCode()).thenReturn(200);
    when(finalResponse.getContent()).thenReturn(new ByteArrayInputStream("OK".getBytes(StandardCharsets.UTF_8)));

    Downloader downloader = new Downloader(transport);
    File file = File.createTempFile("QMAClone", null);
    file.deleteOnExit();
    List<String> visited = new ArrayList<>();

    assertThrows(DownloaderException.class, () -> downloader.downloadToFile(new URL("https://example.com/image.png"),
        file, candidate -> {
          visited.add(candidate.toString());
          if ("127.0.0.1".equals(candidate.getHost())) {
            throw new DownloaderException("blocked");
          }
        }));

    assertEquals(2, visited.size());
    assertEquals("https://example.com/image.png", visited.get(0));
    assertEquals("http://127.0.0.1/private.png", visited.get(1));
    verify(request, times(1)).execute();
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
