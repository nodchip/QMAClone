package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRegistrationData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;
import tv.dyndns.kishibe.qmaclone.client.packet.ProblemIndicationEligibility;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.client.service.ServiceException;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.handwriting.RecognizerZinnia;
import tv.dyndns.kishibe.qmaclone.server.image.BrokenImageLinkDetector;
import tv.dyndns.kishibe.qmaclone.server.image.ImageUtils;
import tv.dyndns.kishibe.qmaclone.server.sns.SnsClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ServiceServletStubTest {
  private static final String GENERIC_SERVICE_ERROR_MESSAGE = "処理中にエラーが発生しました。";
  private static final String QUERY = "query";
  private static final String CREATOR = "creator";
  private static final boolean CREATOR_PERFECT_MATCHING = true;
  private static final Set<ProblemGenre> GENRES = ImmutableSet.of(ProblemGenre.Anige,
      ProblemGenre.Gakumon);
  private static final Set<ProblemType> TYPES = ImmutableSet.of(ProblemType.Click,
      ProblemType.Effect);
  private static final Set<RandomFlag> RANDOM_FLAGS = ImmutableSet.of(RandomFlag.Random1,
      RandomFlag.Random2);
  private static final int USER_CODE = 12345678;
  private static final RestrictionType RESTRICTION_TYPE = RestrictionType.BBS;
  private static final String REMOTE_ADDRESS = "1.2.3.4";
  private static final int PROBLEM_ID = 11111;
  private static final String AUTH_PROVIDER = "google";
  private static final String AUTH_SUBJECT = "sub-1";

  private ServiceServletStub service;
  @Mock
  private ChatManager mockChatManager;
  @Mock
  private NormalModeProblemManager mockNormalModeProblemManager;
  @Mock
  private ThemeModeProblemManager mockThemeModeProblemManager;
  @Mock
  private GameManager mockGameManager;
  @Mock
  private ServerStatusManager mockServerStatusManager;
  @Mock
  private PlayerHistoryManager mockPlayerHistoryManager;
  @Mock
  private VoteManager mockVoteManager;
  @Mock
  private RecognizerZinnia mockRecognizer;
  @Mock
  private ThemeModeEditorManager mockThemeModeEditorManager;
  @Mock
  private AdminAccessManager mockAdminAccessManager;
  @Mock
  private ImageUtils mockImageManager;
  @Mock
  private Database mockDatabase;
  @Mock
  private PrefectureRanking mockPrefectureRanking;
  @Mock
  private RatingDistribution mockRatingDistribution;
  @Mock
  private SnsClient mockSnsClient;
  @Mock
  private GameLogger mockGameLogger;
  @Mock
  private Game mockGame;
  @Mock
  private ThreadPool mockThreadPool;
  @Mock
  private BadUserDetector mockBadUserDetector;
  @Mock
  private RestrictedUserUtils mockRestrictedUserUtils;
  @Mock
  private ProblemCorrectCounterResetCounter mockProblemCorrectCounterResetCounter;
  @Mock
  private ProblemIndicationCounter mockProblemIndicationCounter;
  @Mock
  private BrokenImageLinkDetector mockBrokenImageLinkDetector;
  private PacketProblem problem1;
  private PacketProblem problem2;
  private PacketProblem problem3;

  @BeforeEach
  public void setUp() throws Exception {
    service = spy(new ServiceServletStub(mockChatManager, mockNormalModeProblemManager,
        mockThemeModeProblemManager, mockGameManager, mockServerStatusManager,
        mockPlayerHistoryManager, mockVoteManager, mockRecognizer, mockThemeModeEditorManager,
        mockAdminAccessManager, mockDatabase, mockPrefectureRanking, mockRatingDistribution,
        mockSnsClient, mockGameLogger, mockThreadPool, mockBadUserDetector,
        mockRestrictedUserUtils, mockProblemCorrectCounterResetCounter,
        mockProblemIndicationCounter, mockBrokenImageLinkDetector));
    when(mockAdminAccessManager.isAdministrator(org.mockito.ArgumentMatchers.anyInt(),
        org.mockito.ArgumentMatchers.eq(mockDatabase))).thenReturn(true);
    problem1 = TestDataProvider.getProblem();
    problem1.id = 1;
    problem2 = TestDataProvider.getProblem();
    problem2.id = 2;
    problem3 = TestDataProvider.getProblem();
    problem3.id = 3;
  }

  @Test
  public void searchProblemShouldFilter() throws Exception {
    PacketProblem expected1 = TestDataProvider.getProblem();
    expected1.id = 1;
    expected1.testing = true;

    PacketProblem expected2 = TestDataProvider.getProblem();
    expected2.id = 2;
    expected2.testing = true;

    PacketProblem expected3 = TestDataProvider.getProblem();
    expected3.id = 3;

    when(mockGameManager.getTestingProblemIds()).thenReturn(ImmutableSet.of(1, 2));
    when(
        mockDatabase.searchProblem(QUERY, CREATOR, CREATOR_PERFECT_MATCHING, GENRES, TYPES,
            RANDOM_FLAGS)).thenReturn(ImmutableList.of(problem1, problem2, problem3));

    List<PacketProblem> problems = service.searchProblem(QUERY, CREATOR, CREATOR_PERFECT_MATCHING,
        GENRES, TYPES, RANDOM_FLAGS);

    assertEquals(ImmutableList.of(expected1, expected2, expected3), problems);
  }

  @Test
  public void getWrongAnswersShouldReturnRpcSerializableList() throws Exception {
    List<PacketWrongAnswer> wrongAnswers = ImmutableList.of(new PacketWrongAnswer().setAnswer("孤独")
        .setCount(42));
    when(mockDatabase.getPlayerAnswers(PROBLEM_ID)).thenReturn(wrongAnswers);

    List<PacketWrongAnswer> actual = service.getWrongAnswers(PROBLEM_ID);

    assertThat(actual).containsExactlyElementsIn(wrongAnswers).inOrder();
    assertThat(actual).isNotSameInstanceAs(wrongAnswers);
    assertThat(actual).isInstanceOf(java.util.ArrayList.class);
  }

  @Test
  public void getProblemCorrectCounterResetEligibilityShouldDelegateToBadUserManager()
      throws Exception {
    PacketProblem fakeProblem = new PacketProblem();

    when(mockProblemCorrectCounterResetCounter.isAbleToReset(USER_CODE)).thenReturn(true);
    when(mockDatabase.getProblem(ImmutableList.of(PROBLEM_ID))).thenReturn(
        ImmutableList.of(fakeProblem));

    assertTrue(service.resetProblemCorrectCounter(USER_CODE, PROBLEM_ID));

    verify(mockDatabase).updateProblem(fakeProblem);
  }

  @Test
  public void getProblemIndicationEligibilityShouldReturnOkForAvailableEligibile() throws Exception {
    PacketUserData userData = new PacketUserData();
    userData.playerName = "\u30d7\u30ec\u30a4\u30e4\u30fc\u540d";

    when(mockProblemIndicationCounter.isAbleToIndicate(USER_CODE)).thenReturn(true);
    when(mockDatabase.getUserData(USER_CODE)).thenReturn(userData);

    assertThat(service.getProblemIndicationEligibility(USER_CODE)).isEqualTo(
        ProblemIndicationEligibility.OK);
  }

  @Test
  public void getProblemIndicationEligibilityShouldRejectIfTooManyRequests() throws Exception {
    PacketUserData userData = new PacketUserData();
    userData.playerName = "\u30d7\u30ec\u30a4\u30e4\u30fc\u540d";

    when(mockProblemIndicationCounter.isAbleToIndicate(USER_CODE)).thenReturn(false);
    when(mockDatabase.getUserData(USER_CODE)).thenReturn(userData);

    assertThat(service.getProblemIndicationEligibility(USER_CODE)).isEqualTo(
        ProblemIndicationEligibility.REACHED_MAX_NUMBER_OF_REQUESTS_PER_UNIT_TIME);
  }

  @Test
  public void getProblemIndicationEligibilityShouldRejectIfPlayerNameUnchanged() throws Exception {
    PacketUserData userData = new PacketUserData();
    userData.playerName = "\u672a\u521d\u671f\u5316\u3067\u3059";

    when(mockProblemIndicationCounter.isAbleToIndicate(USER_CODE)).thenReturn(true);
    when(mockDatabase.getUserData(USER_CODE)).thenReturn(userData);

    assertThat(service.getProblemIndicationEligibility(USER_CODE)).isEqualTo(
        ProblemIndicationEligibility.PLAYER_NAME_UNCHANGED);
  }

  @Test
  public void addRestrictedUserCodeShouldDelegateToDatabase() throws Exception {
    service.addRestrictedUserCode(USER_CODE, RESTRICTION_TYPE);

    verify(mockDatabase).addRestrictedUserCode(USER_CODE, RESTRICTION_TYPE);
  }

  @Test
  public void addRestrictedUserCodeShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).addRestrictedUserCode(USER_CODE,
        RESTRICTION_TYPE);

    assertThrows(
        ServiceException.class,
        () -> service.addRestrictedUserCode(USER_CODE, RESTRICTION_TYPE));
  }

  @Test
  public void addRestrictedUserCodeShouldNotExposeStackTraceOnError() throws Exception {
    doThrow(new DatabaseException("db failed")).when(mockDatabase).addRestrictedUserCode(USER_CODE,
        RESTRICTION_TYPE);

    ServiceException exception = assertThrows(
        ServiceException.class,
        () -> service.addRestrictedUserCode(USER_CODE, RESTRICTION_TYPE));

    assertEquals(GENERIC_SERVICE_ERROR_MESSAGE, exception.getMessage());
    assertTrue(!exception.getMessage().contains("DatabaseException"));
  }

  @Test
  public void removeRestrictedUserCodeShouldDelegateToDatabase() throws Exception {
    service.removeRestrictedUserCode(USER_CODE, RESTRICTION_TYPE);

    verify(mockDatabase).removeRestrictedUserCode(USER_CODE, RESTRICTION_TYPE);
  }

  @Test
  public void removeRestrictedUserCodeShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).removeRestrictedUserCode(USER_CODE,
        RESTRICTION_TYPE);

    assertThrows(
        ServiceException.class,
        () -> service.removeRestrictedUserCode(USER_CODE, RESTRICTION_TYPE));
  }

  @Test
  public void getRestrictedUserCodesShouldDelegateToDatabase() throws Exception {
    when(mockDatabase.getRestrictedUserCodes(RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(USER_CODE));

    assertEquals(ImmutableSet.of(USER_CODE), service.getRestrictedUserCodes(RESTRICTION_TYPE));
  }

  @Test
  public void getRestrictedUserCodesShouldReturnJavaUtilSetForRpcSerialization() throws Exception {
    when(mockDatabase.getRestrictedUserCodes(RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(USER_CODE));

    Set<Integer> result = service.getRestrictedUserCodes(RESTRICTION_TYPE);

    assertThat(result).containsExactly(USER_CODE);
    assertThat(result.getClass().getName()).doesNotContain("com.google.common.collect");
  }

  @Test
  public void getRestrictedUserCodesShouldThrowExceptionOnError() throws Exception {
    when(mockDatabase.getRestrictedUserCodes(RESTRICTION_TYPE)).thenThrow(new DatabaseException());

    assertThrows(ServiceException.class, () -> service.getRestrictedUserCodes(RESTRICTION_TYPE));
  }

  @Test
  public void clearRestrictedUserCodesShouldDelegateToDatabase() throws Exception {
    service.clearRestrictedUserCodes(RESTRICTION_TYPE);

    verify(mockDatabase).clearRestrictedUserCodes(RESTRICTION_TYPE);
  }

  @Test
  public void clearRestrictedUserCodesShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).clearRestrictedUserCodes(RESTRICTION_TYPE);

    assertThrows(ServiceException.class, () -> service.clearRestrictedUserCodes(RESTRICTION_TYPE));
  }

  @Test
  public void addRestrictedRemoteAddressShouldDelegateToDatabase() throws Exception {
    service.addRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);

    verify(mockDatabase).addRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);
  }

  @Test
  public void addRestrictedRemoteAddressShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).addRestrictedRemoteAddress(REMOTE_ADDRESS,
        RESTRICTION_TYPE);

    assertThrows(
        ServiceException.class,
        () -> service.addRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE));
  }

  @Test
  public void removeRestrictedRemoteAddressShouldDelegateToDatabase() throws Exception {
    service.removeRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);

    verify(mockDatabase).removeRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);
  }

  @Test
  public void removeRestrictedRemoteAddressShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).removeRestrictedRemoteAddress(
        REMOTE_ADDRESS, RESTRICTION_TYPE);

    assertThrows(
        ServiceException.class,
        () -> service.removeRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE));
  }

  @Test
  public void getRestrictedRemoteAddressesShouldDelegateToDatabase() throws Exception {
    when(mockDatabase.getRestrictedRemoteAddresses(RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(REMOTE_ADDRESS));

    assertEquals(ImmutableSet.of(REMOTE_ADDRESS),
        service.getRestrictedRemoteAddresses(RESTRICTION_TYPE));
  }

  @Test
  public void getRestrictedRemoteAddressesShouldReturnJavaUtilSetForRpcSerialization() throws Exception {
    when(mockDatabase.getRestrictedRemoteAddresses(RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(REMOTE_ADDRESS));

    Set<String> result = service.getRestrictedRemoteAddresses(RESTRICTION_TYPE);

    assertThat(result).containsExactly(REMOTE_ADDRESS);
    assertThat(result.getClass().getName()).doesNotContain("com.google.common.collect");
  }

  @Test
  public void getRestrictedRemoteAddressesShouldThrowExceptionOnError() throws Exception {
    when(mockDatabase.getRestrictedRemoteAddresses(RESTRICTION_TYPE)).thenThrow(
        new DatabaseException());

    assertThrows(
        ServiceException.class,
        () -> service.getRestrictedRemoteAddresses(RESTRICTION_TYPE));
  }

  @Test
  public void clearRestrictedRemoteAddressesShouldDelegateToDatabase() throws Exception {
    service.clearRestrictedRemoteAddresses(RESTRICTION_TYPE);

    verify(mockDatabase).clearRestrictedRemoteAddresses(RESTRICTION_TYPE);
  }

  @Test
  public void clearRestrictedRemoteAddressesShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).clearRestrictedRemoteAddresses(
        RESTRICTION_TYPE);

    assertThrows(
        ServiceException.class,
        () -> service.clearRestrictedRemoteAddresses(RESTRICTION_TYPE));
  }

  @Test
  public void lookupUserDataByExternalAccountShouldDelegateToDatabase() throws Exception {
    when(mockDatabase.lookupUserDataByExternalAccount(AUTH_PROVIDER, AUTH_SUBJECT)).thenReturn(
        ImmutableList.of(TestDataProvider.getUserData()));

    assertEquals(
        ImmutableList.of(TestDataProvider.getUserData()),
        service.lookupUserDataByExternalAccount(AUTH_PROVIDER, AUTH_SUBJECT));
  }

  @Test
  public void lookupUserDataByExternalAccountShouldThrowExceptionOnError() throws Exception {
    when(mockDatabase.lookupUserDataByExternalAccount(AUTH_PROVIDER, AUTH_SUBJECT)).thenThrow(
        new DatabaseException());

    assertThrows(
        ServiceException.class,
        () -> service.lookupUserDataByExternalAccount(AUTH_PROVIDER, AUTH_SUBJECT));
  }

  @Test
  public void disconnectExternalAccountShouldDelegateToDatabase() throws Exception {
    service.disconnectExternalAccount(USER_CODE);

    verify(mockDatabase).disconnectExternalAccount(USER_CODE);
  }

  @Test
  public void disconnectExternalAccountShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).disconnectExternalAccount(USER_CODE);

    assertThrows(ServiceException.class, () -> service.disconnectExternalAccount(USER_CODE));
  }

  @Test
  public void canUploadProblemReturnsFalseIfNewProblemWithManyCreations() throws Exception {
    when(mockDatabase.getProblem(ImmutableList.of(PROBLEM_ID))).thenReturn(
        ImmutableList.of(createProblem(ProblemGenre.Anige)));
    when(mockDatabase.getNumberOfCreationLogWithMachineIp(isA(String.class), isA(Long.class)))
        .thenReturn(100);
    when(mockDatabase.getNumberOfCreationLogWithUserCode(isA(Integer.class), isA(Long.class)))
        .thenReturn(100);
    doReturn(REMOTE_ADDRESS).when(service).getRemoteAddress();

    assertThat(service.canUploadProblem(USER_CODE, null)).isFalse();
  }

  @Test
  public void canUploadProblemReturnsTrueIfFromAnigeWithManyCreations() throws Exception {
    when(mockDatabase.getProblem(ImmutableList.of(PROBLEM_ID))).thenReturn(
        ImmutableList.of(createProblem(ProblemGenre.Anige)));
    when(mockDatabase.getNumberOfCreationLogWithMachineIp(isA(String.class), isA(Long.class)))
        .thenReturn(100);
    when(mockDatabase.getNumberOfCreationLogWithUserCode(isA(Integer.class), isA(Long.class)))
        .thenReturn(100);
    doReturn(REMOTE_ADDRESS).when(service).getRemoteAddress();

    assertThat(service.canUploadProblem(USER_CODE, PROBLEM_ID)).isTrue();
  }

  @Test
  public void canUploadProblemReturnsFalseIfFromNonAnigeWithManyCreations() throws Exception {
    when(mockDatabase.getProblem(ImmutableList.of(PROBLEM_ID))).thenReturn(
        ImmutableList.of(createProblem(ProblemGenre.Gakumon)));
    when(mockDatabase.getNumberOfCreationLogWithMachineIp(isA(String.class), isA(Long.class)))
        .thenReturn(100);
    when(mockDatabase.getNumberOfCreationLogWithUserCode(isA(Integer.class), isA(Long.class)))
        .thenReturn(100);
    doReturn(REMOTE_ADDRESS).when(service).getRemoteAddress();

    assertThat(service.canUploadProblem(USER_CODE, PROBLEM_ID)).isFalse();
  }

  @Test
  public void canUploadProblemReturnsTrueIfFromAnigeWithSmallCreations() throws Exception {
    when(mockDatabase.getProblem(ImmutableList.of(PROBLEM_ID))).thenReturn(
        ImmutableList.of(createProblem(ProblemGenre.Anige)));
    when(mockDatabase.getNumberOfCreationLogWithMachineIp(isA(String.class), isA(Long.class)))
        .thenReturn(0);
    when(mockDatabase.getNumberOfCreationLogWithUserCode(isA(Integer.class), isA(Long.class)))
        .thenReturn(0);
    doReturn(REMOTE_ADDRESS).when(service).getRemoteAddress();

    assertThat(service.canUploadProblem(USER_CODE, PROBLEM_ID)).isTrue();
  }

  private static PacketProblem createProblem(ProblemGenre genre) {
    PacketProblem problem = new PacketProblem();
    problem.genre = genre;
    return problem;
  }

  @Test
  public void sendAnswerShouldLogAcceptedWhenGameAcceptsAnswer() throws Exception {
    doReturn(REMOTE_ADDRESS).when(service).getRemoteAddress();
    when(mockGameManager.getSession(10)).thenReturn(mockGame);
    when(mockGame.receiveAnswer(3, "ans")).thenReturn(true);

    service.sendAnswer(10, 3, "ans", USER_CODE, 1234);

    verify(mockGameLogger).write(org.mockito.ArgumentMatchers.contains("accepted=true"));
  }

  @Test
  public void sendAnswerShouldLogAcceptedFalseWhenGameRejectsAnswer() throws Exception {
    doReturn(REMOTE_ADDRESS).when(service).getRemoteAddress();
    when(mockGameManager.getSession(10)).thenReturn(mockGame);
    when(mockGame.receiveAnswer(3, "ans")).thenReturn(false);

    service.sendAnswer(10, 3, "ans", USER_CODE, 1234);

    verify(mockGameLogger).write(org.mockito.ArgumentMatchers.contains("accepted=false"));
  }

  @Test
  public void generateDiffHtmlShouldIncludeSummaryDiffAndFullDiff() throws Exception {
    String before = String.join("\n",
        "ジャンル: ノンジャンル",
        "出題形式: Click",
        "ランダム: 1",
        "問題文:",
        "A",
        "選択肢:",
        "a",
        "b",
        "解答:",
        "a",
        "問題作成者: creator1",
        "問題ノート:",
        "note1",
        "表示選択肢数: 4");
    String after = String.join("\n",
        "ジャンル: ノンジャンル",
        "出題形式: Click",
        "ランダム: 2",
        "問題文:",
        "A+",
        "選択肢:",
        "a",
        "b",
        "c",
        "解答:",
        "a",
        "問題作成者: creator1",
        "問題ノート:",
        "note2",
        "表示選択肢数: 4");

    String html = service.generateDiffHtml(before, after);

    assertThat(html).contains("項目差分");
    assertThat(html).contains("全文差分");
    assertThat(html).contains("ランダムフラグ");
    assertThat(html).contains("note1");
    assertThat(html).contains("note2");
  }

  @Test
  public void generateDiffHtmlShouldReturnNoSummaryChangeMessageWhenSummaryEquals() throws Exception {
    String summary = String.join("\n",
        "ジャンル: ノンジャンル",
        "出題形式: Click",
        "ランダム: 1",
        "問題文:",
        "A",
        "選択肢:",
        "a",
        "b",
        "解答:",
        "a",
        "問題作成者: creator1",
        "問題ノート:",
        "note1",
        "表示選択肢数: 4");

    String html = service.generateDiffHtml(summary, summary);

    assertThat(html).contains("変更はありません。");
  }

  @Test
  public void registerShouldRequestServerStatusUpdateBeforeAndAfterPlayerJoin() throws Exception {
    PacketPlayerSummary playerSummary = new PacketPlayerSummary();
    playerSummary.name = "プレイヤー";
    PlayerStatus status = new PlayerStatus(playerSummary, 1, 2, 3, true, "greeting",
        Constant.ICON_NO_IMAGE, 5, 1200, USER_CODE, 100, 20);

    doReturn(REMOTE_ADDRESS).when(service).getRemoteAddress();
    when(mockGameManager.getOrCreateMatchingSession(eq(GameMode.WHOLE), eq("room"), eq(5), eq("theme"), eq(GENRES),
        eq(TYPES), eq(true), eq(mockServerStatusManager), eq(USER_CODE), eq(REMOTE_ADDRESS))).thenReturn(mockGame);
    when(mockGame.addPlayer(eq(playerSummary), eq(GENRES), eq(TYPES), eq("greeting"),
        eq(Constant.ICON_NO_IMAGE), eq(5), eq(0), eq(1200), eq(USER_CODE), eq(100), eq(20),
        eq(NewAndOldProblems.Both))).thenReturn(status);

    PacketRegistrationData actual = service.register(playerSummary, GENRES, TYPES, "greeting",
        GameMode.WHOLE, "room", "theme", "", 5, 0, 1200, USER_CODE, 100, 20,
        NewAndOldProblems.Both, true);

    assertThat(actual.playerListIndex).isEqualTo(2);
    assertThat(actual.sessionId).isEqualTo(3);
    verify(mockServerStatusManager, times(2)).requestUpdateDebounced();

    InOrder inOrder = inOrder(mockPlayerHistoryManager, mockServerStatusManager, mockGame);
    ArgumentCaptor<PacketPlayerSummary> historyCaptor = ArgumentCaptor.forClass(PacketPlayerSummary.class);
    inOrder.verify(mockPlayerHistoryManager).push(historyCaptor.capture());
    inOrder.verify(mockServerStatusManager).requestUpdateDebounced();
    inOrder.verify(mockGame).addPlayer(eq(playerSummary), eq(GENRES), eq(TYPES), eq("greeting"),
        eq(Constant.ICON_NO_IMAGE), eq(5), eq(0), eq(1200), eq(USER_CODE), eq(100), eq(20),
        eq(NewAndOldProblems.Both));
    inOrder.verify(mockServerStatusManager).requestUpdateDebounced();

    PacketPlayerSummary pushedHistory = historyCaptor.getValue();
    assertThat(pushedHistory).isNotSameInstanceAs(playerSummary);
    assertThat(pushedHistory.recentMode).isEqualTo("全体対戦");
    assertThat(pushedHistory.recentState).isEqualTo("マッチング中");
    assertThat(pushedHistory.userCode).isEqualTo(USER_CODE);
    assertThat(pushedHistory.imageFileName).isEqualTo(Constant.ICON_NO_IMAGE);
  }
}
