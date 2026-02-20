package tv.dyndns.kishibe.qmaclone.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Strings;

/**
 * 信頼できるプロキシ経由時のみ X-Forwarded-For を採用してクライアントIPを解決する。
 */
public class TrustedProxyResolver {
  private static final Logger logger = Logger.getLogger(TrustedProxyResolver.class.getName());
  private final List<CidrRule> trustedProxyRules;

  private TrustedProxyResolver(List<CidrRule> trustedProxyRules) {
    this.trustedProxyRules = trustedProxyRules;
  }

  /**
   * CSV形式のIP/CIDR設定からインスタンスを作成する。
   */
  public static TrustedProxyResolver fromCsv(String trustedProxyCsv) {
    if (Strings.isNullOrEmpty(trustedProxyCsv)) {
      return new TrustedProxyResolver(Collections.emptyList());
    }
    List<CidrRule> rules = new ArrayList<>();
    for (String token : trustedProxyCsv.split(",")) {
      String trimmed = token.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      CidrRule parsedRule = parseRule(trimmed);
      if (parsedRule != null) {
        rules.add(parsedRule);
      }
    }
    return new TrustedProxyResolver(rules);
  }

  /**
   * remoteAddr と X-Forwarded-For から採用するクライアントIPを返す。
   */
  public String resolveClientIp(String remoteAddr, String forwardedForHeader) {
    if (!isTrustedProxy(remoteAddr)) {
      return remoteAddr;
    }
    String firstForwardedFor = extractFirstForwardedFor(forwardedForHeader);
    if (!isValidIpLiteral(firstForwardedFor)) {
      return remoteAddr;
    }
    return firstForwardedFor;
  }

  private boolean isTrustedProxy(String remoteAddr) {
    if (trustedProxyRules.isEmpty()) {
      return false;
    }
    byte[] remoteAddressBytes = toAddressBytes(remoteAddr);
    if (remoteAddressBytes == null) {
      return false;
    }
    for (CidrRule trustedProxyRule : trustedProxyRules) {
      if (trustedProxyRule.matches(remoteAddressBytes)) {
        return true;
      }
    }
    return false;
  }

  private String extractFirstForwardedFor(String forwardedForHeader) {
    if (Strings.isNullOrEmpty(forwardedForHeader)) {
      return "";
    }
    int commaIndex = forwardedForHeader.indexOf(',');
    if (commaIndex < 0) {
      return forwardedForHeader.trim();
    }
    return forwardedForHeader.substring(0, commaIndex).trim();
  }

  private boolean isValidIpLiteral(String candidate) {
    return toAddressBytes(candidate) != null;
  }

  private static CidrRule parseRule(String token) {
    int slashIndex = token.indexOf('/');
    if (slashIndex < 0) {
      byte[] addressBytes = toAddressBytes(token);
      if (addressBytes == null) {
        logInvalidRule(token);
        return null;
      }
      return new CidrRule(addressBytes, addressBytes.length * 8);
    }
    String networkPart = token.substring(0, slashIndex).trim();
    String prefixPart = token.substring(slashIndex + 1).trim();
    byte[] networkAddressBytes = toAddressBytes(networkPart);
    if (networkAddressBytes == null) {
      logInvalidRule(token);
      return null;
    }
    int prefixLength;
    try {
      prefixLength = Integer.parseInt(prefixPart);
    } catch (NumberFormatException e) {
      logInvalidRule(token);
      return null;
    }
    int maxPrefixLength = networkAddressBytes.length * 8;
    if (prefixLength < 0 || prefixLength > maxPrefixLength) {
      logInvalidRule(token);
      return null;
    }
    return new CidrRule(networkAddressBytes, prefixLength);
  }

  private static byte[] toAddressBytes(String addressLiteral) {
    if (Strings.isNullOrEmpty(addressLiteral)) {
      return null;
    }
    try {
      return InetAddress.getByName(addressLiteral.trim()).getAddress();
    } catch (UnknownHostException e) {
      return null;
    }
  }

  private static void logInvalidRule(String token) {
    logger.log(Level.WARNING, "信頼プロキシ設定を無視します: {0}", token);
  }

  /**
   * CIDRルールを表現する。
   */
  private static class CidrRule {
    private final byte[] networkAddress;
    private final int prefixLength;

    private CidrRule(byte[] networkAddress, int prefixLength) {
      this.networkAddress = networkAddress;
      this.prefixLength = prefixLength;
    }

    private boolean matches(byte[] addressBytes) {
      if (addressBytes.length != networkAddress.length) {
        return false;
      }
      int fullBytes = prefixLength / 8;
      int remainingBits = prefixLength % 8;
      for (int index = 0; index < fullBytes; index++) {
        if (addressBytes[index] != networkAddress[index]) {
          return false;
        }
      }
      if (remainingBits == 0) {
        return true;
      }
      int bitMask = 0xFF << (8 - remainingBits);
      return (addressBytes[fullBytes] & bitMask) == (networkAddress[fullBytes] & bitMask);
    }
  }
}
