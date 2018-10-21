package tv.dyndns.kishibe.qmaclone.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.zschech.gwt.websockets.client.CloseHandler;
import net.zschech.gwt.websockets.client.ErrorHandler;
import net.zschech.gwt.websockets.client.MessageEvent;
import net.zschech.gwt.websockets.client.MessageHandler;
import net.zschech.gwt.websockets.client.WebSocket;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData.WebSocketUsage;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.JavaScriptException;
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
	private static final int ENABLED_USER_CODE_LOWER_DIGIT = 10;
	private static final int MAX_WEBSOCKET_FAILED_COUNT = 2;
	private static final int MAX_RESPONSE_RECIEVE_FAILED_COUNT = 5;

	private final String path;
	private final int intervalMs;
	private WebSocket webSocket;
	private int webSocketFailedCount = 0;
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
				logger.log(Level.SEVERE,
						"レスポンス取得中にエラーが発生しました。致命的なエラーを避けるため通信を終了します。ページをリロードして下さい。", caught);
				stop();
			}
		}
	};

	public StatusUpdater(String path, int intervalMs) {
		this.path = Preconditions.checkNotNull(path);
		this.intervalMs = intervalMs;
	}

	public void start() {
		if (isWebSocketUsed()) {
			try {
				webSocket = WebSocket.create(Constant.WEB_SOCKET_URL + path);
			} catch (JavaScriptException e) {
				// WebSocket is not supported.
			}
		}

		if (webSocket == null) {
			status = Status.RPC;
			Scheduler.get().scheduleFixedDelay(commandUpdate, intervalMs);
		} else {
			status = Status.WEB_SOCKET;
			webSocket.setOnMessage(messageHandler);
			webSocket.setOnClose(closeHandler);
			webSocket.setOnError(errorHandler);
		}
	}

	@VisibleForTesting
	boolean isWebSocketUsed() {
		WebSocketUsage webSocketUsage = UserData.get().getWebSocketUsage();
		return webSocketUsage == WebSocketUsage.On || webSocketUsage == WebSocketUsage.Default
				&& UserData.get().getUserCode() % 10 <= ENABLED_USER_CODE_LOWER_DIGIT;
	}

	private final MessageHandler messageHandler = new MessageHandler() {
		@Override
		public void onMessage(WebSocket webSocket, MessageEvent event) {
			String data = event.getData();

			// pingは無視する
			if (data.isEmpty()) {
				return;
			}

			// System.out.println(data);

			T status;
			try {
				status = parse(data);
			} catch (Exception e) {
				if (++webSocketFailedCount >= MAX_WEBSOCKET_FAILED_COUNT) {
					logger.log(Level.WARNING, "レスポンスのパース中にエラーが発生しました。フォールバックします。", e);
					fallback(webSocket);
				} else {
					logger.log(Level.WARNING, "レスポンスのパース中にエラーが発生しました。続行します。", e);
				}

				return;
			}

			try {
				onReceived(status);
			} catch (Exception e) {
				if (++webSocketFailedCount >= MAX_WEBSOCKET_FAILED_COUNT) {
					logger.log(Level.WARNING, "レスポンス処理中にエラーが発生しました(WebSocket)。フォールバックします。", e);
					fallback(webSocket);
				} else {
					logger.log(Level.WARNING, "レスポンス処理中にエラーが発生しました(WebSocket)。続行します。", e);
				}
			}
		}
	};

	private final CloseHandler closeHandler = new CloseHandler() {
		@Override
		public void onClose(WebSocket webSocket) {
			if (status == Status.WEB_SOCKET) {
				fallback(webSocket);
			}
		}
	};

	private final ErrorHandler errorHandler = new ErrorHandler() {
		@Override
		public void onError(WebSocket webSocket) {
			if (++webSocketFailedCount >= MAX_WEBSOCKET_FAILED_COUNT) {
				logger.log(Level.WARNING, "WebSocket通信中にエラーが発生しました。フォールバックします。");
				fallback(webSocket);
			} else {
				logger.log(Level.WARNING, "WebSocket通信中にエラーが発生しました。続行します。");
			}
		}
	};

	private void fallback(WebSocket webSocket) {
		if (status == Status.WEB_SOCKET) {
			status = Status.RPC;
			webSocket.close();
			webSocket = null;
			Scheduler.get().scheduleFixedDelay(commandUpdate, intervalMs);
		}
	}

	public void stop() {
		if (status == Status.WEB_SOCKET) {
			status = Status.CLOSED;
			webSocket.close();
			webSocket = null;
		}
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
	 *            ステータス
	 */
	protected abstract void onReceived(T status);

	/**
	 * jsonをパースしてメッセージに変換する
	 * 
	 * @param json
	 *            　json文字列
	 * @return メッセージ
	 */
	protected abstract T parse(String json);

}