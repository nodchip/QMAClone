package tv.dyndns.kishibe.qmaclone.client;

import tv.dyndns.kishibe.qmaclone.client.ranking.RankingPresenter;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSetting;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules(QMACloneModule.class)
public interface QMACloneGinjector extends Ginjector {
	RankingPresenter.View getRankingView();

	PanelSetting getSettingView();
}
