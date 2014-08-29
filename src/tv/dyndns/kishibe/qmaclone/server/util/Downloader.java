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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

public class Downloader {
  private static Logger logger = Logger.getLogger(Downloader.class.getName());
  private final HttpClient httpClient;

  @Inject
  public Downloader(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public byte[] downloadAsByteArray(URL url) {
    logger.log(Level.INFO, String.format("Downloading: %s", url.toString()));

    HttpGet httpGet = new HttpGet(url.toString());
    try {
      HttpResponse response = httpClient.execute(httpGet, new BasicHttpContext());
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        return EntityUtils.toByteArray(entity);
      }
    } catch (Exception e) {
      httpGet.abort();
    }
    return null;
  }

  public String downloadAsString(URL url, String encoding) {
    logger.log(Level.INFO, String.format("Downloading: %s", url.toString()));

    HttpGet httpGet = new HttpGet(url.toString());
    try {
      HttpResponse response = httpClient.execute(httpGet, new BasicHttpContext());
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        return EntityUtils.toString(entity, encoding);
      }
    } catch (IOException e) {
      httpGet.abort();
    }
    return null;
  }

  /**
   * 画像ファイルをダウンロードする
   * 
   * @param url
   *          ダウンロード元url
   * @param file
   *          出力先ファイル
   * @return HTTPステータスコード
   * @throws IOException
   */
  public int downloadToFile(URL url, File file) throws DownloaderException {
    logger.log(Level.INFO, String.format("Downloading: %s to %s", url.toString(), file.toString()));

    file.getParentFile().mkdirs();

    HttpEntity httpEntity = null;
    InputStream is = null;
    OutputStream os = null;

    try {
      HttpResponse httpResponse = httpClient.execute(new HttpGet(url.toURI()));
      httpEntity = httpResponse.getEntity();
      // httpEntityに代入してからreturnしないとconsumeContent()が呼ばれない
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode / 100 != 2) {
        return statusCode;
      }

      is = httpEntity.getContent();
      os = new BufferedOutputStream(new FileOutputStream(file));
      ByteStreams.copy(is, os);
      return statusCode;

    } catch (URISyntaxException e) {
      throw new DownloaderException("URLのパースに失敗しました: url=" + url, e);

    } catch (IOException e) {
      throw new DownloaderException("ダウンロードに失敗しました: url=" + url, e);

    } finally {
      try {
        Closeables.close(is, true);
        Closeables.close(os, true);
        if (httpEntity != null) {
          EntityUtils.consume(httpEntity);
        }
      } catch (IOException e) {
        throw new DownloaderException("ストリームを閉じることができませんでした: url=" + url, e);
      }
    }
  }
}
