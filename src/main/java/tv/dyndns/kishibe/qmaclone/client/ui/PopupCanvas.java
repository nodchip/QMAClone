package tv.dyndns.kishibe.qmaclone.client.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseWheelHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.graphics.client.CanvasGradient;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;

/**
 * {@link PopupPanel} を用いて全面に表示される {@link GWTCanvas} 。 {@link Widget} の左上の座標を基準座標として表示する。
 * 
 * @author nodchip
 */
public class PopupCanvas implements HasClickHandlers, HasAllMouseHandlers, HasMouseWheelHandlers {
	private final Map<EventHandler, DomEvent.Type<EventHandler>> eventHandlers = new HashMap<EventHandler, DomEvent.Type<EventHandler>>();
	private final Widget offset;
	private int width;
	private int height;
	private final PopupPanel popupPanel;
	private GWTCanvas canvas;
	private HandlerRegistration resizeHandlerRegistration;
	private HandlerRegistration scrollHandlerRegistration;

	public PopupCanvas(Widget offset, int width, int height) {
		this.offset = offset;
		this.width = width;
		this.height = height;

		popupPanel = new PopupPanel(false, false);
		popupPanel.setStyleName("popup-canvas-background");
		prepare();
	}

	public void prepare() {
		canvas = new GWTCanvas(width, height);
		popupPanel.setWidget(canvas);
		for (Entry<EventHandler, DomEvent.Type<EventHandler>> entry : eventHandlers.entrySet()) {
			canvas.addDomHandler(entry.getKey(), entry.getValue());
		}
	}

