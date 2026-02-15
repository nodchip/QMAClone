package tv.dyndns.kishibe.qmaclone.client;

import java.util.Locale;

/**
 * 旧GWTキャッシュ由来のRPC失敗かどうかを判定するユーティリティ。
 */
public final class StaleRpcFailureDetector {

  private StaleRpcFailureDetector() {
  }

  /**
   * 例外ツリーを走査し、旧シリアライゼーションポリシー由来の失敗か判定する。
   *
   * @param throwable 判定対象例外
   * @return 旧クライアントキャッシュ由来の可能性が高い場合はtrue
   */
  public static boolean isStaleRpcFailure(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      if (containsStaleRpcSignature(current.getClass().getName())
          || containsStaleRpcSignature(current.getMessage())) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  private static boolean containsStaleRpcSignature(String value) {
    if (value == null) {
      return false;
    }
    String normalized = value.toLowerCase(Locale.ROOT);
    return normalized.contains("incompatibleremoteserviceexception")
        || normalized.contains("serialization policy")
        || normalized.contains(".gwt.rpc")
        || normalized.contains("remote service implementation")
        || normalized.contains("policy file");
  }
}
