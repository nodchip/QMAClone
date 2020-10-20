//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.server.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.inject.Inject;

public class Downloader {
  private static Logger logger = Logger.getLogger(Downloader.class.getName());
  private final HttpTransport httpTransport;

  @Inject
  public Downloader(HttpTransport httpTransport) {
    this.httpTransport = Preconditions.checkNotNull(httpTransport);
  }

  public byte[] downloadAsByteArray(URL url) throws DownloaderException {
    logger.info(String.format("Downloading: %s", url.toString()));

    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    try {
      HttpRequest getRequest = requestFactory.buildGetRequest(new GenericUrl(url.toString()));
      HttpResponse getResponse = getRequest.execute();
      return ByteStreams.toByteArray(getResponse.getContent());
    } catch (HttpResponseException e) {
      String message = String.format("\"ファイルのダウンロードに失敗しました: url=%s e.getStatusCode()=%d e.getStatusMessage()=%s", url,
          e.getStatusCode(), e.getStatusMessage());
      throw new DownloaderException(message);
    } catch (IOException e) {
      throw new DownloaderException("ファイルのダウンロードに失敗しました: url=" + url, e);
    }
  }

  public String downloadAsString(URL url) throws DownloaderException {
    logger.info(String.format("Downloading: %s", url.toString()));

    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    try {
      HttpRequest getRequest = requestFactory.buildGetRequest(new GenericUrl(url.toString()));
      HttpResponse getResponse = getRequest.execute();
      return getResponse.parseAsString();
    } catch (HttpResponseException e) {
      String message = String.format("\"ファイルのダウンロードに失敗しました: url=%s e.getStatusCode()=%d e.getStatusMessage()=%s", url,
          e.getStatusCode(), e.getStatusMessage());
      throw new DownloaderException(message);
    } catch (IOException e) {
      throw new DownloaderException("ファイルのダウンロードに失敗しました: url=" + url, e);
    }
  }

  /**
   * 画像ファイルをダウンロードする
   * 
   * @param url  ダウンロード元url
   * @param file 出力先ファイル
   * @return HTTPステータスコード
   * @throws IOException
   */
  public void downloadToFile(URL url, File file) throws DownloaderException {
    logger.log(Level.INFO, String.format("Downloading: %s to %s", url.toString(), file.toString()));

    file.getParentFile().mkdirs();

    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    try {
      HttpRequest getRequest = requestFactory.buildGetRequest(new GenericUrl(url.toString()));

      HttpResponse getResponse;
      try {
        getResponse = getRequest.execute();
      } catch (IllegalArgumentException e) {
        throw new DownloaderException(e);
      }

      Files.asByteSink(file).writeFrom(getResponse.getContent());
    } catch (HttpResponseException e) {
      String message = String.format("\"ファイルのダウンロードに失敗しました: url=%s e.getStatusCode()=%d e.getStatusMessage()=%s", url,
          e.getStatusCode(), e.getStatusMessage());
      throw new DownloaderException(message);
    } catch (IOException e) {
      throw new DownloaderException("ファイルのダウンロードに失敗しました: url=" + url, e);
    }
  }
}
