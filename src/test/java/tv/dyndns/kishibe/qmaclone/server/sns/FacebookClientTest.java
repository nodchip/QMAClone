package tv.dyndns.kishibe.qmaclone.server.sns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.util.Downloader;

/**
 * {@link FacebookClient} のテスト。
 */
public class FacebookClientTest {
  /**
   * ページアクセストークンがレスポンスに含まれる場合、正しく抽出できることを確認する。
   */
  @Test
  public void getPageAccessTokenShouldReturnAccessToken() throws Exception {
    Database mockDatabase = mock(Database.class);
    Downloader mockDownloader = mock(Downloader.class);
    when(mockDatabase.getPassword("facebook_access_token")).thenReturn("token");
    when(mockDownloader.downloadAsString(any(URL.class)))
        .thenReturn("{\"data\":[{\"access_token\":\"page-token\"}]}");

    FacebookClient client = new FacebookClient(mockDatabase, mockDownloader);

    assertEquals("page-token", client.getPageAccessToken());
  }

  /**
   * レスポンスにアクセストークンが含まれない場合は null を返すことを確認する。
   */
  @Test
  public void getPageAccessTokenShouldReturnNullWhenTokenDoesNotExist() throws Exception {
    Database mockDatabase = mock(Database.class);
    Downloader mockDownloader = mock(Downloader.class);
    when(mockDatabase.getPassword("facebook_access_token")).thenReturn("token");
    when(mockDownloader.downloadAsString(any(URL.class)))
        .thenReturn("{\"data\":[]}");

    FacebookClient client = new FacebookClient(mockDatabase, mockDownloader);

    assertNull(client.getPageAccessToken());
  }
}

