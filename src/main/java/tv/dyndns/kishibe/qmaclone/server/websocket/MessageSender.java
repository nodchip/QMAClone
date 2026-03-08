package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;

import tv.dyndns.kishibe.qmaclone.server.ThreadPool;

public abstract class MessageSender<T> implements Closeable {
  private static final Logger logger = Logger.getLogger(MessageSender.class.getName());
  private static final int MAX_PENDING_MESSAGES = 256;
  private final Map<Object, Connection> sessions = new ConcurrentHashMap<>();
  private final ThreadPool threadPool;
  private final ScheduledFuture<?> futurePing;

  protected MessageSender(ThreadPool threadPool) {
    this.threadPool = Preconditions.checkNotNull(threadPool);
    // 25秒ごとに ping 相当の空文字を送る。
    futurePing = threadPool.scheduleAtFixedRate(runnablePing, 25L, 25L, TimeUnit.SECONDS);
  }

  private final Runnable runnablePing = new Runnable() {
    @Override
    public void run() {
      send("", true);
    }
  };

  public void send(T data) {
    String json = encode(data);
    send(json, false);
  }

  private void send(String json, boolean ping) {
    for (Connection connection : sessions.values()) {
      try {
        connection.send(json, ping);
      } catch (Exception e) {
        logger.log(Level.WARNING,
            "WebSocket でのデータ送信に失敗しました。接続を閉じます。remoteAddress="
                + connection.getRemoteAddress(),
            e);
        connection.close();
        sessions.remove(connection.getSessionKey());
      }
    }
  }

  @Override
  public void close() {
    futurePing.cancel(false);

    for (Connection connection : sessions.values()) {
      connection.close();
    }

    sessions.clear();
  }

  public void join(jakarta.websocket.Session session) {
    sessions.put(session, new JavaxConnection(session));
  }

  public void bye(jakarta.websocket.Session session) {
    sessions.remove(session);
  }

  /**
   * T型のオブジェクトを文字列に変換する。
   *
   * @param data 変換するオブジェクト
   * @return 文字列表現
   */
  protected abstract String encode(T data);

  private interface Connection {
    void send(String json, boolean ping);

    void close();

    String getRemoteAddress();

    Object getSessionKey();
  }

  private final class JavaxConnection implements Connection {
    private final jakarta.websocket.Session session;
    private final Queue<String> pendingMessages = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean sending = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean pingQueued = new AtomicBoolean(false);

    private JavaxConnection(jakarta.websocket.Session session) {
      this.session = Preconditions.checkNotNull(session);
    }

    @Override
    public void send(String json, boolean ping) {
      if (closed.get()) {
        return;
      }
      if (ping) {
        if (!pingQueued.compareAndSet(false, true)) {
          return;
        }
      } else if (pendingMessages.size() >= MAX_PENDING_MESSAGES) {
        logger.log(Level.WARNING,
            "WebSocket の送信キューが上限を超えたため接続を閉じます。remoteAddress="
                + getRemoteAddress());
        closeAndRemove();
        return;
      }
      pendingMessages.offer(json);
      flushPendingMessages();
    }

    private void flushPendingMessages() {
      if (!sending.compareAndSet(false, true)) {
        return;
      }
      sendNext();
    }

    private void sendNext() {
      if (closed.get()) {
        sending.set(false);
        pendingMessages.clear();
        return;
      }

      String next = pendingMessages.poll();
      if (next == null) {
        sending.set(false);
        if (!pendingMessages.isEmpty() && sending.compareAndSet(false, true)) {
          sendNext();
        }
        return;
      }

      session.getAsyncRemote().sendText(next, result -> {
        if (next.isEmpty()) {
          pingQueued.set(false);
        }
        if (!result.isOK()) {
          logger.log(Level.WARNING,
              "WebSocket でのデータ送信に失敗しました。接続を閉じます。remoteAddress="
                  + getRemoteAddress(),
              result.getException());
          sending.set(false);
          closeAndRemove();
          return;
        }
        sendNext();
      });
    }

    private void closeAndRemove() {
      pendingMessages.clear();
      pingQueued.set(false);
      close();
      sessions.remove(getSessionKey());
    }

    @Override
    public void close() {
      if (!closed.compareAndSet(false, true)) {
        return;
      }
      pingQueued.set(false);
      try {
        session.close();
      } catch (IOException e) {
      }
    }

    @Override
    public String getRemoteAddress() {
      if (session.getRequestURI() == null) {
        return "unknown";
      }
      return session.getRequestURI().toString();
    }

    @Override
    public Object getSessionKey() {
      return session;
    }
  }
}
