package tv.dyndns.kishibe.qmaclone.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * RPC失敗時に共通処理を必ず通す {@link AsyncCallback} の基底クラス。
 *
 * @param <T> レスポンス型
 */
public abstract class RpcAsyncCallback<T> implements AsyncCallback<T> {

  @Override
  public final void onFailure(Throwable caught) {
    ClientReloadPrompter.maybePrompt(caught);
    onFailureRpc(caught);
  }

  /**
   * 失敗時の画面固有処理を記述する。
   *
   * @param caught 発生例外
   */
  protected abstract void onFailureRpc(Throwable caught);
}
