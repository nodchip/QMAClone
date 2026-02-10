package tv.dyndns.kishibe.qmaclone.client.setting;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class SettingModule extends AbstractGinModule {

  @Override
  protected void configure() {
    install(
        new GinFactoryModuleBuilder()
            .implement(PanelSettingUserCodePresenter.View.class, PanelSettingUserCodeView.class)
            .build(PanelSettingUserCodePresenter.View.Factory.class));
    bind(PanelSettingUserCodePresenter.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  public ExternalAccountConnector provideExternalAccountConnector() {
    return new GoogleExternalAccountConnector();
  }
}
