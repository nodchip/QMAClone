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

import java.rmi.ServerException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

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
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMatchingStatus;
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
import tv.dyndns.kishibe.qmaclone.client.service.ServiceConstants;
import tv.dyndns.kishibe.qmaclone.client.service.ServiceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath(ServiceConstants.SERVICE_PATH)
public interface Service extends RemoteService {
  public static class Util {
    public static ServiceAsync getInstance() {
      return GWT.create(Service.class);
    }
  }

  /***************************************************************************
   * プレイヤー
   **************************************************************************/
  void addProblemIdsToReport(int userCode, List<Integer> problemIds) throws ServiceException;

  void addRatingHistory(int userCode, int rating) throws ServiceException;

  void clearProblemFeedback(int problemId) throws ServiceException;

  void clearProblemIDFromReport(int userCode) throws ServiceException;

  int getNewUserCode() throws ServiceException;

  List<Integer> getRatingHistory(int userCode) throws ServiceException;

  /**
   * ログイン情報を取得する。ゲーム起動時に一回だけ呼び出すこと。
   * 
   * @param userCode
   *          ユーザーコード
   * @return　ログイン
   * @throws Exception
   */
  PacketLogin login(int userCode) throws ServiceException;

  /**
   * ログインしていることを通知する
   * 
   * @param userCode
   *          ユーザーコード
   * @throws Exception
   */
  void keepAlive(int userCode) throws ServiceException;

  List<PacketProblem> getUserProblemReport(int userCode) throws ServiceException;

  PacketUserData loadUserData(int userCode) throws ServiceException;

  void addIgnoreUserCode(int userCode, int targetUserCode) throws ServiceException;

  void removeIgnoreUserCode(int userCode, int targetUserCode) throws ServiceException;

  void removeProblemIDFromReport(int userCode, int problemId) throws ServiceException;

  void saveUserData(PacketUserData userData) throws ServiceException;

  /**
   * Google+ IDを用いてユーザーコードを検索する
   * 
   * @param googlePlusId
   *          Google+ ID
   * @return ユーザーコード
   * @throws ServiceException
   */
  List<PacketUserData> lookupUserDataByGooglePlusId(String googlePlusId) throws ServiceException;

  /**
   * 連携済みのユーザーコードを解除する
   * 
   * @param userCode
   *          ユーザーコード
   * @throws ServiceException
   */
  void disconnectUserCode(int userCode) throws ServiceException;

  /***************************************************************************
   * ランキング
   **************************************************************************/

  List<List<PacketRankingData>> getGeneralRanking() throws ServiceException;

  List<PacketRankingData> getThemeRankingOld(String theme) throws ServiceException;

  List<PacketRankingData> getThemeRankingAll(String theme) throws ServiceException;

  List<PacketRankingData> getThemeRanking(String theme, int year) throws ServiceException;

  List<PacketRankingData> getThemeRanking(String theme, int year, int month)
      throws ServiceException;

  List<PacketMonth> getThemeRankingDateRanges() throws ServiceException;

  /***************************************************************************
   * システム
   **************************************************************************/
  List<PacketUserData> getLoginUsers() throws ServiceException;

  int[][] getPrefectureRanking() throws ServiceException;

  PacketRatingDistribution getRatingDistribution() throws ServiceException;

  /**
   * サーバーの状態を取得する。
   * 
   * @return サーバー状態
   * @throws Exception
   */
  PacketServerStatus getServerStatus() throws ServiceException;

  // ジャンル・正解率ごとの問題数を取得する
  int[][] getStatisticsOfAccuracyRate() throws ServiceException;

  // ジャンル・出題形式ごとの問題数を取得する
  int[][] getStatisticsOfProblemCount() throws ServiceException;

  List<PacketImageLink> getWrongImageLinks() throws ServiceException;

  /***************************************************************************
   * ゲーム
   **************************************************************************/
  List<PacketRoomKey> getEventRooms() throws ServiceException;

  // ゲームの進行状態を取得する
  PacketGameStatus getGameStatus(int sessionId) throws ServiceException;

  void keepAliveGame(int sessionId, int playerListId) throws ServiceException;

  List<PacketPlayerSummary> getPlayerSummaries(int sessionId) throws ServiceException;

  // 結果表示
  // 最終結果を取得する
  List<PacketResult> getResult(int sessionId) throws ServiceException;

  // プレイヤー情報を登録する
  PacketRegistrationData register(PacketPlayerSummary playerSummary, Set<ProblemGenre> genres,
      Set<ProblemType> types, String greeting, GameMode gameMode, String roomName, String theme,
      String imageFileName, int classLevel, int difficultSelect, int rating, int userCode,
      int volatility, int playCount, NewAndOldProblems newAndOldProblems, boolean publicEvent)
      throws ServiceException;

