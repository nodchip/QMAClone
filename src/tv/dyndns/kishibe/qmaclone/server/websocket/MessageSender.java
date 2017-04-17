package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;

import com.google.common.base.Preconditions;

import tv.dyndns.kishibe.qmaclone.server.ThreadPool;

public abstract class MessageSender<T> implements Closeable {
  private static final Logger logger = Logger.getLogger(MessageSender.class.getName());
  private final Set<Session> sessions = new CopyOnWriteArraySet<>();
  private final ThreadPool threadPool;
  private final ScheduledFuture<?> futurePing;

  protected MessageSender(ThreadPool threadPool) {
    this.threadPool = Preconditions.checkNotNull(threadPool);
    // 25秒間隔でpingを送る
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
    for (Session session : sessions) {
      try {
        WriteCallback writeCallback = new WriteCallback() {
          @Override
          public void writeSuccess() {
            // Do nothing.
          }

          @Override
          public void writeFailed(Throwable x) {
            logger.log(Level.WARNING, "WebSocketでのデータの送信に失敗しました。接続を閉じます。 remoteAddress="
                + session.getRemoteAddress().toString(), x);
            try {
              session.close();
            } catch (Exception e) {
            }
            bye(session);
          }
        };
        session.getRemote().sendString(json, writeCallback);
      } catch (Exception e) {
        logger.log(Level.WARNING, "WebSocketでのデータの送信に失敗しました。接続を閉じます。 remoteAddress="
            + session.getRemoteAddress().toString(), e);
        try {
          session.close();
        } catch (Exception e2) {
        }
        bye(session);
      }
    }
  }

  @Override
  public void close() {
    futurePing.cancel(false);

    for (Session session : sessions) {
      session.close();
    }

    sessions.clear();
  }

  public void join(Session session) {
    sessions.add(session);
  }

  public void bye(Session session) {
    sessions.remove(session);
  }

  /**
   * T型のオブジェクトを文字列に変換する
   * 
   * @param data
   *          変換元のオブジェクト
   * @return T型のオブジェクトの文字列表現
   */
  protected abstract String encode(T data);
}
