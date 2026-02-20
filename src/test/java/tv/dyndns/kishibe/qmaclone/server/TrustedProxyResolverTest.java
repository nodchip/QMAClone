package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * TrustedProxyResolver の挙動を確認するテスト。
 */
public class TrustedProxyResolverTest {

  /**
   * 非信頼プロキシからのアクセスではX-Forwarded-Forを無視する。
   */
  @Test
  public void resolveClientIpShouldIgnoreForwardedForWhenProxyIsNotTrusted() {
    TrustedProxyResolver resolver = TrustedProxyResolver.fromCsv("127.0.0.1");

    String resolved = resolver.resolveClientIp("10.0.0.20", "203.0.113.10");

    assertEquals("10.0.0.20", resolved);
  }

  /**
   * 信頼プロキシからのアクセスでは先頭のX-Forwarded-Forを採用する。
   */
  @Test
  public void resolveClientIpShouldUseFirstForwardedForWhenProxyIsTrusted() {
    TrustedProxyResolver resolver = TrustedProxyResolver.fromCsv("10.0.0.0/8");

    String resolved = resolver.resolveClientIp("10.10.10.10", "203.0.113.10, 198.51.100.3");

    assertEquals("203.0.113.10", resolved);
  }

  /**
   * 先頭要素が不正な場合はremoteAddrにフォールバックする。
   */
  @Test
  public void resolveClientIpShouldFallbackToRemoteAddrWhenForwardedForIsInvalid() {
    TrustedProxyResolver resolver = TrustedProxyResolver.fromCsv("10.0.0.0/8");

    String resolved = resolver.resolveClientIp("10.10.10.10", "invalid-ip, 203.0.113.10");

    assertEquals("10.10.10.10", resolved);
  }

  /**
   * allowlistが空の場合は常にremoteAddrを返す。
   */
  @Test
  public void resolveClientIpShouldReturnRemoteAddrWhenAllowlistIsEmpty() {
    TrustedProxyResolver resolver = TrustedProxyResolver.fromCsv("");

    String resolved = resolver.resolveClientIp("10.10.10.10", "203.0.113.10");

    assertEquals("10.10.10.10", resolved);
  }
}
