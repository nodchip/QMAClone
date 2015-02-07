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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.database.ProblemMinimumProcessable;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class NormalModeProblemManager extends ProblemManager {
  private static final Logger logger = Logger.getLogger(NormalModeProblemManager.class.getName());

  private static class GenreAndType {
    public ProblemGenre genre;
    public ProblemType type;

    public GenreAndType(ProblemGenre genre, ProblemType type) {
      this.genre = genre;
      this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof GenreAndType && genre == ((GenreAndType) obj).genre
          && type == ((GenreAndType) obj).type;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(genre, type);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("genre", genre).add("type", type).toString();
    }
  }

  private final Database database;
  private final ThreadPool threadPool;

  private volatile Map<GenreAndType, IntArray> problemss;
  /**
   * 各ジャンル・出題形式毎の問題数
   */
  private volatile int[][] tableProblemCount;
  /**
   * 各ジャンル・正解率毎の問題数
   */
  private volatile int[][] tableProblemRatio;

  @Inject
  public NormalModeProblemManager(Database database, ThreadPool threadPool) {
    super(database);
    this.database = database;
    this.threadPool = threadPool;
  }

  private void initializeIfNotInitialized() {
    if (problemss == null) {
      synchronized (this) {
        if (problemss == null) {
          initialize();
        }
      }
    }
  }

  private void initialize() {
    problemss = new ConcurrentHashMap<NormalModeProblemManager.GenreAndType, IntArray>();
    for (ProblemGenre genre : ProblemGenre.values()) {
      for (ProblemType type : ProblemType.values()) {
        problemss.put(new GenreAndType(genre, type), new IntArray());
      }
    }

    try {
      database.processProblemMinimums(new ProblemMinimumProcessable() {
        @Override
        public void process(PacketProblemMinimum problem) throws Exception {
          List<ProblemGenre> genres = ImmutableList.of(ProblemGenre.Random, problem.genre);
          List<ProblemType> types = ImmutableList.of(ProblemType.Random, problem.type,
              ProblemType.fromRandomFlag(problem.randomFlag.getIndex()));
          for (ProblemGenre genre : genres) {
            for (ProblemType type : types) {
              problemss.get(new GenreAndType(genre, type)).add(problem.id);
            }
          }
        }
      });
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "問題リストの読み込みに失敗しました", e);
    }

    updateReport();

    threadPool.addHourTask(new Runnable() {
      public void run() {
        updateReport();
      }
    });
  }

  public PacketProblemMinimum selectProblem(Set<ProblemGenre> genres, Set<ProblemType> types,
      int classLevel, int difficultSelect, Set<Integer> selectedProblemIds, boolean first,
      NewAndOldProblems newAndOldProblems, boolean tegaki, Set<Integer> userCodes,
      Set<Integer> createrHashes) throws Exception {
    initializeIfNotInitialized();

    // ジャンルの決定
    if (genres.contains(ProblemGenre.Random)) {
      // ノンジャンルが含まれている場合はノンジャンルを除いた全ジャンルから出題する
      genres = EnumSet.complementOf(EnumSet.of(ProblemGenre.Random));
    }

    // 出題形式の決定
    if (types.contains(ProblemType.Random)) {
      // ランダムが含まれている場合はランダムを除いた全ジャンルから出題する
      types = EnumSet.complementOf(EnumSet.of(ProblemType.Random));
    }

    // 手書きクイズは出題見合わせ
    if (!tegaki) {
      types.remove(ProblemType.Tegaki);
      if (types.isEmpty()) {
        types = EnumSet.complementOf(EnumSet.of(ProblemType.Random, ProblemType.Tegaki));
      }
    }

    // 難易度調整
    switch (difficultSelect) {
    case Constant.DIFFICULT_SELECT_NORMAL:
      classLevel = Constant.CLASS_LEVEL_NORMAL;
      break;
    case Constant.DIFFICULT_SELECT_DIFFICULT:
      classLevel = Constant.CLASS_LEVEL_DIFFICULT;
      break;
    case Constant.DIFFICULT_SELECT_LITTLE_DIFFICULT:
      classLevel = Constant.CLASS_LEVEL_LITTLE_DIFFICULT;
      break;
    case Constant.DIFFICULT_SELECT_LITTLE_EASY:
      classLevel = Constant.CLASS_LEVEL_LITTLE_EASY;
      break;
    case Constant.DIFFICULT_SELECT_EASY:
      classLevel = Constant.CLASS_LEVEL_EASY;
      break;

    }

    // 問題の選択
    PacketProblemMinimum data = null;
    for (int findLoop = 0; findLoop < MAX_FIND_LOOP && data == null; ++findLoop) {
      // 出題するジャンルと出題形式を決定する
      ProblemGenre selectedGenre;
      ProblemType selectedType;
      int genreAndTypeFindLoop = MAX_FIND_LOOP;
      GenreAndType genreAndType;
      IntArray problems;
      do {
        selectedGenre = selectFrom(genres, ProblemGenre.Random);
        selectedType = selectFrom(types, ProblemType.Random);
        genreAndType = new GenreAndType(selectedGenre, selectedType);
        problems = problemss.get(genreAndType);
      } while ((problems == null || problems.isEmpty()) && --genreAndTypeFindLoop > 0);

      // 問題が登録されていなかった場合の処理
      if (problems == null || problems.isEmpty() || genreAndTypeFindLoop == 0) {
        selectedGenre = ProblemGenre.Random;
        selectedType = ProblemType.Random;
      }

      data = selectProblemFromList(problems, selectedProblemIds, classLevel, newAndOldProblems,
          tegaki, userCodes, createrHashes, true);

      // ランダムの場合はtegakiフラグにかかわらず手書きクイズを出さない
      if (data != null
          && !Sets.intersection(types, EnumSet.range(ProblemType.Random1, ProblemType.Ranodm5))
              .isEmpty() && data.type == ProblemType.Tegaki) {
        data = null;
      }
    }

    if (data == null) {
      // 問題が選択されなかった場合は条件を緩めて選択しなおす

      if (!userCodes.isEmpty()) {
        // ユーザーコードを空にして再度選択
        Set<Integer> userCodes2 = Sets.newHashSet();
        data = selectProblem(genres, types, classLevel, difficultSelect, selectedProblemIds, true,
            newAndOldProblems, tegaki, userCodes2, createrHashes);
        userCodes.addAll(userCodes2);

      } else if (!createrHashes.isEmpty()) {
        // 作者名ハッシュを空にして再度選択
        Set<Integer> createrHashes2 = new HashSet<Integer>();
        data = selectProblem(genres, types, classLevel, difficultSelect, selectedProblemIds, true,
            newAndOldProblems, tegaki, userCodes, createrHashes2);
        createrHashes.addAll(createrHashes2);

      } else if (newAndOldProblems != NewAndOldProblems.Both) {
        // 新問と旧問から再度選択
        data = selectProblem(genres, types, classLevel, difficultSelect, selectedProblemIds, true,
            NewAndOldProblems.Both, tegaki, userCodes, createrHashes);

      } else if (difficultSelect != Constant.DIFFICULT_SELECT_NORMAL) {
        // 難易度を広げて再度選択
        data = selectProblem(genres, types, classLevel, Constant.DIFFICULT_SELECT_NORMAL,
            selectedProblemIds, true, newAndOldProblems, tegaki, userCodes, createrHashes);

      } else if (!types.equals(EnumSet.complementOf(EnumSet.of(ProblemType.Random)))) {
        // 出題形式を変更して再度選択
        data = selectProblem(genres, EnumSet.of(ProblemType.Random), classLevel, difficultSelect,
            selectedProblemIds, true, newAndOldProblems, tegaki, userCodes, createrHashes);

      } else if (!genres.equals(EnumSet.complementOf(EnumSet.of(ProblemGenre.Random)))) {
        // ジャンルを変えて再度選択
        data = selectProblem(EnumSet.of(ProblemGenre.Random), EnumSet.of(ProblemType.Random),
            classLevel, difficultSelect, selectedProblemIds, true, newAndOldProblems, tegaki,
            userCodes, createrHashes);

      } else if (first) {
        // もう一度だけ選択
        data = selectProblem(EnumSet.of(ProblemGenre.Random), EnumSet.of(ProblemType.Random),
            classLevel, difficultSelect, selectedProblemIds, false, newAndOldProblems, tegaki,
            userCodes, createrHashes);

      } else {
        throw new Exception(
            String
                .format(
                    "問題が見つかりませんでした genre:%s type:%s classLevel:%d difficultSelect:%d selectedProblemIds:%s, first:%b newAndOldProblems:%s, tegaki:%b, userCodes:%s createrHashes:%s",
                    genres.toString(), types.toString(), classLevel, difficultSelect,
                    selectedProblemIds, first, newAndOldProblems.toString(), tegaki,
                    userCodes.toString(), createrHashes.toString()));
      }
    }

    selectedProblemIds.add(data.id);
    createrHashes.add(data.creatorHash);
    userCodes.add(data.userCode);

    return data;
  }

  private <T extends Enum<T>> T selectFrom(Set<T> set, T randomEnum) {
    if (set.isEmpty()) {
      return randomEnum;
    }

    return ImmutableList.copyOf(set).get(random.nextInt(set.size()));
  }

  public synchronized int addProblem(PacketProblem data) throws DatabaseException {
    initializeIfNotInitialized();

    data.id = database.addProblem(data);

    for (ProblemGenre genre : Arrays.asList(ProblemGenre.Random, data.genre)) {
      for (ProblemType type : Arrays.asList(ProblemType.Random, data.type,
          ProblemType.fromRandomFlag(data.randomFlag.getIndex()))) {
        problemss.get(new GenreAndType(genre, type)).add(data.id);
      }
    }

    return data.id;
  }

  public synchronized int getNumberOfProblem() {
    initializeIfNotInitialized();

    return problemss.get(new GenreAndType(ProblemGenre.Random, ProblemType.Random)).size();
  }

  public synchronized void updateProblem(PacketProblem data) throws DatabaseException {
    initializeIfNotInitialized();

    PacketProblemMinimum dataMinimum = new PacketProblemMinimum();
    int id = dataMinimum.id = data.id;
    dataMinimum.genre = data.genre;
    dataMinimum.type = data.type;
    dataMinimum.good = data.good;
    dataMinimum.bad = data.bad;
    dataMinimum.randomFlag = data.randomFlag;

    for (IntArray problemIds : problemss.values()) {
      problemIds.removeElementAndFillWithLastElement(id);
    }

    for (ProblemGenre genre : Arrays.asList(ProblemGenre.Random, data.genre)) {
      for (ProblemType type : Arrays.asList(ProblemType.Random, data.type,
          ProblemType.fromRandomFlag(data.randomFlag.getIndex()))) {
        problemss.get(new GenreAndType(genre, type)).add(data.id);
      }
    }

    database.updateProblem(data);
  }

  /**
   * 正答数/回答数のみ更新する
   * 
   * @param data
   * @throws DatabaseException
   */
  public synchronized void updateMinimumProblem(PacketProblemMinimum data) throws DatabaseException {
    initializeIfNotInitialized();
    database.updateMinimumProblem(data);
  }

  private void updateReport() {
    int[][] tableProblemCount = new int[ProblemGenre.values().length][ProblemType.values().length];
    int[][] tableProblemRatio = new int[ProblemGenre.values().length][11];

    IntArray problemIds = problemss.get(new GenreAndType(ProblemGenre.Random, ProblemType.Random));
    for (int problemId : problemIds.data()) {
      PacketProblemMinimum problem = null;
      try {
        problem = database.getProblemMinimum(problemId);
      } catch (DatabaseException e) {
        logger.log(Level.WARNING, "問題の読み込みに失敗しました", e);
      }

      for (ProblemGenre genre : Arrays.asList(ProblemGenre.Random, problem.genre)) {
        for (ProblemType type : Arrays.asList(ProblemType.Random, problem.type,
            ProblemType.fromRandomFlag(problem.randomFlag.getIndex()))) {
          ++tableProblemCount[genre.ordinal()][type.ordinal()];
        }
      }

      int index;
      int rate = problem.getAccuracyRate();
      if (rate == -1) {
        index = Constant.REPORT_NOT_YET;
      } else {
        index = rate / 10;
        index = Math.min(9, index);
      }
      ++tableProblemRatio[0][index];
      ++tableProblemRatio[problem.genre.ordinal()][index];
    }

    this.tableProblemCount = tableProblemCount;
    this.tableProblemRatio = tableProblemRatio;
  }

  public int[][] getTableProblemCount() {
    initializeIfNotInitialized();

    // 代入だけなので同期しない
    return tableProblemCount;
  }

  public int[][] getTableProblemRatio() {
    initializeIfNotInitialized();

    // 代入だけなので同期しない
    return tableProblemRatio;
  }

}
