package tv.dyndns.kishibe.qmaclone.client.game.input;

import java.util.Collections;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.AnswerView;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.panel.QuestionPanel;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.util.Random;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

public class InputWidgetGroup extends InputWidget implements ClickHandler {
	private static final String STYLE_NAME_GROUP = "gwt-Button-group";
	private static final String STYLE_NAME_GROUP_HEADER = "gwt-Button-group-header";
	private static final String STYLE_NAME_GROUP_CONTROL = "gwt-Button-group-control";
	private static final int draggableOffsetHeight = 30;
	private AbsolutePanel gridConstrainedDropTarget;
	private final List<Label> labelChoices = Lists.newArrayList();
	private final Button buttonOk = new Button("OK", this);
	private PickupDragController dragController;

	public InputWidgetGroup(PacketProblem problem, AnswerView answerView,
			QuestionPanel questionPanel, SessionData sessionData) {
		super(problem, answerView, questionPanel, sessionData);

		// 解答フォーム表示位置の予約
		int numberOfGroups = ImmutableSet.copyOf(problem.getAnswerList()).size();
		int numberOfChoices = problem.getNumberOfShuffledChoices();

		// 解答フォームの表示
		gridConstrainedDropTarget = new AbsolutePanel();
		gridConstrainedDropTarget.setPixelSize(getDraggableOffsetWidth() * numberOfGroups + 8,
				draggableOffsetHeight * (numberOfChoices + 1) + 8);
		add(gridConstrainedDropTarget);
		// 表示先のDOMのサイズが確定しておらず、ラベルを追加すると左上に表示されてしまうため、追加タイミングを遅らせる
		Scheduler.get().scheduleDeferred(cmd);

		// OKボタン
		buttonOk.setStyleName(STYLE_NAME_GROUP_CONTROL);
		add(buttonOk);
	}

	private int getDraggableOffsetWidth() {
		int numberOfGroups = ImmutableSet.copyOf(problem.getAnswerList()).size();
		return numberOfGroups == 2 ? 275 : 200;
	}

	private final Scheduler.ScheduledCommand cmd = new Scheduler.ScheduledCommand() {
		@Override
		public void execute() {
			dragController = new PickupDragController(gridConstrainedDropTarget, false);
			dragController.setBehaviorScrollIntoView(false);

			final GridHorizontalMoveDropController dropController = new GridHorizontalMoveDropController(
					gridConstrainedDropTarget, getDraggableOffsetWidth(), draggableOffsetHeight);
			dragController.registerDropController(dropController);

			int numberOfGroups = ImmutableSet.copyOf(problem.getAnswerList()).size();
			int numberOfChoices = problem.getNumberOfShuffledChoices();

			// header
			List<String> groups = Lists.newArrayList(ImmutableSet.copyOf(problem.getAnswerList()));
			Collections.sort(groups);
			for (int i = 0; i < numberOfGroups; ++i) {
				Label label = new Label(groups.get(i));
				label.addStyleName(STYLE_NAME_GROUP_HEADER);
				label.setPixelSize(getDraggableOffsetWidth(), draggableOffsetHeight);
				gridConstrainedDropTarget.add(label, getDraggableOffsetWidth() * i, 0);
			}

			// choices
			for (int i = 0; i < numberOfChoices; ++i) {
				Label label = new Label(problem.shuffledChoices[i]);
				label.addMouseDownHandler(new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
						dropController.setStartY(event.getRelativeElement().getAbsoluteTop());
					}
				});
				label.addStyleName(STYLE_NAME_GROUP);
				label.setPixelSize(getDraggableOffsetWidth(), draggableOffsetHeight);
				labelChoices.add(label);
				dragController.makeDraggable(label);
				dropController.drop(label, Random.get().nextInt(numberOfGroups)
						* getDraggableOffsetWidth(), draggableOffsetHeight * (i + 1));
			}
		}
	};

	@Override
	public void enable(boolean b) {
		buttonOk.setEnabled(b);
	}

	@Override
	public void onClick(ClickEvent event) {
		Object source = event.getSource();
		if (source == buttonOk) {
			List<String> groups = Lists.newArrayList(ImmutableSet.copyOf(problem.getAnswerList()));
			Collections.sort(groups);

			StringBuilder sb = new StringBuilder();
			for (Label label : labelChoices) {
				if (sb.length() != 0) {
					sb.append(Constant.DELIMITER_GENERAL);
				}

				int x = gridConstrainedDropTarget.getWidgetLeft(label);
				int groupIndex = (x + getDraggableOffsetWidth() / 2) / getDraggableOffsetWidth();
				sb.append(label.getText()).append(Constant.DELIMITER_KUMIAWASE_PAIR)
						.append(groups.get(groupIndex));
			}

			sendAnswer(sb.toString());
		}
	}

	@Override
	protected void onUnload() {
		dragController.unregisterDropControllers();
		super.onUnload();
	}
}
