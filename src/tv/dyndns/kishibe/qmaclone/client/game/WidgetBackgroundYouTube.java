package tv.dyndns.kishibe.qmaclone.client.game;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class WidgetBackgroundYouTube extends HorizontalPanel {
	private static String splitId(String url) {
		int index = url.indexOf('?');
		if (index == -1) {
			index = url.lastIndexOf('/');
			String id = url.substring(index + 1);
			if (id.contains("&")) {
				id = id.substring(0, id.indexOf("&"));
			}
			return id;

		} else {
			for (String pair : url.substring(index + 1).split("&")) {
				String[] p = pair.split("=");
				if (p[0].equals("v")) {
					return p[1];
				}
			}
		}

		return null;
	}

	public WidgetBackgroundYouTube(String url, int width, int height) {
		// setStyleName("gwt-HorizontalPanel-externalContaints");
		// setWidth("600px");
		// setHorizontalAlignment(ALIGN_RIGHT);
		setPixelSize(width, height);

		String html = "<object width='__width__' height='__height__'><param name='movie' value='http://www.youtube.com/v/__id__&rel=0&autoplay=1&disablekb=1'></param><embed src='http://www.youtube.com/v/__id__&rel=0&autoplay=1&disablekb=1' type='application/x-shockwave-flash' width='__width__' height='__height__'></embed></object>";
		html = html.replaceAll("__id__", splitId(url));
		html = html.replaceAll("__width__", "" + width);
		html = html.replaceAll("__height__", "" + height);
		add(new HTML(html));
	}
}
