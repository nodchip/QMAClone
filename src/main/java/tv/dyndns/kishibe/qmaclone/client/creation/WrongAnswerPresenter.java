package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.gwt.user.client.ui.IsWidget;

public class WrongAnswerPresenter {

  private static final int MERGE_THRESHOLD = 200;
  private final WrongAnswerView view;

  public WrongAnswerPresenter(WrongAnswerView view) {
    this.view = Preconditions.checkNotNull(view);
  }

  public IsWidget asWidget() {
    return view;
  }

  /**
   * 文字列を整数に変換する。
   * 
   * @param s
   *          変換元の文字列。
   * @return　変換後の整数。整数に変換できない場合は0。
   */
  private int tryParse(String s) {
    try {
      return (int) Math.rint(Double.valueOf(s));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private List<PacketWrongAnswer> normalizeClickAnswers(List<PacketWrongAnswer> answers) {
    Map<String, Integer> normalizedAnswerToCount = Maps.newHashMap();
    for (PacketWrongAnswer answer : answers) {
      String[] coord = answer.answer.split(" ");
      if (coord.length != 2) {
        continue;
      }

      int x = MoreObjects.firstNonNull(tryParse(coord[0]), 0);
      int y = MoreObjects.firstNonNull(tryParse(coord[1]), 0);

      String minKey = answer.answer;
      for (Entry<String, Integer> entry : normalizedAnswerToCount.entrySet()) {
        String[] dstCoord = entry.getKey().split(" ");
        int dstX = MoreObjects.firstNonNull(tryParse(dstCoord[0]), 0);
        int dstY = MoreObjects.firstNonNull(tryParse(dstCoord[1]), 0);
        if ((x - dstX) * (x - dstX) + (y - dstY) * (y - dstY) <= MERGE_THRESHOLD) {
          minKey = entry.getKey();
          break;
        }
      }

      int count = MoreObjects.firstNonNull(normalizedAnswerToCount.get(minKey), Integer.valueOf(0))
          + answer.count;
      normalizedAnswerToCount.put(minKey, count);
    }

    List<PacketWrongAnswer> normalized = Lists.newArrayList();
    for (Entry<String, Integer> entry : normalizedAnswerToCount.entrySet()) {
      PacketWrongAnswer answer = new PacketWrongAnswer();
      answer.answer = entry.getKey();
      answer.count = entry.getValue();
      normalized.add(answer);
    }

    return normalized;
  }

  private String extractFileName(String fileName) {
    if (!fileName.startsWith("http") || !fileName.contains("://")) {
      return fileName;
    }

    int offset = fileName.lastIndexOf('/') + 1;
    return fileName.substring(offset);
  }

  /**
   * 解答文字列の正規化を行う。実際に行うのはURLからのファイル名の抽出と、解答のソート
   * 
   * @param answer
   * @return
   */
  private String normalizeAnswerString(String answer) {
    List<String> pairs = Lists.newArrayList();

    // 複数回答に対応する
    for (String pair : Splitter.on(Constant.DELIMITER_GENERAL).split(answer)) {
      List<String> elements = Lists.newArrayList();

      // 線結びに対応する
      for (String element : Splitter.on(Constant.DELIMITER_KUMIAWASE_PAIR).split(pair)) {
        // URLはファイル名のみ表示する
        elements.add(extractFileName(element));
      }

      pairs.add(Joiner.on(Constant.DELIMITER_KUMIAWASE_PAIR).join(elements));
    }

    return Joiner.on(Constant.DELIMITER_GENERAL).join(pairs);
  }

  /**
   * 複数回答をソートする
   * 
   * @param answer
   * @return
   */
  private String sortMultipleStrings(String answer) {
    List<String> pairs = Lists.newArrayList(Splitter.on(Constant.DELIMITER_GENERAL).split(answer));
    Collections.sort(pairs);
    return Joiner.on(Constant.DELIMITER_GENERAL).join(pairs);
  }

  /**
   * 正規化後の解答の回答数を再度集計する
   * 
   * @param answers
   * @return 再集計結果。結果は解答文字列でソートされている
   */
  private List<PacketWrongAnswer> recountAnswers(List<PacketWrongAnswer> answers) {
    Map<String, Integer> normalizedAnswerToCount = Maps.newHashMap();
    for (PacketWrongAnswer answer : answers) {
      int count = MoreObjects.firstNonNull(normalizedAnswerToCount.get(answer.answer),
          Integer.valueOf(0))
          + answer.count;
      normalizedAnswerToCount.put(answer.answer, count);
    }

    List<PacketWrongAnswer> normalized = Lists.newArrayList();
    for (Entry<String, Integer> entry : normalizedAnswerToCount.entrySet()) {
      PacketWrongAnswer answer = new PacketWrongAnswer();
      answer.answer = entry.getKey();
      answer.count = entry.getValue();
      normalized.add(answer);
    }

    Collections.sort(normalized, new Comparator<PacketWrongAnswer>() {
      @Override
      public int compare(PacketWrongAnswer o1, PacketWrongAnswer o2) {
        return ComparisonChain.start().compare(o1.count, o2.count, Ordering.natural().reverse())
            .compare(o1.answer, o2.answer).result();
      }
    });

    return normalized;
  }

  @VisibleForTesting
  List<PacketWrongAnswer> normalize(List<PacketWrongAnswer> wrongAnswers, PacketProblem problem) {
    // 画像クリッククイズは近い座標をマージする
    if (problem.type == ProblemType.Click) {
      wrongAnswers = normalizeClickAnswers(wrongAnswers);
    }

    // 各解答文字列の正規化を行う
    for (PacketWrongAnswer wrongAnswer : wrongAnswers) {
      wrongAnswer.answer = normalizeAnswerString(wrongAnswer.answer);
    }

    // 各解答文字列をソートする。順番当てはソートしない。
    // BugTrack-QMAClone/633 - QMAClone wiki
    // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F633
    if (problem.type != ProblemType.Junban) {
      for (PacketWrongAnswer wrongAnswer : wrongAnswers) {
        wrongAnswer.answer = sortMultipleStrings(wrongAnswer.answer);
      }
    }

    return recountAnswers(wrongAnswers);
  }

  public void setWrongAnswers(List<PacketWrongAnswer> wrongAnswers, PacketProblem problem) {
    view.setAnswer(normalize(wrongAnswers, problem));
  }

}
