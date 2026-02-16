package tv.dyndns.kishibe.qmaclone.client.report;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketSimilarProblem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class ProblemReportUi extends Composite {
	private static final Logger logger = Logger.getLogger(ProblemReportUi.class.getName());
	private static ProblemReportUiUiBinder uiBinder = GWT.create(ProblemReportUiUiBinder.class);

	interface ProblemReportUiUiBinder extends UiBinder<Widget, ProblemReportUi> {
	}

	@UiField
	HTML htmlHits;
	@UiField(provided = true)
	SimplePager pager;
	@UiField(provided = true)
	CellTableProblem cellTableProblem;
	@UiField
	Button buttonRegisterAll;

	private final List<ProblemReportRow> rows;

	public ProblemReportUi(List<PacketProblem> problems, boolean regist, boolean initialSort,
			int maxProblemsPerPage) {
		this(ProblemReportRowSorter.fromProblems(problems), regist, initialSort, maxProblemsPerPage,
				ProblemReportViewOptions.defaults(), true);
	}

	public ProblemReportUi(List<PacketProblem> problems, boolean regist, boolean initialSort,
			int maxProblemsPerPage, ProblemReportViewOptions options) {
		this(ProblemReportRowSorter.fromProblems(problems), regist, initialSort, maxProblemsPerPage,
				options, true);
	}

	/**
	 * 類似問題検索結果向けのUIを生成する。
	 * 
	 * @param similarProblems 類似問題検索結果
	 * @param regist 問題登録有効フラグ
	 * @param maxProblemsPerPage 1ページ当たり件数
	 * @return 問題レポートUI
	 */
	public static ProblemReportUi fromSimilarProblems(List<PacketSimilarProblem> similarProblems,
			boolean regist, int maxProblemsPerPage) {
		return new ProblemReportUi(ProblemReportRowSorter.fromSimilarProblems(similarProblems),
				regist, false, maxProblemsPerPage, ProblemReportViewOptions.defaults(), true);
	}

	private ProblemReportUi(List<ProblemReportRow> rows, boolean regist, boolean initialSort,
			int maxProblemsPerPage, ProblemReportViewOptions options, boolean rowsModel) {
		this.rows = rows;
		if (options.useRatioDefaultSort) {
			ProblemReportRowSorter.sortForRatioReport(rows);
		} else {
			ProblemReportRowSorter.sortForDisplay(rows, initialSort);
		}

		cellTableProblem = new CellTableProblem(rows, regist, maxProblemsPerPage, options);

		final SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(cellTableProblem);
		cellTableProblem.addLoadingStateChangeHandler(new LoadingStateChangeEvent.Handler() {
			@Override
			public void onLoadingStateChanged(LoadingStateChangeEvent event) {
				if (event.getLoadingState() != LoadingStateChangeEvent.LoadingState.LOADED) {
					return;
				}
			}
		});
		if (rows.size() < maxProblemsPerPage) {
			pager.setVisible(false);
		}

		initWidget(uiBinder.createAndBindUi(this));

		final int count = rows.size();
		htmlHits.setHTML(SafeHtmlUtils.fromString(count + "件ヒット"));

		if (!regist) {
			buttonRegisterAll.setVisible(false);
		}
	}

	@UiHandler("buttonRegisterAll")
	void onButtonRegisterAll(ClickEvent e) {
		buttonRegisterAll.setEnabled(false);

		final int userCode = UserData.get().getUserCode();

		final List<Integer> problemIds = new ArrayList<Integer>();
		for (ProblemReportRow row : rows) {
			if (row == null || row.problem == null) {
				continue;
			}
			problemIds.add(row.problem.id);
		}

		Service.Util.getInstance().addProblemIdsToReport(userCode, problemIds,
				callbackAddProblemIdsToReport);
	}

	private final AsyncCallback<Void> callbackAddProblemIdsToReport = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
		}

		@Override
		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "問題の登録に失敗しました", caught);
		}
	};
}

