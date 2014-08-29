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
		if (event.getTypeInt() != Event.ONKEYPRESS) {
			return;
		}
		char ch = getCharCode(event.getNativeEvent());
		onKeyPress(ch);
	}

	private native char getCharCode(NativeEvent e)/*-{
		return e.charCode || e.keyCode;
	}-*/;
}
