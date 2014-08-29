package tv.dyndns.kishibe.qmaclone.client.game.panel;

import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerViewImpl;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.input.InputWidget;
import tv.dyndns.kishibe.qmaclone.client.game.input.InputWidget4Taku;
import tv.dyndns.kishibe.qmaclone.client.game.sentence.WidgetProblemSentence;
import tv.dyndns.kishibe.qmaclone.client.game.sentence.WidgetProblemSentenceRensou;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

public class QuestionPanelRensou extends QuestionPanel {
	public QuestionPanelRensou(PacketProblem problem, SessionData sessionData) {
		super(problem, sessionData);
	}

	@Override
	protected WidgetProblemSentence createWidgetProblemSentence() {
		return new WidgetProblemSentenceRensou(problem);
	}

	@Override
	protected AnswerView createAnswerView() {
		return new AnswerViewImpl(1024);
	}

	@Override
	protected InputWidget createWidgetInput() {
		return new InputWidget4Taku(problem, answerView, this, getSessionData());
	}
}