	// 以下PopupPanelより
	/**
	 * 画面上に表示する。 描画メソッドはこのメソッドを呼んだ後でないと効果がない。
	 */
	public void show() {
		updatePopupPosition();
		popupPanel.show();
		resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				updatePopupPosition();
			}
		});
		scrollHandlerRegistration = Window.addWindowScrollHandler(new Window.ScrollHandler() {
			public void onWindowScroll(com.google.gwt.user.client.Window.ScrollEvent event) {
				updatePopupPosition();
			}
		});
	}

	private void updatePopupPosition() {
		popupPanel.setPopupPosition(offset.getAbsoluteLeft(), offset.getAbsoluteTop());
	}

	public void hide() {
		if (scrollHandlerRegistration != null) {
			scrollHandlerRegistration.removeHandler();
			scrollHandlerRegistration = null;
		}

		if (resizeHandlerRegistration != null) {
			resizeHandlerRegistration.removeHandler();
			resizeHandlerRegistration = null;
		}

		popupPanel.hide();
	}

	public int getPopupLeft() {
		return popupPanel.getPopupLeft();
	}

	public int getPopupTop() {
		return popupPanel.getPopupTop();
	}

	public void setPopupPosition(int left, int top) {
		popupPanel.setPopupPosition(left, top);
	}

	// 以下GWTCanvasより
	public void arc(double x, double y, double radius, double startAngle, double endAngle,
			boolean antiClockwise) {
		canvas.arc(x, y, radius, startAngle, endAngle, antiClockwise);
	}

	public void beginPath() {
		canvas.beginPath();
	}

	public void clear() {
		canvas.clear();
	}

	public void closePath() {
		canvas.closePath();
	}

	public CanvasGradient createLinearGradient(double x0, double y0, double x1, double y1) {
		return canvas.createLinearGradient(x0, y0, x1, y1);
	}

	public CanvasGradient createRadialGradient(double x0, double y0, double r0, double x1,
			double y1, double r1) {
		return canvas.createRadialGradient(x0, y0, r0, x1, y1, r1);
	}

	public void cubicCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
		canvas.cubicCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
	}

	public void drawImage(ImageElement img, double offsetX, double offsetY) {
		canvas.drawImage(img, offsetX, offsetY);
	}

	public void drawImage(ImageElement img, double offsetX, double offsetY, double width,
			double height) {
		canvas.drawImage(img, offsetX, offsetY, width, height);
	}

	public void drawImage(ImageElement img, double sourceX, double sourceY, double sourceWidth,
			double sourceHeight, double destX, double destY, double destWidth, double destHeight) {
		canvas.drawImage(img, sourceX, sourceY, sourceWidth, sourceHeight, destX, destY, destWidth,
				destHeight);
	}

	public void fill() {
		canvas.fill();
	}

	public void fillRect(double startX, double startY, double width, double height) {
		canvas.fillRect(startX, startY, width, height);
	}

	public int getCoordHeight() {
		return canvas.getCoordHeight();
	}

	public int getCoordWidth() {
		return canvas.getCoordWidth();
	}

	public double getGlobalAlpha() {
		return canvas.getGlobalAlpha();
	}

	public String getGlobalCompositeOperation() {
		return canvas.getGlobalCompositeOperation();
	}

	public String getLineCap() {
		return canvas.getLineCap();
	}

	public String getLineJoin() {
		return canvas.getLineJoin();
	}

	public double getLineWidth() {
		return canvas.getLineWidth();
	}

	public double getMiterLimit() {
		return canvas.getMiterLimit();
	}

	public void lineTo(double x, double y) {
		canvas.lineTo(x, y);
	}

	public void moveTo(double x, double y) {
		canvas.moveTo(x, y);
	}

	public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
		canvas.quadraticCurveTo(cpx, cpy, x, y);
	}

	public void rect(double startX, double startY, double width, double height) {
		canvas.rect(startX, startY, width, height);
	}

	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		canvas.resize(width, height);
	}

	public void restoreContext() {
		canvas.restoreContext();
	}

	public void rotate(double angle) {
		canvas.rotate(angle);
	}

	public void saveContext() {
		canvas.saveContext();
	}

	public void scale(double x, double y) {
		canvas.scale(x, y);
	}

	public void setBackgroundColor(Color color) {
		canvas.setBackgroundColor(color);
	}

	public void setCoordHeight(int height) {
		canvas.setCoordHeight(height);
	}

	public void setCoordSize(int width, int height) {
		canvas.setCoordSize(width, height);
	}

	public void setCoordWidth(int width) {
		canvas.setCoordWidth(width);
	}

	public void setFillStyle(CanvasGradient grad) {
		canvas.setFillStyle(grad);
	}

	public void setFillStyle(Color color) {
		canvas.setFillStyle(color);
	}

	public void setGlobalAlpha(double alpha) {
		canvas.setGlobalAlpha(alpha);
	}

	public void setGlobalCompositeOperation(String globalCompositeOperation) {
		canvas.setGlobalCompositeOperation(globalCompositeOperation);
	}

	public void setLineCap(String lineCap) {
		canvas.setLineCap(lineCap);
	}

	public void setLineJoin(String lineJoin) {
		canvas.setLineJoin(lineJoin);
	}

	public void setLineWidth(double width) {
		canvas.setLineWidth(width);
	}

	public void setMiterLimit(double miterLimit) {
		canvas.setMiterLimit(miterLimit);
	}

	public void setPixelHeight(int height) {
		canvas.setPixelHeight(height);
	}

	public void setPixelWidth(int width) {
		canvas.setPixelWidth(width);
	}

	public void setStrokeStyle(CanvasGradient grad) {
		canvas.setStrokeStyle(grad);
	}

	public void setStrokeStyle(Color color) {
		canvas.setStrokeStyle(color);
	}

	public void stroke() {
		canvas.stroke();
	}

	public void strokeRect(double startX, double startY, double width, double height) {
		canvas.strokeRect(startX, startY, width, height);
	}

	public void transform(double m11, double m12, double m21, double m22, double dx, double dy) {
		canvas.transform(m11, m12, m21, m22, dx, dy);
	}

	public void translate(double x, double y) {
		canvas.translate(x, y);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		canvas.fireEvent(event);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
		eventHandlers.put(handler, (DomEvent.Type) MouseDownEvent.getType());
		if (canvas != null) {
			canvas.addDomHandler(handler, MouseDownEvent.getType());
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
		eventHandlers.put(handler, (DomEvent.Type) MouseUpEvent.getType());
		if (canvas != null) {
			canvas.addDomHandler(handler, MouseUpEvent.getType());
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		eventHandlers.put(handler, (DomEvent.Type) MouseOutEvent.getType());
		if (canvas != null) {
			canvas.addDomHandler(handler, MouseOutEvent.getType());
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
		eventHandlers.put(handler, (DomEvent.Type) MouseOverEvent.getType());
		if (canvas != null) {
			canvas.addDomHandler(handler, MouseOverEvent.getType());
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
		eventHandlers.put(handler, (DomEvent.Type) MouseMoveEvent.getType());
		if (canvas != null) {
			canvas.addDomHandler(handler, MouseMoveEvent.getType());
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
		eventHandlers.put(handler, (DomEvent.Type) MouseWheelEvent.getType());
		if (canvas != null) {
			canvas.addDomHandler(handler, MouseWheelEvent.getType());
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		eventHandlers.put(handler, (DomEvent.Type) ClickEvent.getType());
		if (canvas != null) {
			canvas.addDomHandler(handler, ClickEvent.getType());
		}
		return null;
	}
}
