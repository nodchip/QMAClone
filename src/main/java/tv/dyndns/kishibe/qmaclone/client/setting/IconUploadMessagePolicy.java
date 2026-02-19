package tv.dyndns.kishibe.qmaclone.client.setting;

/**
 * アイコンアップロード画面のメッセージ表示方針を扱うユーティリティ。
 */
public class IconUploadMessagePolicy {
  static final String FILE_SELECTION_PROMPT = "アップロードする画像を選択してください。";

  private IconUploadMessagePolicy() {
  }

  /**
   * checkForm実行時に表示すべきメッセージを返す。
   */
  static String decideMessageForCheckForm(String currentMessage, String filename) {
    if (filename == null || filename.isEmpty()) {
      return FILE_SELECTION_PROMPT;
    }
    if (FILE_SELECTION_PROMPT.equals(currentMessage)) {
      return "";
    }
    return currentMessage == null ? "" : currentMessage;
  }
}
