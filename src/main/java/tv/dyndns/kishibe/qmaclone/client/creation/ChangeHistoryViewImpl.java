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
  private final Map<Button, PacketProblemCreationLog> detailButtonToLog = Maps.newHashMap();
  private final Map<PacketProblemCreationLog, SafeHtml> detailHtmlByLog = Maps.newHashMap();
  private boolean synchronizingSelection;
  private PacketProblemCreationLog openedDetail;
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
    detailButtonToLog.clear();
    detailHtmlByLog.clear();
    openedDetail = null;
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
    PacketProblemCreationLog previous = null;
    for (PacketProblemCreationLog change : sortedLog) {
      panelHistoryList.add(createHistoryItem(change, previous, false, beforeButtons, afterButtons));
      previous = change;
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
   * @param expanded 初期展開状態
   * @return 履歴カード
   */
  private Widget createHistoryItem(final PacketProblemCreationLog change, PacketProblemCreationLog previous,
      boolean expanded, List<RadioButton> beforeButtons, List<RadioButton> afterButtons) {
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

    detailHtmlByLog.put(change, buildDetailDiffHtml(previous == null ? "" : previous.summary, change.summary));

    final Button toggleDetail = new Button(expanded ? TOGGLE_CLOSE : TOGGLE_OPEN);
    toggleDetail.setStyleName("creationButtonSecondary");
    toggleDetail.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        toggleDetailTarget(change);
      }
    });
    toggleDetail.setEnabled(!Strings.isNullOrEmpty(change.summary));
    detailButtonToLog.put(toggleDetail, change);
    controls.add(toggleDetail);

    item.add(controls);
    refreshDetailButtonLabels();
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

  private SafeHtml buildDetailDiffHtml(String previousSummary, String currentSummary) {
    Map<String, String> beforeSections = parseSummarySections(previousSummary);
    Map<String, String> afterSections = parseSummarySections(currentSummary);
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    String[] sectionIds = { "ジャンル", "出題形式", "ランダムフラグ", "問題文", "選択肢", "解答", "問題作成者",
        "問題ノート", "表示選択肢数" };
    boolean hasDiff = false;

    builder.appendHtmlConstant("<div class='creationHistoryDiffPreview'>");
    builder.appendHtmlConstant("<div class='creationHistoryDiffSummary'>");
    if (previousSummary == null || previousSummary.isEmpty()) {
      builder.appendEscaped("基準となる前回版がありません。");
      builder.appendHtmlConstant("</div></div>");
      return builder.toSafeHtml();
    }

    for (String section : sectionIds) {
      String before = Strings.nullToEmpty(beforeSections.get(section));
      String after = Strings.nullToEmpty(afterSections.get(section));
      if (before.equals(after)) {
        continue;
      }

      hasDiff = true;
      builder.appendHtmlConstant("<div class='creationHistoryDiffItem'>");
      builder.appendEscaped(section).appendHtmlConstant(":");
      builder.appendHtmlConstant("<div class='creationHistoryDiffRow'>");
      builder.appendHtmlConstant("<div class='creationHistoryDiffBefore'>");
      builder.appendEscaped("変更前: ");
      builder.appendHtmlConstant(escapeSummaryToHtml(before));
      builder.appendHtmlConstant("</div>");
      builder.appendHtmlConstant("<div class='creationHistoryDiffAfter'>");
      builder.appendEscaped("変更後: ");
      builder.appendHtmlConstant(escapeSummaryToHtml(after));
      builder.appendHtmlConstant("</div>");
      builder.appendHtmlConstant("</div>");
      builder.appendHtmlConstant("</div>");
    }

    if (!hasDiff) {
      builder.appendHtmlConstant("<div class='creationHistoryDiffNoChange'>");
      builder.appendEscaped("変更点はありません。");
      builder.appendHtmlConstant("</div>");
    }
    builder.appendHtmlConstant("</div></div>");
    return builder.toSafeHtml();
  }

  private Map<String, String> parseSummarySections(String summary) {
    Map<String, String> sections = Maps.newLinkedHashMap();
    if (summary == null) {
      return sections;
    }

    Map<String, StringBuilder> sectionBuilders = Maps.newLinkedHashMap();
    String currentSection = null;
    String[] lines = summary.replace("\r", "").split("\n", -1);
    for (String line : lines) {
      String section = parseSectionId(line);
      if (section != null) {
        if (currentSection != null && sectionBuilders.containsKey(currentSection)) {
          sections.put(currentSection, sectionBuilders.remove(currentSection).toString().trim());
        }

        if (isSingleLineSection(section)) {
          int index = line.indexOf(':');
          if (index < 0) {
            index = line.indexOf('：');
          }
          String value = "";
          if (index + 1 < line.length()) {
            value = line.substring(index + 1).trim();
          }
          sections.put(section, value);
          currentSection = null;
        } else {
          currentSection = section;
          sectionBuilders.put(section, new StringBuilder());
          int index = line.indexOf(':');
          if (index < 0) {
            index = line.indexOf('：');
          }
          if (index + 1 < line.length()) {
            sectionBuilders.get(section).append(line.substring(index + 1).trim());
          }
        }
        continue;
      }

      if (currentSection != null && sectionBuilders.containsKey(currentSection)) {
        if (sectionBuilders.get(currentSection).length() > 0) {
          sectionBuilders.get(currentSection).append('\n');
        }
        sectionBuilders.get(currentSection).append(line);
      }
    }

    if (currentSection != null && sectionBuilders.containsKey(currentSection)) {
      sections.put(currentSection, sectionBuilders.remove(currentSection).toString().trim());
    }
    return sections;
  }

  private String parseSectionId(String line) {
    if (line == null) {
      return null;
    }
    if (line.startsWith("ジャンル:") || line.startsWith("ジャンル：")) {
      return "ジャンル";
    }
    if (line.startsWith("出題形式:") || line.startsWith("出題形式：")) {
      return "出題形式";
    }
    if (line.startsWith("ランダム:") || line.startsWith("ランダム：") || line.startsWith("ランダムフラグ:")
        || line.startsWith("ランダムフラグ：")) {
      return "ランダムフラグ";
    }
    if (line.startsWith("問題文:") || line.startsWith("問題文：")) {
      return "問題文";
    }
    if (line.startsWith("選択肢:") || line.startsWith("選択肢：")) {
      return "選択肢";
    }
    if (line.startsWith("解答:") || line.startsWith("解答：")) {
      return "解答";
    }
    if (line.startsWith("問題作成者:") || line.startsWith("問題作成者：")) {
      return "問題作成者";
    }
    if (line.startsWith("問題ノート:") || line.startsWith("問題ノート：")) {
      return "問題ノート";
    }
    if (line.startsWith("表示選択肢数:") || line.startsWith("表示選択肢数：")) {
      return "表示選択肢数";
    }
    return null;
  }

  private boolean isSingleLineSection(String sectionId) {
    return "ジャンル".equals(sectionId) || "出題形式".equals(sectionId) || "ランダムフラグ".equals(sectionId)
        || "問題作成者".equals(sectionId) || "表示選択肢数".equals(sectionId);
  }

  private String escapeSummaryToHtml(String value) {
    return Strings.nullToEmpty(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n",
        "<br/>");
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

  private void toggleDetailTarget(PacketProblemCreationLog target) {
    if (openedDetail == target) {
      openedDetail = null;
    } else {
      openedDetail = target;
    }
    refreshDetailButtonLabels();
    renderRightPane();
  }

  private void refreshDetailButtonLabels() {
    for (Entry<Button, PacketProblemCreationLog> entry : detailButtonToLog.entrySet()) {
      if (entry.getValue() == openedDetail) {
        entry.getKey().setText(TOGGLE_CLOSE);
      } else {
        entry.getKey().setText(TOGGLE_OPEN);
      }
    }
  }

  private void renderRightPane() {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    if (openedDetail != null && detailHtmlByLog.containsKey(openedDetail)) {
      builder.appendHtmlConstant("<div class='creationHistoryDiffCard creationHistoryDetailTopCard'>");
      builder.appendHtmlConstant("<h3>選択中の履歴詳細</h3>");
      builder.append(detailHtmlByLog.get(openedDetail));
      builder.appendHtmlConstant("</div>");
    }
    builder.append(currentDiffHtml);
    htmlDiff.setHTML(builder.toSafeHtml());
  }

}
