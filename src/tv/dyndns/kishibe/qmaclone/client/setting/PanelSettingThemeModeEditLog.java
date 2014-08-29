package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class PanelSettingThemeModeEditLog {

	interface View extends IsWidget {
		void setPresenter(PanelSettingThemeModeEditLog presenter);

		void setNumberOfEntries(int numberOfEntries);

		void setLog(int start, List<PacketThemeModeEditLog> log);
	}

	private static final Logger logger = Logger.getLogger(PanelSettingThemeModeEditLog.class
			.getName());
	private final View view;
	private final ServiceAsync serviceAsync;

	public PanelSettingThemeModeEditLog(View view, ServiceAsync serviceAsync) {
		this.view = Preconditions.checkNotNull(view);
		this.serviceAsync = Preconditions.checkNotNull(serviceAsync);
		view.setPresenter(this);
		serviceAsync.getNumberOfThemeModeEditLog(callbackGetNumberOfThemeModeEditLog);
	}

	private final AsyncCallback<Integer> callbackGetNumberOfThemeModeEditLog = new AsyncCallback<Integer>() {
		@Override
		public void onSuccess(Integer result) {
			view.setNumberOfEntries(result);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマモード編集ログの数の取得に失敗しました", caught);
		}
	};

	public IsWidget asWidget() {
		return view;
	}

	public void onThemeModeEditLogRequest(final int start, int length) {
		AsyncCallback<List<PacketThemeModeEditLog>> callbackGetThemeModeEditLog = new AsyncCallback<List<PacketThemeModeEditLog>>() {
			@Override
			public void onSuccess(List<PacketThemeModeEditLog> result) {
				view.setLog(start, result);
			}

			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.WARNING, "テーマモード編集ログの取得に失敗しました", caught);
			}
		};
		serviceAsync.getThemeModeEditLog(start, length, callbackGetThemeModeEditLog);
	}
}
