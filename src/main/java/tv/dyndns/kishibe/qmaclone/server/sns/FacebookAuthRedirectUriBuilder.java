package tv.dyndns.kishibe.qmaclone.server.sns;

import com.google.common.base.Strings;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Facebook OAuth用 redirect_uri を構築するユーティリティ。
 */
public final class FacebookAuthRedirectUriBuilder {
  private FacebookAuthRedirectUriBuilder() {
  }

  public static String build(HttpServletRequest request) {
    String scheme = firstToken(request.getHeader("X-Forwarded-Proto"));
    if (Strings.isNullOrEmpty(scheme)) {
      scheme = request.getScheme();
    }

    String host = firstToken(request.getHeader("X-Forwarded-Host"));
    if (Strings.isNullOrEmpty(host)) {
      host = request.getServerName();
    }

    Integer port = parsePort(firstToken(request.getHeader("X-Forwarded-Port")));
    if (port == null) {
      port = request.getServerPort();
    }

    String authority = host;
    if (needsPort(scheme, port) && !host.contains(":")) {
      authority = host + ":" + port;
    }

    return scheme + "://" + authority + request.getContextPath() + "/admin/facebook/auth/callback";
  }

  private static boolean needsPort(String scheme, Integer port) {
    if (port == null) {
      return false;
    }
    if ("http".equalsIgnoreCase(scheme) && port.intValue() == 80) {
      return false;
    }
    if ("https".equalsIgnoreCase(scheme) && port.intValue() == 443) {
      return false;
    }
    return true;
  }

  private static String firstToken(String value) {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
    int commaIndex = value.indexOf(',');
    if (commaIndex >= 0) {
      return value.substring(0, commaIndex).trim();
    }
    return value.trim();
  }

  private static Integer parsePort(String value) {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
