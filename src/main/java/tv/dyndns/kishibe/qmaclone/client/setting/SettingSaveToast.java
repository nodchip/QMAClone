package tv.dyndns.kishibe.qmaclone.client.setting;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * 設定保存完了トーストを右下に1件だけ表示する。
 */
public final class SettingSaveToast {
  private static final int DISPLAY_DURATION_MS = 2000;
  private static final SimplePanel PANEL = new SimplePanel();
  private static final HTML MESSAGE = new HTML();
  private static boolean initialized = false;
  private static final Timer HIDE_TIMER = new Timer() {
    @Override
    public void run() {
      PANEL.setVisible(false);
    }
  };

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
    ensureInitialized();
    MESSAGE.setText(message);
    PANEL.setVisible(true);
    HIDE_TIMER.cancel();
    HIDE_TIMER.schedule(DISPLAY_DURATION_MS);
  }

  private static void ensureInitialized() {
    if (initialized) {
      return;
    }
    initialized = true;
    PANEL.addStyleName("app-setting-save-toast");
    MESSAGE.addStyleName("app-setting-save-toast-message");
    PANEL.setWidget(MESSAGE);
    PANEL.setVisible(false);
    RootPanel.get().add(PANEL);
  }
}
