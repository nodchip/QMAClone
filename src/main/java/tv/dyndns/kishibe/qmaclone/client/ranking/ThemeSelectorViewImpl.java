package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;

import com.google.common.base.Preconditions;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;

public class ThemeSelectorViewImpl extends Tree implements ThemeSelectorPresenter.View,
		SelectionHandler<TreeItem> {

	private ThemeSelectorPresenter presenter;

	@Inject
	public ThemeSelectorViewImpl(ThemeSelectorPresenter presenter) {
		this.presenter = Preconditions.checkNotNull(presenter);
		presenter.setView(this);

		setAnimationEnabled(true);
		addSelectionHandler(this);
		setPixelSize(ThemeRankingViewImpl.LEFT_WIDTH, 400);
	}

	@Override
	public void setTheme(List<List<String>> themess) {
		int index = 1;
		for (ProblemGenre genre : ProblemGenre.values()) {
			TreeItem treeItem = addTextItem(genre.toString());

			List<String> themes = themess.get(genre.getIndex());
			for (String theme : themes) {
				String value = index++ + " " + theme;
				treeItem.addTextItem(value);
			}
		}
	}

	@Override
	public void onSelection(SelectionEvent<TreeItem> event) {
		String label = event.getSelectedItem().getText();
		if (!label.contains(" ")) {
			// ジャンル名が選択された
			return;
		}
		String theme = label.substring(label.indexOf(' ') + 1);
		presenter.onThemeSelected(theme);
	}

}
