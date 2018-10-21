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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Servlet implementation class RssServletStub
 */
public class RssServletStub extends HttpServlet {
  private static final Logger logger = Logger.getLogger(RssServletStub.class.toString());
  private static final long serialVersionUID = -1885300264096126774L;
  private static final String[][] ARGUMENTS_MAPPING = { { "rss090", "rss_0.90" },
      { "rss091", "rss_0.91" }, { "rss092", "rss_0.92" }, { "rss093", "rss_0.93" },
      { "rss094", "rss_0.94" }, { "rss10", "rss_1.0" }, { "rss20", "rss_2.0" },
      { "atom03", "atom_0.3" }, };
  private static final String DEFAULT_RSS_VERSION = "rss_2.0";
  private final Database database;
  private final Map<String, String> argumentsMapping = createArgumentsMapping();
  private volatile List<PacketProblem> lastestProblems;
  private volatile List<SyndEntry> entries;
  private final Object lock = new Object();

  @Inject
  public RssServletStub(Database database) {
    this.database = database;
    try {
      lastestProblems = database.getLastestProblems();
      entries = createEntries();
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "RSSエントリの生成に失敗しました", e);
    }
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      processGet(request, response);
    } catch (Exception e) {
      logger.log(Level.WARNING, "RSSの精製に失敗しました", e);
      throw new ServletException(e);
    }

    try {
      request.getInputStream().close();
    } catch (Exception e) {
    }

    try {
      response.getOutputStream().close();
    } catch (Exception e) {
    }
  }

  private void processGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setCharacterEncoding("utf-8");

    synchronized (lock) {
      try {
        if (database.getLastestProblems() != lastestProblems) {
          lastestProblems = database.getLastestProblems();
          entries = createEntries();
        }
      } catch (DatabaseException e) {
        logger.log(Level.WARNING, "RSSエントリの生成に失敗しました", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
    }

    // RSSのバージョンを決める
    String rssVersion = DEFAULT_RSS_VERSION;
    String argument = request.getParameter("rssVersion");
    if (argument != null && argumentsMapping.containsKey(argument)) {
      rssVersion = argumentsMapping.get(argument);
    }

    SyndFeed feed = new SyndFeedImpl();
    feed.setFeedType(rssVersion);
    feed.setTitle("QMAClone 最新投稿問題");
    feed.setLink("http://kishibe.dyndns.tv/qmaclone/");
    feed.setDescription("QMACloneの最新投稿問題をお知らせいたします。1時間毎の更新です。");
    feed.setEntries(entries);
    feed.setEncoding("utf-8");

    SyndFeedOutput output = new SyndFeedOutput();

    StringWriter stringWriter = new StringWriter();
    try {
      output.output(feed, stringWriter);
    } catch (Exception e) {
      e.printStackTrace();
    }

    byte[] bs = stringWriter.toString().getBytes("utf-8");
    BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
    outputStream.write(bs);
    outputStream.close();
  }

  private List<SyndEntry> createEntries() throws DatabaseException {
    List<SyndEntry> entries = Lists.newArrayList();

    Date publishedData = new Date();

    for (PacketProblem problem : database.getLastestProblems()) {
      SyndEntry entry = new SyndEntryImpl();
      String problemReportSentence = problem.getProblemReportSentence();
      if (problemReportSentence.length() > 20) {
        problemReportSentence = problemReportSentence.substring(0, 20);
      }
      entry.setTitle("問題番号:" + problem.id + " " + problemReportSentence + "...");
      entry.setLink("http://kishibe.dyndns.tv/qmaclone/");
      entry.setPublishedDate(publishedData);
      SyndContent description = new SyndContentImpl();
      description.setType("text/plain");
      description.setValue(problemReportSentence);
      entry.setDescription(description);
      entry.setAuthor(problem.creator);
      entries.add(entry);
    }

    return entries;
  }

  private Map<String, String> createArgumentsMapping() {
    Map<String, String> result = Maps.newHashMap();
    for (String[] mapping : ARGUMENTS_MAPPING) {
      result.put(mapping[0], mapping[1]);
    }
    return result;
  }
}