  // 強制的にゲームをスタートさせる
  int requestSkip(int sessionId, int playerListId) throws ServiceException;

  void sendAnswer(int sessionId, int playerListId, String answer, int userCode, int responseTime)
      throws ServiceException;

  void notifyTimeUp(int sessionId, int playerListId, int userCode) throws ServiceException;

  void notifyGameFinished(int userCode, int oldRating, int newRating, int sessionId)
      throws ServiceException;

  // ゲーム開始待機
  PacketReadyForGame waitForGame(int sessionId) throws ServiceException;

  PacketMatchingStatus getMatchingStatus(int sessionId) throws ServiceException;

  /***************************************************************************
   * 問題
   **************************************************************************/
  List<PacketProblemCreationLog> getProblemCreationLog(int problemId) throws ServiceException;

  List<PacketProblem> getProblem(int sessionId) throws ServiceException;

  // 問題評価文を取得する
  List<String> getProblemFeedback(int problemId) throws ServiceException;

  // 問題を取得する
  List<PacketProblem> getProblemList(List<Integer> problemIds) throws ServiceException;

  List<PacketWrongAnswer> getWrongAnswers(int problemId) throws ServiceException;

  // 誤解答を削除する
  void removePlayerAnswers(int problemID) throws ServiceException;

  // 問題の検索を行う
  List<PacketProblem> searchProblem(String query, String creator, boolean creatorPerfectMatching,
      Set<ProblemGenre> genres, Set<ProblemType> types, Set<RandomFlag> randomFlag)
      throws ServiceException;

  // 類似問題を検索する
  List<PacketProblem> searchSimilarProblem(PacketProblem problem) throws ServiceException;

  // 問題を投稿する
  int uploadProblem(PacketProblem problem, int userCode, boolean resetAnswerCount)
      throws ServiceException;

  // 投票を行う
  void voteToProblem(int userCode, int problemId, boolean good, String feedback, String playerName)
      throws ServiceException;

  /**
   * 問題の投票をリセットする
   * 
   * @param problemId
   *          問題番号
   */
  void resetVote(int problemId) throws ServiceException;

  /**
   * ユーザーが問題を投稿可能かどうかを返す。
   * 
   * @param userCode
   *          ユーザーコード
   * @param problemId
   *          問題ID 新規問題なら{@code null}
   * @return 可能ならtrue
   * @throws Exception
   */
  boolean canUploadProblem(int userCode, @Nullable Integer problemId) throws ServiceException;

  /**
   * 指摘された問題を取得する
   * 
   * @return 指摘された問題
   * @throws ServiceException
   */
  List<PacketProblem> getIndicatedProblems() throws ServiceException;

  /**
   * 問題の不備を指摘する
   * 
   * @param problemId
   *          問題番号
   * @param userCode
   *          ユーザーコード
   * @throws ServiceException
   */
  void indicateProblem(int problemId, int userCode) throws ServiceException;

  /***************************************************************************
   * テーマモード編集
   **************************************************************************/
  List<List<String>> getThemeModeThemes() throws ServiceException;

  /**
   * テーマモード編集ログを取得する
   * 
   * @param start
   *          開始位置
   * @param length
   *          データ数
   * @return ログ
   * @throws ServiceException
   */
  List<PacketThemeModeEditLog> getThemeModeEditLog(int start, int length) throws ServiceException;

  /**
   * テーマモード編集ログの数を取得する
   * 
   * @return テーマモード編集ログの数
   * @throws ServiceException
   */
  int getNumberOfThemeModeEditLog() throws ServiceException;

  List<PacketTheme> getThemes() throws ServiceException;

  /**
   * テーマのクエリを取得する
   * 
   * @param THEME
   *          テーマ
   * @return テーマクエリのリスト
   * @throws ServiceException
   */
  List<PacketThemeQuery> getThemeQueries(String theme) throws ServiceException;

  /**
   * テーマクエリの数を返す
   * 
   * @return テーマクエリの数
   * @throws ServiceException
   */
  int getNumberofThemeQueries() throws ServiceException;

  void addThemeModeQuery(String theme, String query, int userCode) throws ServiceException;

  void removeThemeModeQuery(String theme, String query, int userCode) throws ServiceException;

  boolean isThemeModeEditor(int userCode) throws ServiceException;

  void applyThemeModeEditor(int userCode, String text) throws ServiceException;

  void acceptThemeModeEditor(int userCode) throws ServiceException;

  void rejectThemeModeEditor(int userCode) throws ServiceException;

