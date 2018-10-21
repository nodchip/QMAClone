package tv.dyndns.kishibe.qmaclone.server.database;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
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
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.PageView;
import tv.dyndns.kishibe.qmaclone.server.util.IntArray;

public interface Database {
	// ////////////////////////////////////////////////////////////////////////
	// 正解率統計
	// ////////////////////////////////////////////////////////////////////////
	void addProblemIdsToReport(final int userCode, final List<Integer> problemIds)
			throws DatabaseException;

	void removeProblemIdFromReport(int userCode, int problemID) throws DatabaseException;

	void clearProblemIdFromReport(int userCode) throws DatabaseException;

	List<PacketProblem> getUserProblemReport(int userCode) throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// ユーザーデータ
	// ////////////////////////////////////////////////////////////////////////
	PacketUserData getUserData(int userCode) throws DatabaseException;

	void setUserData(PacketUserData data) throws DatabaseException;

	boolean isUsedUserCode(int userCode) throws DatabaseException;

	Map<Integer, Integer> getUserCodeToIndicatedProblems() throws DatabaseException;

	List<PacketUserData> lookupUserCodeByGooglePlusId(String googlePlusId) throws DatabaseException;

	void disconnectUserCodeFromGooglePlus(int userCode) throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// ページビュー
	// ////////////////////////////////////////////////////////////////////////
	PageView loadPageView() throws DatabaseException;

	void savePageView(PageView pageView) throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// 問題
	// ////////////////////////////////////////////////////////////////////////
	Map<String, IntArray> getThemeToProblems(Map<String, List<String>> themeAndQueryStrings)
			throws DatabaseException;

	int addProblem(PacketProblem data) throws DatabaseException;

	void updateProblem(PacketProblem data) throws DatabaseException;

	void updateMinimumProblem(PacketProblemMinimum data) throws DatabaseException;

	List<PacketProblem> getProblem(Collection<Integer> ids) throws DatabaseException;

	PacketProblemMinimum getProblemMinimum(int problemId) throws DatabaseException;

	List<PacketProblem> searchProblem(String query, String creator, boolean creatorPerfectMatching,
			Set<ProblemGenre> genres, Set<ProblemType> types, Set<RandomFlag> randomFlags)
			throws DatabaseException;

	List<PacketProblem> searchSimilarProblemFromDatabase(PacketProblem problem)
			throws DatabaseException;

	List<PacketProblemCreationLog> getProblemCreationHistory(int problemId)
			throws DatabaseException;

	void processProblems(ProblemProcessable processor) throws DatabaseException;

	void processProblemMinimums(ProblemMinimumProcessable processer) throws DatabaseException;

	List<PacketProblem> getLastestProblems() throws DatabaseException;

	int getNumberOfCreationLogWithUserCode(int userCode, long dateFrom) throws DatabaseException;

	int getNumberOfCreationLogWithMachineIp(String machineIp, long dateFrom)
			throws DatabaseException;

	List<PacketProblem> getIndicatedProblems() throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// プレイヤー解答
	// ////////////////////////////////////////////////////////////////////////
	void addPlayerAnswers(int problemID, ProblemType type, List<String> answers)
			throws DatabaseException;

	List<PacketWrongAnswer> getPlayerAnswers(int problemID) throws DatabaseException;

	void removePlayerAnswers(int problemID) throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// ランキング
	// ////////////////////////////////////////////////////////////////////////
	List<List<PacketRankingData>> getGeneralRankingData() throws DatabaseException;

	List<PacketRankingData> getThemeRankingOld(String theme) throws DatabaseException;

	List<PacketRankingData> getThemeRankingAll(String theme) throws DatabaseException;

	List<PacketRankingData> getThemeRanking(String theme, int year) throws DatabaseException;

	List<PacketRankingData> getThemeRanking(String theme, int year, int month)
			throws DatabaseException;

	List<PacketMonth> getThemeRankingDateRanges() throws DatabaseException;

	void addCreationLog(PacketProblem problem, int userCode, String machineIp)
			throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// チャット
	// ////////////////////////////////////////////////////////////////////////
	void addChatLog(PacketChatMessage data) throws DatabaseException;

	Map<Integer, PacketChatMessage> getLatestChatData() throws DatabaseException;

	int getNumberOfChatLog() throws DatabaseException;

	int getChatLogId(int year, int month, int day, int hour, int minute, int second)
			throws DatabaseException;

