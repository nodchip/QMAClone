package tv.dyndns.kishibe.qmaclone.client.setting;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * 設定保存完了トーストを右下に1件だけ表示する。
 */
public final class SettingSaveToast {
  private static final int DISPLAY_DURATION_MS = 2000;
  private static SimplePanel panel;
  private static HTML message;
  private static Timer hideTimer;
  private static boolean initialized = false;

  private SettingSaveToast() {}

  /**
   * 項目名付きの保存完了メッセージを表示する。
   *
   * @param itemName 設定項目名
   */
  public static void showSaved(String itemName) {
    showRaw("「" + itemName + "」を保存しました");
  }

  private static void showRaw(String message) {
    if (!isClientRuntime()) {
      return;
    }
    ensureInitialized();
    SettingSaveToast.message.setText(message);
    panel.setVisible(true);
    hideTimer.cancel();
    hideTimer.schedule(DISPLAY_DURATION_MS);
  }

  private static void ensureInitialized() {
    if (initialized) {
      return;
    }
    initialized = true;
    panel = new SimplePanel();
    message = new HTML();
    hideTimer = new Timer() {
      @Override
      public void run() {
        panel.setVisible(false);
      }
    };
    panel.addStyleName("app-setting-save-toast");
    message.addStyleName("app-setting-save-toast-message");
    panel.setWidget(message);
    panel.setVisible(false);
    RootPanel.get().add(panel);
  }

  private static boolean isClientRuntime() {
    try {
      return GWT.isClient();
    } catch (Throwable ignored) {
      return false;
    }
  }
}
