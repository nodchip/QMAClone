package tv.dyndns.kishibe.qmaclone.client.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import tv.dyndns.kishibe.qmaclone.client.UserData;

import com.google.common.base.Objects;
import com.google.gwt.core.client.GWT;
import com.google.gwt.logging.client.RemoteLogHandlerBase;
import com.google.gwt.logging.shared.RemoteLoggingService;
import com.google.gwt.logging.shared.RemoteLoggingServiceAsync;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DetailRemoteLogger extends RemoteLogHandlerBase {
	private final AsyncCallback<String> callback = new AsyncCallback<String>() {
		@Override
		public void onSuccess(String result) {
			if (result != null) {
				wireLogger.severe("Remote logging failed: " + result);
			} else {
				wireLogger.finest("Remote logging message acknowledged");
			}
		}

		@Override
		public void onFailure(Throwable caught) {
			wireLogger.log(Level.SEVERE, "Remote logging failed: ", caught);
		}
	};
	private final RemoteLoggingServiceAsync service = (RemoteLoggingServiceAsync) GWT
			.create(RemoteLoggingService.class);

	@Override
	public void publish(LogRecord record) {
		if (isLoggable(record)) {
			record.setMessage(Objects.toStringHelper(this)
					.add("userCode", UserData.get().getUserCode())
					.add("message", record.getMessage())
					.add("userAgent", Window.Navigator.getUserAgent()).toString());
			service.logOnServer(record, callback);
		}
	}
}
