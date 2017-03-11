package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
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

@RunWith(MockitoJUnitRunner.class)
public class ServiceServletStubTest {
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
  private static final String GOOGLE_PLUS_ID = "fake google plus id";

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
  private Server mockServer;
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

  @Before
  public void setUp() throws Exception {
    service = spy(new ServiceServletStub(mockChatManager, mockNormalModeProblemManager,
        mockThemeModeProblemManager, mockGameManager, mockServerStatusManager,
        mockPlayerHistoryManager, mockVoteManager, mockRecognizer, mockThemeModeEditorManager,
        mockServer, mockDatabase, mockPrefectureRanking, mockRatingDistribution,
        mockSnsClient, mockGameLogger, mockThreadPool, mockBadUserDetector,
        mockRestrictedUserUtils, mockProblemCorrectCounterResetCounter,
        mockProblemIndicationCounter, mockBrokenImageLinkDetector));
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
    userData.playerName = "プレイヤー名";

    when(mockProblemIndicationCounter.isAbleToIndicate(USER_CODE)).thenReturn(true);
    when(mockDatabase.getUserData(USER_CODE)).thenReturn(userData);

    assertThat(service.getProblemIndicationEligibility(USER_CODE)).isEqualTo(
        ProblemIndicationEligibility.OK);
  }

  @Test
  public void getProblemIndicationEligibilityShouldRejectIfTooManyRequests() throws Exception {
    PacketUserData userData = new PacketUserData();
    userData.playerName = "プレイヤー名";

    when(mockProblemIndicationCounter.isAbleToIndicate(USER_CODE)).thenReturn(false);
    when(mockDatabase.getUserData(USER_CODE)).thenReturn(userData);

    assertThat(service.getProblemIndicationEligibility(USER_CODE)).isEqualTo(
        ProblemIndicationEligibility.REACHED_MAX_NUMBER_OF_REQUESTS_PER_UNIT_TIME);
  }

  @Test
  public void getProblemIndicationEligibilityShouldRejectIfPlayerNameUnchanged() throws Exception {
    PacketUserData userData = new PacketUserData();
    userData.playerName = "未初期化です";

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

  @Test(expected = ServiceException.class)
  public void addRestrictedUserCodeShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).addRestrictedUserCode(USER_CODE,
        RESTRICTION_TYPE);

