package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * 問題変更履歴表示ビューの実装
 * 
 * @author nodchip
 */
public class ChangeHistoryViewImpl extends Composite implements ChangeHistoryView, ClickHandler {

	private static PanelProblemChangeHistoryUiBinder uiBinder = GWT
			.create(PanelProblemChangeHistoryUiBinder.class);
	private static final String[] HEADER = { "日時", "プレイヤー", "変更前", "変更後" };
	private static final String GROUP_BEFORE = "before";
	private static final String GROUP_AFTER = "after";

	interface PanelProblemChangeHistoryUiBinder extends UiBinder<Widget, ChangeHistoryViewImpl> {
	}

	@UiField
	Grid gridHistory;
	@UiField
	HTML htmlDiff;
	private final ChangeHistoryPresenter presenter;
	private final Map<RadioButton, PacketProblemCreationLog> buttonToLogBefore = Maps.newHashMap();
	private final Map<RadioButton, PacketProblemCreationLog> buttonToLogAfter = Maps.newHashMap();

	public ChangeHistoryViewImpl(ChangeHistoryPresenter presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setCreationLog(List<PacketProblemCreationLog> creationLog) {
		gridHistory.resize(creationLog.size() + 1, HEADER.length);
		for (int column = 0; column < HEADER.length; ++column) {
			gridHistory.setText(0, column, HEADER[column]);
		}

		gridHistory.getColumnFormatter().addStyleName(2, "problemChangeHistoryDiffColumn");
		gridHistory.getColumnFormatter().addStyleName(3, "problemChangeHistoryDiffColumn");

		int row = 0;
		for (PacketProblemCreationLog change : creationLog) {
			++row;
			gridHistory.setHTML(row, 0, change.getDate());
			gridHistory.setHTML(row, 1, change.getPlayer());

			if (Strings.isNullOrEmpty(change.summary)) {
				continue;
			}

			RadioButton buttonBefore = new RadioButton(GROUP_BEFORE);
			buttonBefore.addClickHandler(this);
			gridHistory.setWidget(row, 2, buttonBefore);
			buttonToLogBefore.put(buttonBefore, change);

			RadioButton buttonAfter = new RadioButton(GROUP_AFTER);
			buttonAfter.addClickHandler(this);
			gridHistory.setWidget(row, 3, buttonAfter);
			buttonToLogAfter.put(buttonAfter, change);
		}

		htmlDiff.setHTML("");
	}

	private PacketProblemCreationLog selectedLog(
			Map<RadioButton, PacketProblemCreationLog> buttonToLog) {
		for (Entry<RadioButton, PacketProblemCreationLog> entry : buttonToLog.entrySet()) {
			if (entry.getKey().getValue()) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public void onClick(ClickEvent event) {
		PacketProblemCreationLog before = selectedLog(buttonToLogBefore);
		PacketProblemCreationLog after = selectedLog(buttonToLogAfter);
		presenter.onUpdateDiffTarget(before, after);
	}

	@Override
	public void setDiffHtml(SafeHtml html) {
		htmlDiff.setHTML(html);
	}

}
