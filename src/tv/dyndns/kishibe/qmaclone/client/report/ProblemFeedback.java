package tv.dyndns.kishibe.qmaclone.client.report;

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.PlusOne;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * 良問投票を行うためのダイアログの内容
 * 
 * @author nodchip
 */
public class ProblemFeedback extends Composite {

	private static ProblemFeedbackUiBinder uiBinder = GWT.create(ProblemFeedbackUiBinder.class);
	private static final Logger logger = Logger.getLogger(ProblemFeedback.class.getName());

	interface ProblemFeedbackUiBinder extends UiBinder<Widget, ProblemFeedback> {
	}

	@UiField
	TextBox reasonTextBox;
	@UiField
	Button submitButton;
	@UiField
	HTML plusOneHtml;
	private final PacketProblem problem;
	private final ListDataProvider<PacketProblem> dataProvider;

	public ProblemFeedback(ListDataProvider<PacketProblem> dataProvider, PacketProblem problem) {
		this.dataProvider = dataProvider;
		this.problem = problem;
		initWidget(uiBinder.createAndBindUi(this));
		plusOneHtml.setHTML(PlusOne.getButton(problem.id, true));
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				PlusOne.render();
			}
		});
	}

	@UiHandler("submitButton")
	void onClick(ClickEvent e) {
		String reason = reasonTextBox.getText();
		if (Strings.isNullOrEmpty(reason)) {
			return;
		}

		reasonTextBox.setEnabled(false);
		submitButton.setEnabled(false);

		int userCode = UserData.get().getUserCode();
		String playerName = UserData.get().getPlayerName();
		Service.Util.getInstance().voteToProblem(userCode, problem.id, true, reason, playerName,
				callbackVoteToProblem);
	}

	private final AsyncCallback<Void> callbackVoteToProblem = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			++problem.voteGood;
			dataProvider.refresh();
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "問題への投票に失敗しました", caught);
		}
	};
}