    service.addRestrictedUserCode(USER_CODE, RESTRICTION_TYPE);
  }

  @Test
  public void removeRestrictedUserCodeShouldDelegateToDatabase() throws Exception {
    service.removeRestrictedUserCode(USER_CODE, RESTRICTION_TYPE);

    verify(mockDatabase).removeRestrictedUserCode(USER_CODE, RESTRICTION_TYPE);
  }

  @Test(expected = ServiceException.class)
  public void removeRestrictedUserCodeShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).removeRestrictedUserCode(USER_CODE,
        RESTRICTION_TYPE);

    service.removeRestrictedUserCode(USER_CODE, RESTRICTION_TYPE);
  }

  @Test
  public void getRestrictedUserCodesShouldDelegateToDatabase() throws Exception {
    when(mockDatabase.getRestrictedUserCodes(RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(USER_CODE));

    assertEquals(ImmutableSet.of(USER_CODE), service.getRestrictedUserCodes(RESTRICTION_TYPE));
  }

  @Test(expected = ServiceException.class)
  public void getRestrictedUserCodesShouldThrowExceptionOnError() throws Exception {
    when(mockDatabase.getRestrictedUserCodes(RESTRICTION_TYPE)).thenThrow(new DatabaseException());

    service.getRestrictedUserCodes(RESTRICTION_TYPE);
  }

  @Test
  public void clearRestrictedUserCodesShouldDelegateToDatabase() throws Exception {
    service.clearRestrictedUserCodes(RESTRICTION_TYPE);

    verify(mockDatabase).clearRestrictedUserCodes(RESTRICTION_TYPE);
  }

  @Test(expected = ServiceException.class)
  public void clearRestrictedUserCodesShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).clearRestrictedUserCodes(RESTRICTION_TYPE);

    service.clearRestrictedUserCodes(RESTRICTION_TYPE);
  }

  @Test
  public void addRestrictedRemoteAddressShouldDelegateToDatabase() throws Exception {
    service.addRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);

    verify(mockDatabase).addRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);
  }

  @Test(expected = ServiceException.class)
  public void addRestrictedRemoteAddressShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).addRestrictedRemoteAddress(REMOTE_ADDRESS,
        RESTRICTION_TYPE);

    service.addRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);
  }

  @Test
  public void removeRestrictedRemoteAddressShouldDelegateToDatabase() throws Exception {
    service.removeRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);

    verify(mockDatabase).removeRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);
  }

  @Test(expected = ServiceException.class)
  public void removeRestrictedRemoteAddressShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).removeRestrictedRemoteAddress(
        REMOTE_ADDRESS, RESTRICTION_TYPE);

    service.removeRestrictedRemoteAddress(REMOTE_ADDRESS, RESTRICTION_TYPE);
  }

  @Test
  public void getRestrictedRemoteAddressesShouldDelegateToDatabase() throws Exception {
    when(mockDatabase.getRestrictedRemoteAddresses(RESTRICTION_TYPE)).thenReturn(
        ImmutableSet.of(REMOTE_ADDRESS));

    assertEquals(ImmutableSet.of(REMOTE_ADDRESS),
        service.getRestrictedRemoteAddresses(RESTRICTION_TYPE));
  }

  @Test(expected = ServiceException.class)
  public void getRestrictedRemoteAddressesShouldThrowExceptionOnError() throws Exception {
    when(mockDatabase.getRestrictedRemoteAddresses(RESTRICTION_TYPE)).thenThrow(
        new DatabaseException());

    service.getRestrictedRemoteAddresses(RESTRICTION_TYPE);
  }

  @Test
  public void clearRestrictedRemoteAddressesShouldDelegateToDatabase() throws Exception {
    service.clearRestrictedRemoteAddresses(RESTRICTION_TYPE);

    verify(mockDatabase).clearRestrictedRemoteAddresses(RESTRICTION_TYPE);
  }

  @Test(expected = ServiceException.class)
  public void clearRestrictedRemoteAddressesShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).clearRestrictedRemoteAddresses(
        RESTRICTION_TYPE);

    service.clearRestrictedRemoteAddresses(RESTRICTION_TYPE);
  }

  @Test
  public void lookupUserCodeByGooglePlusIdShouldDelegateToDatabase() throws Exception {
    when(mockDatabase.lookupUserCodeByGooglePlusId(GOOGLE_PLUS_ID)).thenReturn(
        ImmutableList.of(TestDataProvider.getUserData()));

    assertEquals(ImmutableList.of(TestDataProvider.getUserData()),
        service.lookupUserDataByGooglePlusId(GOOGLE_PLUS_ID));
  }

  @Test(expected = ServiceException.class)
  public void lookupUserCodeByGooglePlusIdShouldThrowExceptionOnError() throws Exception {
    when(mockDatabase.lookupUserCodeByGooglePlusId(GOOGLE_PLUS_ID)).thenThrow(
        new DatabaseException());

    assertEquals(USER_CODE, service.lookupUserDataByGooglePlusId(GOOGLE_PLUS_ID));
  }

  @Test
  public void disconnectUserCodeShouldDelegateToDatabase() throws Exception {
    service.disconnectUserCode(USER_CODE);

    verify(mockDatabase).disconnectUserCodeFromGooglePlus(USER_CODE);
  }

  @Test(expected = ServiceException.class)
  public void disconnectUserCodeShouldThrowExceptionOnError() throws Exception {
    doThrow(new DatabaseException()).when(mockDatabase).disconnectUserCodeFromGooglePlus(USER_CODE);

    service.disconnectUserCode(USER_CODE);
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
}
