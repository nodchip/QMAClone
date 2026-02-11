package tv.dyndns.kishibe.qmaclone.server;

import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ServicesTest {
	@Test
	public void test() {
		Injector injector = Guice.createInjector(new QMACloneModule());
		injector.getInstance(IconUploadServletStub.class);
		injector.getInstance(ImageProxyServletStub.class);
		injector.getInstance(ServiceServletStub.class);
		injector.getInstance(RssServletStub.class);
	}
}
