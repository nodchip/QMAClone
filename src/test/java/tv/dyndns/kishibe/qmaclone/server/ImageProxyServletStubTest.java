package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

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

  @Mock
  private ImageProxyServletStub service;
  @Mock
  private HttpServletRequest mockRequest;
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

  @Test
  public void testGetRFC1123NextYear() throws DateParseException {
    Date date = DateUtils.parseDate(service.getRFC1123NextYear());
    assertThat(date.getTime()).isLessThan(System.currentTimeMillis() + 366L * 24 * 60 * 60 * 1000);
  }
}
