package tv.dyndns.kishibe.qmaclone.client.setting;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeCell;
import tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeProvider;
import tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryCell;
import tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryProvider;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class PanelSettingThemeQueryView extends VerticalPanel implements ClickHandler,
		PanelSettingThemeQuery.View {

	interface ThemeModeCellListResources extends CellList.Resources {
		@Override
		@Source("ThemeModeCellList.css")
		CellList.Style cellListStyle();
	}

	private static final String DESCRIPTION = createDescription();
	// この値を変更した場合はデータベースのtheme_modeテーブルのprimary keyのフィールドの長さも変更すること。
	private static final int MAX_THEME_LENGTH = 8;
	private static final int MAX_QUERY_LENGTH = 64;
	private static final ThemeModeCellListResources THEME_MODE_CELL_LIST_RESOURCES = GWT
			.create(ThemeModeCellListResources.class);
	private PanelSettingThemeQuery presenter;
	private final Button addButton = new Button("選択中テーマに追加", this);
	private final Button removeButton = new Button("選択中テーマから削除", this);
	private final Button applyButton = new Button("テーマモードの編集権限の申請をする(確認画面が出ます)", this);
	private final TextBox textBoxTheme = new TextBox();
	private final TextBox textBoxQuery = new TextBox();
	private final TextBox textBoxNote = new TextBox();
	private final HTML htmlSelectionSummary = new HTML();
	private final ProvidesKey<PacketTheme> themeKeyProvider = new ProvidesKey<PacketTheme>() {
		@Override
		public Object getKey(PacketTheme item) {
			return item == null ? null : item.getName();
		}
	};
	private final ProvidesKey<PacketThemeQuery> themeQueryKeyProvider = new ProvidesKey<PacketThemeQuery>() {
		@Override
		public Object getKey(PacketThemeQuery item) {
			return item == null ? null : item.getTheme() + "\t" + item.getQuery();
		}
	};
	private CellList<PacketTheme> themeList;
	private CellList<PacketThemeQuery> queryList;
	private SingleSelectionModel<PacketTheme> themeSelectionModel;
	private SingleSelectionModel<PacketThemeQuery> querySelectionModel;
	private ThemeProvider themeProvider;
	private ThemeQueryProvider themeQueryProvider;
	private String selectedThemeName = "";

	public void setPresenter(PanelSettingThemeQuery panelSettingThemeMode) {
		this.presenter = Preconditions.checkNotNull(panelSettingThemeMode);
		setStyleName("settingThemeModeRoot");
	}

	@Override
	public void showEditForm(int numberOfThemeQueries) {
		VerticalPanel introCard = new VerticalPanel();
		introCard.setWidth("100%");
		introCard.setStyleName("settingThemeModeIntroCard");
		HTML title = new HTML("<h3 class='settingThemeModeTitle'>テーマモード設定</h3>");
		title.addStyleName("settingThemeModeIntroTitle");
		introCard.add(title);
		HTML lead = new HTML(
				"<p class='settingThemeModeLead'>テーマと単語を組み合わせて、テーマモードの出題対象を設定します。</p>");
		lead.addStyleName("settingThemeModeIntroLead");
		introCard.add(lead);
		HTML relationHelp = new HTML(
				"<p class='settingThemeModeRelationHelp'>左列でテーマを選ぶと、右列にそのテーマの単語が表示されます。</p>");
		relationHelp.addStyleName("settingThemeModeRelationHelpCard");
		introCard.add(relationHelp);
		HTML guide = new HTML(new SafeHtmlBuilder().appendEscapedLines(DESCRIPTION).toSafeHtml());
		guide.addStyleName("settingThemeModeGuide");
		introCard.add(guide);
		add(introCard);

		htmlSelectionSummary.setStyleName("settingThemeModeSelectionSummary");
		setSelectionSummary("", "");
		add(htmlSelectionSummary);

		{
			HorizontalPanel inputRow = new HorizontalPanel();
			inputRow.setStyleName("settingThemeModeFormRow");
			textBoxTheme.setWidth("140px");
			textBoxTheme.setMaxLength(MAX_THEME_LENGTH);
			textBoxTheme.addStyleName("settingThemeModeInput");
			textBoxQuery.setWidth("280px");
			textBoxQuery.setMaxLength(MAX_QUERY_LENGTH);
			textBoxQuery.addStyleName("settingThemeModeInput");
			addButton.addStyleName("creationButtonPrimary");
			removeButton.addStyleName("creationButtonSecondary");
			addButton.addStyleName("settingThemeModeActionButton");
			removeButton.addStyleName("settingThemeModeActionButton");
			inputRow.add(new HTML("<span class='settingThemeModeFieldLabel'>テーマ</span>"));
			inputRow.add(textBoxTheme);
			inputRow.add(new HTML("<span class='settingThemeModeFieldLabel'>単語</span>"));
			inputRow.add(textBoxQuery);
			add(inputRow);

			HorizontalPanel actionRow = new HorizontalPanel();
			actionRow.setStyleName("settingThemeModeFormRow settingThemeModeFormActions");
			actionRow.add(addButton);
			actionRow.add(removeButton);
			add(actionRow);
		}

		HorizontalPanel listPanel = new HorizontalPanel();
		listPanel.setWidth("100%");
		listPanel.setStyleName("settingThemeModeListPanel");
		add(listPanel);

		VerticalPanel themeColumn = new VerticalPanel();
		themeColumn.setWidth("100%");
		themeColumn.setStyleName("settingThemeModeListColumn");
		themeColumn.add(new HTML("<h4 class='settingThemeModeListTitle'>テーマ</h4>"));
		listPanel.add(themeColumn);
		listPanel.setCellWidth(themeColumn, "50%");

		VerticalPanel queryColumn = new VerticalPanel();
		queryColumn.setWidth("100%");
		queryColumn.setStyleName("settingThemeModeListColumn");
		queryColumn.add(new HTML("<h4 class='settingThemeModeListTitle'>単語</h4>"));
		listPanel.add(queryColumn);
		listPanel.setCellWidth(queryColumn, "50%");

		themeList = new CellList<PacketTheme>(new ThemeCell(), THEME_MODE_CELL_LIST_RESOURCES);
		themeList.setStyleName("settingThemeModeList");
		themeSelectionModel = new SingleSelectionModel<PacketTheme>(themeKeyProvider);
		themeList.setSelectionModel(themeSelectionModel);
		themeSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				PacketTheme selectedTheme = themeSelectionModel.getSelectedObject();
				selectedThemeName = selectedTheme == null ? "" : selectedTheme.getName();
				presenter.onThemeQuerySelected(selectedThemeName, "");
				bindThemeQueryProvider(selectedThemeName);
				themeList.redraw();
			}
		});
		ScrollPanel themeScroll = new ScrollPanel(themeList);
		themeScroll.setStyleName("settingThemeModeListScroll");
		themeScroll.setHeight("600px");
		themeScroll.setWidth("100%");
		themeColumn.add(themeScroll);

		queryList = new CellList<PacketThemeQuery>(new ThemeQueryCell(),
				THEME_MODE_CELL_LIST_RESOURCES);
		queryList.setStyleName("settingThemeModeList");
		querySelectionModel = new SingleSelectionModel<PacketThemeQuery>(themeQueryKeyProvider);
		queryList.setSelectionModel(querySelectionModel);
		querySelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				PacketThemeQuery selectedQuery = querySelectionModel.getSelectedObject();
				if (selectedQuery == null) {
					return;
				}
				selectedThemeName = selectedQuery.getTheme();
				presenter.onThemeQuerySelected(selectedThemeName, selectedQuery.getQuery());
				queryList.redraw();
			}
		});
		ScrollPanel queryScroll = new ScrollPanel(queryList);
		queryScroll.setStyleName("settingThemeModeListScroll");
		queryScroll.setHeight("600px");
		queryScroll.setWidth("100%");
		queryColumn.add(queryScroll);

		themeProvider = new ThemeProvider(presenter);
		themeProvider.addDataDisplay(themeList);
	}

	@Override
	public void showNoRight() {
		HTML card = new HTML(
				"<h3 class='settingThemeModeTitle'>テーマモード編集権限の申請</h3>"
						+ "<p class='settingThemeModeLead'>100回以上プレイすると、テーマモード編集権限を申請できます。"
						+ "条件達成後に、この画面から申請してください。</p>");
		card.addStyleName("settingThemeModeInfoCard");
		add(card);
	}

	@Override
	public void showHaveRight() {
		HTML card = new HTML(
				"<h3 class='settingThemeModeTitle'>テーマモード編集権限の申請</h3>"
						+ "<p class='settingThemeModeLead'>編集権限の申請が可能です。必要に応じて以下から申請してください。</p>");
		card.addStyleName("settingThemeModeInfoCard");
		add(card);
	}

	@Override
	public void showApplyingRight() {
		applyButton.addStyleName("creationButtonSecondary");
		applyButton.addStyleName("settingThemeModeApplyButton");
		add(applyButton);
		applyButton.setText("現在申請中です。承認されるまでしばらくお待ちください。");
		applyButton.setEnabled(false);
	}

	@Override
	public void showApplyRightForm() {
		HTML card = new HTML(
				"<p class='settingThemeModeLead'>申請理由（作りたい検定や自己アピールなど）を入力してください。</p>");
		card.addStyleName("settingThemeModeApplyCard");
		add(card);
		textBoxNote.setWidth("400px");
		textBoxNote.addStyleName("settingThemeModeInput");
		textBoxNote.addStyleName("settingThemeModeNoteInput");
		add(textBoxNote);
		applyButton.addStyleName("creationButtonPrimary");
		applyButton.addStyleName("settingThemeModeApplyButton");
		add(applyButton);
	}

	@Override
	public String getTheme() {
		return textBoxTheme.getText();
	}

	@Override
	public void setTheme(String theme) {
		textBoxTheme.setText(theme);
	}

	@Override
	public String getQuery() {
		return textBoxQuery.getText();
	}

	@Override
	public void setQuery(String query) {
		textBoxQuery.setText(query);
	}

	@Override
	public void setSelectionSummary(String theme, String query) {
		String selectedTheme = theme == null || theme.isEmpty() ? "未選択" : theme;
		String selectedQuery = query == null || query.isEmpty() ? "未選択" : query;
		htmlSelectionSummary.setHTML(
				"<span class='settingThemeModeSelectionLabel'>選択中テーマ:</span> "
						+ "<span class='settingThemeModeSelectionValue'>" + escape(selectedTheme) + "</span>"
						+ " <span class='settingThemeModeSelectionDivider'>/</span> "
						+ "<span class='settingThemeModeSelectionLabel'>選択中単語:</span> "
						+ "<span class='settingThemeModeSelectionValue'>" + escape(selectedQuery) + "</span>");
	}

	@Override
	public String getNote() {
		return textBoxNote.getText();
	}

	@Override
	public boolean confirmToApply() {
		return Window.confirm("テーマモードの編集権限の申請をします\nよろしいですか？");
	}

	@Override
	public void notifyApplying() {
		applyButton.setText("現在申請中です。承認されるまでしばらくお待ちください。");
		applyButton.setEnabled(false);
	}

	@Override
	public void enableForm(boolean enabled) {
		addButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
	}

	@Override
	public void refreshThemeModeLists(boolean refreshThemes) {
		if (refreshThemes && themeProvider != null) {
			themeProvider.refresh();
		}
		if (themeQueryProvider != null) {
			themeQueryProvider.refresh();
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		Object source = event.getSource();
		if (source == addButton) {
			presenter.onAddButtonClicked();
		} else if (source == removeButton) {
			presenter.onRemoveButtonClicked();
		} else if (source == applyButton) {
			presenter.onApplyButtonClicked();
		}
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		presenter.onViewLoaded();
	}

	private static String createDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("申請有難うございました。\n");
		sb.append("\n");
		sb.append("テーマモードのテーマの設定を行います。\n");
		sb.append("それぞれのテーマは「テーマ」と「単語」の組み合わせでできています。\n");
		sb.append("それぞれのテーマに設定されている単語が文章中に含まれる問題が出題されます。\n");
		sb.append("問題検索同様「-」による除外検索を行うこともできます\n");
		sb.append("テーマの後の()内の数字は条件に合致した問題数です\n");
		sb.append("ロビーには100問以上登録されている問題のみ表示されます\n");
		sb.append("\n");
		sb.append("「ジャンル:」から始まる単語を指定すると特定のジャンルで絞り込むことができます。使用できる単語は以下の通りです。\n");
		for (ProblemGenre genre : ProblemGenre.values()) {
			sb.append("「ジャンル:").append(genre.toString()).append("」");
		}
		sb.append("\n");
		sb.append("\n");
		sb.append("「問題形式:」から始まる単語を指定すると特定の問題形式で絞り込むことができます。使用できる単語は以下のとおりです。\n");
		for (ProblemType type : ProblemType.valuesWithoutRandom) {
			sb.append("「問題形式:").append(type.toString()).append("」");
		}
		sb.append("\n");
		sb.append("\n");
		sb.append("「問題作成者:」から始まる単語を指定すると特定の問題作成者で絞り込むことができます。(例:「問題作成者:ノドチップ」");
		sb.append("\n");
		sb.append("\n");
		sb.append("「ランダム:」から始まる単語を指定するとランダムフラグで絞り込むことができます。(例:「ランダム:1」\n");
		return sb.toString();
	}

	private static String escape(String value) {
		return new SafeHtmlBuilder().appendEscaped(value).toSafeHtml().asString();
	}

	private void bindThemeQueryProvider(String themeName) {
		querySelectionModel.setSelected(querySelectionModel.getSelectedObject(), false);
		queryList.setRowCount(0, true);
		if (themeQueryProvider != null) {
			themeQueryProvider.removeDataDisplay(queryList);
		}
		if (themeName == null || themeName.isEmpty()) {
			queryList.redraw();
			return;
		}
		themeQueryProvider = new ThemeQueryProvider(presenter, themeName);
		themeQueryProvider.addDataDisplay(queryList);
	}

}
