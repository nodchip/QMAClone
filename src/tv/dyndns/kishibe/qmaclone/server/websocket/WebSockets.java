package tv.dyndns.kishibe.qmaclone.server.websocket;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.WebSocket;

import tv.dyndns.kishibe.qmaclone.server.ThreadPool;

import com.google.common.base.Preconditions;

public abstract class WebSockets<T> implements Closeable {

  private static class NullWebSocket implements WebSocket {
    private final Set<NullWebSocket> webSockets;
    private volatile Connection connection;

    private NullWebSocket(Set<NullWebSocket> broadcast) {
      this.webSockets = Preconditions.checkNotNull(broadcast);
    }

    @Override
    public synchronized void onOpen(Connection connection) {
      this.connection = connection;
      webSockets.add(this);
    }

    @Override
    public synchronized void onClose(int closeCode, String message) {
      close();
    }

    public void close() {
      if (connection != null) {
        connection.close();
        connection = null;
      }
      webSockets.remove(this);
    }

    public synchronized void sendMessage(String message) {
      // タイムアウト直後にエラーメッセージが大量に表示されるのをふぐためsynchronizedでスレッドを１つに限定する
      if (connection == null || !connection.isOpen()) {
        close();
        return;
      }

      try {
        connection.sendMessage(message);
      } catch (IOException e) {
        logger.log(Level.WARNING, "WebSocketでのデータ送信に失敗しました。接続を閉じます。\n" + message, e);
        close();
      }
    }
  }

  private static final Logger logger = Logger.getLogger(WebSockets.class.getName());
  private final Set<NullWebSocket> webSockets = new CopyOnWriteArraySet<NullWebSocket>();
  private final ThreadPool threadPool;
  private final ScheduledFuture<?> futurePing;

  protected WebSockets(ThreadPool threadPool) {
    this.threadPool = Preconditions.checkNotNull(threadPool);
    // 25秒間隔でpingを送る
    futurePing = threadPool.scheduleAtFixedRate(runnablePing, 25, 25, TimeUnit.SECONDS);
  }

  private final Runnable runnablePing = new Runnable() {
    @Override
    public void run() {
      send("");
    }
  };

  public synchronized void send(T data) {
    final String json = encode(data);
    send(json);
  }

  private void send(final String json) {
    for (final NullWebSocket webSocket : webSockets) {
      threadPool.submit(new Runnable() {
        @Override
        public void run() {
          webSocket.sendMessage(json);
        }
      });
    }
  }

  @Override
  public void close() {
    futurePing.cancel(false);

    for (NullWebSocket webSocket : webSockets) {
      webSocket.close();
    }

    webSockets.clear();
  }

  protected abstract String encode(T data);

  public WebSocket newWebSocket() {
    return new NullWebSocket(webSockets);
  }
}
