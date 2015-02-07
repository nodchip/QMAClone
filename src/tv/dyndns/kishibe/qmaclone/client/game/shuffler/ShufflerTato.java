package tv.dyndns.kishibe.qmaclone.client.game.shuffler;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ShufflerTato implements Shuffleable {

  private static final Logger logger = Logger.getLogger(ShufflerTato.class.getName());

  @Override
  public void shuffle(PacketProblem problem, int[] answerOrder, int[] choiceOrder) {
    Preconditions.checkNotNull(answerOrder);
    Preconditions.checkNotNull(choiceOrder);

    if (answerOrder.length < 1 || choiceOrder.length < 3) {
      problem.shuffledAnswers = null;
      problem.shuffledChoices = null;
      return;
    }

    Set<String> answers = ImmutableSet.copyOf(problem.getAnswerList());
    List<String> choices = Lists.newArrayList();

    // 正答を一つ追加する
    for (int choiceIndex : choiceOrder) {
      String choice = problem.choices[choiceIndex];
      if (!answers.contains(choice)) {
        continue;
      }

      choices.add(choice);
      break;
    }

    // 選択肢を４つ選ぶ
    int numberOfChoices = Math.min(choiceOrder.length, problem.numberOfDisplayedChoices);
    for (int choiceIndex : choiceOrder) {
      String choice = problem.choices[choiceIndex];
      if (choices.contains(choice)) {
        continue;
      }

      choices.add(choice);

      if (choices.size() >= numberOfChoices) {
        break;
      }
    }

    if (choices.size() < numberOfChoices) {
      String warningMessage = "選択肢シャッフル中に不正なデータを検出しました: "
          + MoreObjects.toStringHelper(this).add("problem", problem)
              .add("answerOrder", Arrays.toString(answerOrder))
              .add("choiceOrder", Arrays.toString(choiceOrder)).toString();
      logger.log(Level.WARNING, warningMessage);
    }

    // 4つの選択肢を並び替える
    List<Integer> secondOrder = Lists.newArrayList();
    for (int choiceIndex : choiceOrder) {
      if (choiceIndex < choices.size()) {
        secondOrder.add(choiceIndex);
      }
    }

    problem.shuffledChoices = new String[numberOfChoices];
    for (int i = 0; i < choices.size(); ++i) {
      problem.shuffledChoices[i] = choices.get(secondOrder.get(i));
    }

    // 選択肢に対応した解答を作成する
    Set<String> remainedAnswers = Sets.newHashSet(answers);
    remainedAnswers.retainAll(Arrays.asList(problem.shuffledChoices));
    problem.shuffledAnswers = remainedAnswers.toArray(new String[0]);
  }

}
