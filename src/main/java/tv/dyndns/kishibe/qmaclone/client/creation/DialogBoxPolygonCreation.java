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
package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.ArrayList;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.geom.Point;
import tv.dyndns.kishibe.qmaclone.client.geom.Polygon;
import tv.dyndns.kishibe.qmaclone.client.geom.PolygonException;
import tv.dyndns.kishibe.qmaclone.client.geom.Segment;
import tv.dyndns.kishibe.qmaclone.client.ui.MouseEventsCanvas;
import tv.dyndns.kishibe.qmaclone.client.util.ImageCache;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.widgetideas.graphics.client.ImageLoader;

public class DialogBoxPolygonCreation extends DialogBox implements ClickHandler {
  private static final String DESCRIPTION = "画像クリック問題の正解範囲をマウスクリックで指定していきます<br>画像中の正解範囲の外枠をマウスで順にクリックしてください<br>";
  private final Button buttonOk = new Button("OK", this);
  private final Button buttonCancel = new Button("キャンセル", this);
  private final Button buttonUndo = new Button("頂点を一つ消す", this);
  private final Button buttonClear = new Button("頂点をすべて消す", this);
  private Polygon polygon; // 実際に入力されたポリゴンデータ
  private boolean mouseIn = false;
  private boolean illegalIfAddPoint = false;
  private int mouseX = 0;
  private int mouseY = 0;
  private static final double LINE_WIDTH = 3.0;
  private static final Color COLOR_OK = Color.BLUE;
  private static final Color COLOR_NG = Color.RED;
  private final List<DialogBoxPolygonCreationListener> listeners = new ArrayList<DialogBoxPolygonCreationListener>();
  private final MouseEventsCanvas canvas = new MouseEventsCanvas(Constant.CLICK_IMAGE_WIDTH,
      Constant.CLICK_IMAGE_HEIGHT);
  private ImageElement imageElement = null;

