package tv.dyndns.kishibe.qmaclone.client.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StepValidationResultTest {

  @Test
  public void hasErrors_returnsTrue_whenFieldErrorsExist() {
    StepValidationResult result = new StepValidationResult();
    result.addError("genre", "ジャンルを選択してください");
    assertTrue(result.hasErrors());
  }

  @Test
  public void firstError_isFirstInsertedKey() {
    StepValidationResult result = new StepValidationResult();
    result.addError("genre", "ジャンルを選択してください");
    result.addError("type", "出題形式を選択してください");
    assertEquals("genre", result.getFirstErrorFieldId());
  }
}
