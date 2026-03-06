package tv.dyndns.kishibe.qmaclone.server.handwriting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * zinnia 依存テストの実行前提をまとめる。
 */
final class ZinniaTestEnvironment {
  static final Path ZINNIA_DLL_PATH = Paths.get("C:/home/nodchip/zinnia/zinnia/zinnia.dll");
  static final Path ZINNIA_MODEL_PATH = Paths.get("C:/home/tomcat/qmaclone/handwriting-ja.model");

  private ZinniaTestEnvironment() {
  }

  /**
   * zinnia 実行に必要なファイルが揃っているかを返す。
   */
  static boolean hasRequiredFiles(Path dllPath, Path modelPath) {
    return Files.exists(dllPath) && Files.exists(modelPath);
  }

  /**
   * 現在の環境で zinnia テストを実行できるかを返す。
   */
  static boolean isReady() {
    return hasRequiredFiles(ZINNIA_DLL_PATH, ZINNIA_MODEL_PATH);
  }

  /**
   * スキップ理由を返す。
   */
  static String getSkipReason() {
    if (!Files.exists(ZINNIA_DLL_PATH)) {
      return "zinnia.dll が見つからないためスキップ";
    }
    if (!Files.exists(ZINNIA_MODEL_PATH)) {
      return "handwriting-ja.model が見つからないためスキップ";
    }
    return "";
  }
}
