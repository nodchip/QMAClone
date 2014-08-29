package tv.dyndns.kishibe.qmaclone.client.setting;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryTreeViewModel;

import com.google.common.base.Preconditions;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingThemeQueryView extends VerticalPanel implements ClickHandler,
		PanelSettingThemeQuery.View {

	private static final String DESCRIPTION = createDescription();
	// この値を変更した場合はデータベースのtheme_modeテーブルのprimary keyのフィールドの長さも変更すること。
	private static final int MAX_THEME_LENGTH = 8;
	private static final int MAX_QUERY_LENGTH = 64;
	private PanelSettingThemeQuery presenter;
	private final Button addButton = new Button("追加", this);
	private final Button removeButton = new Button("削除", this);
	private final Button applyButton = new Button("テーマモードの編集権限の申請をする(確認画面が出ます)", this);
	private CellBrowser cellBrowser;
	private final TextBox textBoxTheme = new TextBox();
	private final TextBox textBoxQuery = new TextBox();
	private final TextBox textBoxNote = new TextBox();
	private ThemeQueryTreeViewModel themeQueryTreeViewModel;

	public void setPresenter(PanelSettingThemeQuery panelSettingThemeMode,
			ThemeQueryTreeViewModel themeQueryTreeViewModel) {
		this.presenter = Preconditions.checkNotNull(panelSettingThemeMode);
		this.themeQueryTreeViewModel = Preconditions.checkNotNull(themeQueryTreeViewModel);
	}

	@Override
	public void showEditForm(int numberOfThemeQueries) {
		add(new HTML(new SafeHtmlBuilder().appendEscapedLines(DESCRIPTION).toSafeHtml()));

		{
			HorizontalPanel panel = new HorizontalPanel();
			textBoxTheme.setWidth("200px");
			textBoxTheme.setMaxLength(MAX_THEME_LENGTH);
			textBoxQuery.setWidth("200px");
			textBoxQuery.setMaxLength(MAX_QUERY_LENGTH);
			panel.add(new HTML("テーマ"));
			panel.add(textBoxTheme);
			panel.add(new HTML("単語"));
			panel.add(textBoxQuery);
			panel.add(addButton);
			panel.add(removeButton);
			add(panel);
		}

		cellBrowser = new CellBrowser.Builder<Object>(themeQueryTreeViewModel, null).build();
		cellBrowser.setWidth("100%");
		cellBrowser.setHeight("600px");
		cellBrowser.setAnimationEnabled(true);
		cellBrowser.setDefaultColumnWidth(300);
		add(cellBrowser);
	}

	@Override
	public void showNoRight() {
		add(new HTML(
				"100回以上プレイされた方はテーマモードの編集が出来ます。<br>編集権限を御希望の方は100回以上プレイされた後、<br>この画面から申請してください。"));
	}

	@Override
	public void showHaveRight() {
		add(new HTML("テーマモードの編集権限の申請が出来ます。<br>編集権限を御希望の方は以下のボタンを押してください。"));
	}

	@Override
	public void showApplyingRight() {
		add(applyButton);
		applyButton.setText("現在申請中です。承認されるまでしばらくお待ちください。");
		applyButton.setEnabled(false);
	}

	@Override
	public void showApplyRightForm() {
		add(new HTML("申請にあたり作りたい検定や自己アピール等をお願いいたします。"));
		textBoxNote.setWidth("400px");
		add(textBoxNote);
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

}
