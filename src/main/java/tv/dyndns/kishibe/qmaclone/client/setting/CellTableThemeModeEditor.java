package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor.ThemeModeEditorStatus;

public class CellTableThemeModeEditor extends CellTable<PacketThemeModeEditor> {

	interface ActionHandler {
		void onApply(int userCode);

		void onAccept(int userCode);

		void onReject(int userCode);
	}

	private final ListDataProvider<PacketThemeModeEditor> dataProvider = new ListDataProvider<PacketThemeModeEditor>();

	public CellTableThemeModeEditor(final ActionHandler actionHandler) {
		super(100, GWT.<CellTable.BasicResources> create(CellTable.BasicResources.class));
		setPageSize(100);
		setWidth("100%");
		setEmptyTableWidget(new HTML("<div class='settingAdminEmpty'>対象ユーザーはありません。</div>"));
		setLoadingIndicator(new HTML("<div class='settingAdminLoading'>管理者データを読み込み中です...</div>"));
		dataProvider.addDataDisplay(this);

		TextColumn<PacketThemeModeEditor> userCodeColumn = new TextColumn<PacketThemeModeEditor>() {
			@Override
			public String getValue(PacketThemeModeEditor object) {
				return Integer.toString(object.userCode);
			}
		};
		addColumn(userCodeColumn, "ユーザーコード");
		setColumnWidth(userCodeColumn, 17, Unit.PCT);

		TextColumn<PacketThemeModeEditor> playerNameColumn = new TextColumn<PacketThemeModeEditor>() {
			@Override
			public String getValue(PacketThemeModeEditor object) {
				return object.name == null ? "" : object.name;
			}
		};
		addColumn(playerNameColumn, "プレイヤー名");
		setColumnWidth(playerNameColumn, 23, Unit.PCT);

		Column<PacketThemeModeEditor, SafeHtml> statusColumn = new Column<PacketThemeModeEditor, SafeHtml>(
				new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(PacketThemeModeEditor object) {
				SafeHtmlBuilder builder = new SafeHtmlBuilder();
				builder.appendHtmlConstant("<span class='")
						.appendEscaped(ThemeModeEditorUiSupport.toStatusStyle(object.themeModeEditorStatus))
						.appendHtmlConstant("'>")
						.appendEscaped(ThemeModeEditorUiSupport.toStatusLabel(object.themeModeEditorStatus))
						.appendHtmlConstant("</span>");
				return builder.toSafeHtml();
			}
		};
		addColumn(statusColumn, "現在状態");
		setColumnWidth(statusColumn, 20, Unit.PCT);

		Column<PacketThemeModeEditor, String> applyColumn = new Column<PacketThemeModeEditor, String>(
				new ButtonCell()) {
			@Override
			public String getValue(PacketThemeModeEditor object) {
				return "申請中";
			}
		};
		applyColumn.setFieldUpdater(new FieldUpdater<PacketThemeModeEditor, String>() {
			@Override
			public void update(int index, PacketThemeModeEditor object, String value) {
				actionHandler.onApply(object.userCode);
			}
		});
		addColumn(applyColumn, "申請中へ戻す");
		setColumnWidth(applyColumn, 13, Unit.PCT);

		Column<PacketThemeModeEditor, String> acceptColumn = new Column<PacketThemeModeEditor, String>(
				new ButtonCell()) {
			@Override
			public String getValue(PacketThemeModeEditor object) {
				return "承認";
			}
		};
		acceptColumn.setFieldUpdater(new FieldUpdater<PacketThemeModeEditor, String>() {
			@Override
			public void update(int index, PacketThemeModeEditor object, String value) {
				actionHandler.onAccept(object.userCode);
			}
		});
		addColumn(acceptColumn, "承認");
		setColumnWidth(acceptColumn, 13, Unit.PCT);

		Column<PacketThemeModeEditor, String> rejectColumn = new Column<PacketThemeModeEditor, String>(
				new ButtonCell()) {
			@Override
			public String getValue(PacketThemeModeEditor object) {
				return "却下";
			}
		};
		rejectColumn.setFieldUpdater(new FieldUpdater<PacketThemeModeEditor, String>() {
			@Override
			public void update(int index, PacketThemeModeEditor object, String value) {
				actionHandler.onReject(object.userCode);
			}
		});
		addColumn(rejectColumn, "却下");
		setColumnWidth(rejectColumn, 14, Unit.PCT);
	}

	public void setEditors(List<PacketThemeModeEditor> editors) {
		setRowData(editors);
		dataProvider.setList(editors);
	}

}
