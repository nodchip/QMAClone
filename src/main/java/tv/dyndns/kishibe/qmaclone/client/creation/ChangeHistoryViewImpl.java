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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
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
  private static final String TOGGLE_OPEN = "詳細を開く";
  private static final String TOGGLE_CLOSE = "詳細を閉じる";
  private static final int PREVIEW_MAX_LENGTH = 96;

	interface PanelProblemChangeHistoryUiBinder extends UiBinder<Widget, ChangeHistoryViewImpl> {
	}

	@UiField
  VerticalPanel panelHistoryList;
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
		panelHistoryList.clear();
    buttonToLogBefore.clear();
    buttonToLogAfter.clear();
    htmlDiff.setHTML("履歴を選択すると、ここに差分を表示します。");

    if (creationLog == null || creationLog.isEmpty()) {
      panelHistoryList.add(new Label("編集履歴はまだありません。"));
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

    int index = 0;
    for (PacketProblemCreationLog change : sortedLog) {
      panelHistoryList.add(createHistoryItem(change, index == 0));
      ++index;
    }
	}

  /**
   * 変更履歴1件分のカードUIを生成する。
   *
   * @param change 履歴データ
   * @param expanded 初期展開状態
   * @return 履歴カード
   */
  private Widget createHistoryItem(final PacketProblemCreationLog change, boolean expanded) {
    final VerticalPanel item = new VerticalPanel();
    item.setStyleName("creationHistoryItem");

    HorizontalPanel meta = new HorizontalPanel();
    meta.setStyleName("creationHistoryMeta");
    meta.add(new HTML(change.getDate()));
    meta.add(new HTML(change.getPlayer()));
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

    RadioButton buttonAfter = new RadioButton(GROUP_AFTER, "変更後");
    buttonAfter.addClickHandler(this);
    controls.add(buttonAfter);
    buttonToLogAfter.put(buttonAfter, change);

    final HTML detail = new HTML(new SafeHtmlBuilder().appendEscapedLines(nullToDefault(change.summary))
        .toSafeHtml());
    detail.setStyleName("creationHistoryDetail");
    detail.setVisible(expanded && !Strings.isNullOrEmpty(change.summary));

    final Button toggleDetail = new Button(expanded ? TOGGLE_CLOSE : TOGGLE_OPEN);
    toggleDetail.setStyleName("creationButtonSecondary");
    toggleDetail.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean open = !detail.isVisible();
        detail.setVisible(open);
        toggleDetail.setText(open ? TOGGLE_CLOSE : TOGGLE_OPEN);
      }
    });
    toggleDetail.setEnabled(!Strings.isNullOrEmpty(change.summary));
    controls.add(toggleDetail);

    item.add(controls);
    item.add(detail);
    return item;
  }

  private String nullToDefault(String text) {
    return Strings.isNullOrEmpty(text) ? "変更サマリーはありません。" : text;
  }

  /**
   * 変更内容の先頭を短く要約して表示する。
   *
   * @param summary サマリー本文
   * @return プレビュー文字列
   */
  private String buildSummaryPreview(String summary) {
    if (Strings.isNullOrEmpty(summary)) {
      return "変更サマリー: なし";
    }
    String compact = summary.replace('\n', ' ').replace('\r', ' ').trim().replaceAll("\\s+", " ");
    if (compact.length() <= PREVIEW_MAX_LENGTH) {
      return "変更サマリー: " + compact;
    }
    return "変更サマリー: " + compact.substring(0, PREVIEW_MAX_LENGTH) + "...";
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
