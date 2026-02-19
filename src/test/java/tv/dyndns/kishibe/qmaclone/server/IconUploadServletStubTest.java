package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;

import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.server.database.Database;

@ExtendWith(MockitoExtension.class)
public class IconUploadServletStubTest {
  @Mock
  private Database mockDatabase;
  private IconUploadServletStub servlet;

  @BeforeEach
  public void setUp() {
    servlet = new IconUploadServletStub(mockDatabase);
  }

  /**
   * サイズ上限超過時は専用の応答コードを返すことを確認する。
   */
  @Test
  public void resolveUploadParseFailureResponseShouldReturnFileTooLarge() {
    FileUploadException exception =
        new SizeLimitExceededException("too large", Constant.ICON_UPLOAD_MAX_FILE_SIZE + 1L,
            Constant.ICON_UPLOAD_MAX_FILE_SIZE);

    String result = servlet.resolveUploadParseFailureResponse(exception);

    assertThat(result).isEqualTo(Constant.ICON_UPLOAD_RESPONSE_FILE_TOO_LARGE);
  }

  /**
   * それ以外の解析失敗は従来の応答コードを返すことを確認する。
   */
  @Test
  public void resolveUploadParseFailureResponseShouldReturnParseErrorForOtherExceptions() {
    FileUploadException exception = new FileUploadException("parse error");

    String result = servlet.resolveUploadParseFailureResponse(exception);

    assertThat(result).isEqualTo(Constant.ICON_UPLOAD_RESPONSE_FAILED_TO_PARSE_REQUEST);
  }
}
