package tv.dyndns.kishibe.qmaclone.server.websocket;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.server.ThreadPool;

@RunWith(MockitoJUnitRunner.class)
public class MessageSenderTest {
  public static class FakeMessage {
  }

  private static final String MESSAGE = "message";
  @Mock
  private ThreadPool mockThreadPool;
  @Mock
  private Session mockSession;
  @Mock
  private RemoteEndpoint mockRemoteEndpoint;
  @SuppressWarnings("rawtypes")
  @Mock
  private ScheduledFuture mockScheduledFuture;
  private MessageSender<FakeMessage> sender;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    when(mockThreadPool.scheduleAtFixedRate(any(Runnable.class), eq(25L), eq(25L),
        eq(TimeUnit.SECONDS))).thenReturn(mockScheduledFuture);

    sender = new MessageSender<MessageSenderTest.FakeMessage>(mockThreadPool) {
      @Override
      protected String encode(FakeMessage data) {
        return MESSAGE;
      }
    };
  }

  @Test
  public void sendSendsMessage() {
    when(mockSession.getRemote()).thenReturn(mockRemoteEndpoint);

    sender.join(mockSession);
    sender.send(new FakeMessage());

    verify(mockRemoteEndpoint).sendString(eq(MESSAGE), any());
  }

  @Test
  public void closeClosesSession() {
    sender.join(mockSession);
    sender.close();

    verify(mockSession).close();
    verify(mockScheduledFuture).cancel(false);
  }

  @Test
  public void byeRemovesSession() {
    sender.join(mockSession);
    sender.close();
    sender.send(new FakeMessage());

    verify(mockRemoteEndpoint, times(0)).sendString(eq(MESSAGE), any());
  }
}
