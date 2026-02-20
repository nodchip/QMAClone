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
  private static final int MAX_REDIRECTS = 5;
  private final HttpTransport httpTransport;

  /**
   * ダウンロード対象URLの検証コールバック。
   */
  @FunctionalInterface
  public interface UrlAccessValidator {
    void validate(URL url) throws DownloaderException;
  }

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
    downloadToFile(url, file, null);
  }

  /**
   * URL検証を挟みながら画像ファイルをダウンロードする。
   * 
   * @param url          ダウンロード元url
   * @param file         出力先ファイル
   * @param urlValidator URL検証コールバック
   * @throws DownloaderException ダウンロード失敗時
   */
  public void downloadToFile(URL url, File file, UrlAccessValidator urlValidator) throws DownloaderException {
    logger.log(Level.INFO, String.format("Downloading: %s to %s", url.toString(), file.toString()));

    file.getParentFile().mkdirs();

    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    try {
      URL currentUrl = url;
      for (int redirectCount = 0; redirectCount <= MAX_REDIRECTS; redirectCount++) {
        if (urlValidator != null) {
          urlValidator.validate(currentUrl);
        }
        HttpRequest getRequest = requestFactory.buildGetRequest(new GenericUrl(currentUrl.toString()));
        getRequest.setFollowRedirects(false);

        HttpResponse getResponse;
        try {
          getResponse = getRequest.execute();
        } catch (IllegalArgumentException e) {
          throw new DownloaderException(e);
        }

        int statusCode = getResponse.getStatusCode();
        if (statusCode >= 300 && statusCode < 400) {
          String location = getResponse.getHeaders().getLocation();
          if (location == null || location.trim().isEmpty()) {
            throw new DownloaderException("リダイレクト先が不正です: url=" + currentUrl);
          }
          currentUrl = new URL(currentUrl, location);
          continue;
        }

        Files.asByteSink(file).writeFrom(getResponse.getContent());
        return;
      }
      throw new DownloaderException("リダイレクト回数が上限を超えました: url=" + url);
    } catch (HttpResponseException e) {
      String message = String.format("\"ファイルのダウンロードに失敗しました: url=%s e.getStatusCode()=%d e.getStatusMessage()=%s", url,
          e.getStatusCode(), e.getStatusMessage());
      throw new DownloaderException(message);
    } catch (IOException e) {
      throw new DownloaderException("ファイルのダウンロードに失敗しました: url=" + url, e);
    }
  }
}
