package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.server.testing.QMACloneTestEnv;

@RunWith(JUnit4.class)
public class ChatManagerTest {

  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(QMACloneTestEnv.class);
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

  // TODO(nodchip): Write tests for write().
}
