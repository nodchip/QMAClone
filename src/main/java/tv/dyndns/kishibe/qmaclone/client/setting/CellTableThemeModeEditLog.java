package tv.dyndns.kishibe.qmaclone.client.setting;

import static java.lang.Math.max;

import java.util.Date;

import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;

public class CellTableThemeModeEditLog extends CellTable<PacketThemeModeEditLog> {

	private static final int NUMBER_OF_ROWS = 100;
	private final PanelSettingThemeModeEditLog presenter;

	public CellTableThemeModeEditLog(PanelSettingThemeModeEditLog presenter, int numberOfEntries) {
		super(NUMBER_OF_ROWS,
				GWT.<CellTable.BasicResources> create(CellTable.BasicResources.class),
				new ProvidesKey<PacketThemeModeEditLog>() {
					@Override
					public Object getKey(PacketThemeModeEditLog item) {
						return item.getUserCode() + "-" + item.getTimeMs();
					}
				});
		this.presenter = Preconditions.checkNotNull(presenter);

		setRowCount(numberOfEntries, true);
		setPageSize(NUMBER_OF_ROWS);
		setVisibleRange(max(0, numberOfEntries - NUMBER_OF_ROWS), NUMBER_OF_ROWS);
		dataProvider.addDataDisplay(this);
		setEmptyTableWidget(new HTML("<div class='settingThemeModeEditLogEmpty'>表示できる履歴がありません。</div>"));
		setLoadingIndicator(new HTML("<div class='settingThemeModeEditLogLoading'>履歴を読み込み中です...</div>"));

		// プレイヤー
		TextColumn<PacketThemeModeEditLog> playerColumn = new TextColumn<PacketThemeModeEditLog>() {
			@Override
			public String getValue(PacketThemeModeEditLog object) {
				return object.getUserName() + "◆" + Utility.makeTrip(object.getUserCode());
			}
		};
		addColumn(playerColumn, "プレイヤー");
		setColumnWidth(playerColumn, 24, Unit.PCT);

		// 日時
		TextColumn<PacketThemeModeEditLog> dateColumn = new TextColumn<PacketThemeModeEditLog>() {
			@Override
			public String getValue(PacketThemeModeEditLog object) {
				return Utility.toDateFormat(new Date(object.getTimeMs()));
			}
		};
		addColumn(dateColumn, "日時");
		setColumnWidth(dateColumn, 22, Unit.PCT);

		// 操作
		Column<PacketThemeModeEditLog, SafeHtml> operationColumn = new Column<PacketThemeModeEditLog, SafeHtml>(
				new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(PacketThemeModeEditLog object) {
				String operation = object.getType().toString();
				String operationClass = "settingThemeModeEditLogOperation--" + sanitizeCssToken(operation);
				SafeHtmlBuilder builder = new SafeHtmlBuilder();
				builder.appendHtmlConstant("<span class='settingThemeModeEditLogOperation ")
						.appendEscaped(operationClass).appendHtmlConstant("'>")
						.appendEscaped(operation).appendHtmlConstant("</span>");
				return builder.toSafeHtml();
			}
		};
		addColumn(operationColumn, "操作");
		setColumnWidth(operationColumn, 12, Unit.PCT);

		// テーマ
		Column<PacketThemeModeEditLog, SafeHtml> themeColumn = new Column<PacketThemeModeEditLog, SafeHtml>(
				new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(PacketThemeModeEditLog object) {
				return renderMultilineValue(object.getTheme());
			}
		};
		addColumn(themeColumn, "テーマ");
		setColumnWidth(themeColumn, 19, Unit.PCT);

		// クエリ
		Column<PacketThemeModeEditLog, SafeHtml> queryColumn = new Column<PacketThemeModeEditLog, SafeHtml>(
				new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(PacketThemeModeEditLog object) {
				return renderMultilineValue(object.getQuery());
			}
		};
		addColumn(queryColumn, "クエリ");
		setColumnWidth(queryColumn, 23, Unit.PCT);
	}

	/**
	 * テーマ名とクエリの長文表示向けに、改行対応のラッパーを返す。
	 */
	private static SafeHtml renderMultilineValue(String value) {
		if (value == null || value.trim().isEmpty()) {
			return SafeHtmlUtils.fromSafeConstant("<span class='settingThemeModeEditLogValueEmpty'>（未設定）</span>");
		}
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<span class='settingThemeModeEditLogMultiline'>")
				.appendEscaped(value).appendHtmlConstant("</span>");
		return builder.toSafeHtml();
	}

	/**
	 * 操作種別をCSSクラス名として扱える形に正規化する。
	 */
	private static String sanitizeCssToken(String raw) {
		if (raw == null || raw.isEmpty()) {
			return "unknown";
		}
		return raw.replaceAll("[^a-zA-Z0-9_-]", "-").toLowerCase();
	}

	private final AsyncDataProvider<PacketThemeModeEditLog> dataProvider = new AsyncDataProvider<PacketThemeModeEditLog>() {
		@Override
		protected void onRangeChanged(HasData<PacketThemeModeEditLog> display) {
			Range range = display.getVisibleRange();
			presenter.onThemeModeEditLogRequest(range.getStart(), range.getLength());
		}
	};

}
