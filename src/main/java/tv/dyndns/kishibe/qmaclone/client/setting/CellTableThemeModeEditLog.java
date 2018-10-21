package tv.dyndns.kishibe.qmaclone.client.setting;

import static java.lang.Math.max;

import java.util.Date;

import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditLog;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
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
		setVisibleRange(max(0, numberOfEntries - NUMBER_OF_ROWS), NUMBER_OF_ROWS);
		dataProvider.addDataDisplay(this);

		// プレイヤー
		addColumn(new TextColumn<PacketThemeModeEditLog>() {
			@Override
			public String getValue(PacketThemeModeEditLog object) {
				return object.getUserName() + "◆" + Utility.makeTrip(object.getUserCode());
			}
		}, "プレイヤー");

		// 日時
		addColumn(new TextColumn<PacketThemeModeEditLog>() {
			@Override
			public String getValue(PacketThemeModeEditLog object) {
				return Utility.toDateFormat(new Date(object.getTimeMs()));
			}
		}, "日時");

		// 操作
		addColumn(new TextColumn<PacketThemeModeEditLog>() {
			@Override
			public String getValue(PacketThemeModeEditLog object) {
				return object.getType().toString();
			}
		}, "操作");

		// テーマ
		addColumn(new TextColumn<PacketThemeModeEditLog>() {
			@Override
			public String getValue(PacketThemeModeEditLog object) {
				return object.getTheme();
			}
		}, "テーマ");

		// クエリ
		addColumn(new TextColumn<PacketThemeModeEditLog>() {
			@Override
			public String getValue(PacketThemeModeEditLog object) {
				return object.getQuery();
			}
		}, "クエリ");
	}

	private final AsyncDataProvider<PacketThemeModeEditLog> dataProvider = new AsyncDataProvider<PacketThemeModeEditLog>() {
		@Override
		protected void onRangeChanged(HasData<PacketThemeModeEditLog> display) {
			Range range = display.getVisibleRange();
			presenter.onThemeModeEditLogRequest(range.getStart(), range.getLength());
		}
	};

}
