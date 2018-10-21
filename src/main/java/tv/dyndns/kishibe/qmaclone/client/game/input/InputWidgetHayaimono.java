package tv.dyndns.kishibe.qmaclone.client.game.input;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketGameStatus.GamePlayerStatus;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;

public class InputWidgetHayaimono extends InputWidget implements ClickHandler {
	private static final String BUTTON_HAYAIMONO = "gwt-Button-hayaimono";
	private static final Map<Integer, Integer> numberOfAnswersToNumberOfSeats = ImmutableMap.of(2,
			4, 3, 3, 4, 2);
	private final Button[] buttons;
	private final Map<Button, String> buttonToChoice = Maps.newHashMap();
	private final int numberOfSeats;

	public InputWidgetHayaimono(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);

		int numberOfChoices = problem.getNumberOfShuffledChoices();
		buttons = new Button[numberOfChoices];

		int numberOfRows = numberOfChoices / 2;
		Grid grid = new Grid(numberOfRows, 4);

		add(grid);
		for (int i = 0; i < numberOfChoices; ++i) {
			String choice = problem.shuffledChoices[i];
			// BugTrack-QMAClone/387 - QMAClone wiki
			// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F387#1322577322
			Button button = new Button(toMultilineSafeHtml(choice), this);
			button.setStyleName(BUTTON_HAYAIMONO);
			buttonToChoice.put(button, choice);
			buttons[i] = button;

			grid.setHTML(i % numberOfRows, i / numberOfRows * 2, Integer.toString(i + 1));
			grid.setWidget(i % numberOfRows, i / numberOfRows * 2 + 1, button);
		}

		int numberOfAnswers = problem.getNumberOfShuffledAnswers();
		// BugTrack-QMAClone/384 - QMAClone wiki
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F384
		numberOfSeats = numberOfAnswersToNumberOfSeats.get(numberOfAnswers);
	}

	@Override
	public void enable(boolean b) {
		for (Button button : buttons) {
			button.setEnabled(b);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		String choice = Preconditions.checkNotNull(buttonToChoice.get(sender));
		sendAnswer(choice);
	}

	@Override
	public void onReceivedGameStatus(PacketGameStatus gameStatus) {
		super.onReceivedGameStatus(gameStatus);

		// 満席になったらボタンを無効化する
		List<String> answers = Lists.newArrayList();
		for (GamePlayerStatus playerStatus : gameStatus.status) {
			answers.add(playerStatus.answer);
		}

		// エラー発生のため
		Map<Button, String> buttonToChoice = this.buttonToChoice;
		if (buttonToChoice == null) {
			return;
		}
		for (Entry<Button, String> entry : buttonToChoice.entrySet()) {
			Button button = entry.getKey();
			String choice = entry.getValue();

			if (numberOfSeats <= Collections.frequency(answers, choice)) {
				button.setEnabled(false);
			}
		}
	}
}
