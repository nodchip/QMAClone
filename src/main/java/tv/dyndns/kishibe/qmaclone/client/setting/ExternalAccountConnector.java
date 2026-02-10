package tv.dyndns.kishibe.qmaclone.client.setting;

/**
 * 外部アカウントの認可と識別子取得を抽象化する。
 */
public interface ExternalAccountConnector {

  /**
   * 認可後に外部アカウント識別子を返す。
   */
  void authorize(Callback callback);

  /**
   * 認可処理の結果を受け取るコールバック。
   */
  interface Callback {
    void onSuccess(String provider, String subject);

    void onFailure(Exception reason);
  }
}
