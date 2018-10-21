package tv.dyndns.kishibe.qmaclone.client.setting;

import com.google.api.gwt.client.GoogleApiRequestTransport;
import com.google.api.gwt.client.OAuth2Login;
import com.google.api.gwt.services.plus.shared.Plus;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class SettingModule extends AbstractGinModule {

	private static final String API_KEY = "AvW1Q0GS2481K0-epCGzWXS9";
	private static final String APPLICATION_NAME = "QMAClone";

	@Override
	protected void configure() {
		install(new GinFactoryModuleBuilder().implement(PanelSettingUserCodePresenter.View.class,
				PanelSettingUserCodeView.class).build(
				PanelSettingUserCodePresenter.View.Factory.class));
		bind(PanelSettingUserCodePresenter.class).in(Singleton.class);
	}

	@Provides
	@Singleton
	public OAuth2Login provideOAuth2Login() {
		return OAuth2Login.get();
	}

	@Provides
	@Singleton
	public Plus providePlus() {
		Plus plus = GWT.create(Plus.class);
		plus.initialize(new SimpleEventBus(), new GoogleApiRequestTransport(APPLICATION_NAME,
				API_KEY));
		return plus;
	}

}
