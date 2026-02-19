package tv.dyndns.kishibe.qmaclone.client;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;

/**
 * ドキュメントが受け取ったキーイベントを処理するためのハンドラ抽象クラス
 * 
 * @author nodchip
 */
public abstract class GlobalKeyEventHandler implements NativePreviewHandler {
	protected abstract void onKeyPress(char ch);

	@Override
	public final void onPreviewNativeEvent(NativePreviewEvent event) {
		// BackSpace 等の制御キーは環境によって keypress が発火しないため keydown で拾う。
		if (event.getTypeInt() == Event.ONKEYDOWN) {
			int keyCode = event.getNativeEvent().getKeyCode();
			switch (keyCode) {
			case 8: // BackSpace
				event.cancel();
				onKeyPress('\b');
				return;
			case 13: // Enter
				event.cancel();
				onKeyPress('\n');
				return;
			default:
				return;
			}
		}

		if (event.getTypeInt() == Event.ONKEYPRESS) {
			char ch = getCharCode(event.getNativeEvent());
			onKeyPress(ch);
		}
	}

	private native char getCharCode(NativeEvent e)/*-{
		return e.charCode || e.keyCode;
	}-*/;
}
