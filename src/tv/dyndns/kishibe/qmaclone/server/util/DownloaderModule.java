package tv.dyndns.kishibe.qmaclone.server.util;

import static com.google.inject.Scopes.SINGLETON;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DownloaderModule extends AbstractModule {

  private static final int MAX_NUMBER_OF_CONNECTIONS = 100;

  @Override
  protected void configure() {
    bind(Downloader.class).in(SINGLETON);
  }

  @Provides
  @Singleton
  private HttpClient providesHttpClient() throws NoSuchAlgorithmException, KeyStoreException,
      KeyManagementException {
    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(MAX_NUMBER_OF_CONNECTIONS);
    return HttpClients.custom().setConnectionManager(cm).build();
  }
}
