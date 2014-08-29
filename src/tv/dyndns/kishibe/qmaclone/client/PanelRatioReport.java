//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.report.ProblemReportUi;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelRatioReport extends VerticalPanel implements ClickHandler {
	private static final Logger logger = Logger.getLogger(PanelRatioReport.class.getName());
	private static final int MAX_PROBLEMS_PER_PAGE = 100;
	private final Button buttonAdd = new Button("問題番号追加", this);
	private final Button buttonRemove = new Button("問題番号削除", this);
	private final Button buttonUpdate = new Button("情報更新", this);
	private final TextBox textBoxProblemNumber = new TextBox();
	private final SimplePanel panelGrid = new SimplePanel();
	private boolean enabled = true;
	private final RepeatingCommand commandCheckProblemId = new RepeatingCommand() {
		@Override
		public boolean execute() {
			final boolean ok = checkProblemId();
			buttonAdd.setEnabled(ok && enabled);
			buttonRemove.setEnabled(ok && enabled);
			return isAttached();
		}
	};

	private void setEnable(boolean enabled) {
		this.enabled = enabled;
		buttonAdd.setEnabled(enabled);
		buttonRemove.setEnabled(enabled);
		buttonUpdate.setEnabled(enabled);
	}

	public PanelRatioReport() {
		setWidth("800px");
		setHorizontalAlignment(ALIGN_CENTER);

		add(new HTML(
				"登録した問題の正答率を表示します。問題を投稿した場合は自動的に問題が登録されます。<br/>問題番号を登録、又は問題投稿後に「情報更新」ボタンを押すと、画面が更新されます。<br/>表のボタンを押すと項目別にソートします。二度押すと逆順にソートします。<br/>登録されている問題の一括消去は設定画面より行えます。"));

		HorizontalPanel idPanel = new HorizontalPanel();
		idPanel.setVerticalAlignment(ALIGN_MIDDLE);

		textBoxProblemNumber.setWidth("240px");
		textBoxProblemNumber.addFocusHandler(focusHandler);

		idPanel.add(textBoxProblemNumber);
		idPanel.add(buttonAdd);
		idPanel.add(buttonRemove);
		add(idPanel);

		add(buttonUpdate);

		add(panelGrid);
	}

	private final FocusHandler focusHandler = new FocusHandler() {
		@Override
		public void onFocus(FocusEvent event) {
			textBoxProblemNumber.selectAll();
		}
	};

	private void update() {
		final int userCode = UserData.get().getUserCode();
		Service.Util.getInstance().getUserProblemReport(userCode, callbackGetProblemList);
	}

	private final AsyncCallback<List<PacketProblem>> callbackGetProblemList = new AsyncCallback<List<PacketProblem>>() {
		@Override
		public void onSuccess(List<PacketProblem> result) {
			if (result == null) {
				logger.log(Level.WARNING, "無効な問題データが返されました");
				return;
			}

			panelGrid.setWidget(new ProblemReportUi(result, false, true, MAX_PROBLEMS_PER_PAGE));
			setEnable(true);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "問題データの取得に失敗しました", caught);
		}
	};

	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();
		final String message = "番号の書式が間違っています。半角数字であることを確認してください。";
		if (sender == buttonAdd) {
			// 問題番号追加
			try {
				final int problemId = Integer.parseInt(textBoxProblemNumber.getText());
				final int userCode = UserData.get().getUserCode();
				final List<Integer> problemIds = new ArrayList<Integer>();
				problemIds.add(problemId);
				Service.Util.getInstance().addProblemIdsToReport(userCode, problemIds,
						callbackAddProblemIdsToReport);
			} catch (Exception e) {
				logger.log(Level.WARNING, message);
			}

		} else if (sender == buttonRemove) {
			// 問題番号削除
			try {
				final int problemID = Integer.parseInt(textBoxProblemNumber.getText());
				final int userCode = UserData.get().getUserCode();
				Service.Util.getInstance().removeProblemIDFromReport(userCode, problemID,
						callbackRemoveProblemIdFromReport);
			} catch (Exception e) {
				logger.log(Level.WARNING, message);
			}

		} else if (sender == buttonUpdate) {
			// 情報更新
			setEnable(false);
			update();
		}
	}

	private final AsyncCallback<Void> callbackAddProblemIdsToReport = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			textBoxProblemNumber.setText("問題番号を追加しました");
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "問題追加中にエラーが発生しました", caught);
		}
	};
	private final AsyncCallback<Void> callbackRemoveProblemIdFromReport = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			textBoxProblemNumber.setText("問題番号を削除しました");
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "問題削除にエラーが発生しました", caught);
		}
	};

	private boolean checkProblemId() {
		try {
			Integer.parseInt(textBoxProblemNumber.getText());
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleFixedDelay(commandCheckProblemId, 1000);
	}
}
