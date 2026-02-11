package tv.dyndns.kishibe.qmaclone.server.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider;
import tv.dyndns.kishibe.qmaclone.server.testing.GuiceInjectionExtension;

@ExtendWith(GuiceInjectionExtension.class)
public class DatabaseReentrySmokeTest {

  private static final int USER_CODE = 81234567;

  @Inject
  private CachedDatabase database;

  @BeforeEach
  public void setUp() {
    database.clearCache();
  }

  @Test
  public void setAndGetUserDataShouldRoundTrip() throws Exception {
    PacketUserData expected = TestDataProvider.getUserData();
    expected.userCode = USER_CODE;
    expected.playerName = "ReentrySmokeUser";

    database.setUserData(expected);
    PacketUserData actual = database.getUserData(USER_CODE);

    assertNotNull(actual);
    assertEquals(USER_CODE, actual.userCode);
    assertEquals(expected.playerName, actual.playerName);
  }

  @Test
  public void addAndGetProblemShouldRoundTrip() throws Exception {
    PacketProblem expected = TestDataProvider.getProblem();
    expected.sentence = "再投入スモーク用問題文";
    expected.creator = "ReentrySmokeCreator";
    expected.voteGood = 0;
    expected.voteBad = 0;
    expected.id = database.addProblem(expected);

    List<PacketProblem> actual = database.getProblem(Arrays.asList(expected.id));
    assertNotNull(actual);
    assertFalse(actual.isEmpty());
    assertEquals(expected.id, actual.get(0).id);
    assertEquals(expected.sentence, actual.get(0).sentence);
  }

  @Test
  public void searchProblemShouldFindInsertedProblem() throws Exception {
    PacketProblem expected = TestDataProvider.getProblem();
    expected.sentence = "再投入スモーク検索文";
    expected.creator = "ReentrySmokeSearcher";
    expected.id = database.addProblem(expected);

    List<PacketProblem> results = database.searchProblem(
        "再投入スモーク検索文",
        null,
        false,
        EnumSet.of(expected.genre),
        EnumSet.of(expected.type),
        EnumSet.of(expected.randomFlag));

    assertNotNull(results);
    assertTrue(results.stream().anyMatch(p -> p.id == expected.id));
  }
}
