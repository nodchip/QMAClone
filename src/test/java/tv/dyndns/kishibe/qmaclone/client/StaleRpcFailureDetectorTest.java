package tv.dyndns.kishibe.qmaclone.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@link StaleRpcFailureDetector} の判定テスト。
 */
public class StaleRpcFailureDetectorTest {

  /**
   * 代表的な旧ポリシーエラー文言を検知できることを確認する。
   */
  @Test
  public void testDetectBySerializationPolicyMessage() {
    RuntimeException throwable =
        new RuntimeException("Type 'InvocationException' was not assignable to 'java.lang.Throwable'");
    RuntimeException wrapped = new RuntimeException(
        "The serialization policy file 'xxxxx.gwt.rpc' was not found", throwable);
    assertTrue(StaleRpcFailureDetector.isStaleRpcFailure(wrapped));
  }

  /**
   * IncompatibleRemoteServiceException 名称を検知できることを確認する。
   */
  @Test
  public void testDetectByExceptionClassNameString() {
    RuntimeException throwable =
        new RuntimeException("com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException");
    assertTrue(StaleRpcFailureDetector.isStaleRpcFailure(throwable));
  }

  /**
   * 無関係の例外は検知しないことを確認する。
   */
  @Test
  public void testIgnoreRegularError() {
    RuntimeException throwable = new RuntimeException("network timeout");
    assertFalse(StaleRpcFailureDetector.isStaleRpcFailure(throwable));
  }

  /**
   * リロードURL生成時にフラグメントを保持することを確認する。
   */
  @Test
  public void testBuildReloadUrlWithFragment() {
    String url = ClientReloadPrompter.buildReloadUrl("https://example.com/app?p=1#hash", 123L);
    assertTrue(url.startsWith("https://example.com/app?p=1&gwt_refresh=123"));
    assertTrue(url.endsWith("#hash"));
  }
}
