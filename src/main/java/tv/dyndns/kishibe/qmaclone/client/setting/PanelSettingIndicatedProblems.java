package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.report.CellTableProblem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingIndicatedProblems extends VerticalPanel {

	private static final Logger logger = Logger.getLogger(PanelSettingIndicatedProblems.class
			.getName());
	private static final int PAGE_SIZE = 100;

	public PanelSettingIndicatedProblems() {
		setHorizontalAlignment(ALIGN_CENTER);
	}

	private void update(List<PacketProblem> problems) {
		clear();

		CellTableProblem table = new CellTableProblem(problems, false, PAGE_SIZE);
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		SimplePager simplePager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0,
				true);
		simplePager.setDisplay(table);

		add(simplePager);
		add(table);
	}

	@Override
	protected void onLoad() {
		super.onLoad();

		Service.Util.getInstance().getIndicatedProblems(callbackGetIndicatedProblems);
	}

	private AsyncCallback<List<PacketProblem>> callbackGetIndicatedProblems = new AsyncCallback<List<PacketProblem>>() {
		@Override
		public void onSuccess(List<PacketProblem> result) {
			update(result);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "指摘された問題の取得に失敗しました", caught);
		}
	};
}
