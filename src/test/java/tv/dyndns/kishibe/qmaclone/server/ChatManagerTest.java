package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.server.testing.GuiceInjectionExtension;

import com.google.inject.Inject;

@ExtendWith(GuiceInjectionExtension.class)
public class ChatManagerTest {

  @Inject
  private ChatManager manager;

  @Test
  public void testWriteRead() {
    PacketChatMessage expected = new PacketChatMessage();
    expected.body = "body";
    expected.classLevel = 123;
    expected.date = 123456789L;
    expected.imageFileName = "image.jpg";
    expected.name = "name";
    expected.remoteAddress = "remote address";
    expected.resId = 123;
    expected.userCode = 12345678;

    manager.write(expected, expected.remoteAddress);

    PacketChatMessages list = manager.read(0);
    PacketChatMessage actual = list.list.get(list.list.size() - 1);

    assertEquals(expected.body, actual.body);
    assertEquals(expected.classLevel, actual.classLevel);
    assertEquals(expected.date, actual.date);
    assertEquals(expected.imageFileName, actual.imageFileName);
    assertEquals(expected.name, actual.name);
    assertEquals(expected.remoteAddress, actual.remoteAddress);
    assertEquals(expected.resId, actual.resId);
    assertEquals(expected.userCode, actual.userCode);
  }

  @Test
  public void testGetChatMessagesWebSocketSessions() {
    assertNotNull(manager.getChatMessagesMessageSender());
  }

  // TODO(nodchip): Write tests for write().
}
