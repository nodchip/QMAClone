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
package tv.dyndns.kishibe.qmaclone.client.game.click;

import tv.dyndns.kishibe.qmaclone.client.ui.PopupCanvas;

import com.google.gwt.widgetideas.graphics.client.Color;

public class MarkNo extends Mark {
	private static final double WIDTH = 3.0;
	private static final Color COLOR = Color.RED;
	private static final int RADIUS = 8;
	private final int x;
	private final int y;

	public MarkNo(PopupCanvas canvas, int x, int y) {
		super(canvas);
		this.x = x;
		this.y = y;
	}

	public void paint() {
		PopupCanvas canvas = getCanvas();
		canvas.saveContext();
		canvas.setLineWidth(WIDTH);
		canvas.setStrokeStyle(COLOR);

		canvas.beginPath();
		canvas.moveTo(x - RADIUS, y - RADIUS);
		canvas.lineTo(x + RADIUS, y + RADIUS);
		canvas.stroke();

		canvas.beginPath();
		canvas.moveTo(x - RADIUS, y + RADIUS);
		canvas.lineTo(x + RADIUS, y - RADIUS);
		canvas.stroke();

		canvas.restoreContext();
	}
}
