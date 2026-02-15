package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.Collections;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 問題変更履歴表示ビューの実装
 * 
 * @author nodchip
 */
public class ChangeHistoryViewImpl extends Composite implements ChangeHistoryView, ClickHandler {

	private static PanelProblemChangeHistoryUiBinder uiBinder = GWT
			.create(PanelProblemChangeHistoryUiBinder.class);
	private static final String GROUP_BEFORE = "before";
	private static final String GROUP_AFTER = "after";
  private static final int PREVIEW_MAX_LENGTH = 72;
  private static final String DIFF_EMPTY_MESSAGE = "比較したい履歴を「変更前」「変更後」で1件ずつ選択してください。";
  private static final String DIFF_NEED_MORE_HISTORY_MESSAGE = "比較対象が不足しています。履歴が2件以上あると差分を表示できます。";
  private static final String DIFF_LOADING_MESSAGE = "差分を更新中...";

	interface PanelProblemChangeHistoryUiBinder extends UiBinder<Widget, ChangeHistoryViewImpl> {
	}

	@UiField
  VerticalPanel panelHistoryList;
	@UiField
	HTML htmlDiff;
	private final ChangeHistoryPresenter presenter;
	private final Map<RadioButton, PacketProblemCreationLog> buttonToLogBefore = Maps.newHashMap();
	private final Map<RadioButton, PacketProblemCreationLog> buttonToLogAfter = Maps.newHashMap();
  private boolean synchronizingSelection;
  private SafeHtml currentDiffHtml = SafeHtmlUtils.fromString(DIFF_EMPTY_MESSAGE);

