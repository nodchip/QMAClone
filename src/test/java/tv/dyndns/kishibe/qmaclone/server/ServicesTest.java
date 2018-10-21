package tv.dyndns.kishibe.qmaclone.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(JUnit4.class)
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
