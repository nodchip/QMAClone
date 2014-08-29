package tv.dyndns.kishibe.qmaclone.server.testing;

import tv.dyndns.kishibe.qmaclone.server.QMACloneModule;

import com.google.guiceberry.GuiceBerryModule;
import com.google.inject.AbstractModule;

public class QMACloneTestEnv extends AbstractModule {

	@Override
	protected void configure() {
		install(new GuiceBerryModule());
		install(new QMACloneModule());
	}

}