	public ChangeHistoryViewImpl(ChangeHistoryPresenter presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
  public void setCreationLog(List<PacketProblemCreationLog> creationLog) {
		panelHistoryList.clear();
    buttonToLogBefore.clear();
    buttonToLogAfter.clear();
    currentDiffHtml = SafeHtmlUtils.fromString(DIFF_EMPTY_MESSAGE);
    renderRightPane();

    if (creationLog == null || creationLog.isEmpty()) {
      panelHistoryList.add(new Label("編集履歴はまだありません。"));
      currentDiffHtml = SafeHtmlUtils.fromString(DIFF_NEED_MORE_HISTORY_MESSAGE);
      renderRightPane();
      return;
    }

    List<PacketProblemCreationLog> sortedLog = Lists.newArrayList(creationLog);
    Collections.sort(sortedLog, new Comparator<PacketProblemCreationLog>() {
      @Override
      public int compare(PacketProblemCreationLog a, PacketProblemCreationLog b) {
        if (a == null || a.date == null) {
          return 1;
        }
        if (b == null || b.date == null) {
          return -1;
        }
        return b.date.compareTo(a.date);
      }
    });

    List<RadioButton> beforeButtons = Lists.newArrayList();
    List<RadioButton> afterButtons = Lists.newArrayList();
    int index = 0;
    for (PacketProblemCreationLog change : sortedLog) {
      panelHistoryList.add(createHistoryItem(change, beforeButtons, afterButtons));
      ++index;
    }

    if (sortedLog.size() >= 2) {
      synchronizingSelection = true;
      afterButtons.get(0).setValue(true);
      beforeButtons.get(1).setValue(true);
      synchronizingSelection = false;
      currentDiffHtml = SafeHtmlUtils.fromString(DIFF_LOADING_MESSAGE);
      renderRightPane();
      presenter.onUpdateDiffTarget(sortedLog.get(1), sortedLog.get(0));
      return;
    }

    currentDiffHtml = SafeHtmlUtils.fromString(DIFF_NEED_MORE_HISTORY_MESSAGE);
    renderRightPane();
	}

  /**
   * 変更履歴1件分のカードUIを生成する。
   *
   * @param change 履歴データ
   * @return 履歴カード
   */
  private Widget createHistoryItem(final PacketProblemCreationLog change, List<RadioButton> beforeButtons,
      List<RadioButton> afterButtons) {
    final VerticalPanel item = new VerticalPanel();
    item.setStyleName("creationHistoryItem");

    VerticalPanel meta = new VerticalPanel();
    meta.setStyleName("creationHistoryMeta");
    HTML date = new HTML(change.getDate());
    date.setStyleName("creationHistoryMetaDate");
    meta.add(date);
    HTML player = new HTML(change.getPlayer());
    player.setStyleName("creationHistoryMetaPlayer");
    meta.add(player);
    item.add(meta);

    Label preview = new Label(buildSummaryPreview(change.summary));
    preview.setStyleName("creationHistoryPreview");
    item.add(preview);

    HorizontalPanel controls = new HorizontalPanel();
    controls.setStyleName("creationHistoryControls");

    RadioButton buttonBefore = new RadioButton(GROUP_BEFORE, "変更前");
    buttonBefore.addClickHandler(this);
    controls.add(buttonBefore);
    buttonToLogBefore.put(buttonBefore, change);
    beforeButtons.add(buttonBefore);

    RadioButton buttonAfter = new RadioButton(GROUP_AFTER, "変更後");
    buttonAfter.addClickHandler(this);
    controls.add(buttonAfter);
    buttonToLogAfter.put(buttonAfter, change);
    afterButtons.add(buttonAfter);

    item.add(controls);
    return item;
  }

  /**
   * 変更内容の先頭を短く要約して表示する。
   *
   * @param summary サマリー本文
   * @return プレビュー文字列
   */
  private String buildSummaryPreview(String summary) {
    if (Strings.isNullOrEmpty(summary)) {
      return "なし";
    }
    String compact = summary.replace('\n', ' ').replace('\r', ' ').trim().replaceAll("\\s+", " ");
    compact = compact.replaceFirst("^変更サマリー\\s*:\\s*", "");
    if (compact.length() <= PREVIEW_MAX_LENGTH) {
      return compact;
    }
    return compact.substring(0, PREVIEW_MAX_LENGTH) + "...";
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
    if (synchronizingSelection) {
      return;
    }
    if (event.getSource() instanceof RadioButton) {
      normalizeSameLogSelection((RadioButton) event.getSource());
    }

		PacketProblemCreationLog before = selectedLog(buttonToLogBefore);
		PacketProblemCreationLog after = selectedLog(buttonToLogAfter);
    if (before == null || after == null) {
      currentDiffHtml = SafeHtmlUtils.fromString(DIFF_EMPTY_MESSAGE);
      renderRightPane();
      return;
    }
    currentDiffHtml = SafeHtmlUtils.fromString(DIFF_LOADING_MESSAGE);
    renderRightPane();
		presenter.onUpdateDiffTarget(before, after);
	}

  private void normalizeSameLogSelection(RadioButton source) {
    PacketProblemCreationLog before = buttonToLogBefore.get(source);
    if (before != null && source.getValue()) {
      unselectSameLog(buttonToLogAfter, before);
      return;
    }

    PacketProblemCreationLog after = buttonToLogAfter.get(source);
    if (after != null && source.getValue()) {
      unselectSameLog(buttonToLogBefore, after);
    }
  }

  private void unselectSameLog(Map<RadioButton, PacketProblemCreationLog> buttonMap, PacketProblemCreationLog target) {
    synchronizingSelection = true;
    for (Entry<RadioButton, PacketProblemCreationLog> entry : buttonMap.entrySet()) {
      if (entry.getValue() == target && entry.getKey().getValue()) {
        entry.getKey().setValue(false);
      }
    }
    synchronizingSelection = false;
  }

	@Override
	public void setDiffHtml(SafeHtml html) {
    currentDiffHtml = html;
    renderRightPane();
	}

  private void renderRightPane() {
    htmlDiff.setHTML(currentDiffHtml);
  }

}
