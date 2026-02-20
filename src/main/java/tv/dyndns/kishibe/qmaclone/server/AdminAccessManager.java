package tv.dyndns.kishibe.qmaclone.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

/**
 * 管理者アクセス判定を行う。
 */
public class AdminAccessManager {
  private static final Logger logger = Logger.getLogger(AdminAccessManager.class.getName());
  private static final String KEY_CONFIG_PATH = "qmaclone.admin.config";
  private static final String DEFAULT_CONFIG_PATH = "ops/config/live/tomcat9/qmaclone-admin.properties";
  private static final String KEY_ENFORCEMENT_ENABLED = "admin.enforcement.enabled";
  private static final String KEY_GOOGLE_SUB_ALLOWLIST = "admin.google.sub.allowlist";
  private static final String KEY_NETWORK_TRUSTED_PROXIES = "network.trusted.proxies";
  private static final String GOOGLE_PROVIDER = "google";

  private final boolean enforcementEnabled;
  private final Set<String> allowlist;
  private final TrustedProxyResolver trustedProxyResolver;

  public AdminAccessManager() {
    Properties properties = loadProperties();
    this.enforcementEnabled = Boolean.parseBoolean(
        properties.getProperty(KEY_ENFORCEMENT_ENABLED, "true"));
    this.allowlist = parseAllowlist(properties.getProperty(KEY_GOOGLE_SUB_ALLOWLIST, ""));
    this.trustedProxyResolver = TrustedProxyResolver
        .fromCsv(properties.getProperty(KEY_NETWORK_TRUSTED_PROXIES, ""));
  }

  /**
   * 指定ユーザーが管理者か判定する。
   */
  public boolean isAdministrator(int userCode, Database database) throws DatabaseException {
    Preconditions.checkNotNull(database);
    if (!enforcementEnabled) {
      return false;
    }
    if (allowlist.isEmpty()) {
      return false;
    }

    PacketUserData userData = database.getUserData(userCode);
    if (userData == null) {
      return false;
    }
    String provider = Strings.nullToEmpty(userData.authProvider).trim();
    String subject = Strings.nullToEmpty(userData.authSubject).trim();
    if (!GOOGLE_PROVIDER.equals(provider)) {
      return false;
    }
    if (subject.isEmpty() || !allowlist.contains(subject)) {
      return false;
    }

    List<PacketUserData> linkedUsers = database.lookupUserDataByExternalAccount(provider, subject);
    for (PacketUserData linkedUser : linkedUsers) {
      if (linkedUser != null && linkedUser.userCode == userCode) {
        return true;
      }
    }
    return false;
  }

  private Properties loadProperties() {
    Properties properties = new Properties();
    String configPath = System.getProperty(KEY_CONFIG_PATH, DEFAULT_CONFIG_PATH);
    File file = new File(configPath);
    if (!file.exists() || !file.isFile()) {
      logger.log(Level.WARNING, "管理者設定ファイルが見つかりません: {0}", configPath);
      return properties;
    }
    try (FileInputStream inputStream = new FileInputStream(file)) {
      properties.load(inputStream);
      return properties;
    } catch (IOException e) {
      logger.log(Level.WARNING, "管理者設定ファイルの読み込みに失敗しました: " + configPath, e);
      return properties;
    }
  }

  private Set<String> parseAllowlist(String value) {
    if (Strings.isNullOrEmpty(value)) {
      return Collections.emptySet();
    }
    return new LinkedHashSet<>(Splitter.on(',')
        .trimResults()
        .omitEmptyStrings()
        .splitToList(value));
  }

  /**
   * リモートアドレスとX-Forwarded-Forから採用するクライアントIPを返す。
   */
  public String resolveClientIp(String remoteAddr, String forwardedForHeader) {
    return trustedProxyResolver.resolveClientIp(remoteAddr, forwardedForHeader);
  }
}
