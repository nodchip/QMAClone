package tv.dyndns.kishibe.qmaclone.server.handwriting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * zinnia テスト実行前提の判定を確認する。
 */
public class ZinniaTestEnvironmentTest {
  /**
   * DLL だけでは実行前提を満たさないことを確認する。
   */
  @Test
  public void hasRequiredFilesShouldReturnFalseWhenModelFileIsMissing() {
    assertFalse(ZinniaTestEnvironment.hasRequiredFiles(Path.of("C:/tmp/zinnia.dll"),
        Path.of("C:/tmp/missing/handwriting-ja.model")));
  }

  /**
   * 必要ファイルが揃っている場合は実行可能と判定する。
   */
  @Test
  public void hasRequiredFilesShouldReturnTrueWhenDllAndModelExist() {
    assertTrue(ZinniaTestEnvironment.hasRequiredFiles(
        Path.of(System.getProperty("java.io.tmpdir")),
        Path.of(System.getProperty("java.io.tmpdir"))));
  }
}
