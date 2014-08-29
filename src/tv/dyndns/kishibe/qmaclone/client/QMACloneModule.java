package tv.dyndns.kishibe.qmaclone.client;

import tv.dyndns.kishibe.qmaclone.client.ranking.RankingModule;
import tv.dyndns.kishibe.qmaclone.client.setting.SettingModule;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class QMACloneModule extends AbstractGinModule {

	@Override
	protected void configure() {
		install(new RankingModule());
		install(new SettingModule());
	}

	@Provides
	@Singleton
	private UserData provideUserData() {
		return UserData.get();
	}

}
