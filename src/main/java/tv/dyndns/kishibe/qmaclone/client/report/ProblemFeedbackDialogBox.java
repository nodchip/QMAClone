package tv.dyndns.kishibe.qmaclone.client.report;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.view.client.ListDataProvider;

/**
 * 良問投票を行うためのダイアログ
 * 
 * @author nodchip
 */
public class ProblemFeedbackDialogBox extends DialogBox {
	public ProblemFeedbackDialogBox(ListDataProvider<PacketProblem> dataProvider,
			PacketProblem problem) {
		super(true, true);
		setText("良問投票");
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setWidget(new ProblemFeedback(dataProvider, problem));
	}
}
