package tv.dyndns.kishibe.qmaclone.server.websocket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tv.dyndns.kishibe.qmaclone.server.ThreadPool;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
  @BeforeEach
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
