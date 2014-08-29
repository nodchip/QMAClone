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
package tv.dyndns.kishibe.qmaclone.server.database;

import static com.google.common.base.Strings.emptyToNull;
import static tv.dyndns.kishibe.qmaclone.client.constant.Constant.MAX_NUMBER_OF_ANSWERS;
import static tv.dyndns.kishibe.qmaclone.client.constant.Constant.MAX_NUMBER_OF_CHOICES;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.AbstractKeyedHandler;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.joda.time.DateTime;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsResponse;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsThread;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMonth;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor.ThemeModeEditorStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData.WebSocketUsage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.PageView;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class DirectDatabase implements Database {

  private static final Logger logger = Logger.getLogger(DirectDatabase.class.toString());
  private static final int MAX_PLAYER_ANSWER_LENGTH = 255;
  private final Database cachedDatabase;
  @VisibleForTesting
  final QueryRunner runner;
  private final FullTextSearch fullTextSearch;
  private final WrongAnswerHandler wrongAnswerHandler;

  @Inject
  public DirectDatabase(QueryRunner queryRunner, @Nullable Database cachedDatabase,
      @Nullable FullTextSearch fullTextSearch, @Nullable WrongAnswerHandler wrongAnswerHandler) {
    this.runner = Preconditions.checkNotNull(queryRunner);
    this.cachedDatabase = cachedDatabase;
    this.fullTextSearch = fullTextSearch;
    this.wrongAnswerHandler = wrongAnswerHandler;
  }

  @Override
  public void addProblemIdsToReport(int userCode, List<Integer> problemIds)
      throws DatabaseException {
    List<Object[]> params = Lists.newArrayList();
    for (int problemId : problemIds) {
      params.add(new Object[] { userCode, problemId });
    }

    try {
      runner.batch("INSERT IGNORE INTO report_problem (USER_CODE, PROBLEM_ID) VALUES (?, ?)",
          params.toArray(new Object[0][]));
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void removeProblemIdFromReport(int userCode, int problemID) throws DatabaseException {
    try {
      runner.update("DELETE IGNORE FROM report_problem WHERE USER_CODE = ? AND PROBLEM_ID = ?",
          userCode, problemID);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void clearProblemIdFromReport(int userCode) throws DatabaseException {
    try {
      runner.update("DELETE IGNORE FROM report_problem WHERE USER_CODE = ?", userCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketProblem> getUserProblemReport(int userCode) throws DatabaseException {
    try {
      return runner
          .query(
              "SELECT * FROM problem, report_problem WHERE report_problem.USER_CODE = ? AND problem.ID = report_problem.PROBLEM_ID",
              problemHandler, userCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<List<PacketProblem>> problemHandler = new AbstractListHandler<PacketProblem>() {
    @Override
    protected PacketProblem handleRow(ResultSet rs) throws SQLException {
      return toPacketProblem(rs);
    }
  };

  private static PacketProblem toPacketProblem(ResultSet resultSet) throws SQLException {
    PacketProblem data = new PacketProblem();
    data.id = resultSet.getInt("problem.ID");
    int genreIndex = resultSet.getInt("problem.GENRE");
    Preconditions.checkState(0 <= genreIndex && genreIndex < ProblemGenre.values().length,
        "ジャンル番号が範囲外です: problemId=%d genreIndex=%d", data.id, data.genre);
    data.genre = ProblemGenre.values()[genreIndex];
    int typeIndex = resultSet.getInt("problem.TYPE");
    Preconditions.checkState(0 <= typeIndex && typeIndex < ProblemType.values().length,
        "問題形式番号が範囲外です: problemId=%d typeIndex=%d", data.id, typeIndex);
    data.type = ProblemType.values()[typeIndex];
    data.sentence = resultSet.getString("problem.SENTENCE");
    data.answers = new String[MAX_NUMBER_OF_ANSWERS];
    data.choices = new String[MAX_NUMBER_OF_CHOICES];
    for (int i = 0; i < MAX_NUMBER_OF_ANSWERS; ++i) {
      data.answers[i] = emptyToNull(resultSet.getString("problem.ANSWER" + i));
      data.choices[i] = emptyToNull(resultSet.getString("problem.CHOICE" + i));
    }
    data.good = resultSet.getInt("problem.GOOD");
    data.bad = resultSet.getInt("problem.BAD");
    data.creator = resultSet.getString("problem.CREATER");
    data.note = resultSet.getString("problem.NOTE");
    data.imageAnswer = resultSet.getBoolean("IMAGE_ANSWER");
    data.imageChoice = resultSet.getBoolean("IMAGE_CHOICE");
    int randomFlagIndex = resultSet.getInt("problem.RANDOM_FLAG");
    Preconditions.checkState(0 <= randomFlagIndex && randomFlagIndex < RandomFlag.values().length,
        "ランダムフラグ番号が範囲外です: problemId=%d randomFlagIndex=%d", data.id, randomFlagIndex);
    data.randomFlag = RandomFlag.values()[randomFlagIndex];
    data.voteGood = resultSet.getInt("problem.VOTE_GOOD");
    data.voteBad = resultSet.getInt("problem.VOTE_BAD");
    data.imageUrl = resultSet.getString("problem.IMAGE_URL");
    data.movieUrl = resultSet.getString("problem.MOVIE_URL");
    long indication = resultSet.getLong("problem.INDICATION");
    data.indication = indication == 0 ? null : new Date(indication);
    data.indicationMessage = resultSet.getString("problem.INDICATION_MESSAGE");
    long indicationResolved = resultSet.getLong("problem.INDICATION_RESOLVED");
    if (indicationResolved < System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000) {
      indicationResolved = 0;
    }
    data.indicationResolved = indicationResolved == 0 ? null : new Date(indicationResolved);
    data.numberOfDisplayedChoices = resultSet.getInt("problem.NUMBER_OF_DISPLAYED_CHOICES");
    // prepareShuffledData()はGame側で呼び出す
    return data;
  }

  private static PacketProblemMinimum toPacketProblemMinimum(ResultSet resultSet)
      throws SQLException {
    PacketProblemMinimum data = new PacketProblemMinimum();
    data.id = resultSet.getInt("problem.ID");
    int genreIndex = resultSet.getInt("problem.GENRE");
    Preconditions.checkState(0 <= genreIndex && genreIndex < ProblemGenre.values().length,
        "ジャンル番号が範囲外です: problemId=%d genreIndex=%d", data.id, data.genre);
    data.genre = ProblemGenre.values()[genreIndex];
    int typeIndex = resultSet.getInt("problem.TYPE");
    Preconditions.checkState(0 <= typeIndex && typeIndex < ProblemType.values().length,
        "問題形式番号が範囲外です: problemId=%d typeIndex=%d", data.id, typeIndex);
    data.type = ProblemType.values()[typeIndex];
    data.good = resultSet.getInt("problem.GOOD");
    data.bad = resultSet.getInt("problem.BAD");
    int randomFlagIndex = resultSet.getInt("problem.RANDOM_FLAG");
    Preconditions.checkState(0 <= randomFlagIndex && randomFlagIndex < RandomFlag.values().length,
        "ランダムフラグ番号が範囲外です: problemId=%d randomFlagIndex=%d", data.id, randomFlagIndex);
    data.randomFlag = RandomFlag.values()[randomFlagIndex];
    data.creatorHash = resultSet.getString("problem.CREATER").hashCode();
    long indication = resultSet.getLong("problem.INDICATION");
    data.indication = indication == 0 ? null : new Date(indication);
    return data;
  }

  @VisibleForTesting
  static final AbstractListHandler<PacketUserData> userDataHandler = new AbstractListHandler<PacketUserData>() {
    @Override
    protected PacketUserData handleRow(ResultSet rs) throws SQLException {
      PacketUserData data = new PacketUserData();
      data.userCode = rs.getInt("USER_CODE");
      data.playerName = rs.getString("NAME");
      data.greeting = rs.getString("GREETING");
      data.highScore = rs.getInt("HIGH_SCORE");
      data.averageScore = rs.getInt("AVERAGE_SCORE");
      data.playCount = rs.getInt("PLAY_COUNT");
      data.rating = rs.getInt("VICTORY_POINT");
      data.levelName = rs.getInt("LEVEL_NAME");
      data.levelNumber = rs.getInt("LEVEL_NUMBER");
      data.averageRank = rs.getFloat("AVERAGE_RANK");
      data.genres = ProblemGenre.fromBitFlag(rs.getInt("GENRE"));
      data.types = ProblemType.fromBitFlag(rs.getInt("TYPE"));
      data.classLevel = rs.getInt("CLASS_LEVEL");
      data.imageFileName = rs.getString("IMAGE_FILE_NAME");
      data.playSound = rs.getBoolean("PLAY_SOUND");
      data.multiGenre = rs.getBoolean("MULTI_GENRE");
      data.multiType = rs.getBoolean("MULTI_TYPE");
      data.difficultSelect = rs.getInt("DIFFICULT_SELECT");
      data.rankingMove = rs.getBoolean("RANKING_MOVE");
      data.bbsDispInfo = rs.getInt("BBS_DISP_INFO");
      data.bbsAge = rs.getBoolean("BBS_AGE");
      data.timerMode = rs.getInt("TIMER_MODE");
      data.prefecture = rs.getInt("PREFECTURE");
      data.chat = rs.getBoolean("CHAT");
      data.newAndOldProblems = NewAndOldProblems.values()[rs.getInt("NEW_AND_OLD")];
      data.publicEvent = rs.getBoolean("PUBLIC_EVENT");
      data.hideAnswer = rs.getBoolean("HIDE_ANSWER");
      data.showInfo = rs.getBoolean("SHOW_INFO");
      data.reflectEventResult = rs.getBoolean("REFLECT_EVENT_RESULT");
      data.webSocketUsage = WebSocketUsage.values()[rs.getInt("WEB_SOCKET_USAGE")];
      data.volatility = rs.getInt("VOLATILITY");
      data.qwertyHiragana = rs.getBoolean("QWERTY_HIRAGANA");
      data.qwertyKatakana = rs.getBoolean("QWERTY_KATAKANA");
      data.qwertyAlphabet = rs.getBoolean("QWERTY_ALPHABET");
      data.registerCreatedProblem = rs.getBoolean("REGISTER_CREATED_PROBLEM");
      data.registerIndicatedProblem = rs.getBoolean("REGISTER_INDICATED_PROBLEM");
      data.googlePlusId = rs.getString("GOOGLE_PLUS_ID");

      String correctCountCsv = rs.getString("CORRECT_COUNT");
      if (correctCountCsv != null) {
        int[][][] cc = new int[ProblemGenre.values().length][ProblemType.values().length][2];
        String[] rows = correctCountCsv.split("\n");
        for (int rowIndex = 0; rowIndex < rows.length; ++rowIndex) {
          String[] elements = rows[rowIndex].split(",");

          // 出題形式
          int randomElementIndex = elements.length - ProblemType.numberOfRandoms * 2;
          for (int elementIndex = 0; elementIndex < randomElementIndex; ++elementIndex) {
            int columnIndex = elementIndex / 2;
            int goodBad = elementIndex % 2;
            cc[rowIndex][columnIndex][goodBad] = Integer.parseInt(elements[elementIndex]);
          }

          // ランダム
          for (int elementIndex = randomElementIndex; elementIndex < elements.length; ++elementIndex) {
            int columnIndex = ProblemType.values().length - (elements.length - elementIndex + 1)
                / 2;
            int goodBad = elementIndex % 2;
            cc[rowIndex][columnIndex][goodBad] = Integer.parseInt(elements[elementIndex]);
          }
        }
        data.correctCount = cc;
      }

      return data;
    }
  };

  @Override
  public PacketUserData getUserData(int userCode) throws DatabaseException {
    try {
      List<PacketUserData> userDataList = runner.query("SELECT * FROM player WHERE USER_CODE = ?",
          userDataHandler, userCode);

      if (userDataList.isEmpty()) {
        return new PacketUserData();
      }

      PacketUserData userData = userDataList.get(0);
      userData.ignoreUserCodes = Sets.newHashSet(runner.query(
          "SELECT TARGET_USER_CODE FROM ignore_id WHERE USER_CODE = ?",
          new ColumnListHandler<Integer>(Integer.class), userCode));
      userData.ignoreUserCodes.addAll(cachedDatabase.getServerIgnoreUserCode());
      return userData;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void setUserData(PacketUserData data) throws DatabaseException {
    // correctCountをcsv形式に変換する
    StringBuilder sb = new StringBuilder();
    for (ProblemGenre genre : ProblemGenre.values()) {
      if (genre.getIndex() != 0) {
        sb.append("\n");
      }

      for (ProblemType type : ProblemType.values()) {
        if (type.getIndex() != 0) {
          sb.append(",");
        }

        sb.append(data.correctCount[genre.getIndex()][type.getIndex()][0]).append(",")
            .append(data.correctCount[genre.getIndex()][type.getIndex()][1]);
      }
    }

    try {
      runner
          .update(
              "REPLACE INTO player (USER_CODE, NAME, GREETING, HIGH_SCORE, AVERAGE_SCORE, PLAY_COUNT, VICTORY_POINT, LEVEL_NAME, LEVEL_NUMBER, AVERAGE_RANK, GENRE, TYPE, CLASS_LEVEL, IMAGE_FILE_NAME, PLAY_SOUND, MULTI_GENRE, MULTI_TYPE, DIFFICULT_SELECT, RANKING_MOVE, LAST_LOGIN, BBS_DISP_INFO, BBS_AGE, TIMER_MODE, PREFECTURE, CHAT, NEW_AND_OLD, PUBLIC_EVENT, HIDE_ANSWER, SHOW_INFO, REFLECT_EVENT_RESULT, WEB_SOCKET_USAGE, CORRECT_COUNT, VOLATILITY, QWERTY_HIRAGANA, QWERTY_KATAKANA, QWERTY_ALPHABET, REGISTER_CREATED_PROBLEM, REGISTER_INDICATED_PROBLEM, GOOGLE_PLUS_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
              data.userCode, Strings.nullToEmpty(data.playerName), Strings
                  .nullToEmpty(data.greeting), data.highScore, data.averageScore, data.playCount,
              data.rating, data.levelName, data.levelNumber, data.averageRank, ProblemGenre
                  .toBitFlag(data.genres), ProblemType.toBitFlag(data.types), data.classLevel,
              Strings.isNullOrEmpty(data.imageFileName) ? Constant.ICON_NO_IMAGE
                  : data.imageFileName, data.playSound, data.multiGenre, data.multiType,
              data.difficultSelect, data.rankingMove, new Timestamp(System.currentTimeMillis()),
              data.bbsDispInfo, data.bbsAge, data.timerMode, data.prefecture, data.chat,
              data.newAndOldProblems.ordinal(), data.publicEvent, data.hideAnswer, data.showInfo,
              data.reflectEventResult, data.webSocketUsage.getIndex(), sb.toString(),
              data.volatility, data.qwertyHiragana, data.qwertyKatakana, data.qwertyAlphabet,
              data.registerCreatedProblem, data.registerIndicatedProblem, data.googlePlusId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketUserData> lookupUserCodeByGooglePlusId(String googlePlusId)
      throws DatabaseException {
    try {
      return runner.query("SELECT * FROM player WHERE GOOGLE_PLUS_ID = ?", userDataHandler,
          googlePlusId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void disconnectUserCodeFromGooglePlus(int userCode) throws DatabaseException {
    try {
      runner.update("UPDATE player SET GOOGLE_PLUS_ID = NULL WHERE USER_CODE = ?", userCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final String KEY_SESSION = "SESSION";
  private static final String KEY_PLAY = "PLAY";
  private static final String KEY_PAGE_VIEW = "page_view";

  @Override
  public PageView loadPageView() throws DatabaseException {
    try {
      return runner.query("SELECT * FROM page_view", pageViewHandler);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<PageView> pageViewHandler = new ResultSetHandler<PageView>() {
    @Override
    public PageView handle(ResultSet resultSet) throws SQLException {
      PageView pageView = new PageView();
      while (resultSet.next()) {
        String type = resultSet.getString("TYPE");
        int number = resultSet.getInt("COUNT");

        if (type.equals(KEY_SESSION)) {
          pageView.numberOfSessions = number;
        } else if (type.equals(KEY_PLAY)) {
          pageView.numberOfPlayers = number;
        } else if (type.equals(KEY_PAGE_VIEW)) {
          pageView.numberOfPageView = number;
        }
      }
      return pageView;
    }
  };

  @Override
  public void savePageView(PageView pageView) throws DatabaseException {
    List<Object[]> params = Lists.newArrayList();
    params.add(new Object[] { KEY_SESSION, pageView.numberOfSessions });
    params.add(new Object[] { KEY_PLAY, pageView.numberOfPlayers });
    params.add(new Object[] { KEY_PAGE_VIEW, pageView.numberOfPageView });
    try {
      runner.batch("REPLACE INTO page_view (TYPE, COUNT) VALUES (?, ?)",
          params.toArray(new Object[0][]));
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public Map<String, IntArray> getThemeToProblems(Map<String, List<String>> themeAndQueryStrings)
      throws DatabaseException {
    return fullTextSearch.getThemeModeProblemMinimums(themeAndQueryStrings);
  }

  private final Object lockAddProblem = new Object();

  // 戻り値は問題番号
  @Override
  public int addProblem(PacketProblem problem) throws DatabaseException {
    synchronized (lockAddProblem) {
      try {
        problem.id = runner.query("SELECT MAX(ID) FROM problem", new ScalarHandler<Integer>()) + 1;

        String[] answers = Arrays.copyOf(problem.answers, MAX_NUMBER_OF_ANSWERS);
        String[] choices = Arrays.copyOf(problem.choices, MAX_NUMBER_OF_CHOICES);
        for (int i = 0; i < MAX_NUMBER_OF_ANSWERS; ++i) {
          answers[i] = emptyToNull(answers[i]);
          choices[i] = emptyToNull(choices[i]);
        }

        long indication = problem.indication == null ? 0 : problem.indication.getTime();
        long indicationResolved = problem.indicationResolved == null ? 0
            : problem.indicationResolved.getTime();
        runner
            .update(
                "INSERT INTO problem (ID, GENRE, TYPE, SENTENCE, ANSWER0, ANSWER1, ANSWER2, ANSWER3, ANSWER4, ANSWER5, ANSWER6, ANSWER7, CHOICE0, CHOICE1, CHOICE2, CHOICE3, CHOICE4, CHOICE5, CHOICE6, CHOICE7, GOOD, BAD, CREATER, NOTE, IMAGE_ANSWER, IMAGE_CHOICE, RANDOM_FLAG, IMAGE_URL, MOVIE_URL, INDICATION, INDICATION_MESSAGE, INDICATION_RESOLVED, NUMBER_OF_DISPLAYED_CHOICES) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                problem.id, problem.genre.getIndex(), problem.type.getIndex(), problem.sentence,
                answers[0], answers[1], answers[2], answers[3], answers[4], answers[5], answers[6],
                answers[7], choices[0], choices[1], choices[2], choices[3], choices[4], choices[5],
                choices[6], choices[7], problem.good, problem.bad, problem.creator, problem.note,
                problem.imageAnswer, problem.imageChoice, problem.randomFlag.getIndex(),
                problem.imageUrl, problem.movieUrl, indication, problem.indicationMessage,
                indicationResolved, problem.numberOfDisplayedChoices);

        fullTextSearch.addProblem(problem);
        return problem.id;
      } catch (SQLException | IOException e) {
        throw new DatabaseException(e);
      }
    }
  }

  @Override
  public void updateProblem(PacketProblem problem) throws DatabaseException {
    String[] answers = Arrays.copyOf(problem.answers, MAX_NUMBER_OF_ANSWERS);
    String[] choices = Arrays.copyOf(problem.choices, MAX_NUMBER_OF_CHOICES);
    for (int i = 0; i < MAX_NUMBER_OF_ANSWERS; ++i) {
      answers[i] = emptyToNull(answers[i]);
      choices[i] = emptyToNull(choices[i]);
    }

    long indication = problem.indication == null ? 0L : problem.indication.getTime();
    long indicationResolved = problem.indicationResolved == null ? 0L : problem.indicationResolved
        .getTime();
    try {
      runner
          .update(
              "UPDATE problem SET GENRE = ?, TYPE = ?, SENTENCE = ?, ANSWER0 = ?, ANSWER1 = ?, ANSWER2 = ?, ANSWER3 = ?, ANSWER4 = ?, ANSWER5 = ?, ANSWER6 = ?, ANSWER7 = ?, CHOICE0 = ?, CHOICE1 = ?, CHOICE2 = ?, CHOICE3 = ?, CHOICE4 = ?, CHOICE5 = ?, CHOICE6 = ?, CHOICE7 = ?, GOOD = ?, BAD = ?, CREATER = ?, NOTE = ?, IMAGE_ANSWER = ?, IMAGE_CHOICE = ?, RANDOM_FLAG = ?, VOTE_GOOD = ?, VOTE_BAD = ?, IMAGE_URL = ?, MOVIE_URL = ?, INDICATION = ?, INDICATION_MESSAGE = ?, INDICATION_RESOLVED = ?, NUMBER_OF_DISPLAYED_CHOICES = ? WHERE ID = ?",
              problem.genre.getIndex(), problem.type.getIndex(), problem.sentence, answers[0],
              answers[1], answers[2], answers[3], answers[4], answers[5], answers[6], answers[7],
              choices[0], choices[1], choices[2], choices[3], choices[4], choices[5], choices[6],
              choices[7], problem.good, problem.bad, problem.creator, problem.note,
              problem.imageAnswer, problem.imageChoice, problem.randomFlag.getIndex(),
              problem.voteGood, problem.voteBad, problem.imageUrl, problem.movieUrl, indication,
              problem.indicationMessage, indicationResolved, problem.numberOfDisplayedChoices,
              problem.id);

      fullTextSearch.updateProblem(problem);
    } catch (SQLException | IOException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void updateMinimumProblem(PacketProblemMinimum data) throws DatabaseException {
    try {
      runner
          .update(
              "UPDATE problem SET GENRE = ?, TYPE = ?, GOOD = ?, BAD = ?, RANDOM_FLAG = ? WHERE ID = ?",
              data.genre.getIndex(), data.type.getIndex(), data.good, data.bad,
              data.randomFlag.getIndex(), data.id);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketProblem> getProblem(Collection<Integer> ids) throws DatabaseException {
    if (ids.isEmpty()) {
      return Collections.emptyList();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("SELECT * FROM problem");
    sb.append(" WHERE ID IN (");
    sb.append(concat(ids));
    sb.append(')');
    try {
      return runner.query(sb.toString(), problemHandler);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketProblem> getIndicatedProblems() throws DatabaseException {
    try {
      return runner.query("SELECT * FROM problem WHERE INDICATION != 0 OR ? < INDICATION_RESOLVED",
          problemHandler, System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public PacketProblemMinimum getProblemMinimum(int problemId) throws DatabaseException {
    try {
      return runner.query(
          "SELECT ID, GENRE, TYPE, GOOD, BAD, RANDOM_FLAG FROM problem WHERE ID = ?",
          problemMinimumHandler, problemId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<PacketProblemMinimum> problemMinimumHandler = new ResultSetHandler<PacketProblemMinimum>() {
    @Override
    public PacketProblemMinimum handle(ResultSet rs) throws SQLException {
      if (rs.next()) {
        return toPacketProblemMinimum(rs);
      }
      return null;
    }
  };

  @Override
  public List<PacketProblem> searchProblem(String query, String creator,
      boolean creatorPerfectMatching, Set<ProblemGenre> genres, Set<ProblemType> types,
      Set<RandomFlag> randomFlags) throws DatabaseException {
    List<Integer> problemIds = fullTextSearch.searchProblem(query, creator, creatorPerfectMatching,
        genres, types, randomFlags);
    return getProblem(problemIds);
  }

  private String concat(Collection<?> objects) {
    StringBuilder sb = new StringBuilder();
    for (Object object : objects) {
      if (sb.length() != 0) {
        sb.append(", ");
      }
      sb.append(object.toString());
    }
    return sb.toString();
  }

  @Override
  public List<PacketProblem> searchSimilarProblemFromDatabase(PacketProblem problem)
      throws DatabaseException {
    List<Integer> problemIds = fullTextSearch.searchSimilarProblemFromDatabase(problem);
    return getProblem(problemIds);
  }

  @Override
  public void addPlayerAnswers(int problemId, ProblemType type, List<String> answers)
      throws DatabaseException {
    List<Object[]> params = Lists.newArrayList();
    for (String answer : answers) {
      if (answer.isEmpty()) {
        continue;
      }

      // 線結びの回答整理
      if (type == ProblemType.Senmusubi || type == ProblemType.Tato) {
        // 解答をソート後再度結合する
        List<String> split = Lists.newArrayList(Splitter.on(Constant.DELIMITER_GENERAL)
            .omitEmptyStrings().split(answer));
        Collections.sort(split);
        answer = Joiner.on(Constant.DELIMITER_GENERAL).join(split);
      }

      if (answer.length() > MAX_PLAYER_ANSWER_LENGTH) {
        answer = answer.substring(0, MAX_PLAYER_ANSWER_LENGTH);
      }

      params.add(new Object[] { problemId, answer });
    }
    try {
      runner
          .batch(
              "INSERT DELAYED player_answer (PROBLEM_ID, ANSWER, COUNT) VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE COUNT = COUNT + 1",
              params.toArray(new Object[0][]));
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketWrongAnswer> getPlayerAnswers(int problemId) throws DatabaseException {
    List<PacketWrongAnswer> list;
    try {
      list = runner.query(
          "SELECT ANSWER, COUNT FROM player_answer WHERE PROBLEM_ID = ? ORDER BY COUNT DESC",
          wrongAnswerHandler, problemId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }

    Map<String, PacketWrongAnswer> map = Maps.newTreeMap();
    for (PacketWrongAnswer answer : list) {
      if (map.containsKey(answer.answer)) {
        map.get(answer.answer).count += answer.count;
      } else {
        map.put(answer.answer, answer);
      }
    }

    return ImmutableList.copyOf(map.values());
  }

  @VisibleForTesting
  static class WrongAnswerHandler extends AbstractListHandler<PacketWrongAnswer> {
    @VisibleForTesting
    @Override
    protected PacketWrongAnswer handleRow(ResultSet rs) throws SQLException {
      PacketWrongAnswer wrongAnswer = new PacketWrongAnswer();
      wrongAnswer.answer = rs.getString("ANSWER");
      wrongAnswer.count = rs.getInt("COUNT");

      // 線結び以外は整形しない
      if (!wrongAnswer.answer.contains("---")) {
        return wrongAnswer;
      }

      // 解答をソート後再度結合する
      List<String> split = Lists.newArrayList(Splitter.on(Constant.DELIMITER_GENERAL)
          .omitEmptyStrings().split(wrongAnswer.answer));
      // 組み合わせクイズの古い回答を補正する
      split = Lists.newArrayList(Collections2.transform(split, new Function<String, String>() {
        @Override
        public String apply(String input) {
          // 選択肢・回答に「---」が含まれていないことを仮定する
          return input.replaceAll(Constant.DELIMITER_KUMIAWASE_PAIR, "---").replaceAll("---",
              Constant.DELIMITER_KUMIAWASE_PAIR);
        }
      }));
      Collections.sort(split);
      wrongAnswer.answer = Joiner.on(Constant.DELIMITER_GENERAL).join(split);

      return wrongAnswer;
    }
  }

  @Override
  public void removePlayerAnswers(int problemId) throws DatabaseException {
    try {
      runner.update("DELETE FROM player_answer WHERE PROBLEM_ID = ?", problemId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final String[] RANKING_QUERIES = {
      "SELECT USER_CODE, NAME, IMAGE_FILE_NAME, HIGH_SCORE AS DATA FROM player WHERE PLAY_COUNT >= 10 AND LAST_LOGIN >= ? ORDER BY DATA DESC LIMIT 0, ?",
      "SELECT USER_CODE, NAME, IMAGE_FILE_NAME, AVERAGE_SCORE AS DATA FROM player WHERE PLAY_COUNT >= 10 AND LAST_LOGIN >= ? ORDER BY DATA DESC LIMIT 0, ?",
      "SELECT USER_CODE, NAME, IMAGE_FILE_NAME, PLAY_COUNT AS DATA FROM player WHERE PLAY_COUNT >= 10 AND LAST_LOGIN >= ? ORDER BY DATA DESC LIMIT 0, ?",
      "SELECT USER_CODE, NAME, IMAGE_FILE_NAME, VICTORY_POINT AS DATA FROM player WHERE PLAY_COUNT >= 10 AND LAST_LOGIN >= ? ORDER BY DATA DESC LIMIT 0, ?",
      "SELECT USER_CODE, NAME, IMAGE_FILE_NAME, AVERAGE_RANK AS DATA FROM player WHERE PLAY_COUNT >= 10 AND LAST_LOGIN >= ? ORDER BY DATA ASC LIMIT 0, ?",
      "SELECT USER_CODE, NAME, IMAGE_FILE_NAME, CLASS_LEVEL AS DATA FROM player WHERE PLAY_COUNT >= 10 AND LAST_LOGIN >= ? ORDER BY DATA DESC LIMIT 0, ?" };

  @Override
  public List<List<PacketRankingData>> getGeneralRankingData() throws DatabaseException {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis()
        - (long) Constant.RANKING_DISPLAY_DAY * 24 * 60 * 60 * 1000);
    List<List<PacketRankingData>> result = Lists.newArrayList();
    try {
      for (String rankingQuery : RANKING_QUERIES) {
        List<PacketRankingData> ranking = runner.query(rankingQuery, rankingDataHandler, timestamp,
            Constant.NUMBER_OF_RANKING_DATA);
        for (int i = 0; i < ranking.size(); ++i) {
          ranking.get(i).ranking = i + 1;
        }
        result.add(ranking);
      }
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
    return result;
  }

  private static final ResultSetHandler<List<PacketRankingData>> rankingDataHandler = new AbstractListHandler<PacketRankingData>() {
    @Override
    protected PacketRankingData handleRow(ResultSet rs) throws SQLException {
      PacketRankingData rankingData = new PacketRankingData();
      rankingData.userCode = rs.getInt("USER_CODE");
      rankingData.name = rs.getString("NAME");
      rankingData.imageFileName = rs.getString("IMAGE_FILE_NAME");
      rankingData.data = rs.getString("DATA");
      return rankingData;
    }
  };

  @Override
  public void addCreationLog(PacketProblem problem, int userCode, String machineIp)
      throws DatabaseException {
    try {
      runner
          .update(
              "INSERT INTO creation_log (PROBLEM_ID, USER_CODE, DATE, MACHINE_IP, SUMMARY) VALUES (?, ?, ?, ?, ?)",
              problem.id, userCode, new Timestamp(System.currentTimeMillis()), machineIp,
              problem.toChangeSummary());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void addIgnoreUserCode(int userCode, int targetUserCode) throws DatabaseException {
    try {
      runner.update("INSERT INTO ignore_id (USER_CODE, TARGET_USER_CODE) VALUES (?, ?)", userCode,
          targetUserCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void removeIgnoreUserCode(int userCode, int targetUserCode) throws DatabaseException {
    try {
      runner.update("DELETE FROM ignore_id WHERE USER_CODE = ? AND TARGET_USER_CODE = ?", userCode,
          targetUserCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void addServerIgnoreUserCode(int userCode) throws DatabaseException {
    try {
      runner.update("INSERT IGNORE INTO ignore_id (USER_CODE, TARGET_USER_CODE) VALUES (?, ?)", 0,
          userCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public Set<Integer> getServerIgnoreUserCode() throws DatabaseException {
    try {
      return ImmutableSet.copyOf(runner.query(
          "SELECT TARGET_USER_CODE FROM ignore_id WHERE USER_CODE = ?",
          serverIgnoreUserCodeHandler, 0));
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<List<Integer>> serverIgnoreUserCodeHandler = new AbstractListHandler<Integer>() {
    @Override
    protected Integer handleRow(ResultSet rs) throws SQLException {
      return rs.getInt("TARGET_USER_CODE");
    }
  };

  @Override
  public void addChatLog(PacketChatMessage data) throws DatabaseException {
    try {
      runner
          .update(
              "INSERT IGNORE INTO chat_log (DATE, NAME, BODY, CLASS_LEVEL, USER_CODE, MACHINE_IP) VALUES (?, ?, ?, ?, ?, ?)",
              new Timestamp(System.currentTimeMillis()), data.name, data.body, data.classLevel,
              data.userCode, data.remoteAddress);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public Map<Integer, PacketChatMessage> getLatestChatData() throws DatabaseException {
    Map<Integer, PacketChatMessage> result;
    try {
      result = runner.query("SELECT * FROM chat_log ORDER BY RES_ID DESC LIMIT 100",
          latestChatDataHandler);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
    for (PacketChatMessage chatData : result.values()) {
      chatData.imageFileName = cachedDatabase.getUserData(chatData.userCode).imageFileName;
    }
    return result;
  }

  private static final AbstractKeyedHandler<Integer, PacketChatMessage> latestChatDataHandler = new AbstractKeyedHandler<Integer, PacketChatMessage>() {
    @Override
    protected PacketChatMessage createRow(ResultSet rs) throws SQLException {
      PacketChatMessage data = new PacketChatMessage();
      data.resId = rs.getInt("RES_ID");
      data.date = rs.getTimestamp("DATE").getTime();
      data.name = rs.getString("NAME");
      data.body = rs.getString("BODY");
      data.classLevel = rs.getInt("CLASS_LEVEL");
      data.userCode = rs.getInt("USER_CODE");
      data.remoteAddress = rs.getString("MACHINE_IP");
      return data;
    }

    @Override
    protected Integer createKey(ResultSet rs) throws SQLException {
      return rs.getInt("RES_ID");
    }
  };

  @Override
  public List<PacketProblemCreationLog> getProblemCreationHistory(int problemId)
      throws DatabaseException {
    try {
      return runner
          .query(
              "SELECT player.NAME, creation_log.USER_CODE, creation_log.DATE, creation_log.MACHINE_IP, creation_log.SUMMARY FROM player, creation_log WHERE creation_log.PROBLEM_ID = ? AND creation_log.USER_CODE = player.USER_CODE",
              problemCreationLogHandler, problemId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<List<PacketProblemCreationLog>> problemCreationLogHandler = new AbstractListHandler<PacketProblemCreationLog>() {
    @Override
    protected PacketProblemCreationLog handleRow(ResultSet rs) throws SQLException {
      PacketProblemCreationLog history = new PacketProblemCreationLog();
      history.name = rs.getString("player.NAME");
      history.userCode = rs.getInt("creation_log.USER_CODE");
      history.date = new Date(rs.getTimestamp("creation_log.DATE").getTime());
      history.ip = rs.getString("creation_log.MACHINE_IP");
      history.summary = rs.getString("creation_log.SUMMARY");
      return history;
    }
  };

  @Override
  public List<PacketBbsThread> getBbsThreads(int bbsId, int start, int count)
      throws DatabaseException {
    try {
      return runner.query(
          "SELECT id, title FROM bbs_thread WHERE bbsId = ? ORDER BY lastUpdate DESC LIMIT ?, ?",
          bbsThreadHandler, bbsId, start, count);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<List<PacketBbsThread>> bbsThreadHandler = new AbstractListHandler<PacketBbsThread>() {
    @Override
    protected PacketBbsThread handleRow(ResultSet rs) throws SQLException {
      PacketBbsThread thread = new PacketBbsThread();
      thread.id = rs.getLong("id");
      thread.title = rs.getString("title");
      return thread;
    }
  };

  private static final ResultSetHandler<List<PacketBbsResponse>> handlerBbsResponse = new AbstractListHandler<PacketBbsResponse>() {
    @Override
    protected PacketBbsResponse handleRow(ResultSet rs) throws SQLException {
      PacketBbsResponse response = new PacketBbsResponse();
      response.id = rs.getLong("id");
      response.threadId = rs.getLong("threadId");
      response.name = rs.getString("name");
      response.userCode = rs.getInt("userCode");
      response.remoteAddress = rs.getString("userCode");
      response.dispInfo = rs.getInt("dispInfo");
      response.postTime = rs.getLong("postTime");
      response.body = rs.getString("body");
      return response;
    }
  };

  @Override
  public List<PacketBbsResponse> getBbsResponses(int threadId, int count) throws DatabaseException {
    try {
      List<PacketBbsResponse> list = Lists.newArrayList();
      list.addAll(runner.query(
          "SELECT * FROM bbs_response WHERE threadId = ? ORDER BY id DESC LIMIT 0, ?",
          handlerBbsResponse, threadId, count));
      list.addAll(runner.query(
          "SELECT * FROM bbs_response WHERE threadId = ? ORDER BY id ASC LIMIT 0, 1",
          handlerBbsResponse, threadId));
      if (list.size() >= 2 && list.get(list.size() - 1).id == list.get(list.size() - 2).id) {
        list.remove(list.size() - 1);
      }
      Collections.reverse(list);
      return list;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private final Object lockBuildBbsThread = new Object();

  @Override
  public void buildBbsThread(int bbsId, PacketBbsThread thread, PacketBbsResponse response)
      throws DatabaseException {
    synchronized (lockBuildBbsThread) {
      try {
        runner.update("INSERT INTO bbs_thread (lastUpdate, title, bbsId) VALUES (?, ?, ?)",
            System.currentTimeMillis(), thread.title, bbsId);
        thread.id = runner.query("SELECT MAX(id) FROM bbs_thread", new ScalarHandler<BigInteger>())
            .longValue();
        response.threadId = thread.id;
        runner
            .update(
                "INSERT INTO bbs_response (threadId, name, userCode, machineIp, dispInfo, postTime, body) VALUES (?, ?, ?, ?, ?, ?, ?)",
                response.threadId, response.name, response.userCode, response.remoteAddress,
                response.dispInfo, System.currentTimeMillis(), response.body);
      } catch (SQLException e) {
        throw new DatabaseException(e);
      }
    }
  }

  @Override
  public void writeToBbs(PacketBbsResponse response, boolean age) throws DatabaseException {
    try {
      runner
          .update(
              "INSERT INTO bbs_response (threadId, name, userCode, machineIp, dispInfo, postTime, body) VALUES (?, ?, ?, ?, ?, ?, ?)",
              response.threadId, response.name, response.userCode, response.remoteAddress,
              response.dispInfo, System.currentTimeMillis(), response.body);
      if (age) {
        runner.update("UPDATE bbs_thread SET lastUpdate = ? WHERE id = ?",
            System.currentTimeMillis(), response.threadId);
      }
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public int getNumberOfBbsThread(int bbsId) throws DatabaseException {
    try {
      return (int) (long) runner.query("SELECT COUNT(*) FROM bbs_thread WHERE bbsId = ?",
          new ScalarHandler<Long>(), bbsId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public boolean isUsedUserCode(int userCode) throws DatabaseException {
    try {
      return 0 < runner.query("SELECT COUNT(*) FROM player WHERE USER_CODE = ?",
          new ScalarHandler<Long>(), userCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void addLinkData(PacketLinkData linkData) throws DatabaseException {
    try {
      runner
          .update(
              "INSERT INTO link (lastUpdate, homePageName, authorName, url, bannerUrl, description, userCode) VALUES (?, ?, ?, ?, ?, ?, ?)",
              System.currentTimeMillis(), linkData.homePageName, linkData.authorName, linkData.url,
              linkData.bannerUrl, linkData.description, linkData.userCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void updateLinkData(PacketLinkData linkData) throws DatabaseException {
    try {
      runner
          .update(
              "UPDATE link SET lastUpdate = ?, homePageName = ?, authorName = ?, url = ?, bannerUrl = ?, description = ?, userCode = ? WHERE id = ?",
              System.currentTimeMillis(), linkData.homePageName, linkData.authorName, linkData.url,
              linkData.bannerUrl, linkData.description, linkData.userCode, linkData.id);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void removeLinkData(int id) throws DatabaseException {
    try {
      runner.update("UPDATE link SET valid = FALSE WHERE id = ?", id);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketLinkData> getLinkDatas(int start, int count) throws DatabaseException {
    try {
      return runner.query(
          "SELECT * FROM link WHERE valid = TRUE ORDER BY lastUpdate DESC LIMIT ?, ?",
          linkDataHandler, start, count);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<List<PacketLinkData>> linkDataHandler = new AbstractListHandler<PacketLinkData>() {
    @Override
    protected PacketLinkData handleRow(ResultSet rs) throws SQLException {
      PacketLinkData linkData = new PacketLinkData();
      linkData.id = (int) rs.getLong("id");
      linkData.lastUpdate = rs.getLong("lastUpdate");
      linkData.homePageName = rs.getString("homePageName");
      linkData.authorName = rs.getString("authorName");
      linkData.url = rs.getString("url");
      linkData.bannerUrl = rs.getString("bannerUrl");
      linkData.description = rs.getString("description");
      linkData.userCode = rs.getInt("userCode");
      return linkData;
    }
  };

  @Override
  public int getNumberOfLinkDatas() throws DatabaseException {
    try {
      return (int) (long) runner.query("SELECT COUNT(*) FROM link WHERE valid = TRUE",
          new ScalarHandler<Long>());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public int getNumberOfActiveUsers() throws DatabaseException {
    long time = System.currentTimeMillis() - (long) Constant.RANKING_DISPLAY_DAY * 24 * 60 * 60
        * 1000;
    try {
      return (int) (long) runner.query(
          "SELECT COUNT(*) FROM player WHERE PLAY_COUNT >= 10 AND LAST_LOGIN >= ?",
          new ScalarHandler<Long>(), time);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public Map<Integer, List<Integer>> getRatingGroupedByPrefecture() throws DatabaseException {
    long time = System.currentTimeMillis() - (long) Constant.RANKING_DISPLAY_DAY * 24 * 60 * 60
        * 1000;
    try {
      return runner
          .query(
              "SELECT VICTORY_POINT, PREFECTURE FROM player WHERE PLAY_COUNT >= 10 AND LAST_LOGIN >= ? AND PREFECTURE != 0",
              ratingGroupedByPrefectureHandler, time);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<Map<Integer, List<Integer>>> ratingGroupedByPrefectureHandler = new ResultSetHandler<Map<Integer, List<Integer>>>() {
    @Override
    public Map<Integer, List<Integer>> handle(ResultSet resultSet) throws SQLException {
      Map<Integer, List<Integer>> result = Maps.newHashMap();
      while (resultSet.next()) {
        int rating = resultSet.getInt("VICTORY_POINT");
        int prefecture = resultSet.getInt("PREFECTURE");

        if (!result.containsKey(prefecture)) {
          result.put(prefecture, Lists.<Integer> newArrayList());
        }
        result.get(prefecture).add(rating);
      }
      return result;
    }
  };

  @Override
  public void addRatingHistory(int userCode, int rating) throws DatabaseException {
    try {
      runner.update("INSERT INTO rating_history (USER_CODE, TIME, RATING) VALUES (?, ?, ?)",
          userCode, System.currentTimeMillis(), rating);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<Integer> getRatingHistory(int userCode) throws DatabaseException {
    try {
      return runner.query(
          "SELECT RATING FROM rating_history WHERE USER_CODE = ? ORDER BY TIME DESC LIMIT 0, ?",
          new ColumnListHandler<Integer>(Integer.class), userCode, Constant.MAX_RATING_HISTORY);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<Integer> getWholeRating() throws DatabaseException {
    long time = System.currentTimeMillis() - (long) Constant.RANKING_DISPLAY_DAY * 24 * 60 * 60
        * 1000;
    try {
      return runner.query(
          "SELECT VICTORY_POINT FROM player WHERE PLAY_COUNT >= 10 AND LAST_LOGIN >= ?",
          new ColumnListHandler<Integer>(Integer.class), time);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketThemeQuery> getThemeModeQueries() throws DatabaseException {
    try {
      return runner.query("SELECT THEME AS theme, QUERY AS query FROM theme_mode",
          new BeanListHandler<PacketThemeQuery>(PacketThemeQuery.class));
    } catch (Exception e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketThemeQuery> getThemeModeQueries(String theme) throws DatabaseException {
    try {
      return runner.query("SELECT THEME AS theme, QUERY AS query FROM theme_mode WHERE THEME = ?",
          new BeanListHandler<PacketThemeQuery>(PacketThemeQuery.class), theme);
    } catch (Exception e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public int getNumberOfThemeQueries() throws DatabaseException {
    try {
      return (int) (long) runner
          .query("SELECT COUNT(*) FROM theme_mode", new ScalarHandler<Long>());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void addThemeModeQuery(String theme, String query) throws DatabaseException {
    try {
      runner.update("REPLACE theme_mode (THEME, QUERY) VALUES (?, ?)", theme, query);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void removeThemeModeQuery(String theme, String query) throws DatabaseException {
    try {
      runner.update("DELETE FROM theme_mode WHERE THEME = ? AND QUERY = ?", theme, query);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void voteToProblem(int problemId, boolean good, String feedback) throws DatabaseException {
    int voteGood = good ? 1 : 0;
    int voteBad = good ? 0 : 1;
    try {
      runner.update(
          "UPDATE problem SET VOTE_GOOD = VOTE_GOOD + ?, VOTE_BAD = VOTE_BAD + ? WHERE ID = ?",
          voteGood, voteBad, problemId);
      runner.update("INSERT problem_questionnaire (problemId, text, date) VALUES (?, ?, ?)",
          problemId, feedback, System.currentTimeMillis());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void resetVote(int problemId) throws DatabaseException {
    try {
      runner.update("UPDATE problem SET VOTE_GOOD = 0, VOTE_BAD = 0 WHERE ID = ?", problemId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void clearProblemFeedback(int problemId) throws DatabaseException {
    try {
      runner.update("UPDATE problem_questionnaire SET deleted = TRUE WHERE problemId = ?",
          problemId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<String> getProblemFeedback(int problemId) throws DatabaseException {
    try {
      return runner.query(
          "SELECT text FROM problem_questionnaire WHERE problemId = ? && deleted = FALSE",
          new ColumnListHandler<String>(String.class), problemId);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void processProblems(final ProblemProcessable processor) throws DatabaseException {
    try {
      runner.query("SELECT * FROM problem", new ResultSetHandler<Void>() {
        @Override
        public Void handle(ResultSet rs) throws SQLException {
          int counter = 0;
          while (rs.next()) {
            PacketProblem data = toPacketProblem(rs);

            try {
              processor.process(data);
            } catch (Exception e) {
              // スタックトレースが保持されなかったため、ここでログを出力する
              logger.log(Level.WARNING, "問題処理オブジェクト中で例外が投げられました", e);
              throw new SQLException(e);
            }

            if (++counter % 10000 == 0) {
              System.err.println(counter);
            }
          }

          return null;
        }
      });
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void processProblemMinimums(final ProblemMinimumProcessable processer)
      throws DatabaseException {
    String sql = "SELECT ID, GENRE, TYPE, GOOD, BAD, RANDOM_FLAG, CREATER, INDICATION FROM problem";
    try {
      runner.query(sql, new ResultSetHandler<Void>() {
        @Override
        public Void handle(ResultSet rs) throws SQLException {
          int counter = 0;
          while (rs.next()) {
            PacketProblemMinimum problem = toPacketProblemMinimum(rs);

            try {
              processer.process(problem);
            } catch (Exception e) {
              e.printStackTrace();
            }

            if (++counter % 10000 == 0) {
              System.err.println(counter);
            }
          }

          return null;
        }
      });
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketProblem> getLastestProblems() throws DatabaseException {
    try {
      return runner.query("SELECT * FROM problem ORDER BY ID DESC LIMIT 0, 100", problemHandler);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketThemeModeEditor> getThemeModeEditors() throws DatabaseException {
    try {
      return runner.query("SELECT player.USER_CODE, player.NAME, theme_mode_editor.status "
          + "FROM theme_mode_editor, player "
          + "WHERE theme_mode_editor.userCode = player.USER_CODE "
          + "ORDER BY theme_mode_editor.userCode ASC, theme_mode_editor.status DESC",
          themeModeEditorsHandler);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<List<PacketThemeModeEditor>> themeModeEditorsHandler = new AbstractListHandler<PacketThemeModeEditor>() {
    @Override
    protected PacketThemeModeEditor handleRow(ResultSet rs) throws SQLException {
      PacketThemeModeEditor editor = new PacketThemeModeEditor();
      editor.userCode = rs.getInt("player.USER_CODE");
      editor.name = rs.getString("player.NAME");
      editor.themeModeEditorStatus = ThemeModeEditorStatus.values()[rs
          .getInt("theme_mode_editor.status")];
      return editor;
    }
  };

  @Override
  public ThemeModeEditorStatus getThemeModeEditorsStatus(int userCode) throws DatabaseException {
    try {
      return runner.query("SELECT status FROM theme_mode_editor WHERE userCode = ?",
          themeModeEditorsStatusHandler, userCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<ThemeModeEditorStatus> themeModeEditorsStatusHandler = new ResultSetHandler<ThemeModeEditorStatus>() {
    @Override
    public ThemeModeEditorStatus handle(ResultSet resultSet) throws SQLException {
      if (resultSet.next()) {
        return ThemeModeEditorStatus.values()[resultSet.getInt("status")];
      }
      return null;
    }
  };

  @Override
  public void updateThemeModeEdtorsStatus(int userCode, ThemeModeEditorStatus status)
      throws DatabaseException {
    try {
      runner.update("REPLACE theme_mode_editor (userCode, status) VALUES (?, ?)", userCode,
          status.ordinal());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public String getPassword(String type) throws DatabaseException {
    try {
      return runner.query("SELECT password FROM password WHERE type = ?",
          new ScalarHandler<String>(), type);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public int getNumberOfChatLog() throws DatabaseException {
    try {
      return (int) (long) runner.query("SELECT COUNT(*) FROM chat_log", new ScalarHandler<Long>());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public int getChatLogId(int year, int month, int day, int hour, int minute, int second)
      throws DatabaseException {
    String s = String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute,
        second);
    try {
      return (int) runner.query("SELECT RES_ID FROM chat_log WHERE ? <= DATE LIMIT 1",
          new ScalarHandler<Integer>(), s);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketChatMessage> getChatLog(int start) throws DatabaseException {
    // ORDER BYを使うと精査が入って遅くなる
    try {
      return runner
          .query(
              "SELECT * FROM chat_log c, player p WHERE c.RES_ID >= ? AND c.USER_CODE = p.USER_CODE LIMIT ?",
              chatLogHandler, start, Constant.CHAT_MAX_RESPONSES);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<List<PacketChatMessage>> chatLogHandler = new AbstractListHandler<PacketChatMessage>() {
    @Override
    protected PacketChatMessage handleRow(ResultSet rs) throws SQLException {
      PacketChatMessage data = new PacketChatMessage();
      data.resId = rs.getInt("c.RES_ID");
      data.date = rs.getTimestamp("c.DATE").getTime();
      data.name = rs.getString("c.NAME");
      data.body = rs.getString("c.BODY");
      data.classLevel = rs.getInt("c.CLASS_LEVEL");
      data.userCode = rs.getInt("c.USER_CODE");
      data.remoteAddress = rs.getString("c.MACHINE_IP");
      data.imageFileName = rs.getString("p.IMAGE_FILE_NAME");
      return data;
    }
  };

  @Override
  public int getNumberOfCreationLogWithUserCode(int userCode, long dateFrom)
      throws DatabaseException {
    try {
      return (int) (long) runner.query(
          "SELECT COUNT(*) FROM creation_log WHERE ? < DATE AND USER_CODE = ?",
          new ScalarHandler<Long>(), new Timestamp(dateFrom), userCode);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public int getNumberOfCreationLogWithMachineIp(String machineIp, long dateFrom)
      throws DatabaseException {
    try {
      return (int) (long) runner.query(
          "SELECT COUNT(*) FROM creation_log WHERE ? < DATE AND MACHINE_IP = ?",
          new ScalarHandler<Long>(), new Timestamp(dateFrom), machineIp);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  // ////////////////////////////////////////////////////////////////////////
  // テーマモード編集ログ
  @Override
  public void addThemeModeEditLog(PacketThemeModeEditLog log) throws DatabaseException {
    try {
      runner
          .update(
              "INSERT INTO theme_mode_edit_log (userCode, timeMs, type, theme, query) VALUES (?, ?, ?, ?, ?)",
              log.getUserCode(), log.getTimeMs(), log.getType(), log.getTheme(), log.getQuery());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketThemeModeEditLog> getThemeModeEditLog(int start, int length)
      throws DatabaseException {
    try {
      return runner.query(
          "SELECT theme_mode_edit_log.userCode AS userCode, player.NAME AS userName, "
              + "theme_mode_edit_log.timeMs AS timeMs, theme_mode_edit_log.type AS type, "
              + "theme_mode_edit_log.theme AS theme, theme_mode_edit_log.query AS query "
              + "FROM theme_mode_edit_log, player "
              + "WHERE theme_mode_edit_log.userCode = player.USER_CODE " + "LIMIT ? OFFSET ?",
          new BeanListHandler<>(PacketThemeModeEditLog.class), length, start);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public int getNumberOfThemeModeEditLog() throws DatabaseException {
    try {
      return (int) (long) runner.query("SELECT COUNT(*) FROM theme_mode_edit_log",
          new ScalarHandler<Long>());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketProblem> getAdsenseProblems(String queryString) throws DatabaseException {
    List<Integer> problemIds = fullTextSearch.getAdsenseProblems(queryString);
    return getProblem(problemIds);
  }

  // ////////////////////////////////////////////////////////////////////////
  // 制限ユーザー
  // ////////////////////////////////////////////////////////////////////////
  @Override
  public void addRestrictedUserCode(int userCode, RestrictionType restrictionType)
      throws DatabaseException {
    try {
      runner.update("REPLACE restricted_user_code (USER_CODE, TYPE) VALUES (?, ?)", userCode,
          restrictionType.getValue());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void removeRestrictedUserCode(int userCode, RestrictionType restrictionType)
      throws DatabaseException {
    try {
      runner.update(
          "UPDATE restricted_user_code SET VALID = FALSE WHERE USER_CODE = ? AND TYPE = ?",
          userCode, restrictionType.getValue());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public Set<Integer> getRestrictedUserCodes(RestrictionType restrictionType)
      throws DatabaseException {
    try {
      return ImmutableSet.copyOf(runner.query(
          "SELECT USER_CODE FROM restricted_user_code WHERE TYPE = ? AND VALID = TRUE",
          new ColumnListHandler<Integer>(Integer.class), restrictionType.getValue()));
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void clearRestrictedUserCodes(RestrictionType restrictionType) throws DatabaseException {
    try {
      runner.update("UPDATE restricted_user_code SET VALID = FALSE WHERE TYPE = ?",
          restrictionType.getValue());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void addRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType)
      throws DatabaseException {
    try {
      runner.update("REPLACE restricted_remote_address (REMOTE_ADDRESS, TYPE) VALUES (?, ?)",
          remoteAddress, restrictionType.getValue());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void removeRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType)
      throws DatabaseException {
    try {
      runner
          .update(
              "UPDATE restricted_remote_address SET VALID = FALSE WHERE REMOTE_ADDRESS = ? AND TYPE = ?",
              remoteAddress, restrictionType.getValue());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public Set<String> getRestrictedRemoteAddresses(RestrictionType restrictionType)
      throws DatabaseException {
    try {
      return ImmutableSet.copyOf(runner.query(
          "SELECT REMOTE_ADDRESS FROM restricted_remote_address WHERE TYPE = ? AND VALID = TRUE",
          new ColumnListHandler<String>(String.class), restrictionType.getValue()));
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void clearRestrictedRemoteAddresses(RestrictionType restrictionType)
      throws DatabaseException {
    try {
      runner.update("UPDATE restricted_remote_address SET VALID = FALSE WHERE TYPE = ?",
          restrictionType.getValue());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public Map<Integer, Integer> getUserCodeToIndicatedProblems() throws DatabaseException {
    final Map<Integer, Integer> problemIdToUserCode = Maps.newHashMap();
    try {
      runner.query(
          "SELECT problem.ID, creation_log.USER_CODE, creation_log.DATE FROM problem, creation_log "
              + "WHERE INDICATION != 0 AND problem.ID = creation_log.PROBLEM_ID",
          new ResultSetHandler<Void>() {
            @Override
            public Void handle(ResultSet resultSet) throws SQLException {
              while (resultSet.next()) {
                int problemId = resultSet.getInt("ID");
                int userCode = resultSet.getInt("creation_log.USER_CODE");
                // 複数含まれる場合は新しい日付を持つ行データで上書きする
                problemIdToUserCode.put(problemId, userCode);
              }
              return null;
            }
          });
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }

    Multiset<Integer> userCodes = HashMultiset.create(problemIdToUserCode.values());
    Map<Integer, Integer> userCodeToIndicatedProblems = Maps.newHashMap();
    for (int userCode : userCodes) {
      userCodeToIndicatedProblems.put(userCode, userCodes.count(userCode));
    }
    return userCodeToIndicatedProblems;
  }

  private List<PacketRankingData> getThemeRanking(String theme, String additionalWhereCondition)
      throws DatabaseException {
    DateTime dateTime = new DateTime().minusDays(Constant.RANKING_DISPLAY_DAY);
    try {
      List<PacketRankingData> ranking = runner.query(
          "SELECT player.USER_CODE AS USER_CODE, NAME, IMAGE_FILE_NAME, MAX(SCORE) AS DATA "
              + "FROM player, theme_mode_score " + "WHERE theme_mode_score.theme = ? "
              + "AND player.USER_CODE = theme_mode_score.USER_CODE "
              + "AND player.PLAY_COUNT >= 10 " + "AND player.LAST_LOGIN >= ? " + "AND "
              + additionalWhereCondition + " GROUP BY USER_CODE " + "ORDER BY DATA DESC "
              + "LIMIT 0, ? ", rankingDataHandler, theme, dateTime.getMillis(),
          Constant.NUMBER_OF_RANKING_DATA);
      for (int i = 0; i < ranking.size(); ++i) {
        ranking.get(i).ranking = i + 1;
      }
      return ranking;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void updateThemeModeScore(int userCode, String theme, int score) throws DatabaseException {
    DateTime dateTime = new DateTime();
    try {
      runner.update("INSERT LOW_PRIORITY theme_mode_score (THEME, USER_CODE, SCORE, YEAR, MONTH) "
          + "VALUES (?, ?, ?, ?, ?) "
          + "ON DUPLICATE KEY UPDATE SCORE = ((SCORE < ?) * ? + (SCORE > ?) * SCORE)", theme,
          userCode, score, dateTime.getYear(), dateTime.getMonthOfYear(), score, score, score);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<PacketRankingData> getThemeRankingOld(String theme) throws DatabaseException {
    // YEAR = 0 にはスコア変更時の記録が残っている
    return getThemeRanking(theme, "theme_mode_score.YEAR = 0");
  }

  @Override
  public List<PacketRankingData> getThemeRankingAll(String theme) throws DatabaseException {
    // YEAR = 1 には月別ランキング以前の記録が残っている
    return getThemeRanking(theme, "theme_mode_score.YEAR != 0");
  }

  @Override
  public List<PacketRankingData> getThemeRanking(String theme, int year) throws DatabaseException {
    return getThemeRanking(theme, "theme_mode_score.YEAR = " + year);
  }

  @Override
  public List<PacketRankingData> getThemeRanking(String theme, int year, int month)
      throws DatabaseException {
    return getThemeRanking(theme, "theme_mode_score.YEAR = " + year + " "
        + "AND theme_mode_score.MONTH = " + month);
  }

  @Override
  public List<PacketMonth> getThemeRankingDateRanges() throws DatabaseException {
    try {
      return runner.query("SELECT YEAR, MONTH " + "FROM theme_mode_score " + "WHERE YEAR > 2000 "
          + "GROUP BY YEAR, MONTH " + "ORDER BY YEAR, MONTH", yearMonthHandler);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private static final ResultSetHandler<List<PacketMonth>> yearMonthHandler = new AbstractListHandler<PacketMonth>() {
    @Override
    protected PacketMonth handleRow(ResultSet rs) throws SQLException {
      PacketMonth month = new PacketMonth();
      month.year = rs.getInt("YEAR");
      month.month = rs.getInt("MONTH");
      return month;
    }
  };

}
