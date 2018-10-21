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
package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.primitives.Chars;

public abstract class Validator {
  private static final List<Set<Character>> LETTER_SETS = ImmutableList.of(
      toSet("あかさたなはまやらわいきしちにひみりをうくすつぬふむゆるんえけせてねへめれおこそとのほもよろーがぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽぁぃぅぇぉゃゅょっ"),
      toSet("アカサタナハマヤラワイキシチニヒミリヲウクスツヌフムユルンエケセテネヘメレオコソトノホモヨローガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポァィゥェォャュョッヴ"),
      toSet("１２３４５６７８９０ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ"));
  private static final String HALF_FULL_LETTER[] = { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
      "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ０１２３４５６７８９" };
  private static final Map<Character, Character> HALF_TO_FULL = createHalfToFull();
  private static final String regex = "\\b(?:https?|shttp)://(?:(?:[-_.!~*'()a-zA-Z0-9;:&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*@)?(?:(?:[a-zA-Z0-9](?:[-a-zA-Z0-9]*[a-zA-Z0-9])?\\.)*[a-zA-Z](?:[-a-zA-Z0-9]*[a-zA-Z0-9])?\\.?|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(?::[0-9]*)?(?:/(?:[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*(?:;(?:[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*)*(?:/(?:[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*(?:;(?:[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*)*)*)?(?:\\?(?:[-_.!~*'()a-zA-Z0-9;/?:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*)?(?:#(?:[-_.!~*'()a-zA-Z0-9;/?:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*)?";

  private static Set<Character> toSet(String s) {
    return ImmutableSet.copyOf(Chars.asList(s.toCharArray()));
  }

  private static Map<Character, Character> createHalfToFull() {
    char[] halfs = HALF_FULL_LETTER[0].toCharArray();
    char[] fulls = HALF_FULL_LETTER[1].toCharArray();
    Map<Character, Character> halfToFull = Maps.newHashMap();
    for (int i = 0; i < halfs.length; ++i) {
      halfToFull.put(halfs[i], fulls[i]);
    }
    return ImmutableMap.copyOf(halfToFull);
  }

  protected Validator() {
  }

  protected boolean checkTypingAnswer(String answer) {
    if (Strings.isNullOrEmpty(answer)) {
      return false;
    }

    Set<Character> answerLetters = toSet(answer);
    for (Set<Character> set : LETTER_SETS) {
      if (set.containsAll(answerLetters)) {
        return true;
      }
    }

    return false;
  }

  protected String toFull(String answer) {
    StringBuilder bs = new StringBuilder();
    for (char ch : answer.toCharArray()) {
      if (HALF_TO_FULL.containsKey(ch)) {
        bs.append(HALF_TO_FULL.get(ch));
      } else {
        bs.append(ch);
      }
    }

    return bs.toString();
  }

  /**
   * 問題を検証する
   * 
   * @param problem
   *          問題
   * @return 問題がなければ空のリスト
   */
  public Evaluation check(PacketProblem problem) {
    return check(problem, true);
  }

  /**
   * 問題を検証する
   * 
   * @param problem
   *          問題
   * @param checkDuplicatedAnswers
   *          解答欄の重複を検証する場合は{@code true}
   * @return 問題がなければ空のリスト
   */
  public Evaluation check(PacketProblem problem, boolean checkDuplicatedAnswers) {
    Evaluation eval = new Evaluation();
    List<String> warn = eval.warn;
    List<String> info = eval.info;

    if (problem.genre == ProblemGenre.Random) {
      warn.add("ジャンルを選択してください");
    }

    if (problem.type == ProblemType.Random) {
      warn.add("出題形式を選択してください");
    }

    if (problem.sentence == null || problem.sentence.trim().isEmpty()) {
      warn.add("問題文を入力してください");
    } else if (problem.creator == null || problem.creator.trim().isEmpty()) {
      warn.add("作成者名を入力してください");
    }

    if (problem.creator.equals("未初期化です")) {
      warn.add("「未初期化です」名義での問題作成はできません。作者名を入力してください。");
    }

    if (problem.imageAnswer) {
      final int numberOfAnswers = problem.getNumberOfAnswers();
      for (int i = 0; i < numberOfAnswers; ++i) {
        final String answer = problem.answers[i];
        if (!isUrl(answer)) {
          warn.add((i + 1) + "個目の解答が画像URL形式になっていません");
        }
      }
    }

    if (problem.imageChoice) {
      final int numberOfChoice = problem.getNumberOfChoices();
      for (int i = 0; i < numberOfChoice; ++i) {
        final String choice = problem.choices[i];
        if (!isUrl(choice)) {
          warn.add((i + 1) + "個目の選択肢が画像URL形式になっていません");
        }
      }
    }

    if (problem.randomFlag == RandomFlag.Random5) {
      warn.add("下記を参考にランダムフラグを1～4の中から選んでください");
    }

    if (problem.imageUrl != null) {
      final String imageUrl = problem.imageUrl;
      if (!isUrl(imageUrl)) {
        warn.add("外部コンテンツの画像URLがURL形式になっていません");
      }
    }

    if (problem.movieUrl != null) {
      final String movieUrl = problem.movieUrl;
      if (!isUrl(movieUrl) || !movieUrl.startsWith("http://www.youtube.com/watch?v=")) {
        warn.add("外部コンテンツのYouTubeのURLが正しくありません");
      }
    }

    if (checkDuplicatedAnswers
        && ImmutableSet.copyOf(problem.getAnswerList()).size() != problem.getNumberOfAnswers()) {
      warn.add("解答が重複しています");
    }

    if (ImmutableSet.copyOf(problem.getChoiceList()).size() != problem.getNumberOfChoices()) {
      warn.add("選択肢が重複しています");
    }

    String sentence = Strings.nullToEmpty(problem.sentence).trim();
    if (sentence.contains("現在") || sentence.contains("今まで") || sentence.contains("今では")) {
      info.add("問題文中に現在時刻を表す単語が検出されました。最新情報に関する問題を作成する際は『何年何月現在』等、日付日時を指定して下さい");
    }

    return eval;
  }

  protected boolean isUrl(String url) {
    return !Strings.isNullOrEmpty(url) && url.matches(regex);
  }

  protected boolean consistsOfTheSameLetters(String a, String b) {
    char[] aa = a.toCharArray();
    Arrays.sort(aa);
    char[] bb = b.toCharArray();
    Arrays.sort(bb);
    return Arrays.equals(aa, bb);
  }
}
