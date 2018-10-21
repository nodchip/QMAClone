package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

@RunWith(JUnit4.class)
public class ValidatorClickTest extends ValidatorTestBase {
  private ValidatorClick validator;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    validator = new ValidatorClick();
    problem.type = ProblemType.Click;
    problem.choices = toArray("http://www.google.com/logo.jpg");
    problem.answers = toArray("0 0 0 100 100 100 100 0");
  }

  @Test
  public void checkShouldWork() {
    assertEquals(Arrays.asList(), validator.check(problem).warn);
  }

  @Test
  public void checkShouldReturnFalseIfInvalidUrl() {
    problem.choices = toArray("hoge");
    assertEquals(Arrays.asList("選択肢に正しいURLが入力されていません"), validator.check(problem).warn);
  }

  @Test
  public void chkeckShouldReturnFalseIfNotImage() {
    problem.choices = toArray("http://www.google.com/");
    assertEquals(Arrays.asList("使用可能な画像形式はBMP・PNG・GIF・JPGのみです"), validator.check(problem).warn);
  }

  @Test
  public void checkShouldReturnFalseIfNoAnswers() {
    problem.answers = toArray();
    assertEquals(Arrays.asList("解答が入力されていません"), validator.check(problem).warn);
  }

  @Test
  public void testCheck() {
    problem.answers = toArray("hoge");
    assertThat(validator.check(problem).warn).isEqualTo(
        Arrays.asList("1番目の解答が領域を表現した文字列になっていません: 数字以外の文字が入力されました: hoge"));
  }
}
