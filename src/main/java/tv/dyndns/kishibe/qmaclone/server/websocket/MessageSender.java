package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;

import tv.dyndns.kishibe.qmaclone.server.ThreadPool;

public abstract class MessageSender<T> implements Closeable {
  private static final Logger logger = Logger.getLogger(MessageSender.class.getName());
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
      send("");
    }
  };

  public void send(T data) {
    String json = encode(data);
    send(json);
  }

  private void send(String json) {
    for (Connection connection : sessions.values()) {
      try {
        connection.send(json);
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

  public void join(javax.websocket.Session session) {
    sessions.put(session, new JavaxConnection(session));
  }

  public void bye(javax.websocket.Session session) {
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
    void send(String json);

    void close();

    String getRemoteAddress();

    Object getSessionKey();
  }

  private final class JavaxConnection implements Connection {
    private final javax.websocket.Session session;

    private JavaxConnection(javax.websocket.Session session) {
      this.session = Preconditions.checkNotNull(session);
    }

    @Override
    public void send(String json) {
      session.getAsyncRemote().sendText(json, result -> {
        if (result.isOK()) {
          return;
        }
        logger.log(Level.WARNING,
            "WebSocket でのデータ送信に失敗しました。接続を閉じます。remoteAddress="
                + getRemoteAddress(),
            result.getException());
        close();
        sessions.remove(getSessionKey());
      });
    }

    @Override
    public void close() {
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
