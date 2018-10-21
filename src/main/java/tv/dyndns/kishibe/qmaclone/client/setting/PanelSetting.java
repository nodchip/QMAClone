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

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.SharedData;
import tv.dyndns.kishibe.qmaclone.client.ui.TwoColumnSelectionPanel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PanelSetting extends TwoColumnSelectionPanel {

	@Inject
	public PanelSetting(final PanelSettingUserCodePresenter.View.Factory userCodeFactory) {
		super(120);
		setWidth("800px");
		add("設定トップ", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return new PanelSettingTop();
			}
		});
		add("アイコン", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return new PanelSettingIcon();
			}
		});
		add("ユーザーコード", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return userCodeFactory.create().asWidget();
			}
		});
		add("正答率統計", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return new PanelSettingRatioReport();
			}
		});
		add("チャット", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return PanelSettingChat.getInstance();
			}
		});
		add("テーマモード", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return new PanelSettingThemeQuery(new PanelSettingThemeQueryView(), Service.Util
						.getInstance(), Scheduler.get()).asWidget().asWidget();
			}
		});
		add("テーマモード<br>編集ログ", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return new PanelSettingThemeModeEditLog(new PanelSettingThemeModeEditLogView(),
						Service.Util.getInstance()).asWidget().asWidget();
			}
		});
		add("画像リンク切れ", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return new PanelSettingImageLink();
			}
		});
		add("指摘問題", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return new PanelSettingIndicatedProblems();
			}
		});
		add("その他", new LazyPanel() {
			@Override
			protected Widget createWidget() {
				return new OtherUi();
			}
		});

		if (SharedData.get().isAdministoratorMode()) {
			add("管理者用", new LazyPanel() {
				@Override
				protected Widget createWidget() {
					return new PanelSettingAdministrator();
				}
			});
		}
	}
}
