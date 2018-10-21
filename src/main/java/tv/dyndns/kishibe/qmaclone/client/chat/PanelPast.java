package tv.dyndns.kishibe.qmaclone.client.chat;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;

public class PanelPast extends Composite implements ValueChangeHandler<Date>, HasValue<Date> {
	private static final Logger logger = Logger.getLogger(PanelPast.class.getName());
	private static PanelPastUiBinder uiBinder = GWT.create(PanelPastUiBinder.class);

	interface PanelPastUiBinder extends UiBinder<Widget, PanelPast> {
	}

	@UiField(provided = true)
	SimplePager simplePagerHeader;
	@UiField(provided = true)
	SimplePager simplePagerFooter;
	@UiField
	DatePicker datePicker;
	@UiField(provided = true)
	CellListChatLog cellListChatLog;

	public PanelPast() {
		cellListChatLog = new CellListChatLog(new PastChatDataProvider(this));

		final SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		simplePagerHeader = new SimplePager(TextLocation.CENTER, pagerResources, true, 10, true);
		simplePagerHeader.setDisplay(cellListChatLog);
		simplePagerHeader.setPageSize(Constant.CHAT_MAX_RESPONSES);

		simplePagerFooter = new SimplePager(TextLocation.CENTER, pagerResources, true, 10, true);
		simplePagerFooter.setDisplay(cellListChatLog);
		simplePagerFooter.setPageSize(Constant.CHAT_MAX_RESPONSES);

		initWidget(uiBinder.createAndBindUi(this));

		datePicker.addValueChangeHandler(this);
	}

	@Override
	public void onValueChange(ValueChangeEvent<Date> event) {
		final Date value = event.getValue();
		final int year = value.getYear() + 1900;
		final int month = value.getMonth() + 1;
		final int date = value.getDate();
		final int hours = 0;
		final int minutes = 0;
		final int seconds = 0;
		Service.Util.getInstance().getChatLogId(year, month, date, hours, minutes, seconds,
				callbackGetChatLogId);
	}

	private final AsyncCallback<Integer> callbackGetChatLogId = new AsyncCallback<Integer>() {
		@Override
		public void onSuccess(Integer result) {
			final int start = result;
			simplePagerHeader.setPageStart(start);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "チャット過去ログのレス番号の取得に失敗しました", caught);
		}
	};

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
		return datePicker.addValueChangeHandler(handler);
	}

	@Override
	public Date getValue() {
		return datePicker.getValue();
	}

	@Override
	public void setValue(Date value) {
		datePicker.setValue(value);
		datePicker.setCurrentMonth(value);
	}

	@Override
	public void setValue(Date value, boolean fireEvents) {
		datePicker.setValue(value, fireEvents);
		datePicker.setCurrentMonth(value);
	}

	public SimplePager getPager() {
		return simplePagerHeader;
	}
}
