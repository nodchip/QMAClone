package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;

/**
 * ChangeHistoryViewImpl の表示仕様を検証するテスト。
 */
public class ChangeHistoryViewImplTest extends QMACloneGWTTestCaseBase {

  @Test
  public void shouldRenderNewerLogBeforeOlderLogAndExpandLatest() {
    final PacketProblemCreationLog[] selectedBefore = new PacketProblemCreationLog[1];
    final PacketProblemCreationLog[] selectedAfter = new PacketProblemCreationLog[1];
    ChangeHistoryViewImpl view = new ChangeHistoryViewImpl(new ChangeHistoryView.ChangeHistoryPresenter() {
      @Override
      public void onUpdateDiffTarget(PacketProblemCreationLog before, PacketProblemCreationLog after) {
        selectedBefore[0] = before;
        selectedAfter[0] = after;
      }
    });

    PacketProblemCreationLog older = new PacketProblemCreationLog();
    older.name = "older";
    older.userCode = 1;
    older.ip = "127.0.0.1";
    older.date = new Date(1000L);
    older.summary = "old summary";

    PacketProblemCreationLog newer = new PacketProblemCreationLog();
    newer.name = "newer";
    newer.userCode = 2;
    newer.ip = "127.0.0.2";
    newer.date = new Date(2000L);
    newer.summary = "new summary";

    view.setCreationLog(Arrays.asList(older, newer));
    String text = view.getElement().getInnerText();

    assertTrue(text.indexOf("newer") < text.indexOf("older"));
    assertFalse(text.contains("詳細を開く"));
    assertFalse(text.contains("詳細を閉じる"));
    assertEquals(older, selectedBefore[0]);
    assertEquals(newer, selectedAfter[0]);
  }

  @Test
  public void shouldShowNeedMoreHistoryMessageWhenOnlyOneLogExists() {
    ChangeHistoryViewImpl view = new ChangeHistoryViewImpl(new ChangeHistoryView.ChangeHistoryPresenter() {
      @Override
      public void onUpdateDiffTarget(PacketProblemCreationLog before, PacketProblemCreationLog after) {
      }
    });

    PacketProblemCreationLog single = new PacketProblemCreationLog();
    single.summary = "問題文: 1件のみ";
    single.date = new Date(1000L);

    view.setCreationLog(Arrays.asList(single));
    String text = view.getElement().getInnerText();

    assertTrue(text.contains("比較対象が不足しています。"));
  }
}
