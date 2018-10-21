package tv.dyndns.kishibe.qmaclone.client.chat;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class PastChatDataProvider extends AsyncDataProvider<PacketChatMessage> {
	private static final Logger logger = Logger.getLogger(PastChatDataProvider.class.getName());
	private final PanelPast panelPast;

	public PastChatDataProvider(PanelPast panelPast) {
		this.panelPast = panelPast;
		Service.Util.getInstance().getNumberOfChatLog(callbackGetNumberOfChatLog);
	}

	private final AsyncCallback<Integer> callbackGetNumberOfChatLog = new AsyncCallback<Integer>() {
		@Override
		public void onSuccess(Integer result) {
			updateRowCount(result, true);
			panelPast.getPager().lastPage();
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "チャット過去ログ件数の取得に失敗しました", caught);
		}
	};

	@Override
	protected void onRangeChanged(HasData<PacketChatMessage> display) {
		final Range range = display.getVisibleRange();

		Service.Util.getInstance().getChatLog(range.getStart(),
				new AsyncCallback<List<PacketChatMessage>>() {
					@Override
					public void onSuccess(List<PacketChatMessage> result) {
						updateRowData(range.getStart(), result);
						if (result.isEmpty()) {
							return;
						}
						final PacketChatMessage first = result.get(0);
						panelPast.setValue(new Date(first.date), false);
					}

					@Override
					public void onFailure(Throwable caught) {
						logger.log(Level.WARNING, "チャット過去ログの取得に失敗しました", caught);
					}
				});
	}
}
