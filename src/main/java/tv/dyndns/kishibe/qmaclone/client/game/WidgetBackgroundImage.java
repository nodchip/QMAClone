package tv.dyndns.kishibe.qmaclone.client.game;

import static tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem.SENTENCE_IMAGE_HEIGHT;
import static tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem.SENTENCE_IMAGE_WIDTH;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.util.ImageCache;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

public class WidgetBackgroundImage extends HorizontalPanel {
	public WidgetBackgroundImage(PacketProblem problem, int width, int height) {
		setPixelSize(width, height);
		Image image = new Image(
				(width == SENTENCE_IMAGE_WIDTH && height == SENTENCE_IMAGE_HEIGHT) ? problem
						.getSentenceImageUrl() : ImageCache.getUrl(problem.imageUrl, width, height));
		add(image);
	}
}
