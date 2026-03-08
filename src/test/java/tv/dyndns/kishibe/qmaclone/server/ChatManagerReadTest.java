package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.websocket.MessageSender;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
public class ChatManagerReadTest {

  @Mock
  private Database database;
  @Mock
  private ThreadPool threadPool;
  @Mock
  private ChatPostCounter chatPostCounter;
  @Mock
  private MessageSender<PacketChatMessages> messageSender;

  @Test
  public void readShouldCheckRestrictionsOnlyForUnreadMessages() throws Exception {
    when(database.getLatestChatData()).thenReturn(createMessages());
    when(database.getRestrictedUserCodes(RestrictionType.CHAT)).thenReturn(ImmutableSet.of());
    when(database.getRestrictedRemoteAddresses(RestrictionType.CHAT)).thenReturn(ImmutableSet.of());

    RestrictedUserUtils restrictedUserUtils = new RestrictedUserUtils(database);
    ChatManager manager =
        new ChatManager(
            database,
            threadPool,
            restrictedUserUtils,
            chatPostCounter,
            threadPool,
            messageSender);

    PacketChatMessages actual = manager.read(3);

    assertEquals(1, actual.list.size());
    assertEquals(3, actual.list.get(0).resId);
    verify(database, times(1)).getRestrictedUserCodes(RestrictionType.CHAT);
    verify(database, times(1)).getRestrictedRemoteAddresses(RestrictionType.CHAT);
  }

  /**
   * 読み込み用のチャットメッセージ群を作成する。
   *
   * @return レスポンスID順のメッセージ
   */
  private static NavigableMap<Integer, PacketChatMessage> createMessages() {
    NavigableMap<Integer, PacketChatMessage> messages = new ConcurrentSkipListMap<Integer, PacketChatMessage>();
    messages.put(1, createMessage(1, 101, "1.1.1.1"));
    messages.put(2, createMessage(2, 102, "2.2.2.2"));
    messages.put(3, createMessage(3, 103, "3.3.3.3"));
    return messages;
  }

  /**
   * テスト用チャットメッセージを作成する。
   *
   * @param resId レスポンスID
   * @param userCode ユーザーコード
   * @param remoteAddress リモートアドレス
   * @return 生成したメッセージ
   */
  private static PacketChatMessage createMessage(int resId, int userCode, String remoteAddress) {
    PacketChatMessage message = new PacketChatMessage();
    message.resId = resId;
    message.userCode = userCode;
    message.remoteAddress = remoteAddress;
    return message;
  }
}
