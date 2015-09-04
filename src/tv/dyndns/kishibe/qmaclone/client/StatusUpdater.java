package tv.dyndns.kishibe.qmaclone.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * ポーリングを行うための補助クラス。タイマー+RPCによるポーリングとWebSocketによるポーリングエミュレーションの両方に対応している。
 * 
 * @author nodchip
 * @param <T>
 */
public abstract class StatusUpdater<T> {

  @VisibleForTesting
  enum Status {
    WEB_SOCKET, RPC, CLOSED
  }

  private static final Logger logger = Logger.getLogger(StatusUpdater.class.getName());
  private static final int MAX_RESPONSE_RECIEVE_FAILED_COUNT = 5;

  private final int intervalMs;
  private int responseRecieveFailedCount = 0;
  @VisibleForTesting
  Status status;
  @VisibleForTesting
  final RepeatingCommand commandUpdate = new RepeatingCommand() {
    @Override
    public boolean execute() {
      if (status != Status.RPC) {
        return false;
      }

      try {
        request(callback);
      } catch (Exception e) {
        logger.log(Level.WARNING, "リクエスト中にエラーが発生しました", e);
      }
      return true;
    }
  };
  @VisibleForTesting
  final AsyncCallback<T> callback = new AsyncCallback<T>() {
    @Override
    public void onSuccess(T result) {
      try {
        onReceived(result);
      } catch (Exception e) {
        logger.log(Level.WARNING, "レスポンス処理中にエラーが発生しました(RPC)", e);
      }
    };

    @Override
    public void onFailure(Throwable caught) {
      if (++responseRecieveFailedCount < MAX_RESPONSE_RECIEVE_FAILED_COUNT) {
        logger.log(Level.WARNING, "レスポンス取得中にエラーが発生しました", caught);
      } else {
        logger.log(Level.SEVERE, "レスポンス取得中にエラーが発生しました。致命的なエラーを避けるため通信を終了します。ページをリロードして下さい。",
            caught);
        stop();
      }
    }
  };

  public StatusUpdater(String path, int intervalMs) {
    Preconditions.checkArgument(intervalMs > 0);
    this.intervalMs = intervalMs;
  }

  public void start() {
    status = Status.RPC;
    Scheduler.get().scheduleFixedDelay(commandUpdate, intervalMs);
  }

  public void stop() {
    status = Status.CLOSED;
  }

  /**
   * RPCによるリクエストを行う。コールバックメソッドとして第一引数のcallbackを渡さなければならない。
   * 
   * @param callback
   */
  protected abstract void request(AsyncCallback<T> callback);

  /**
   * メッセージを取得した後の動作を行う
   * 
   * @param status
   *          ステータス
   */
  protected abstract void onReceived(T status);

  /**
   * jsonをパースしてメッセージに変換する
   * 
   * @param json
   *          json文字列
   * @return メッセージ
   */
  protected abstract T parse(String json);

}