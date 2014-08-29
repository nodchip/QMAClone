package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor.ThemeModeEditorStatus;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingThemeModeEditor extends VerticalPanel implements ClickHandler {
	private static final Logger logger = Logger.getLogger(PanelSettingThemeModeEditor.class
			.getName());
	private final Grid grid = new Grid();
	private final Button buttonUpdate = new Button("申請リスト更新", this);

	public PanelSettingThemeModeEditor() {
		add(new HTML("<h2>テーマモード編集権限管理</h2>"));

		// TODO CellTable化
		grid.addStyleName("gridFrame");
		add(grid);
		add(buttonUpdate);

		updateThemeModeEditors();
	}

	private void updateThemeModeEditors() {
		Service.Util.getInstance().getThemeModeEditors(callbackGetThemeModeEditors);
	}

	private final AsyncCallback<List<PacketThemeModeEditor>> callbackGetThemeModeEditors = new AsyncCallback<List<PacketThemeModeEditor>>() {
		@Override
		public void onSuccess(List<PacketThemeModeEditor> result) {
			Collections.sort(result, new Comparator<PacketThemeModeEditor>() {
				@Override
				public int compare(PacketThemeModeEditor o1, PacketThemeModeEditor o2) {
					return o1.themeModeEditorStatus.compareTo(o2.themeModeEditorStatus);
				}
			});

			grid.resize(result.size() + 1, 5);
			grid.setHTML(0, 0, "ユーザーコード");
			grid.setHTML(0, 1, "プレイヤー名");
			grid.setHTML(0, 2, "申請中");
			grid.setHTML(0, 3, "承認");
			grid.setHTML(0, 4, "却下");

			int row = 1;
			for (PacketThemeModeEditor editor : result) {
				final int userCode = editor.userCode;
				grid.setText(row, 0, Integer.toString(userCode));
				grid.setText(row, 1, editor.name);
				final RadioButton buttonApplying = new RadioButton(Integer.toString(userCode));
				final RadioButton buttonAccepted = new RadioButton(Integer.toString(userCode));
				final RadioButton buttonRejected = new RadioButton(Integer.toString(userCode));
				grid.setWidget(row, 2, buttonApplying);
				grid.setWidget(row, 3, buttonAccepted);
				grid.setWidget(row, 4, buttonRejected);

				if (editor.themeModeEditorStatus == ThemeModeEditorStatus.Applying) {
					buttonApplying.setValue(true);
				} else if (editor.themeModeEditorStatus == ThemeModeEditorStatus.Accepted) {
					buttonAccepted.setValue(true);
				} else if (editor.themeModeEditorStatus == ThemeModeEditorStatus.Refected) {
					buttonRejected.setValue(true);
				}

				final ValueChangeHandler<Boolean> handlerApplying = new ValueChangeHandler<Boolean>() {
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue()) {
							Service.Util.getInstance().applyThemeModeEditor(userCode,
									"(管理人により申請状態になりました)", callbackApplyThemeModeEditor);
						}
					}
				};
				buttonApplying.addValueChangeHandler(handlerApplying);

				final ValueChangeHandler<Boolean> handlerAccepted = new ValueChangeHandler<Boolean>() {
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue()) {
							Service.Util.getInstance().acceptThemeModeEditor(userCode,
									callbackAcceptThemeModeEditor);
						}
					}
				};
				buttonAccepted.addValueChangeHandler(handlerAccepted);

				final ValueChangeHandler<Boolean> handlerReject = new ValueChangeHandler<Boolean>() {
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue()) {
							Service.Util.getInstance().rejectThemeModeEditor(userCode,
									callbackAddValueChangeHandler);
						}
					}
				};
				buttonRejected.addValueChangeHandler(handlerReject);
				++row;
			}
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマモード編集者一覧の取得に失敗しました", caught);
		}
	};
	private final AsyncCallback<Void> callbackApplyThemeModeEditor = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマモードの申請に失敗しました(管理者モード)", caught);
		}
	};
	private final AsyncCallback<Void> callbackAcceptThemeModeEditor = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマモード編集権限の承認に失敗しました", caught);
		}
	};
	private final AsyncCallback<Void> callbackAddValueChangeHandler = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
		}

		@Override
		public void onFailure(Throwable caught) {
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
