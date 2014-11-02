package tv.dyndns.kishibe.qmaclone.server.util;

import static com.google.inject.Scopes.SINGLETON;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.inject.AbstractModule;

public class DownloaderModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Downloader.class).in(SINGLETON);
    bind(HttpTransport.class).to(NetHttpTransport.class).in(SINGLETON);
  }
}
