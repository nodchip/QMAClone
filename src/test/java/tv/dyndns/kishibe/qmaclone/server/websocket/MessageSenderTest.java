package tv.dyndns.kishibe.qmaclone.server.websocket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;

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
  private Async mockAsyncRemoteEndpoint;
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
    when(mockSession.getAsyncRemote()).thenReturn(mockAsyncRemoteEndpoint);

    sender.join(mockSession);
    sender.send(new FakeMessage());

    verify(mockAsyncRemoteEndpoint).sendText(eq(MESSAGE), any());
  }

  @Test
  public void closeClosesSession() throws Exception {
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

    verify(mockAsyncRemoteEndpoint, times(0)).sendText(eq(MESSAGE), any());
  }

  @Test
  public void sendShouldSerializeMessagesUntilCallbackReturns() {
    when(mockSession.getAsyncRemote()).thenReturn(mockAsyncRemoteEndpoint);
    List<SendHandler> handlers = new ArrayList<>();
    doAnswer(invocation -> {
      handlers.add(invocation.getArgument(1));
      return null;
    }).when(mockAsyncRemoteEndpoint).sendText(eq(MESSAGE), any(SendHandler.class));

    sender.join(mockSession);
    sender.send(new FakeMessage());
    sender.send(new FakeMessage());

    verify(mockAsyncRemoteEndpoint, times(1)).sendText(eq(MESSAGE), any(SendHandler.class));

    handlers.get(0).onResult(new SendResult());

    verify(mockAsyncRemoteEndpoint, times(2)).sendText(eq(MESSAGE), any(SendHandler.class));
  }
}