	List<PacketChatMessage> getChatLog(int start) throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// 制限ユーザー
	// ////////////////////////////////////////////////////////////////////////
	void addRestrictedUserCode(int userCode, RestrictionType restrictionType)
			throws DatabaseException;

	void removeRestrictedUserCode(int userCode, RestrictionType restrictionType)
			throws DatabaseException;

	Set<Integer> getRestrictedUserCodes(RestrictionType restrictionType) throws DatabaseException;

	void clearRestrictedUserCodes(RestrictionType restrictionType) throws DatabaseException;

	void addRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType)
			throws DatabaseException;

	void removeRestrictedRemoteAddress(String remoteAddress, RestrictionType restrictionType)
			throws DatabaseException;

	Set<String> getRestrictedRemoteAddresses(RestrictionType restrictionType)
			throws DatabaseException;

	void clearRestrictedRemoteAddresses(RestrictionType restrictionType) throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// 無視ユーザーコード
	// ////////////////////////////////////////////////////////////////////////
	void addIgnoreUserCode(int userCode, int targetUserCode) throws DatabaseException;

	void removeIgnoreUserCode(int userCode, int targetUserCode) throws DatabaseException;

	Set<Integer> getServerIgnoreUserCode() throws DatabaseException;

	void addServerIgnoreUserCode(int userCode) throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// 掲示板
	// ////////////////////////////////////////////////////////////////////////
	List<PacketBbsThread> getBbsThreads(int bbsId, int start, int count) throws DatabaseException;

	List<PacketBbsResponse> getBbsResponses(int threadId, int count) throws DatabaseException;

	void buildBbsThread(int bbsId, PacketBbsThread thread, PacketBbsResponse response)
			throws DatabaseException;

	void writeToBbs(PacketBbsResponse response, boolean age) throws DatabaseException;

	int getNumberOfBbsThread(int bbsId) throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// リンク
	// ////////////////////////////////////////////////////////////////////////
	void addLinkData(PacketLinkData linkData) throws DatabaseException;

	void updateLinkData(PacketLinkData linkData) throws DatabaseException;

	void removeLinkData(int id) throws DatabaseException;

	List<PacketLinkData> getLinkDatas(int start, int count) throws DatabaseException;

	int getNumberOfLinkDatas() throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// レーティング
	// ////////////////////////////////////////////////////////////////////////
	Map<Integer, List<Integer>> getRatingGroupedByPrefecture() throws DatabaseException;

	void addRatingHistory(int userCode, int rating) throws DatabaseException;

	List<Integer> getRatingHistory(int userCode) throws DatabaseException;

	List<Integer> getWholeRating() throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// テーマモード
	// ////////////////////////////////////////////////////////////////////////
	List<PacketThemeQuery> getThemeModeQueries() throws DatabaseException;

	List<PacketThemeQuery> getThemeModeQueries(String theme) throws DatabaseException;

	int getNumberOfThemeQueries() throws DatabaseException;

	void addThemeModeQuery(String theme, String query) throws DatabaseException;

	void removeThemeModeQuery(String theme, String query) throws DatabaseException;

	void updateThemeModeScore(int userCode, String theme, int score) throws DatabaseException;

	List<PacketThemeModeEditor> getThemeModeEditors() throws DatabaseException;

	ThemeModeEditorStatus getThemeModeEditorsStatus(int userCode) throws DatabaseException;

	void updateThemeModeEdtorsStatus(int userCode, ThemeModeEditorStatus status)
			throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// 投票+フィードバック
	// ////////////////////////////////////////////////////////////////////////
	void voteToProblem(int problemId, boolean good, String feedback) throws DatabaseException;

	void resetVote(int problemId) throws DatabaseException;

	void clearProblemFeedback(int problemId) throws DatabaseException;

	List<String> getProblemFeedback(int problemId) throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// テーマモード編集ログ
	// ////////////////////////////////////////////////////////////////////////
	void addThemeModeEditLog(PacketThemeModeEditLog log) throws DatabaseException;

	List<PacketThemeModeEditLog> getThemeModeEditLog(int start, int length)
			throws DatabaseException;

	int getNumberOfThemeModeEditLog() throws DatabaseException;

	// ////////////////////////////////////////////////////////////////////////
	// その他
	// ////////////////////////////////////////////////////////////////////////
	int getNumberOfActiveUsers() throws DatabaseException;

	String getPassword(String type) throws DatabaseException;

	public List<PacketProblem> getAdsenseProblems(String query) throws DatabaseException;

}
