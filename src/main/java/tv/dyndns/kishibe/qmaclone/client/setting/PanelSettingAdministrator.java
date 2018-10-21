package tv.dyndns.kishibe.qmaclone.client.setting;

import tv.dyndns.kishibe.qmaclone.client.Service;

import com.google.gwt.user.client.ui.TabPanel;

public class PanelSettingAdministrator extends TabPanel {

	public PanelSettingAdministrator() {
		setAnimationEnabled(true);

		// テーマモード編集権限
		add(new PanelSettingThemeModeEditor(), "テーマモード編集権限");

		// 制限ユーザー
		PanelSettingRestrictedUser presenter = new PanelSettingRestrictedUser(
				Service.Util.getInstance());
		presenter.setView(new PanelSettingRestrictedUserView(presenter));
		add(presenter, "制限ユーザー");

		selectTab(0);
	}

}
