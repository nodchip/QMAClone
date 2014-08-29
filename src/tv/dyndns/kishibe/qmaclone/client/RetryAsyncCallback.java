package tv.dyndns.kishibe.qmaclone.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 自動再試行機能付きの {@link AsyncCallback}.
 * 
 * @author nodchip
 * 
 * @param <T>
 */
public abstract class RetryAsyncCallback<T> implements AsyncCallback<T> {

	public static final int DEFAULT_MAX_RETRY = 3;
	public static final int DEFAULT_RETRY_WAIT = 1000;
	private final int maxRetry;
	private final int retryWaitMs;
	private int retryCount;

	public RetryAsyncCallback() {
		this(DEFAULT_MAX_RETRY);
	}

	public RetryAsyncCallback(int maxRetry) {
		this(maxRetry, DEFAULT_RETRY_WAIT);
	}

	public RetryAsyncCallback(int maxRetry, int retryWaitMs) {
		this.maxRetry = maxRetry;
		this.retryWaitMs = retryWaitMs;
	}

	@Override
	public void onSuccess(T result) {
		if (onReceived(result)) {
			return;
		}

		retry(null);
	}

	@Override
	public void onFailure(Throwable caught) {
		retry(caught);
	}

	public void start() {
		request();
	}

	private void retry(Throwable caught) {
		if (++retryCount > maxRetry) {
			onHeavyFailure(caught);
			return;
		}

		onLightFailure(caught);

		new Timer() {
			@Override
			public void run() {
				request();
			}
		}.schedule(Math.max(1, retryWaitMs));
	}

	/**
	 * RPCリクエストを記述する。
	 */
	protected abstract void request();

	/**
	 * RPCリクエストの結果を受信した場合の処理を記述する
	 * 
	 * @param result
	 *            受信結果
	 * @return　受信に成功した場合はtrue。そうでない場合はfalse。
	 */
	protected abstract boolean onReceived(T result);

	/**
	 * 軽微なエラーが起こった場合の処理を記述する。RPCリクエストの記述は不要。
	 * 
	 * @param caught
	 *            起こったエラー
	 */
	protected abstract void onLightFailure(Throwable caught);

	/**
	 * 重大なエラーが起こった場合の処理を記述する。RPCリクエストはこれ以降行われない。
	 * 
	 * @param caught
	 *            起こったエラー
	 */
	protected abstract void onHeavyFailure(Throwable caught);

}
