package tv.dyndns.kishibe.qmaclone.client.setting;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

/**
 * アイコンアップロード画面のメッセージ表示方針を検証するテスト。
 */
public class IconUploadMessagePolicyTest {

  /**
   * 成功メッセージは、ファイルが選択済みの間は維持されること。
   */
  @Test
  public void decideMessageForCheckFormShouldKeepResultMessageWhenFileIsSelected() {
    String resultMessage = "アイコンのアップロードに成功しました。";

    String nextMessage = IconUploadMessagePolicy.decideMessageForCheckForm(
        resultMessage, "sample.png");

    assertThat(nextMessage).isEqualTo(resultMessage);
  }

  /**
   * ファイル未選択時は入力促進メッセージを返すこと。
   */
  @Test
  public void decideMessageForCheckFormShouldShowPromptWhenFileIsEmpty() {
    String nextMessage = IconUploadMessagePolicy.decideMessageForCheckForm(
        "任意メッセージ", "");

    assertThat(nextMessage).isEqualTo("アップロードする画像を選択してください。");
  }

  /**
   * 入力促進メッセージのみ、ファイル選択後に消すこと。
   */
  @Test
  public void decideMessageForCheckFormShouldClearPromptWhenFileIsSelected() {
    String nextMessage = IconUploadMessagePolicy.decideMessageForCheckForm(
        "アップロードする画像を選択してください。", "sample.png");

    assertThat(nextMessage).isEqualTo("");
  }
}
