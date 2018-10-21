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

import java.util.ArrayList;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.ui.PopupCanvas;

import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

public class MarkedCanvas implements HasMouseDownHandlers {
	public static final int REGISTER = Integer.MAX_VALUE;
	private final PopupCanvas canvas;
	private List<Mark> marks = new ArrayList<Mark>();

	public MarkedCanvas(Widget base, int width, int height) {
		canvas = new PopupCanvas(base, width, height);
	}

	public void show() {
		canvas.show();
	}

	public void hide() {
		canvas.hide();
	}

	public void update() {
		canvas.prepare();

		for (Mark mark : marks) {
			if (mark == null) {
				continue;
			}

			mark.paint();
		}
	}

	public int getPopupLeft() {
		return canvas.getPopupLeft();
	}

	public int getPopupTop() {
		return canvas.getPopupTop();
	}

	public int addSegmentMark(int startX, int startY, int endX, int endY, int id) {
		Mark mark = new MarkSegment(canvas, startX, startY, endX, endY);
		return add(id, mark);
	}

	public int addPointerMark(int x, int y, int id) {
		Mark mark = new MarkPointer(canvas, x, y);
		return add(id, mark);
	}

	public int addYesMark(int x, int y, int id) {
		Mark mark = new MarkYes(canvas, x, y);
		return add(id, mark);
	}

	public int addNoMark(int x, int y, int id) {
		Mark mark = new MarkNo(canvas, x, y);
		return add(id, mark);
	}

	public void removeMark(int id) {
		if (id < 0 || marks.size() <= id) {
			return;
		}

		marks.set(id, null);
	}

	private int add(int id, final Mark mark) {
		if (id == REGISTER) {
			id = marks.size();
			marks.add(mark);
		} else {
			marks.set(id, mark);
		}

		return id;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		canvas.fireEvent(event);
	}

	@Override
	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		return canvas.addMouseDownHandler(handler);
	}
}
