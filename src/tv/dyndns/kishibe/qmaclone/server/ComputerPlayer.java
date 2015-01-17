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
package tv.dyndns.kishibe.qmaclone.server;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.LetterType;
import tv.dyndns.kishibe.qmaclone.client.geom.Point;
import tv.dyndns.kishibe.qmaclone.client.geom.Polygon;
import tv.dyndns.kishibe.qmaclone.client.geom.PolygonException;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.util.Random;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

//TODO(nodchip):リファクタリング
public class ComputerPlayer {
  private static final Logger logger = Logger.getLogger(ComputerPlayer.class.getName());
  private final Random random = new Random();
  @VisibleForTesting
  double comStrength = 0.5;
  private static final String COM_NAME[] = {
      //
      "レオン", "ユルグ", "ルッツ",//
      "セリオス", "マテウス", "レクトール",//
      "ユウ", "イオリ", "チヒロ",//
      "ハルト", "ケント", "ライト",//
      "リック", "シン", "マルク",//
      "シャロン", "トゥエット", "レイア",//
      "ユリ", "サラ", "ナツミ",//
      "リエル", "コロン", "マドカ",//
      "アイコ", "ミイコ", "マイコ",//
      "メディア", "アンジェ", "マリア",//
      "ミュー", "ニュー", "シュー",//
      "マヤ", "マコ", "マミ ",//
  };
  private static final String GREETING[] = {
      // 以下ぷよぷよシリーズより
      "行っきまーす", "おちゃー", "はらほろひれはれー♪", "ひゃっほう ぬはー!", "負っけないからねぇ!", "彼女～v", "ふぃーっしゅ!", "お掃除しちゃうぞ☆",
      "のっほっほー", "じゃいやー!", "おいッス", "魔王だぞぅ", "近う寄れ", "勇者に敗北はない!", "かかってらっしゃい!", "行くぜ!", "抵抗する気か",
      "きゃはっ", "やっほぉー", "ピチピチよ!", "がぉー", "戦うのですか？", "ふふっ",
      "毎度ー",
      "ウゥモォー！",
      "ぐーーっ！",
      // 以下アニメ名台詞
      "これでいいのだ", "お前はもう死んでいる", "ボールは友達さ", "はやく人間になりたい", "オイ！鬼太郎", "立つんだ、ジョー！", "まいっちんぐ！",
      "キーーーン！んちゃ！", "どぼじで～", "響子さん好きじゃー", "コナン…生きて！", "ゲットだぜ！", "さようならラスカル", "いけずぅ～", "真実はいつもひとつ",
      "零ちゃんぶつじょ～", "それからどしたの？", "教師生活25年！", "まだまだだね", "坊やだからさ", "ここで僕が投了！", "俺様の美技に酔いな",
      "笑えばいいと思うよ", "クリリンのことかー！", "おすわり", "テニス楽しんでる？", "おねがい♪", "まだだまだ終わらんよ", "禁則事項です", "あんたバカぁ？",
      "天才ですから", "あくまで執事ですから", "いいでそべつに", "うまいぞぉ～！", };
  private final PlayerAnswer playerAnswer;
  private final String[] comIconFileNames = loadIconFileNames();
  private static final Set<String> ACCEPTABLE_EXTENSIONS = ImmutableSet.of("jpg", "jpeg", "gif",
      "png", "bmp");

  public static interface Factory {
    ComputerPlayer create(List<Integer> problemIds);
  }

  @Inject
  public ComputerPlayer(PlayerAnswer.Factory playerAnswerFactory, @Assisted List<Integer> problemIds) {
    playerAnswer = playerAnswerFactory.create(problemIds);
  }

