package tv.dyndns.kishibe.qmaclone.client.game.input;

import tv.dyndns.kishibe.qmaclone.client.geom.Point;

/**
 * 画像クリック座標の表示サイズと元サイズの相互変換を提供する。
 */
public final class ClickCoordinateScaler {
	private ClickCoordinateScaler() {
	}

	public static Point toOriginal(Point displayedPoint, int displayedWidth, int displayedHeight,
			int originalWidth, int originalHeight) {
		if (displayedPoint == null || displayedWidth <= 0 || displayedHeight <= 0
				|| originalWidth <= 0 || originalHeight <= 0) {
			return null;
		}

		double scaleX = (double) originalWidth / (double) displayedWidth;
		double scaleY = (double) originalHeight / (double) displayedHeight;
		int x = (int) Math.round(displayedPoint.x * scaleX);
		int y = (int) Math.round(displayedPoint.y * scaleY);
		x = Math.max(0, Math.min(originalWidth - 1, x));
		y = Math.max(0, Math.min(originalHeight - 1, y));
		return new Point(x, y);
	}

	/**
	 * 元画像座標を表示座標へ変換する。
	 */
	public static Point toDisplayed(Point originalPoint, int originalWidth, int originalHeight,
			int displayedWidth, int displayedHeight) {
		if (originalPoint == null || displayedWidth <= 0 || displayedHeight <= 0 || originalWidth <= 0
				|| originalHeight <= 0) {
			return null;
		}

		double scaleX = (double) displayedWidth / (double) originalWidth;
		double scaleY = (double) displayedHeight / (double) originalHeight;
		int x = (int) Math.round(originalPoint.x * scaleX);
		int y = (int) Math.round(originalPoint.y * scaleY);
		x = Math.max(0, Math.min(displayedWidth - 1, x));
		y = Math.max(0, Math.min(displayedHeight - 1, y));
		return new Point(x, y);
	}
}
