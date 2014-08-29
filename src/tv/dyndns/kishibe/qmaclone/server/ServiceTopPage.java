//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.ByteStreams;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class ServiceTopPage extends HttpServlet {

	private static final Logger logger = Logger.getLogger(ServiceTopPage.class.getName());
	private static final String PREFIX = "<html>\n<head>\n<title>QMAClone by nodchip (ノドチップ)</title>\n<meta http-equiv='content-type' content='text/html; charset=UTF-8'>\n<link rel='stylesheet' type='text/css' href='QMAClone.css'>\n<link rel='alternate' type='application/rss+xml'\n\ttitle='QMAClone RSSフィード' href='rss.xml' />\n<link rel='alternate' type='application/atom+xml'\n\ttitle='QMAClone Atomフィード' href='rss.xml?rssVersion=atom03' />\n<meta name='google-site-verification'\n\tcontent='mDQyFPpWAu6I_S35Y2Ud7ptxAZtpKGldvi8IN7f3ibY' />\n<script type='text/javascript'>\n\twindow.___gcfg = {\n\t\tlang : 'ja'\n\t};\n</script>\n<script type='text/javascript'>\n\tdocument.onkeydown = function(e) {\n\t\t// Backspaceキー\n\t\tif (e && e.keyCode == 8) {\n\t\t\telement = e.target || e.srcElement;\n\t\t\tif (element.tagName && element.tagName.toLowerCase() != 'input'\n\t\t\t\t\t&& element.tagName.toLowerCase() != 'textarea') {\n\t\t\t\t// Google Chrome & FireFox\n\t\t\t\te.preventDefault();\n\t\t\t\te.stopPropagation();\n\t\t\t\treturn false;\n\t\t\t}\n\t\t} else if (event && event.keyCode == 8 && event.srcElement.type == null) {\n\t\t\t// IE\n\t\t\tevent.returnValue = false;\n\t\t\tevent.cancelBubble = true;\n\t\t\treturn false;\n\t\t}\n\t}\n</script>\n</head>\n\n<body>\n\t<iframe src='javascript:''' id='__gwt_historyFrame'\n\t\tstyle='width: 0; height: 0; border: 0'></iframe>\n\n\t<script type='text/javascript'\n\t\tsrc='tv.dyndns.kishibe.qmaclone.QMAClone/tv.dyndns.kishibe.qmaclone.QMAClone.nocache.js'></script>\n\n\t<h1 id='title' align='right'>QMAClone by nodchip(ノドチップ)</h1>\n\n\t<div id='slot' style='width: 100%; height: 100%;'></div>\n\t<div id='sound' style='width: 0px; height: 0px'></div>\n\t<div id='position_selecter'></div>\n\t<div id='click_canvas'></div>\n\n\t<div style='visibility: hidden'></div>\n\n\t<script type='text/javascript'>\n\t\tvar gaJsHost = (('https:' == document.location.protocol) ? 'https://ssl.'\n\t\t\t\t: 'http://www.');\n\t\tdocument\n\t\t\t\t.write(unescape('%3Cscript src=''\n\t\t\t\t\t\t+ gaJsHost\n\t\t\t\t\t\t+ 'google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E'));\n\t</script>\n\t<script type='text/javascript'>\n\t\ttry {\n\t\t\tvar pageTracker = _gat._getTracker('UA-12390713-3');\n\t\t\tpageTracker._trackPageview();\n\t\t} catch (err) {\n\t\t}\n\t</script>\n\n<div style='visibility: hidden'>\n";
	private static final String SURFIX = "</div>\n\n</body>\n</html>\n";
	private static final Object STATIC_KEY = new Object();
	private final Database database;
	private final LoadingCache<Object, byte[]> content = CacheBuilder.newBuilder()
			.concurrencyLevel(1).expireAfterWrite(1, TimeUnit.DAYS)
			.build(new CacheLoader<Object, byte[]>() {
				@Override
				public byte[] load(Object arg0) throws Exception {
					return createHtml();
				}
			});
	private long lastLastModified;

	@Inject
	public ServiceTopPage(Database database) {
		this.database = Preconditions.checkNotNull(database);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html;charset=utf-8");
		resp.setHeader("Cache-Control", "max-age=86400");
		resp.setHeader("Expires", getRFC1123NextDay());
		try (InputStream inputStream = req.getInputStream();
				OutputStream outputStream = resp.getOutputStream()) {
			try {
				ByteStreams.skipFully(inputStream, Long.MAX_VALUE);
			} catch (EOFException e) {
				// Do nothing.
			}
			outputStream.write(content.get(STATIC_KEY));

		} catch (ExecutionException e) {
			logger.log(Level.SEVERE, "トップページHTMLの作成に失敗しました", e);
			throw new IOException(e);
		}
	}

	private byte[] createHtml() throws DatabaseException, UnsupportedEncodingException {
		lastLastModified = System.currentTimeMillis();

		StringBuilder b = new StringBuilder();
		b.append(PREFIX);

		StringBuilder sb = new StringBuilder("MMO");

		List<PacketProblem> adsenseProblems = database.getAdsenseProblems(sb.toString());
		for (PacketProblem problem : adsenseProblems) {
			String sentence = problem.sentence;
			String note = problem.note;
			String[] choices = problem.choices;
			String[] answers = problem.answers;

			b.append("<div>\n");

			b.append("\t<div>").append(SafeHtmlUtils.htmlEscape(sentence)).append("</div>\n");

			b.append("\t<div>");
			for (String choice : choices) {
				if (choice != null && !choice.isEmpty()) {
					b.append("<span>").append(SafeHtmlUtils.htmlEscape(choice)).append("</span>");
				}
			}
			for (String answer : answers) {
				if (answer != null && !answer.isEmpty()) {
					b.append("<span>").append(SafeHtmlUtils.htmlEscape(answer)).append("</span>");
				}
			}
			b.append("</div>\n");

			if (note != null && !note.isEmpty()) {
				b.append("\t<div>").append(SafeHtmlUtils.htmlEscape(note)).append("</div>\n");
			}

			b.append("</div>\n");
		}

		b.append(SURFIX);

		return b.toString().getBytes("utf-8");
	}

	@Override
	protected long getLastModified(HttpServletRequest req) {
		return lastLastModified;
	}

	public String getRFC1123NextDay() {
		SimpleDateFormat rfc1123DateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz",
				java.util.Locale.US);
		rfc1123DateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date current = calendar.getTime();
		return rfc1123DateFormat.format(current);
	}

}
