package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.report.ProblemReportUi;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingIndicatedProblems extends VerticalPanel {

	private static final Logger logger = Logger.getLogger(PanelSettingIndicatedProblems.class
			.getName());
	private static final int PAGE_SIZE = 100;
	private final HTML introCard = new HTML("<h3 class='settingThemeModeTitle'>指摘問題</h3>"
			+ "<p class='settingThemeModeLead settingIndicatedProblemsLead'>"
			+ "他プレイヤーから指摘された問題の一覧です。問題番号を押すと編集画面を開き、内容の確認と修正を行えます。"
			+ "</p>");
	private final HTML loadingMessage = new HTML(
			"<p class='settingIndicatedProblemsLoading'>指摘問題を読み込み中です...</p>");

	public PanelSettingIndicatedProblems() {
		setStyleName("settingIndicatedProblemsRoot");
		introCard.setStyleName("settingIndicatedProblemsIntroCard");
		add(introCard);
		add(loadingMessage);
	}

	private void update(List<PacketProblem> problems) {
		clear();
		add(introCard);
		add(new ProblemReportUi(problems, false, true, PAGE_SIZE));
	}

	@Override
	protected void onLoad() {
		super.onLoad();

		Service.Util.getInstance().getIndicatedProblems(callbackGetIndicatedProblems);
	}

	private AsyncCallback<List<PacketProblem>> callbackGetIndicatedProblems = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<List<PacketProblem>>() {
		@Override
		public void onSuccess(List<PacketProblem> result) {
			update(result);
		}

		@Override
		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "指摘された問題の取得に失敗しました", caught);
		}
	};
}