  private static String[] loadIconFileNames() {
    String[] iconFileNames = new File(Constant.ICON_FOLDER_PATH).list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        String extension = Files.getFileExtension(name).toLowerCase();
        return ACCEPTABLE_EXTENSIONS.contains(extension);
      }
    });

    if (iconFileNames == null || iconFileNames.length == 0) {
      return new String[] { Constant.ICON_NO_IMAGE };
    }

    return iconFileNames;
  }

  /**
   * アイコン画像を選択する
   * 
   * @return アイコン画像ファイル名
   */
  public String selectIconFileName() {
    return comIconFileNames[random.nextInt(comIconFileNames.length)];
  }

  public PacketPlayerSummary newPlayer(int classLevel) {
    final PacketPlayerSummary player = new PacketPlayerSummary();

    if (classLevel == Constant.MAX_CLASS_LEVEL) {
      if (random.nextBoolean()) {
        player.name = "マテウス";
      } else {
        player.name = "デスマテウス";
      }
    } else {
      player.name = COM_NAME[random.nextInt(COM_NAME.length)];
    }

    player.level = "(COM)";
    player.prefecture = "無所属";
    player.rating = 1300;
    return player;
  }

  public String getAnswer(PacketProblem problem, List<String> playerAnswers) {
    double correctRatio = (problem.good + 1) / (double) (problem.good + problem.bad + 2);
    comStrength = Math.max(0.0, correctRatio - 0.2);

    switch (problem.type) {
    case Marubatsu:
      return getAnswerMaruBatu(problem);
    case YonTaku:
    case Rensou:
      return getAnswerChoice(problem);
    case Narabekae:
    case Flash:
      return getAnswerNarabekae(problem);
    case MojiPanel:
      return getAnswerMojiPanel(problem);
    case Typing:
    case Effect:
      return getAnswerTyping(problem);
    case Tato:
      return getAnswerTato(problem);
    case Junban:
      return getAnswerJunban(problem);
    case Senmusubi:
      return getAnswerSenmusubi(problem);
    case Slot:
      return getAnswerSlot(problem);
    case Click:
      return getAnswerClick(problem);
    case Tegaki:
      return getAnswerTegaki(problem);
    case Hayaimono:
      return getAnswerHayaimono(problem, playerAnswers);
    case Group:
      return getAnswerSenmusubi(problem);
    default:
      break;
    }

    return "・・・・・・・・";
  }

  @VisibleForTesting
  boolean correct() {
    return random.nextDouble() < comStrength;
  }

  @VisibleForTesting
  String joke(int problemID) {
    if (random.nextBoolean()) {
      return null;
    }

    return playerAnswer.get(problemID);
  }

  static private final String MARU_BATU[] = { "○", "×" };

  private String getAnswerMaruBatu(PacketProblem problem) {
    if (correct()) {
      return problem.shuffledAnswers[0];
    }

    if (problem.imageChoice) {
      return problem.shuffledChoices[random.nextInt(2)];
    }

    return MARU_BATU[random.nextInt(2)];
  }

  private String getAnswerChoice(PacketProblem problem) {
    try {
      if (correct()) {
        return problem.shuffledAnswers[0];
      }
    } catch (Exception e) {
      return "・・・・・・・・";
    }

    return problem.shuffledChoices[random.nextInt(4)];
  }

  private String randomShuffle(String s) {
    char[] cs = s.toCharArray();
    for (int i = 0; i < cs.length; ++i) {
      int j = random.nextInt(cs.length);
      char ch = cs[i];
      cs[i] = cs[j];
      cs[j] = ch;
    }
    return new String(cs);
  }

  private String getAnswerNarabekae(PacketProblem problem) {
    if (correct()) {
      return problem.shuffledAnswers[0];
    }

    String answer = joke(problem.id);
    if (answer != null) {
      return answer;
    }

    return randomShuffle(problem.shuffledAnswers[0]);
  }

  @VisibleForTesting
  String getAnswerMojiPanel(PacketProblem problem) {
    if (correct()) {
      return problem.shuffledAnswers[0];
    }

    String joke = joke(problem.id);
    if (joke != null) {
      return joke;
    }

    String answer = problem.shuffledAnswers[0];
    String s = randomShuffle(problem.shuffledChoices[0]);
    if (s.length() > answer.length()) {
      s = s.substring(0, answer.length());
    }
    return s;
  }

  private static final String LETTERS[] = { "あかさたなはまやらわいきしちにひみりをうくすつぬふむゆるんえけせてねへめれおこそとのほもよろー",
      "アカサタナハマヤラワイキシチニヒミリヲウクスツヌフムユルンエケセテネヘメレオコソトノホモヨロー", "０１２３４５６７８９ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ" };

  private String getAnswerTyping(PacketProblem problem) {
    if (correct()) {
      int numberOfAnswer = problem.getNumberOfShuffledAnswers();
      return problem.shuffledAnswers[random.nextInt(numberOfAnswer)];
    }

    {
      final String answer = joke(problem.id);
      if (answer != null) {
        return answer;
      }
    }

    LetterType type;
    char ch = problem.shuffledAnswers[0].charAt(0);
    if (0x3041 <= ch && ch <= 0x309e) {
      type = LetterType.Hiragana;
    } else if (0x30a1 <= ch && ch <= 0x30f6) {
      type = LetterType.Katakana;
    } else if ('Ａ' <= ch && ch <= 'Ｚ' || '０' <= ch && ch <= '９') {
      type = LetterType.Alphabet;
    } else {
      type = null;

      logger.log(Level.WARNING, "文字種別を判定できませんでした");
    }

    final StringBuilder sb = new StringBuilder();
    int answerIndex = 0;
    for (int i = 0; i < problem.shuffledAnswers[answerIndex].length(); ++i) {
      int index = random.nextInt(LETTERS[type.ordinal()].length());
      sb.append(LETTERS[type.ordinal()].charAt(index));
    }

    return sb.toString();
  }

  private String getAnswerTato(PacketProblem problem) {
    final int numberOfAnswer = problem.getNumberOfShuffledAnswers();
    final StringBuilder sb = new StringBuilder();
    if (correct()) {
      for (int i = 0; i < numberOfAnswer; ++i) {
        if (sb.length() != 0) {
          sb.append('\n');
        }
        sb.append(problem.shuffledAnswers[i]);
      }
      return sb.toString();
    }

    for (int i = 0; i < numberOfAnswer; ++i) {
      if (random.nextBoolean()) {
        if (sb.length() != 0) {
          sb.append(Constant.DELIMITER_GENERAL);
        }
        sb.append(problem.shuffledChoices[i]);
      }
    }
    return sb.toString();
  }

  private String getAnswerJunban(PacketProblem problem) {
    final int numberOfAnswer = problem.getNumberOfShuffledAnswers();
    final StringBuilder sb = new StringBuilder();
    if (correct()) {
      for (int i = 0; i < numberOfAnswer; ++i) {
        if (sb.length() != 0) {
          sb.append(Constant.DELIMITER_GENERAL);
        }
        sb.append(problem.shuffledAnswers[i]);
      }
      return sb.toString();
    }

    int perm[] = Random.get().makePermutationArray(numberOfAnswer);

    for (int i = 0; i < numberOfAnswer; ++i) {
      if (sb.length() != 0) {
        sb.append('\n');
      }
      sb.append(problem.shuffledAnswers[perm[i]]);
    }
    return sb.toString();

  }

  private String getAnswerSenmusubi(PacketProblem problem) {
    final int numberOfAnswer = problem.getNumberOfShuffledAnswers();
    final StringBuilder sb = new StringBuilder();
    int perm[] = Random.get().makePermutationArray(numberOfAnswer);
    if (correct()) {
      for (int i = 0; i < numberOfAnswer; ++i) {
        if (i != 0) {
          sb.append(Constant.DELIMITER_GENERAL);
        }
        sb.append(problem.shuffledChoices[perm[i]]);
        sb.append(Constant.DELIMITER_KUMIAWASE_PAIR);
        sb.append(problem.shuffledAnswers[perm[i]]);
      }

      return sb.toString();
    }

    int perm2[] = random.makePermutationArray(numberOfAnswer);
    for (int i = 0; i < numberOfAnswer; ++i) {
      if (i != 0) {
        sb.append(Constant.DELIMITER_GENERAL);
      }
      sb.append(problem.shuffledChoices[perm[i]]);
      sb.append(Constant.DELIMITER_KUMIAWASE_PAIR);
      sb.append(problem.shuffledAnswers[perm2[i]]);
    }
    return sb.toString();
  }

  private String getAnswerSlot(PacketProblem problem) {
    if (correct()) {
      return problem.shuffledAnswers[0];
    }

    String answer = joke(problem.id);
    if (answer != null) {
      return answer;
    }

    final StringBuilder b = new StringBuilder();
    int length = problem.shuffledAnswers[0].length();
    final int numberOfAnswer = problem.getNumberOfShuffledAnswers();
    for (int x = 0; x < length; ++x) {
      b.append(problem.shuffledAnswers[random.nextInt(numberOfAnswer)].substring(x, x + 1));
    }

    return b.toString();
  }

  private String getAnswerClick(PacketProblem problem) {
    if (correct()) {
      // 領域の最大最小を求める
      int minX = Integer.MAX_VALUE;
      int minY = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int maxY = Integer.MIN_VALUE;
      int answerIndex = random.nextInt(problem.getNumberOfShuffledAnswers());
      Polygon polygon;

      try {
        polygon = Preconditions.checkNotNull(Polygon
            .fromString(problem.shuffledAnswers[answerIndex]));
      } catch (PolygonException e) {
        logger.log(Level.WARNING, "ポリゴンデータが不正です", e);
        return new Point(random.nextInt(Constant.CLICK_IMAGE_WIDTH),
            random.nextInt(Constant.CLICK_IMAGE_HEIGHT)).toString();
      }

      for (Point point : polygon) {
        minX = Math.min(minX, point.x);
        minY = Math.min(minY, point.y);
        maxX = Math.max(maxX, point.x);
        maxY = Math.max(maxY, point.y);
      }

      for (int loop = 0; loop < 100; ++loop) {
        int x = random.nextInt(maxX - minX) + minX;
        int y = random.nextInt(maxY - minY) + minY;
        Point point = new Point(x, y);
        if (polygon.contains(point)) {
          break;
        }
      }

      polygon.toString();
    }

    String answer = joke(problem.id);
    if (Point.fromString(answer) != null) {
      return answer;
    }

    final Point point = new Point(random.nextInt(Constant.CLICK_IMAGE_WIDTH),
        random.nextInt(Constant.CLICK_IMAGE_HEIGHT));
    return point.toString();
  }

  private String getAnswerTegaki(PacketProblem problem) {
    if (correct()) {
      return problem.shuffledAnswers[0];
    }

    String answer = joke(problem.id);
    if (answer != null) {
      return answer;
    }

    final char[] array = problem.shuffledAnswers[0].toCharArray();
    for (int i = 0; i < array.length; ++i) {
      array[i] += random.nextInt(3) - 1;
    }
    return new String(array);
  }

  private String getAnswerHayaimono(PacketProblem problem, List<String> playerAnswers) {
    if (correct()) {
      for (String answer : problem.getAnswerList()) {
        if (canBeSelectedHayaimono(problem, playerAnswers, answer)) {
          return answer;
        }
      }
    }

    String answer = joke(problem.id);
    if (answer != null && canBeSelectedHayaimono(problem, playerAnswers, answer)) {
      return answer;
    }

    for (int index : random.makePermutationArray(problem.getNumberOfChoices())) {
      answer = problem.choices[index];
      if (canBeSelectedHayaimono(problem, playerAnswers, answer)) {
        return answer;
      }
    }

    return null;
  }

  private boolean canBeSelectedHayaimono(PacketProblem problem, List<String> playerAnswers,
      String answer) {
    int numberOfAnswers = problem.getNumberOfAnswers();
    int numberOfSeats = (Constant.MAX_PLAYER_PER_SESSION + numberOfAnswers - 1) / numberOfAnswers;
    return Collections.frequency(playerAnswers, answer) < numberOfSeats;
  }

  public String getGreeting() {
    return GREETING[random.nextInt(GREETING.length)];
  }
}
