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
package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 設定画面の「正答率統計」セクションを表示するパネル。
 */
public class PanelSettingRatioReport extends VerticalPanel implements ClickHandler {
	private static final Logger logger = Logger.getLogger(PanelSettingRatioReport.class.getName());
	private final Button buttonClearReportProblemID = new Button("登録問題一覧を一括消去する", this);

	/**
	 * 正答率統計の設定UIを初期化する。
	 */
	public PanelSettingRatioReport() {
		setHorizontalAlignment(ALIGN_CENTER);
		setWidth("100%");
		setStyleName("settingRatioReportRoot");

		VerticalPanel card = new VerticalPanel();
		card.setWidth("100%");
		card.setStyleName("settingRatioReportCard");
		add(card);

		card.add(new HTML("<h3 class='settingRatioReportTitle'>登録問題一覧の一括消去</h3>"));
		card.add(new HTML(
				"<p class='settingRatioReportLead'>登録問題一覧に保持している問題IDをまとめて解除できます。"
						+ "不要な登録を整理したいときに実行してください。</p>"));
		card.add(new HTML(
				"<p class='settingRatioReportWarning'>この操作は取り消せません。必要な問題IDは事前に控えてから実行してください。</p>"));

		HorizontalPanel actionRow = new HorizontalPanel();
		actionRow.setStyleName("settingRatioReportActionRow");
		buttonClearReportProblemID.setStyleName("settingRatioReportDangerButton");
		actionRow.add(buttonClearReportProblemID);
		card.add(actionRow);
	}

	/**
	 * 登録問題一覧に保持している問題IDを一括消去する。
	 */
	private void clearReportProblemId() {
		if (!Window.confirm("正解率統計に登録されている問題を一括消去しますか？")) {
			return;
		}

		Service.Util.getInstance().clearProblemIDFromReport(UserData.get().getUserCode(),
				callbackClearProblemIDFromReport);
	}

	private final AsyncCallback<Void> callbackClearProblemIDFromReport = new RpcAsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			SettingSaveToast.showSaved("登録問題一覧の一括消去");
		}

		@Override
		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "問題登録の一括解除に失敗しました", caught);
		}
	};

	/**
	 * クリックイベントを処理する。
	 */
	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();
		if (sender == buttonClearReportProblemID) {
			clearReportProblemId();
		}
	}
}

