package tv.dyndns.kishibe.qmaclone.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.impl.cookie.DateUtils;

import tv.dyndns.kishibe.qmaclone.server.image.ImageUtils;
import tv.dyndns.kishibe.qmaclone.server.image.ImageUtils.Parameter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class ImageProxyServletStub extends HttpServlet {

  private static final Logger logger = Logger.getLogger(ImageProxyServletStub.class.toString());
  private static final int MAX_WIDTH = 512;
  private static final int MAX_HEIGHT = 384;
  private static String HEX = "0123456789abcdef";
  private final ImageUtils imageUtils;

  @Inject
  public ImageProxyServletStub(ImageUtils imageUtils) {
    this.imageUtils = imageUtils;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    try (InputStream inputStream = req.getInputStream();
        OutputStream outputStream = resp.getOutputStream()) {
      processGet(req, resp);
    }
  }

  /**
   * doGet()の内容を処理する
   * 
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  private void processGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Parameter parameter = parseParameter(req);
    resp.setContentType("image/jpeg");
    resp.setHeader("Cache-Control", "max-age=31536000");
    resp.setHeader("Expires", getRFC1123NextYear());
    imageUtils.writeToStream(parameter, resp.getOutputStream());
  }

  @VisibleForTesting
  Parameter parseParameter(HttpServletRequest req) throws ServletException {
    Map<String, String> parameters = Maps.newHashMap();
    String requestURI = req.getRequestURI();
    List<String> segments = Lists.newArrayList(Splitter.on('/').omitEmptyStrings()
        .split(requestURI));
    while (!segments.get(0).equals("image")) {
      segments.remove(0);
    }
    segments.remove(0);

    for (int i = 0; i < segments.size() / 2; ++i) {
      parameters.put(segments.get(i * 2), segments.get(i * 2 + 1));
    }

    String url = parameters.get("url");
    String widthString = parameters.get("width");
    String heightString = parameters.get("height");
    String keepAspectRatioString = parameters.get("keepAspectRatio");

    if (url == null || heightString == null || widthString == null) {
      throw new ServletException("パラメーターが指定されていません");
    }

    try {
      url = decode(url);
    } catch (RuntimeException e) {
      throw new ServletException("URL部のデコードに失敗しました", e);
    }

    int width = 0;
    try {
      width = Integer.parseInt(widthString);
    } catch (Exception e) {
      throw new ServletException("幅パラメーターがintとして解釈できません", e);
    }

    if (width > MAX_WIDTH) {
      throw new ServletException("幅パラメーターの値が大きすぎます " + width);
    }

    if (width <= 0) {
      throw new ServletException("幅パラメーターの値が小さすぎます " + width);
    }

    int height = 0;
    try {
      height = Integer.parseInt(heightString);
    } catch (Exception e) {
      throw new ServletException("高さパラメーターがintとして解釈できません", e);
    }

    if (height > MAX_HEIGHT) {
      throw new ServletException("高さパラメーターの値が大きすぎます " + height);
    }

    if (height <= 0) {
      throw new ServletException("高さパラメーターの値が小さすぎます " + height);
    }

    boolean keepAspectRatio = false;
    try {
      keepAspectRatio = Boolean.parseBoolean(keepAspectRatioString);
    } catch (Exception e) {
      throw new ServletException("アクペクト比パラメーターがbooleanとして解釈できません", e);
    }

    return new Parameter(url, width, height, keepAspectRatio);
  }

  private String decode(String s) {
    StringBuilder sb = new StringBuilder();
    int index = 0;
    while (index < s.length()) {
      int ch = 0;
      for (int i = 0; i < 4; ++i) {
        ch <<= 4;
        ch |= HEX.indexOf(s.charAt(index++));
      }
      sb.append((char) ch);
    }
    return sb.toString();
  }

  @Override
  protected long getLastModified(HttpServletRequest req) {
    long result = 0;
    try {
      result = imageUtils.getLastModified(parseParameter(req));
    } catch (ServletException e) {
      logger.log(Level.WARNING, "LastModifiedの取得に失敗しました", e);
    }

    if (result == 0) {
      result = super.getLastModified(req);
    }

    return result;
  }

  /**
   * 一年後の日時をRFC1123形式で返す
   * 
   * @return
   */
  String getRFC1123NextYear() {
    final Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.YEAR, 1);
    return DateUtils.formatDate(calendar.getTime());
  }
}
