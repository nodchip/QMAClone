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
    ChangeHistoryViewImpl view = new ChangeHistoryViewImpl(new ChangeHistoryView.ChangeHistoryPresenter() {
      @Override
      public void onUpdateDiffTarget(PacketProblemCreationLog before, PacketProblemCreationLog after) {
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
    assertTrue(text.contains("詳細を閉じる"));
    assertTrue(text.contains("詳細を開く"));
  }
}
