package tv.dyndns.kishibe.qmaclone.server;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;

public class PlayerHistoryManagerTest {
  @Test
  public void pushShouldKeepAtMostTenPlayers() {
    PlayerHistoryManager manager = new PlayerHistoryManager();
    for (int i = 1; i <= 12; i++) {
      PacketPlayerSummary summary = new PacketPlayerSummary();
      summary.userCode = i;
      summary.name = "player_" + i;
      manager.push(summary);
    }

    List<PacketPlayerSummary> actual = manager.get();
    assertThat(actual).hasSize(10);
    assertThat(actual.get(0).name).isEqualTo("player_12");
    assertThat(actual.get(9).name).isEqualTo("player_3");
  }

  @Test
  public void pushShouldKeepSingleEntryPerUser() {
    PlayerHistoryManager manager = new PlayerHistoryManager();
    PacketPlayerSummary first = new PacketPlayerSummary();
    first.userCode = 1;
    first.name = "first";
    manager.push(first);

    PacketPlayerSummary second = new PacketPlayerSummary();
    second.userCode = 2;
    second.name = "second";
    manager.push(second);

    PacketPlayerSummary firstAgain = new PacketPlayerSummary();
    firstAgain.userCode = 1;
    firstAgain.name = "firstAgain";
    manager.push(firstAgain);

    List<PacketPlayerSummary> actual = manager.get();
    assertThat(actual).hasSize(2);
    assertThat(actual.get(0).name).isEqualTo("firstAgain");
    assertThat(actual.get(1).name).isEqualTo("second");
  }
}
