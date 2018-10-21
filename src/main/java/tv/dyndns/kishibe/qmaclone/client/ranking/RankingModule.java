package tv.dyndns.kishibe.qmaclone.client.ranking;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;

public class RankingModule extends AbstractGinModule {

	@Override
	protected void configure() {
		install(new GinFactoryModuleBuilder().build(CellTableRanking.Factory.class));
		install(new GinFactoryModuleBuilder().build(PanelRanking.Factory.class));

		bind(DateRangeSelectorPresenter.class).in(Singleton.class);
		bind(DateRangeSelectorPresenter.View.class).to(DateRangeSelectorViewImpl.class).in(
				Singleton.class);

		bind(GeneralRankingPresenter.class).in(Singleton.class);
		bind(GeneralRankingPresenter.View.class).to(GeneralRankingViewImpl.class).in(
				Singleton.class);

		bind(RankingPresenter.class).in(Singleton.class);
		bind(RankingPresenter.View.class).to(RankingViewImpl.class).in(Singleton.class);

		bind(ThemeRankingPresenter.class).in(Singleton.class);
		bind(ThemeRankingPresenter.View.class).to(ThemeRankingViewImpl.class).in(Singleton.class);

		bind(ThemeSelectorPresenter.class).in(Singleton.class);
		bind(ThemeSelectorPresenter.View.class).to(ThemeSelectorViewImpl.class).in(Singleton.class);
	}

}