  public DialogBoxPolygonCreation(String imageUrl, String polygonDescription) {
    super(false, true);
    setAnimationEnabled(true);
    setGlassEnabled(true);

    ImageLoader.loadImages(new String[] { ImageCache.getUrl(imageUrl, Constant.CLICK_IMAGE_WIDTH,
        Constant.CLICK_IMAGE_HEIGHT) }, callbackLoadImages);
    canvas.addMouseMoveHandler(imageMouseMoveHandler);
    canvas.addMouseOutHandler(imageMouseOutHandler);
    canvas.addMouseOverHandler(imageMouseOverHandler);
    canvas.addMouseDownHandler(imageMouseDownHandler);

    try {
      this.polygon = Polygon.fromString(polygonDescription);
    } catch (PolygonException e) {
      this.polygon = new Polygon();
    }

    final VerticalPanel rootPanel = new VerticalPanel();
    setWidget(rootPanel);

    rootPanel.add(canvas);

    {
      final HorizontalPanel panel = new HorizontalPanel();
      panel.add(buttonOk);
      panel.add(buttonCancel);
      panel.add(buttonUndo);
      panel.add(buttonClear);
      rootPanel.add(panel);
    }

    rootPanel.add(new HTML(DESCRIPTION));

    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        updateCanvas();
      }
    });
  }

  private final ImageLoader.CallBack callbackLoadImages = new ImageLoader.CallBack() {
    public void onImagesLoaded(ImageElement[] imageElements) {
      imageElement = imageElements[0];
    }
  };

  private void ok() {
    hide();

    for (DialogBoxPolygonCreationListener listener : listeners) {
      listener.onOk();
    }
  }

  private void cancel() {
    polygon.clear();
    hide();

    for (DialogBoxPolygonCreationListener listener : listeners) {
      listener.onCancel();
    }
  }

  private void undoPolygon() {
    polygon.remove(polygon.size() - 1);
    update();
  }

  private void clearPolygon() {
    polygon.clear();
    update();
  }

  private void update() {
    updateCanvas();
    updateWidgets();
  }

  private void updateCanvas() {
    final boolean ok = mouseIn && illegalIfAddPoint || !mouseIn && checkPolygonCompleted();
    final Color color = ok ? COLOR_OK : COLOR_NG;

    canvas.clear();

    if (imageElement != null) {
      canvas.drawImage(imageElement, 0, 0, Constant.CLICK_IMAGE_WIDTH, Constant.CLICK_IMAGE_HEIGHT);
    }

    canvas.setLineWidth(LINE_WIDTH);
    canvas.setStrokeStyle(color);
    canvas.beginPath();
    final int n = polygon.size();
    if (n > 0) {
      canvas.moveTo(polygon.get(0).x, polygon.get(0).y);

      for (int i = 1; i < n; ++i) {
        final Point point = polygon.get(i);
        canvas.lineTo(point.x, point.y);
      }

      if (mouseIn) {
        canvas.lineTo(mouseX, mouseY);
      }

      canvas.lineTo(polygon.get(0).x, polygon.get(0).y);
    }

    canvas.stroke();
  }

  private void updateWidgets() {
    buttonOk.setEnabled(checkPolygonCompleted());
    buttonCancel.setEnabled(true);
    buttonUndo.setEnabled(checkPolygonUndoable());
    buttonClear.setEnabled(checkPolygonClearable());
  }

  private void mouseMove(int x, int y) {
    // System.out.println("mouseMove (" + x + "," + y + ")");
    mouseIn = true;
    mouseX = x;
    mouseY = y;
    illegalIfAddPoint = !checkPointCrossingWithMousePointer();
    update();
  }

  private void mouseLeave() {
    // System.out.println("mouseLeave");
    mouseIn = false;
    update();
  }

  private void mouseEnter() {
    // System.out.println("mouseEnter");
    mouseIn = true;
    update();
  }

  private void mouseDown(int x, int y) {
    // System.out.println("mouseDown (" + x + ", " + y + ")");

    // if (!checkPointAddable()) {
    // return;
    // }

    if (polygon.size() < Constant.MAX_NUMBER_OF_POLYGON_VERTICES) {
      polygon.add(new Point(x, y));
    }
    update();
  }

  private boolean checkPolygonCompleted() {
    return polygon.isCompleted();
  }

  private boolean checkPolygonUndoable() {
    return polygon.size() >= 1;
  }

  private boolean checkPolygonClearable() {
    return polygon.size() >= 1;
  }

  private boolean checkPointCrossingWithMousePointer() {
    final Point currentPoint = new Point(mouseX, mouseY);
    final int n = polygon.size();

    // 点が線上にないか？
    {
      for (int i = 0; i < n - 1; ++i) {
        final Segment segment = new Segment(polygon.get(i), polygon.get(i + 1));
        if (segment.on(currentPoint)) {
          return true;
        }
      }
    }

    // 線分が交差していないか？
    if (!polygon.isEmpty()) {
      final Segment segmentToBegin = new Segment(currentPoint, polygon.get(0));
      final Segment segmentToEnd = new Segment(currentPoint, polygon.get(n - 1));

      for (int i = 1; i < n - 1; ++i) {
        final Segment currentSegment = new Segment(polygon.get(i), polygon.get(i + 1));
        if (i != 0 && currentSegment.cross(segmentToBegin)) {
          return true;
        }
      }

      for (int i = 0; i < n - 2; ++i) {
        final Segment currentSegment = new Segment(polygon.get(i), polygon.get(i + 1));
        if (i != n - 2 && currentSegment.cross(segmentToEnd)) {
          return true;
        }
      }
    }

    return false;
  }

  private final MouseMoveHandler imageMouseMoveHandler = new MouseMoveHandler() {
    @Override
    public void onMouseMove(MouseMoveEvent event) {
      mouseMove(event.getX(), event.getY());
    }
  };
  private final MouseOutHandler imageMouseOutHandler = new MouseOutHandler() {
    @Override
    public void onMouseOut(MouseOutEvent event) {
      mouseLeave();
    }
  };
  private final MouseOverHandler imageMouseOverHandler = new MouseOverHandler() {
    @Override
    public void onMouseOver(MouseOverEvent event) {
      mouseEnter();
    }
  };
  private final MouseDownHandler imageMouseDownHandler = new MouseDownHandler() {
    @Override
    public void onMouseDown(MouseDownEvent event) {
      mouseDown(event.getX(), event.getY());
    }
  };

  public String getPolygonDescription() {
    return polygon.toString();
  }

  public void addDialogBoxPolygonCreationListener(DialogBoxPolygonCreationListener listener) {
    listeners.add(listener);
  }

  public void removeDialogBoxAreaCreationListener(DialogBoxPolygonCreationListener listener) {
    listeners.remove(listener);
  }

  private final ResizeHandler resizeHandler = new ResizeHandler() {
    @Override
    public void onResize(ResizeEvent event) {
      update();
    }
  };

  protected void onLoad() {
    super.onLoad();
    Window.addResizeHandler(resizeHandler);
  }

  protected void onUnload() {
    Window.addResizeHandler(resizeHandler);
    super.onUnload();
  }

  @Override
  public void onClick(ClickEvent event) {
    if (event.getSource() == buttonOk) {
      ok();
    } else if (event.getSource() == buttonCancel) {
      cancel();
    } else if (event.getSource() == buttonUndo) {
      undoPolygon();
    } else if (event.getSource() == buttonClear) {
      clearPolygon();
    }
  }
}
