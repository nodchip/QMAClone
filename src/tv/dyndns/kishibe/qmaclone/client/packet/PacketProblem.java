//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.client.packet;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Objects.equal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.Evaluation;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.util.ImageCache;
import tv.dyndns.kishibe.qmaclone.client.util.Random;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketProblem extends PacketProblemMinimum implements IsSerializable {

  private static final String[] REMOVE = { "!<div>", "<\\/div>", "<object.*?object>" };
  // 改行・HTML的にヤバイ記号・SQLインジェクション関連は%～に置き換えられて保存される
  public String sentence; // 問題文
  public String[] answers;// 解答
  public String[] choices;// 選択肢
  public String creator;// 作成者
  public String note;// ノート
  public String[] shuffledAnswers;// シャッフル後の解答
  public String[] shuffledChoices;// シャッフル後の選択肢
  public boolean imageAnswer;// 画像解答
  public boolean imageChoice;// 画像選択肢
  public int voteGood;// 投票(良)
  public int voteBad;// 投票(悪)
  public String imageUrl;
  public String movieUrl;
  public String indicationMessage;
  public Date indicationResolved;
  public int numberOfDisplayedChoices;
  /**
   * 出題中かどうか
   */
  public boolean testing;
  /**
   * 回答数をリセットする必要があるか。データベースには保存されない。
   */
  public boolean needsResetAnswerCount;
  /**
   * 良問悪問投票数をリセットする必要があるか。データベースには保存されない。
   */
  public boolean needsResetVote;
  /**
   * プレイヤーの回答履歴をリセットする必要があるか。データベースには保存されない。
   */
  public boolean needsRemovePlayerAnswers;

  /**
   * 問題文を保存用の記号に変換しつつ代入する。
   * 
   * @param s
   *          問題文
   */
  public void setSentence(String s) {
    sentence = s.trim();
    sentence = sentence.replaceAll("\r", "");
    sentence = sentence.replaceAll("\n", "%n");
    sentence = sentence.replaceAll(Constant.WAIT_SPACE, "%w");
    sentence = sentence.replaceAll("<", "%lt;");
    sentence = sentence.replaceAll(">", "%gt;");
    sentence = sentence.replaceAll("&", "%amp;");
    sentence = sentence.replaceAll("'", "%dash;");
  }

  public String getPanelSentence() {
    String s = sentence;
    s = s.replaceAll("%n", "\n");
    s = s.replaceAll("%w", Constant.WAIT_SPACE);
    s = s.replaceAll("%lt;", "<");
    s = s.replaceAll("%gt;", ">");
    s = s.replaceAll("%amp;", "&");
    s = s.replaceAll("%dash;", "'");
    return s;
  }

  public String getProblemCreationSentence() {
    String s = sentence;
    s = s.replaceAll("%lt;", "<");
    s = s.replaceAll("%gt;", ">");
    s = s.replaceAll("%amp;", "&");
    s = s.replaceAll("%dash;", "'");
    return s;
  }

  /**
   * 問題一覧に表示される問題文を返す
   * 
   * @return
   */
  public String getProblemReportSentence() {
    StringBuilder sb = new StringBuilder(sentence);

    // BugTrack-QMAClone/585 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F585
    if (testing) {
      sb.append(" (出題中)");
    } else if (imageAnswer || imageChoice) {
      sb.append(" (画像)");
    } else {
      for (String answer : getAnswerList()) {
        sb.append(' ');
        sb.append(answer);
      }
    }

    String s = sb.toString();
    s = s.replaceAll("%lt;", "<");
    s = s.replaceAll("%gt;", ">");
    s = s.replaceAll("%amp;", "&");
    s = s.replaceAll("%dash;", "'");
    s = s.replaceAll("%w", " ");
    s = s.replaceAll("%n", " ");
    s = s.replaceAll("%c", " ");
    s = s.replaceAll(" +", " ");

    for (int i = 0; i < REMOVE.length; ++i) {
      s = s.replaceAll(REMOVE[i], "");
    }

    s = s.trim();
    return s;
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("id", id).add("genre", genre).add("type", type)
        .add("sentence", sentence).add("answers", getAnswerList()).add("choices", getChoiceList())
        .toString();
  }

  public boolean isCorrect(String answer) {
    return type.judge(this, answer);
  }

  private int getNumberOfValidStrings(String[] strings) {
    if (strings == null) {
      return 0;
    }
    int result = 0;
    for (String string : strings) {
      if (Strings.isNullOrEmpty(string)) {
        break;
      }
      ++result;
    }
    return result;
  }

  public int getNumberOfAnswers() {
    return getNumberOfValidStrings(answers);
  }

  public int getNumberOfChoices() {
    return getNumberOfValidStrings(choices);
  }

  public int getNumberOfShuffledAnswers() {
    return getNumberOfValidStrings(shuffledAnswers);
  }

  public int getNumberOfShuffledChoices() {
    return getNumberOfValidStrings(shuffledChoices);
  }

  private List<String> toList(String[] strings) {
    if (strings == null) {
      return Collections.emptyList();
    }

    Builder<String> builder = new ImmutableList.Builder<String>();
    for (String string : strings) {
      if (Strings.isNullOrEmpty(string)) {
        continue;
      }
      builder.add(string);
    }
    return builder.build();
  }

  public List<String> getAnswerList() {
    return toList(answers);
  }

  public List<String> getChoiceList() {
    return toList(choices);
  }

  public List<String> getShuffledAnswerList() {
    return toList(shuffledAnswers);
  }

  public List<String> getShuffledChoiceList() {
    return toList(shuffledChoices);
  }

  /**
   * 問題検索に使用する文字列を返す
   * 
   * @return
   */
  public String getSearchDocument() {
    StringBuilder builder = new StringBuilder();
    // 問題文
    builder.append(sentence);

    // 解答
    for (String answer : getAnswerList()) {
      builder.append(' ');
      builder.append(answer);
    }

    // 選択肢
    for (String choice : getChoiceList()) {
      builder.append(' ');
      builder.append(choice);
    }

    // ノート
    if (!Strings.isNullOrEmpty(note)) {
      builder.append(' ');
      builder.append(note);
    }

    return builder.toString();
  }

  public void prepareShuffledAnswersAndChoices() {
    Preconditions.checkNotNull(answers);
    Preconditions.checkNotNull(choices);
    int[] answerOrder = Random.get().makePermutationArray(getNumberOfAnswers());
    int[] choiceOrder = Random.get().makePermutationArray(getNumberOfChoices());
    type.shuffleAnswersAndChoices(this, answerOrder, choiceOrder);
  }

  public int getShuffledAnswerIndex(String answer) {
    int index = Arrays.asList(shuffledAnswers).indexOf(answer);
    return index == -1 ? Integer.MAX_VALUE : index;
  }

  public int getShuffledChoiceIndex(String choice) {
    int index = Arrays.asList(shuffledChoices).indexOf(choice);
    return index == -1 ? Integer.MAX_VALUE : index;
  }

  public boolean hasImage() {
    return imageAnswer || imageChoice || !Strings.isNullOrEmpty(imageUrl)
        || type == ProblemType.Click;
  }

  public boolean hasMovie() {
    return !Strings.isNullOrEmpty(movieUrl);
  }

  /**
   * {@link PacketProblemMinimum}のインスタンスとして返す。インスタンスは新しく生成される。
   * 
   * @return {@link PacketProblemMinimum}のインスタンス
   */
  public PacketProblemMinimum asMinimum() {
    PacketProblemMinimum minimum = new PacketProblemMinimum();
    minimum.id = id;
    minimum.genre = genre;
    minimum.type = type;
    minimum.good = good;
    minimum.bad = bad;
    minimum.randomFlag = randomFlag;
    minimum.creatorHash = creator.hashCode();
    minimum.userCode = userCode;
    minimum.indication = indication;
    return minimum;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PacketProblem)) {
      return false;
    }
    PacketProblem rh = (PacketProblem) obj;
    // [userCode, creatorHash, shuffledAnswers,
    // shuffledChoices]は等値チェックに含めない
    return id == rh.id && genre == rh.genre && type == rh.type && good == rh.good && bad == rh.bad
        && randomFlag == rh.randomFlag && equal(sentence, rh.sentence)
        && getAnswerList().equals(rh.getAnswerList()) && getChoiceList().equals(rh.getChoiceList())
        && equal(creator, rh.creator) && equal(note, rh.note) && imageAnswer == rh.imageAnswer
        && imageChoice == rh.imageChoice && voteGood == rh.voteGood && voteBad == rh.voteBad
        && equal(imageUrl, rh.imageUrl) && equal(movieUrl, rh.movieUrl)
        && equal(indicationMessage, rh.indicationMessage)
        && equal(indicationResolved, rh.indicationResolved)
        && numberOfDisplayedChoices == rh.numberOfDisplayedChoices;
  }

  @Override
  public int hashCode() {
    // [userCode, creatorHash, shuffledAnswers,
    // shuffledChoices]はハッシュコードに含めない
    return Objects.hashCode(id, genre, type, good, bad, randomFlag, sentence, answers, choices,
        creator, note, imageAnswer, imageChoice, voteGood, voteBad, imageUrl, movieUrl,
        indicationMessage, indicationResolved, numberOfDisplayedChoices);
  }

  public Evaluation validate() {
    return type.validate(this);
  }

  public List<String> getImageUrls() {
    List<String> urls = Lists.newArrayList();

    if (imageChoice) {
      for (String url : choices) {
        if (Strings.isNullOrEmpty(url)) {
          continue;
        }
        urls.add(url);
      }
    }

    if (imageAnswer) {
      for (String url : answers) {
        if (Strings.isNullOrEmpty(url)) {
          continue;
        }
        urls.add(url);
      }
    }

    if (!Strings.isNullOrEmpty(imageUrl)) {
      urls.add(imageUrl);
    }

    if (type == ProblemType.Click) {
      urls.add(choices[0]);
    }

    return urls;
  }

  private static final int CHOICE2_IMAGE_WIDTH = 200;
  private static final int CHOICE2_IMAGE_HEIGHT = 150;
  private static final int CHOICE4_IMAGE_WIDTH = 160;
  private static final int CHOICE4_IMAGE_HEIGHT = 120;
  private static final int CHOICE4SMALL_IMAGE_WIDTH = 120;
  private static final int CHOICE4SMALL_IMAGE_HEIGHT = 90;
  private static final int CLICK_IMAGE_WIDTH = 512;
  private static final int CLICK_IMAGE_HEIGHT = 384;
  public static final int SENTENCE_IMAGE_WIDTH = 240;
  public static final int SENTENCE_IMAGE_HEIGHT = 180;

  public String getShuffledChoice2AsImageUrl(int index) {
    return ImageCache.getUrl(shuffledChoices[index], CHOICE2_IMAGE_WIDTH, CHOICE2_IMAGE_HEIGHT);
  }

  public String getShuffledChoice4AsImageUrl(int index) {
    return ImageCache.getUrl(shuffledChoices[index], CHOICE4_IMAGE_WIDTH, CHOICE4_IMAGE_HEIGHT);
  }

  public String getShuffledChoice4SmallAsImageUrl(int index) {
    return ImageCache.getUrl(shuffledChoices[index], CHOICE4SMALL_IMAGE_WIDTH,
        CHOICE4SMALL_IMAGE_HEIGHT);
  }

  public String getShuffledAnswer4SmallAsImageUrl(int index) {
    return ImageCache.getUrl(shuffledAnswers[index], CHOICE4SMALL_IMAGE_WIDTH,
        CHOICE4SMALL_IMAGE_HEIGHT);
  }

  public String getClickImageUrl() {
    return ImageCache.getUrl(shuffledChoices[0], CLICK_IMAGE_WIDTH, CLICK_IMAGE_HEIGHT);
  }

  public String getSentenceImageUrl() {
    return ImageCache.getUrl(imageUrl, SENTENCE_IMAGE_WIDTH, SENTENCE_IMAGE_HEIGHT);
  }

  public List<String> getResizedImageUrls() {
    List<String> urls = Lists.newArrayList();
    int numberOfRandomChoices = getNumberOfShuffledChoices();
    int numberOfRandomAnswers = getNumberOfShuffledAnswers();
    switch (type) {
    case Marubatsu:
      if (imageChoice) {
        for (int i = 0; i < numberOfRandomChoices; ++i) {
          urls.add(getShuffledChoice2AsImageUrl(i));
        }
      }
      break;
    case YonTaku:
    case Rensou:
    case Tato:
    case Junban:
      if (imageChoice) {
        for (int i = 0; i < numberOfRandomChoices; ++i) {
          urls.add(getShuffledChoice4AsImageUrl(i));
        }
      }
      break;
    case Senmusubi:
      if (imageChoice) {
        for (int i = 0; i < numberOfRandomChoices; ++i) {
          urls.add(getShuffledChoice4SmallAsImageUrl(i));
        }
      }
      if (imageAnswer) {
        for (int i = 0; i < numberOfRandomAnswers; ++i) {
          urls.add(getShuffledAnswer4SmallAsImageUrl(i));
        }
      }
      break;
    case Click:
      urls.add(getClickImageUrl());
      break;
    default:
      break;
    }

    if (!Strings.isNullOrEmpty(imageUrl)) {
      urls.add(getSentenceImageUrl());
    }

    return urls;
  }

  private String getChangeSummaryProblemSentence() {
    String s = sentence;
    s = s.replaceAll("%n", "\n");
    s = s.replaceAll("%w", "");
    s = s.replaceAll("%c", "");
    s = s.replaceAll("%lt;", "<");
    s = s.replaceAll("%gt;", ">");
    s = s.replaceAll("%amp;", "&");
    s = s.replaceAll("%dash;", "'");
    return s;
  }

  public String toChangeSummary() {
    StringBuilder sb = new StringBuilder();
    sb.append("ジャンル: ").append(genre.toString()).append('\n');
    sb.append("出題形式: ").append(type.toString()).append('\n');
    sb.append("ランダム: ").append(randomFlag.toString()).append('\n');
    sb.append("問題文:").append('\n');
    sb.append(getChangeSummaryProblemSentence().trim()).append('\n');
    sb.append("選択肢:").append('\n');
    for (String choice : getChoiceList()) {
      sb.append(choice).append('\n');
    }
    sb.append("解答:").append('\n');
    for (String answer : getAnswerList()) {
      sb.append(answer).append('\n');
    }
    sb.append("問題作成者: ").append(creator).append('\n');
    sb.append("問題ノート:").append('\n');
    sb.append(note.trim()).append('\n');
    if (type == ProblemType.Junban || type == ProblemType.Senmusubi || type == ProblemType.Tato) {
      sb.append("表示選択肢数: ").append(numberOfDisplayedChoices).append('\n');
    }
    return sb.toString();
  }

  public boolean isCopiedProblem() {
    return id == CREATING_PROBLEM_ID;
  }

  public PacketProblem clone() {
    PacketProblem problem = new PacketProblem();

    problem.id = id;
    problem.genre = genre;
    problem.type = type;
    problem.good = good;
    problem.bad = bad;
    problem.randomFlag = randomFlag;
    problem.creatorHash = creatorHash;
    problem.userCode = userCode;
    problem.indication = indication;

    problem.sentence = sentence;
    problem.answers = copy(answers);
    problem.choices = copy(choices);
    problem.creator = creator;
    problem.note = note;
    problem.shuffledAnswers = copy(shuffledAnswers);
    problem.shuffledChoices = copy(shuffledChoices);
    problem.imageAnswer = imageAnswer;
    problem.imageChoice = imageChoice;
    problem.voteGood = voteGood;
    problem.voteBad = voteBad;
    problem.imageUrl = imageUrl;
    problem.movieUrl = movieUrl;
    problem.indicationMessage = indicationMessage;
    problem.indicationResolved = indicationResolved;
    problem.numberOfDisplayedChoices = numberOfDisplayedChoices;
    problem.testing = testing;
    problem.needsResetAnswerCount = needsResetAnswerCount;
    problem.needsResetVote = needsResetVote;
    problem.needsRemovePlayerAnswers = needsRemovePlayerAnswers;

    return problem;
  }

  private static String[] copy(@Nullable String[] strings) {
    if (strings == null) {
      return null;
    }
    return Arrays.copyOf(strings, strings.length);
  }

  /**
   * 問題コピー向けに clone を行う。
   * 
   * @return clone した問題オブジェクト
   */
  public PacketProblem cloneForCopyingProblem() {
    PacketProblem problem = (PacketProblem) clone();
    problem.id = CREATING_PROBLEM_ID;
    problem.good = 0;
    problem.bad = 0;
    problem.indication = null;
    problem.creator = UserData.get().getPlayerName();
    problem.voteGood = 0;
    problem.voteBad = 0;
    problem.indicationMessage = null;
    problem.indicationResolved = null;
    return problem;
  }
}
