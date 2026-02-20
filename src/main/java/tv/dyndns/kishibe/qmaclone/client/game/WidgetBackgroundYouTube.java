package tv.dyndns.kishibe.qmaclone.client.game;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class WidgetBackgroundYouTube extends HorizontalPanel {
  private static final String YOUTUBE_NOCOOKIE_EMBED_URL_PREFIX = "https://www.youtube-nocookie.com/embed/";
  private static final String YOUTUBE_EMBED_QUERY = "?rel=0&autoplay=1&disablekb=1";

  /**
   * YouTube埋め込みテンプレート。
   */
  interface YouTubeTemplate extends SafeHtmlTemplates {
    @Template("<iframe width='{0}' height='{1}' src='{2}' frameborder='0' allow='autoplay; encrypted-media' allowfullscreen></iframe>")
    SafeHtml create(int width, int height, SafeUri src);
  }

  public WidgetBackgroundYouTube(String url, int width, int height) {
    setPixelSize(width, height);

    String videoId = YouTubeEmbedUrlPolicy.extractValidVideoId(url);
    if (videoId == null) {
      return;
    }

    SafeUri movieUri = UriUtils.fromTrustedString(
        YOUTUBE_NOCOOKIE_EMBED_URL_PREFIX + videoId + YOUTUBE_EMBED_QUERY);
    YouTubeTemplate template = GWT.create(YouTubeTemplate.class);
    add(new HTML(template.create(width, height, movieUri)));
  }
}
