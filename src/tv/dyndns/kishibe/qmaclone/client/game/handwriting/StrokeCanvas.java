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
package tv.dyndns.kishibe.qmaclone.client.game.handwriting;

import java.util.ArrayList;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.ui.MouseEventsCanvas;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.widgetideas.graphics.client.Color;

public class StrokeCanvas extends MouseEventsCanvas implements MouseDownHandler, MouseOutHandler, MouseMoveHandler, MouseUpHandler {
	private static final double CANVAS_SIZE = 300.0;
	private static final double THRESHOLD = 5.0 / CANVAS_SIZE;
	private static final double STROKE_WIDTH = 2.0;
	private static final Color STROKE_COLOR = Color.BLACK;
	private List<List<double[]>> strokes = new ArrayList<List<double[]>>();
	private List<double[]> lastStroke = null;
	private final StrokeCanvasListener strokeCanvasListener;

	public StrokeCanvas(StrokeCanvasListener strokeCanvasListener) {
		super(300, 300);
		this.strokeCanvasListener = strokeCanvasListener;
		addStyleName("gwt-Canvas-stroke");
		addMouseDownHandler(this);
		addMouseOutHandler(this);
		addMouseMoveHandler(this);
		addMouseUpHandler(this);
	}

	public double[][][] getStrokes() {
		List<double[][]> strokesList = new ArrayList<double[][]>();
		for (List<double[]> stroke : strokes) {
			strokesList.add(stroke.toArray(new double[0][]));
		}
		return strokesList.toArray(new double[0][][]);
	}

	public void clear() {
		strokes.clear();
		lastStroke = null;
		updateCanvas();
	}

	private void updateCanvas() {
		final List<List<double[]>> canvasStrokes = new ArrayList<List<double[]>>(strokes);
		if (lastStroke != null && lastStroke.size() >= 2) {
			canvasStrokes.add(lastStroke);
		}

		super.clear();

		beginPath();
		setLineWidth(STROKE_WIDTH);
		setStrokeStyle(STROKE_COLOR);

		for (List<double[]> stroke : canvasStrokes) {
			beginPath();

			for (int i = 0; i < stroke.size(); ++i) {
				final double x = stroke.get(i)[0] * CANVAS_SIZE;
				final double y = stroke.get(i)[1] * CANVAS_SIZE;

				if (i == 0) {
					moveTo(x, y);
				} else {
					lineTo(x, y);
				}
			}

			stroke();
		}
	}

	private void addPointToLastStroke(int x, int y) {
		final double[] point = new double[] { x, y };
		point[0] /= CANVAS_SIZE;
		point[1] /= CANVAS_SIZE;
		final double[] lastPoint = lastStroke.size() == 0 ? point : lastStroke.get(lastStroke.size() - 1);
		final double dx = point[0] - lastPoint[0];
		final double dy = point[1] - lastPoint[1];
		if (lastStroke.size() == 0 || Math.sqrt(dx * dx + dy * dy) > THRESHOLD) {
			lastStroke.add(point);
		}
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		final int x = event.getX();
		final int y = event.getY();
		// System.out.println("onMouseDown(" + x + "," + y + ")");

		if (lastStroke != null) {
			System.out.println("error : 前のストローク情報が終わらないまま次のストロークが始まった");
		}
		lastStroke = new ArrayList<double[]>();
		addPointToLastStroke(x, y);

		// updateCanvas();
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		// System.out.println("onMouseLeave()");

		if (lastStroke != null) {
			if (lastStroke.size() >= 2) {
				strokes.add(lastStroke);
				strokeCanvasListener.onStrokeFinished();
			}
			lastStroke = null;

			updateCanvas();
		}
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		final int x = event.getX();
		final int y = event.getY();
		// System.out.println("onMouseMove(" + x + "," + y + ")");

		if (lastStroke != null) {
			addPointToLastStroke(x, y);

			updateCanvas();
		}
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		final int x = event.getX();
		final int y = event.getY();
		// System.out.println("onMouseUp(" + x + "," + y + ")");

		if (lastStroke == null) {
			return;
		}

		addPointToLastStroke(x, y);
		if (lastStroke.size() >= 2) {
			strokes.add(lastStroke);
			strokeCanvasListener.onStrokeFinished();
		}

		lastStroke = null;

		updateCanvas();
	}
}
