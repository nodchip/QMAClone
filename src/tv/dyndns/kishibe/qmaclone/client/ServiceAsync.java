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
package tv.dyndns.kishibe.qmaclone.client;

import java.util.List;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsResponse;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsThread;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLogin;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMonth;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRatingDistribution;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketReadyForGame;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRegistrationData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketResult;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKey;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.packet.ProblemIndicationEligibility;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServiceAsync {
  void addIgnoreUserCode(int userCode, int targetUserCode, AsyncCallback<Void> callback);

  void addProblemIdsToReport(int userCode, List<Integer> problemIds, AsyncCallback<Void> callback);

  void addRatingHistory(int userCode, int rating, AsyncCallback<Void> callback);

  // 問題評価文をクリアする
  void clearProblemFeedback(int problemId, AsyncCallback<Void> callback);

  void clearProblemIDFromReport(int userCode, AsyncCallback<Void> callback);

  void getAvailableChalactersForHandwriting(AsyncCallback<String> callback);

  // イベント部屋情報を取得する
  void getEventRooms(AsyncCallback<List<PacketRoomKey>> callback);

  // ゲームの進行状態を取得する
  void getGameStatus(int sessionId, AsyncCallback<PacketGameStatus> callback);

  void getLoginUsers(AsyncCallback<List<PacketUserData>> callback);

  void getNewUserCode(AsyncCallback<Integer> callback);

  void getPlayerSummaries(int sessionId, AsyncCallback<List<PacketPlayerSummary>> callback);

  void getPrefectureRanking(AsyncCallback<int[][]> callback);

  void getProblemCreationLog(int problemId, AsyncCallback<List<PacketProblemCreationLog>> callback);

  void getProblem(int sessionId, AsyncCallback<List<PacketProblem>> callback);

  // 問題評価文を取得する
  void getProblemFeedback(int problemId, AsyncCallback<List<String>> callback);

  // 問題を取得する
  void getProblemList(List<Integer> problemIds, AsyncCallback<List<PacketProblem>> callback);

  void getGeneralRanking(AsyncCallback<List<List<PacketRankingData>>> callback);

  void getRatingDistribution(AsyncCallback<PacketRatingDistribution> callback);

  void getRatingHistory(int userCode, AsyncCallback<List<Integer>> callback);

  // 結果表示
  // 最終結果を取得する
  void getResult(int sessionId, AsyncCallback<List<PacketResult>> callback);

  /**
   * サーバーの状態を取得する。
   * 
   * @param callback
   */
  void getServerStatus(AsyncCallback<PacketServerStatus> callback);

  /**
   * ログイン情報を取得する。ゲーム起動時に一回だけ呼び出すこと。
   * 
   * @param userCode
   *          ユーザーコード
   * @param callback
   */
  void login(int userCode, AsyncCallback<PacketLogin> callback);

  /**
   * ログインしていることを通知する
   * 
   * @param userCode
   *          ユーザーコード
   * @param callback
   */
  void keepAlive(int userCode, AsyncCallback<Void> callback);

  void getStatisticsOfAccuracyRate(AsyncCallback<int[][]> callback);

  // 問題統計情報を取得する
  void getStatisticsOfProblemCount(AsyncCallback<int[][]> callback);

  void getUserProblemReport(int userCode, AsyncCallback<List<PacketProblem>> callback);

  void getWrongAnswers(int problemId, AsyncCallback<List<PacketWrongAnswer>> callback);

  void loadUserData(int userCode, AsyncCallback<PacketUserData> callback);

  // チャットからメッセージを受信する
  void receiveMessageFromChat(int nextArrayIndex, AsyncCallback<PacketChatMessages> callback);

  // 手書き文字の識別を行う
  void recognizeHandwriting(double[][][] strokes, AsyncCallback<String[]> callback);

  void register(PacketPlayerSummary playerSummary, Set<ProblemGenre> genres,
      Set<ProblemType> types, String greeting, GameMode gameMode, String roomName, String theme,
      String imageFileName, int classLevel, int difficultSelect, int rating, int userCode,
      int volatility, int playCount, NewAndOldProblems newAndOldProblems, boolean publicEvent,
      AsyncCallback<PacketRegistrationData> callback);

  void removeIgnoreUserCode(int userCode, int targetUserCode, AsyncCallback<Void> callback);

  // 誤解答を削除する
  void removePlayerAnswers(int problemID, AsyncCallback<Void> callback);

  void removeProblemIDFromReport(int userCode, int problemId, AsyncCallback<Void> callback);

  // 強制的にゲームをスタートさせる
  void requestSkip(int sessionId, int playerListId, AsyncCallback<Integer> callback);

  void saveUserData(PacketUserData userData, AsyncCallback<Void> callback);

  // 問題の検索を行う
  void searchProblem(String query, String creator, boolean creatorPerfectMatching,
      Set<ProblemGenre> genres, Set<ProblemType> types, Set<RandomFlag> randomFlag,
      AsyncCallback<List<PacketProblem>> callback);

  // 類似問題を検索する
  void searchSimilarProblem(PacketProblem problem, AsyncCallback<List<PacketProblem>> callback);

  // チャットにメッセージを送信する
  void sendMessageToChat(PacketChatMessage chatData, AsyncCallback<Void> callback);

  void uploadProblem(PacketProblem problem, int userCode, boolean resetAnswerCount,
      AsyncCallback<Integer> callback);

  // 投票を行う
  void voteToProblem(int userCode, int problemId, boolean good, String feedback, String playerName,
      AsyncCallback<Void> callback);

  /**
   * 問題の投票をリセットする
   * 
   * @param problemId
   *          問題番号
   */
  void resetVote(int problemId, AsyncCallback<Void> callback);

  // ゲーム開始待機
  void waitForGame(int sessionId, AsyncCallback<PacketReadyForGame> callback);

  void getThemes(AsyncCallback<List<PacketTheme>> callback);

  void addThemeModeQuery(String theme, String query, int userCode, AsyncCallback<Void> callback);

  void removeThemeModeQuery(String theme, String query, int userCode, AsyncCallback<Void> callback);

  void isThemeModeEditor(int userCode, AsyncCallback<Boolean> callback);

  void applyThemeModeEditor(int userCode, String text, AsyncCallback<Void> callback);

  void acceptThemeModeEditor(int userCode, AsyncCallback<Void> callback);

  void rejectThemeModeEditor(int userCode, AsyncCallback<Void> callback);

  void getThemeModeEditors(AsyncCallback<List<PacketThemeModeEditor>> callback);

  void isApplyingThemeModeEditor(int userCode, AsyncCallback<Boolean> callback);

  void getMatchingData(int sessionId, AsyncCallback<PacketMatchingData> callback);

  void getNumberOfChatLog(AsyncCallback<Integer> callback);

  void getChatLogId(int year, int month, int day, int hour, int minute, int second,
      AsyncCallback<Integer> callback);

  void getChatLog(int start, AsyncCallback<List<PacketChatMessage>> callback);

  void getThemeModeThemes(AsyncCallback<List<List<String>>> callback);

  void getWrongImageLinks(AsyncCallback<List<PacketImageLink>> callback);

  void canUploadProblem(int userCode, AsyncCallback<Boolean> callback);

  void keepAliveGame(int sessionId, int playerListId, AsyncCallback<Void> callback);

  /**
   * 自分の解答を送信する
   * 
   * @param sessionId
   *          セッションid
   * @param playerListId
   *          プレイヤーリストid
   * @param answer
   *          解答
   * @param userCode
   *          ユーザーコード
   * @param responseTime
   *          回答時間
   * @param callback
   *          コールバック
   */
  void sendAnswer(int sessionId, int playerListId, String answer, int userCode, int responseTime,
      AsyncCallback<Void> callback);

  /**
   * ゲーム終了を報告する
   * 
   * @param userCode
   *          ユーザーコード
   * @param oldRating
   *          旧レーティング
   * @param newRating
   *          新レーティング
   * @param callback
   *          コールバック
   * @param sessionId
   *          ゲームセッションID
   */
  void notifyGameFinished(int userCode, int oldRating, int newRating, int sessionId,
      AsyncCallback<Void> callback);

  /**
   * タイムアップを通知する
   * 
   * @param sessionId
   *          セッションid
   * @param playerListId
   *          　プレイヤーリストif
   * @param userCode
   *          ユーザーコード
   * @param callback
   *          コールバック
   */
  void notifyTimeUp(int sessionId, int playerListId, int userCode, AsyncCallback<Void> callback);

  /**
   * 指摘された問題を取得する
   * 
   * @param callback
   *          コールバック
   */
  void getIndicatedProblems(AsyncCallback<List<PacketProblem>> callback);

  void indicateProblem(int problemId, int userCode, AsyncCallback<Void> callback);

  // ////////////////////////////////////////////////////////////////////////
  // BBS
  // ////////////////////////////////////////////////////////////////////////
  /**
   * BBSスレッドを立てる
   * 
   * @param bbsId
   *          BBS id
   * @param thread
   *          スレッド
   * @param response
   *          第1コメント
   * @param callback
   *          　コールバック
   */
  void buildBbsThread(int bbsId, PacketBbsThread thread, PacketBbsResponse response,
      AsyncCallback<Void> callback);

  /**
   * スレッドレスポンスを取得する
   * 
   * @param threadId
   *          スレッドid
   * @param count
   *          最大レスポンス数
   * @param callback
   *          コールバック
   */
  void getBbsResponses(int threadId, int count, AsyncCallback<List<PacketBbsResponse>> callback);

  /**
   * BBSスレッドを取得する
   * 
   * @param bbsId
   *          BBS id
   * @param start
   *          開始スレッドオフセット
   * @param count
   *          　最大スレッド数
   * @param callback
   *          コールバック
   */
  void getBbsThreads(int bbsId, int start, int count, AsyncCallback<List<PacketBbsThread>> callback);

  /**
   * BBSスレッド数を取得する
   * 
   * @param bbsId
   *          BBS id
   * @return BBSスレッド数
   * @param callback
   *          コールバック
   */
  void getNumberOfBbsThreads(int bbsId, AsyncCallback<Integer> callback);

  /**
   * BBSスレッドに書き込む
   * 
   * @param response
   *          レスポンス
   * @param age
   *          スレッド順位を上げるならtrue。そうでないならfalse。
   * @param callback
   *          コールバック
   */
  void writeToBbs(PacketBbsResponse response, boolean age, AsyncCallback<Void> callback);

  void resetProblemCorrectCounter(int userCode, int problemId, AsyncCallback<Boolean> callback);

  /**
   * 対象のユーザーが問題の指摘が可能かどうか返す
   * 
   * @param userCode
   *          ユーザーコード
   * @param callback
   *          コールバック
   */
  void getProblemIndicationEligibility(int userCode,
      AsyncCallback<ProblemIndicationEligibility> callback);

  void generateDiffHtml(String before, String after, AsyncCallback<String> callback);

  /**
   * テーマモード編集ログを取得する
   * 
   * @param start
   *          開始位置
   * @param length
   *          データ数
   * @param callback
   *          　コールバック
   */
  void getThemeModeEditLog(int start, int length,
      AsyncCallback<List<PacketThemeModeEditLog>> callback);

  /**
   * テーマモード編集ログの数を取得する
   * 
   * @param callback
   *          コールバック
   */
  void getNumberOfThemeModeEditLog(AsyncCallback<Integer> callback);

  /**
   * テーマクエリの数を返す
   * 
   * @param callback
   *          コールバック
   */
  void getNumberofThemeQueries(AsyncCallback<Integer> callback);

  /**
   * テーマのクエリを取得する
   * 
   * @param THEME
   *          テーマ
   * @param callback
   *          コールバック
   */
  void getThemeQueries(String theme, AsyncCallback<List<PacketThemeQuery>> callback);

  void addRestrictedUserCode(int userCode, RestrictionType restrictionType,
      AsyncCallback<Void> callback);

  void removeRestrictedUserCode(int userCode, RestrictionType restrictionType,
      AsyncCallback<Void> callback);

  void getRestrictedUserCodes(RestrictionType restrictionType, AsyncCallback<Set<Integer>> callback);

  void clearRestrictedUserCodes(RestrictionType restrictionType, AsyncCallback<Void> callback);

  void addRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType,
      AsyncCallback<Void> callback);

  void removeRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType,
      AsyncCallback<Void> callback);

  void getRestrictedRemoteAddresses(RestrictionType restrictionType,
      AsyncCallback<Set<String>> callback);

  void clearRestrictedRemoteAddresses(RestrictionType restrictionType, AsyncCallback<Void> callback);

  void getThemeRankingOld(String theme, AsyncCallback<List<PacketRankingData>> callback);

  void getThemeRankingAll(String theme, AsyncCallback<List<PacketRankingData>> callback);

  void getThemeRanking(String theme, int year, AsyncCallback<List<PacketRankingData>> callback);

  void getThemeRanking(String theme, int year, int month,
      AsyncCallback<List<PacketRankingData>> callback);

  void getThemeRankingDateRanges(AsyncCallback<List<PacketMonth>> callback);

  /**
   * Google+ IDを用いてユーザーコードを検索する
   * 
   * @param googlePlusId
   *          Google+ ID
   */
  void lookupUserDataByGooglePlusId(String googlePlusId,
      AsyncCallback<List<PacketUserData>> callback);

  /**
   * 連携済みのユーザーコードを解除する
   * 
   * @param userCode
   *          ユーザーコード
   */
  void disconnectUserCode(int userCode, AsyncCallback<Void> callback);
}
