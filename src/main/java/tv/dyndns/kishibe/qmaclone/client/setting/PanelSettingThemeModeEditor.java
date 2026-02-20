package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingThemeModeEditor extends VerticalPanel implements ClickHandler {
	private static final Logger logger = Logger.getLogger(PanelSettingThemeModeEditor.class
			.getName());
	private final CellTableThemeModeEditor table = new CellTableThemeModeEditor(
			new CellTableThemeModeEditor.ActionHandler() {
				@Override
				public void onApply(int userCode) {
					Service.Util.getInstance().applyThemeModeEditor(userCode, "(管理人により申請状態になりました)",
							callbackApplyThemeModeEditor);
				}

				@Override
				public void onAccept(int userCode) {
					Service.Util.getInstance().acceptThemeModeEditor(userCode, callbackAcceptThemeModeEditor);
				}

				@Override
				public void onReject(int userCode) {
					Service.Util.getInstance().rejectThemeModeEditor(userCode, callbackRejectThemeModeEditor);
				}
			});
	private final Button buttonUpdate = new Button("申請リスト更新", this);

	public PanelSettingThemeModeEditor() {
		setStyleName("settingAdminRoot");
		add(new HTML("<h3 class='settingThemeModeTitle'>テーマモード編集権限管理</h3>"
				+ "<p class='settingThemeModeLead'>申請状態をテーブルで一覧管理します。行ごとの操作で状態を変更できます。</p>"));

		FlowPanel actionBar = new FlowPanel();
		actionBar.setStyleName("settingAdminActionBar");
		buttonUpdate.addStyleName("creationButtonSecondary");
		actionBar.add(buttonUpdate);
		add(actionBar);

		SimplePanel tableScroll = new SimplePanel(table);
		tableScroll.setStyleName("settingAdminTableScroll");
		table.addStyleName("settingAdminThemeModeTable");
		add(tableScroll);

		updateThemeModeEditors();
	}

	private void updateThemeModeEditors() {
		Service.Util.getInstance().getThemeModeEditors(callbackGetThemeModeEditors);
	}

	private final AsyncCallback<List<PacketThemeModeEditor>> callbackGetThemeModeEditors = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<List<PacketThemeModeEditor>>() {
		@Override
		public void onSuccess(List<PacketThemeModeEditor> result) {
			table.setEditors(ThemeModeEditorUiSupport.sortEditors(result));
		}

		@Override
		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "テーマモード編集者一覧の取得に失敗しました", caught);
		}
	};
	private final AsyncCallback<Void> callbackApplyThemeModeEditor = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			SettingSaveToast.showSaved("テーマモード編集権限の状態更新");
			updateThemeModeEditors();
		}

		@Override
		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "テーマモードの申請に失敗しました(管理者モード)", caught);
		}
	};
	private final AsyncCallback<Void> callbackAcceptThemeModeEditor = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			SettingSaveToast.showSaved("テーマモード編集権限の承認");
			updateThemeModeEditors();
		}

		@Override
		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "テーマモード編集権限の承認に失敗しました", caught);
		}
	};
	private final AsyncCallback<Void> callbackRejectThemeModeEditor = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			SettingSaveToast.showSaved("テーマモード編集権限の却下");
			updateThemeModeEditors();
		}

		@Override
		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "テーマモード編集権限の却下に失敗しました", caught);
		}
	};

	@Override
	public void onClick(ClickEvent event) {
		final Object source = event.getSource();
		if (source == buttonUpdate) {
			updateThemeModeEditors();
		}

	}
}

