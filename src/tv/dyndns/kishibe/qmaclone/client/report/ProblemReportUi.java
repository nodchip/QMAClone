package tv.dyndns.kishibe.qmaclone.client.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.PlusOne;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
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

	private final List<PacketProblem> problems;

	public ProblemReportUi(List<PacketProblem> problems, boolean regist, boolean initialSort,
			int maxProblemsPerPage) {
		this.problems = problems;
		if (initialSort) {
			Collections.sort(problems, new Comparator<PacketProblem>() {
				@Override
				public int compare(PacketProblem o1, PacketProblem o2) {
					return o1.id - o2.id;
				}
			});
		}

		cellTableProblem = new CellTableProblem(problems, regist, maxProblemsPerPage);

		final SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(cellTableProblem);
		// cellTableProblem.addRangeChangeHandler(new RangeChangeEvent.Handler() {
		// @Override
		// public void onRangeChange(RangeChangeEvent EVENT) {
		// Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
		// @Override
		// public void execute() {
		// PlusOne.render();
		// }
		// });
		// }
		// });
		cellTableProblem.addLoadingStateChangeHandler(new LoadingStateChangeEvent.Handler() {
			@Override
			public void onLoadingStateChanged(LoadingStateChangeEvent event) {
				if (event.getLoadingState() != LoadingStateChangeEvent.LoadingState.LOADED) {
					return;
				}
				Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
					@Override
					public void execute() {
						PlusOne.render();
					}
				});
			}
		});
		if (problems.size() < maxProblemsPerPage) {
			pager.setVisible(false);
		}

		initWidget(uiBinder.createAndBindUi(this));

		final int count = problems.size();
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
		for (PacketProblem problem : problems) {
			problemIds.add(problem.id);
		}

		Service.Util.getInstance().addProblemIdsToReport(userCode, problemIds,
				callbackAddProblemIdsToReport);
	}

	private final AsyncCallback<Void> callbackAddProblemIdsToReport = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "問題の登録に失敗しました", caught);
		}
	};
}
