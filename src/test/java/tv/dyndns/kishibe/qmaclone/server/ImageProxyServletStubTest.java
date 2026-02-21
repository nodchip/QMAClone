package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.image.ImageUtils;
import tv.dyndns.kishibe.qmaclone.server.image.ImageUtils.Parameter;

@ExtendWith(MockitoExtension.class)
public class ImageProxyServletStubTest {

  private ImageProxyServletStub service;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private ImageUtils mockImageManager;

  @BeforeEach
  public void setUp() {
    try {
      FileUtils.deleteDirectory(new File(Constant.FILE_PATH_BASE + "image"));
    } catch (IOException e) {
    }
    service = new ImageProxyServletStub(mockImageManager);
  }

  @Test
  public void parseParameterShouldWork() throws ServletException {
    when(mockRequest.getRequestURI()).thenReturn("/image/url/006100620063/width/512/height/384");

    Parameter parameter = service.parseParameter(mockRequest);

    assertThat(parameter.url).isEqualTo("abc");
    assertThat(parameter.width).isEqualTo(512);
    assertThat(parameter.height).isEqualTo(384);
  }

  @Test
  public void parseParameterShouldWorkWithRoot() throws ServletException {
    when(mockRequest.getRequestURI()).thenReturn(
        "/QMAClone/image/url/006100620063/width/512/height/384");

    Parameter parameter = service.parseParameter(mockRequest);

    assertThat(parameter.url).isEqualTo("abc");
    assertThat(parameter.width).isEqualTo(512);
    assertThat(parameter.height).isEqualTo(384);
  }

  /**
   * 画像取得に失敗したとき、サーブレットがSEVERE化する例外を投げず404を返すことを確認する。
   */
  @Test
  public void doGetShouldReturnNotFoundWhenImageFetchFails() throws Exception {
    when(mockRequest.getRequestURI()).thenReturn("/image/url/006100620063/width/512/height/384");
    when(mockRequest.getInputStream()).thenReturn(createEmptyServletInputStream());
    when(mockResponse.getOutputStream()).thenReturn(createDiscardServletOutputStream());
    doThrow(new IOException("failed")).when(mockImageManager).writeToStream(any(Parameter.class),
        any(OutputStream.class));

    service.doGet(mockRequest, mockResponse);

    verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(mockResponse).setContentType("text/plain; charset=UTF-8");
  }

  /**
   * パラメーター不正時に400が返ることを確認する。
   */
  @Test
  public void doGetShouldReturnBadRequestWhenParameterIsInvalid() throws Exception {
    when(mockRequest.getRequestURI()).thenReturn("/image/url/006100620063/width/1024/height/384");
    when(mockRequest.getInputStream()).thenReturn(createEmptyServletInputStream());
    when(mockResponse.getOutputStream()).thenReturn(createDiscardServletOutputStream());

    service.doGet(mockRequest, mockResponse);

    verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(mockResponse).setContentType("text/plain; charset=UTF-8");
  }

  @Test
  public void testGetRFC1123NextYear() throws DateParseException {
    Date date = DateUtils.parseDate(service.getRFC1123NextYear());
    assertThat(date.getTime()).isLessThan(System.currentTimeMillis() + 366L * 24 * 60 * 60 * 1000);
  }

  /**
   * 空入力用のServletInputStreamを生成する。
   */
  private ServletInputStream createEmptyServletInputStream() {
    return new ServletInputStream() {
      private final InputStream delegate = new ByteArrayInputStream(new byte[0]);

      @Override
      public int read() throws IOException {
        return delegate.read();
      }

      @Override
      public boolean isFinished() {
        return true;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener readListener) {
      }
    };
  }

  /**
   * 出力を破棄するServletOutputStreamを生成する。
   */
  private ServletOutputStream createDiscardServletOutputStream() {
    return new ServletOutputStream() {
      @Override
      public void write(int b) {
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
      }
    };
  }
}
