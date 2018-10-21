//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.setting.theme.ThemeQueryTreeViewModel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;

public class PanelSettingThemeQuery {

	public interface View extends IsWidget {
		void setPresenter(PanelSettingThemeQuery panelSettingThemeMode,
				ThemeQueryTreeViewModel themeQueryTreeViewModel);

		boolean isAttached();

		void showEditForm(int numberOfThemeQueries);

		void showNoRight();

		void showHaveRight();

		void showApplyingRight();

		void showApplyRightForm();

		String getTheme();

		void setTheme(String theme);

		String getQuery();

		void setQuery(String query);

		String getNote();

		boolean confirmToApply();

		void notifyApplying();

		void enableForm(boolean enabled);
	}

	private static final Logger logger = Logger.getLogger(PanelSettingThemeQuery.class.getName());
	private static final int PLAY_COUNT_THRESHOLD = 100;
	@VisibleForTesting
	static final int UPDATE_PERIOD = 1000;
	private final View view;
	private final ServiceAsync serviceAsync;
	private final Scheduler scheduler;
	@VisibleForTesting
	final RepeatingCommand commandUpdateForm = new RepeatingCommand() {
		@Override
		public boolean execute() {
			updateForm();
			return view.isAttached();
		}
	};
	private ThemeQueryTreeViewModel themeQueryTreeViewModel;
	private Set<String> themeNames;

	public PanelSettingThemeQuery(View view, ServiceAsync serviceAsync, Scheduler scheduler) {
		this.view = Preconditions.checkNotNull(view);
		this.serviceAsync = Preconditions.checkNotNull(serviceAsync);
		this.scheduler = Preconditions.checkNotNull(scheduler);
		this.themeQueryTreeViewModel = new ThemeQueryTreeViewModel(this);
		view.setPresenter(this, themeQueryTreeViewModel);
		serviceAsync.isThemeModeEditor(UserData.get().getUserCode(), callbackIsThemeModeEditor);
	}

	public IsWidget asWidget() {
		return view;
	}

	@VisibleForTesting
	final AsyncCallback<Boolean> callbackIsThemeModeEditor = new AsyncCallback<Boolean>() {
		public void onSuccess(Boolean result) {
			if (result) {
				// テーマクエリ一覧を表示するために数を取得する
				serviceAsync.getNumberofThemeQueries(callbackGetNumberofThemeQueries);

			} else if (UserData.get().getPlayCount() >= PLAY_COUNT_THRESHOLD) {
				view.showHaveRight();
				serviceAsync.isApplyingThemeModeEditor(UserData.get().getUserCode(),
						callbackIsApplyingThemeModeEditor);

			} else {
				view.showNoRight();
			}
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマモード編集権限の取得に失敗しました", caught);
		}
	};

	@VisibleForTesting
	final AsyncCallback<Boolean> callbackIsApplyingThemeModeEditor = new AsyncCallback<Boolean>() {
		public void onSuccess(Boolean result) {
			if (result) {
				view.showApplyingRight();
			} else {
				view.showApplyRightForm();
			}
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマモード編集権限の申請に失敗しました", caught);
		}
	};

	@VisibleForTesting
	final AsyncCallback<Integer> callbackGetNumberofThemeQueries = new AsyncCallback<Integer>() {
		@Override
		public void onSuccess(Integer result) {
			view.showEditForm(result);
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマクエリの数の取得に失敗しました", caught);
		}
	};

	public void onAddButtonClicked() {
		String theme = view.getTheme();
		String query = view.getQuery();
		serviceAsync.addThemeModeQuery(theme, query, UserData.get().getUserCode(),
				callbackUpdateThemeModeQuery);
	}

	public void onRemoveButtonClicked() {
		String theme = view.getTheme();
		String query = view.getQuery();
		serviceAsync.removeThemeModeQuery(theme, query, UserData.get().getUserCode(),
				callbackUpdateThemeModeQuery);
	}

	@VisibleForTesting
	final AsyncCallback<Void> callbackUpdateThemeModeQuery = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			String theme = view.getTheme();
			// 新規テーマを追加した場合はテーマリストを更新する
			themeQueryTreeViewModel.refresh(themeNames != null && !themeNames.contains(theme));
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマクエリの更新に失敗しました", caught);
		}
	};

	public void onApplyButtonClicked() {
		if (!view.confirmToApply()) {
			return;
		}

		serviceAsync.applyThemeModeEditor(UserData.get().getUserCode(), view.getNote(),
				callbackApplyThemeModeEditor);
		view.notifyApplying();
	}

	@VisibleForTesting
	final AsyncCallback<Void> callbackApplyThemeModeEditor = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "テーマモードの申請に失敗しました", caught);
		}
	};

	private void updateForm() {
		boolean enabled = checkForm();
		view.enableForm(enabled);
	}

	private boolean checkForm() {
		return !view.getTheme().isEmpty() && !view.getQuery().isEmpty();
	}

	public void onViewLoaded() {
		scheduler.scheduleFixedDelay(commandUpdateForm, UPDATE_PERIOD);
	}

	public void onThemeQuerySelected(String theme, String query) {
		view.setTheme(theme);
		view.setQuery(query);
	}

	public void onThemeRequested(final HasData<PacketTheme> display) {
		serviceAsync.getThemes(new AsyncCallback<List<PacketTheme>>() {
			@Override
			public void onSuccess(List<PacketTheme> result) {
				display.setRowCount(result.size(), true);
				display.setRowData(0, result);
				display.setVisibleRange(0, result.size());

				themeNames = Sets.newHashSet();
				for (PacketTheme theme : result) {
					themeNames.add(theme.getName());
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.WARNING, "テーマリストの取得に失敗しました", caught);
			}
		});
	}

	public void onThemeQueryRequested(String theme, final HasData<PacketThemeQuery> display) {
		serviceAsync.getThemeQueries(theme, new AsyncCallback<List<PacketThemeQuery>>() {
			@Override
			public void onSuccess(List<PacketThemeQuery> result) {
				display.setRowCount(result.size(), true);
				display.setRowData(0, result);
				display.setVisibleRange(0, result.size());
			}

			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.WARNING, "テーマクエリの取得に失敗しました", caught);
			}
		});
	}

}
