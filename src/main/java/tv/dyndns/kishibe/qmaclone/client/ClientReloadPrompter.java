package tv.dyndns.kishibe.qmaclone.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Window;

/**
 * 旧GWTキャッシュが疑われる場合に、再読み込み導線を表示する。
 */
public final class ClientReloadPrompter {
  private static final Logger logger = Logger.getLogger(ClientReloadPrompter.class.getName());
  private static final String CACHE_BUSTER_KEY = "gwt_refresh";
  private static boolean prompted = false;

  private ClientReloadPrompter() {
  }

  /**
   * 旧キャッシュ由来の失敗であれば、リロード確認を表示する。
   *
   * @param throwable 失敗例外
   * @return リロード導線を表示した場合はtrue
   */
  public static boolean maybePrompt(Throwable throwable) {
    if (prompted || !StaleRpcFailureDetector.isStaleRpcFailure(throwable)) {
      return false;
    }
    prompted = true;
    logger.log(Level.WARNING, "旧GWTキャッシュ由来の通信失敗を検知しました。再読み込みを促します。", throwable);
    boolean accepted = Window.confirm(
        "ページの更新が必要です。最新のクライアントを読み込むため再読み込みしますか？\n"
            + "（更新後に問題が続く場合は Ctrl+F5 をお試しください）");
    if (accepted) {
      Window.Location
          .replace(buildReloadUrl(Window.Location.getHref(), (long) Duration.currentTimeMillis()));
    }
    return true;
  }

  /**
   * キャッシュ回避用クエリを付与したURLを生成する。
   *
   * @param href 現在URL
   * @param timestamp クエリ値
   * @return 再読み込み先URL
   */
  static String buildReloadUrl(String href, long timestamp) {
    int hashIndex = href.indexOf('#');
    String base = hashIndex >= 0 ? href.substring(0, hashIndex) : href;
    String fragment = hashIndex >= 0 ? href.substring(hashIndex) : "";
    String separator = base.contains("?") ? "&" : "?";
    return base + separator + CACHE_BUSTER_KEY + "=" + timestamp + fragment;
  }
}
