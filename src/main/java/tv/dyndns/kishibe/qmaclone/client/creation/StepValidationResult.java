package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ステップ単位バリデーションの結果を保持する。
 */
public class StepValidationResult {
  private final Map<String, String> fieldErrors = new LinkedHashMap<String, String>();

  /**
   * 項目エラーを追加する。
   *
   * @param fieldId 項目ID
   * @param message エラーメッセージ
   */
  public void addError(String fieldId, String message) {
    if (fieldId == null || message == null) {
      return;
    }
    fieldErrors.put(fieldId, message);
  }

  /**
   * 項目エラーを返す。
   *
   * @return 項目エラー
   */
  public Map<String, String> getFieldErrors() {
    return fieldErrors;
  }

  /**
   * エラーがあるか返す。
   *
   * @return エラーが1件以上あればtrue
   */
  public boolean hasErrors() {
    return !fieldErrors.isEmpty();
  }

  /**
   * 最初のエラー項目IDを返す。
   *
   * @return 最初のエラー項目ID。未存在ならnull
   */
  public String getFirstErrorFieldId() {
    if (fieldErrors.isEmpty()) {
      return null;
    }
    return fieldErrors.keySet().iterator().next();
  }
}
