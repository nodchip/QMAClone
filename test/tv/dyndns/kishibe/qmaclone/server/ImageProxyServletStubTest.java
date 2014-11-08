package tv.dyndns.kishibe.qmaclone.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.server.image.ImageUtils;
import tv.dyndns.kishibe.qmaclone.server.image.ImageUtils.Parameter;

@RunWith(JUnit4.class)
public class ImageProxyServletStubTest extends EasyMockSupport {
	private ImageProxyServletStub service;
	private HttpServletRequest mockRequest;
	private ImageUtils mockImageManager;

	@Before
	public void setUp() {
		try {
			FileUtils.deleteDirectory(new File("/tmp/qmaclone/image"));
		} catch (IOException e) {
		}
		mockImageManager = createMock(ImageUtils.class);
		service = new ImageProxyServletStub(mockImageManager);
		mockRequest = createMock(HttpServletRequest.class);
	}

	@Test
	public void parseParameterShouldWork() throws ServletException {
		EasyMock.expect(mockRequest.getRequestURI()).andStubReturn(
				"/image/url/006100620063/width/512/height/384");

		replayAll();

		Parameter parameter = service.parseParameter(mockRequest);

		verifyAll();

		assertEquals("abc", parameter.url);
		assertEquals(512, parameter.width);
		assertEquals(384, parameter.height);
	}

	@Test
	public void parseParameterShouldWorkWithRoot() throws ServletException {
		EasyMock.expect(mockRequest.getRequestURI()).andStubReturn(
				"/QMAClone/image/url/006100620063/width/512/height/384");

		replayAll();

		Parameter parameter = service.parseParameter(mockRequest);

		verifyAll();

		assertEquals("abc", parameter.url);
		assertEquals(512, parameter.width);
		assertEquals(384, parameter.height);
	}

	@Test
	public void testGetRFC1123NextYear() throws DateParseException {
		Date date = DateUtils.parseDate(service.getRFC1123NextYear());
		assertTrue(date.getTime() < System.currentTimeMillis() + 366L * 24 * 60 * 60 * 1000);
	}
}