  List<PacketThemeModeEditor> getThemeModeEditors() throws ServiceException;

  boolean isApplyingThemeModeEditor(int userCode) throws ServiceException;

  /***************************************************************************
   * チャット
   **************************************************************************/
  /**
   * チャットの最新メッセージを受け取る
   * 
   * @param nextArrayIndex
   *          どのチャットメッセージ移行を受け取るか
   * @return チャットメッセージのリスト
   * @throws Exception
   */
  PacketChatMessages receiveMessageFromChat(int nextArrayIndex) throws ServiceException;

  // チャットにメッセージを送信する
  void sendMessageToChat(PacketChatMessage chatData) throws ServiceException;

  // 過去ログの件数を返す
  int getNumberOfChatLog() throws ServiceException;

  // 指定した日付から始まるチャットログのIDを返す
  int getChatLogId(int year, int month, int day, int hour, int minute, int second)
      throws ServiceException;

  // 過去ログを返す
  List<PacketChatMessage> getChatLog(int start) throws ServiceException;

  /***************************************************************************
   * 手書き文字認識
   **************************************************************************/
  // 手書き文字の識別を行う
  String[] recognizeHandwriting(double[][][] strokes) throws ServiceException;

  String getAvailableChalactersForHandwriting() throws ServiceException;

  /***************************************************************************
   * 掲示板
   **************************************************************************/
  /**
   * BBSスレッドを立てる
   * 
   * @param bbsId
   *          BBS id
   * @param thread
   *          スレッド
   * @param response
   *          第1コメント
   * @throws ServiceException
   */
  void buildBbsThread(int bbsId, PacketBbsThread thread, PacketBbsResponse response)
      throws ServiceException;

  /**
   * スレッドレスポンスを取得する
   * 
   * @param threadId
   *          スレッドid
   * @param count
   *          最大レスポンス数
   * @return スレッドレスポンス
   * @throws ServiceException
   */
  List<PacketBbsResponse> getBbsResponses(int threadId, int count) throws ServiceException;

  /**
   * BBSスレッドを取得する
   * 
   * @param bbsId
   *          BBS id
   * @param start
   *          開始スレッドオフセット
   * @param count
   *          　最大スレッド数
   * @return
   * @throws ServiceException
   */
  List<PacketBbsThread> getBbsThreads(int bbsId, int start, int count) throws ServiceException;

  /**
   * BBSスレッド数を取得する
   * 
   * @param bbsId
   *          BBS id
   * @return BBSスレッド数
   * @throws ServiceException
   */
  int getNumberOfBbsThreads(int bbsId) throws ServiceException;

  /**
   * BBSスレッドに書き込む
   * 
   * @param response
   *          レスポンス
   * @param age
   *          スレッド順位を上げるならtrue。そうでないならfalse。
   * @throws ServiceException
   */
  void writeToBbs(PacketBbsResponse response, boolean age) throws ServiceException;

  /**
   * 回答数カウンターをリセットする
   * 
   * @param userCode
   *          ユーザーコード
   * @param problemId
   *          問題ID
   * @return 成功した場合は {@code true}。回数の上限に達していた場合は {@code false}。
   * @throws ServiceException
   *           エラー時
   */
  boolean resetProblemCorrectCounter(int userCode, int problemId) throws ServiceException;

  /**
   * 対象のユーザーが問題の指摘が可能かどうか返す
   * 
   * @param userCode
   *          ユーザーコード
   * @return
   * @throws ServiceException
   */
  ProblemIndicationEligibility getProblemIndicationEligibility(int userCode)
      throws ServiceException;

  /**
   * 二つの文字列の差分を表すhtmlを生成する
   * 
   * @param before
   *          差分元
   * @param after
   *          　差分先
   * @return
   * @throws ServerException
   */
  String generateDiffHtml(String before, String after) throws ServiceException;

  /***************************************************************************
   * 制限ユーザー
   **************************************************************************/
  void addRestrictedUserCode(int userCode, RestrictionType restrictionType) throws ServiceException;

  void removeRestrictedUserCode(int userCode, RestrictionType restrictionType)
      throws ServiceException;

  Set<Integer> getRestrictedUserCodes(RestrictionType restrictionType) throws ServiceException;

  void clearRestrictedUserCodes(RestrictionType restrictionType) throws ServiceException;

  void addRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType)
      throws ServiceException;

  void removeRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType)
      throws ServiceException;

  Set<String> getRestrictedRemoteAddresses(RestrictionType restrictionType) throws ServiceException;

  void clearRestrictedRemoteAddresses(RestrictionType restrictionType) throws ServiceException;

}
