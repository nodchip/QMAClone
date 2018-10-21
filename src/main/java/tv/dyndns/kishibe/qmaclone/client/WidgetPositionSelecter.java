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
package tv.dyndns.kishibe.qmaclone.client;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.SimplePanel;

public class WidgetPositionSelecter extends SimplePanel {
	private static final WidgetPositionSelecter instance = new WidgetPositionSelecter();

	public static WidgetPositionSelecter getIstance() {
		return instance;
	}

	private WidgetPositionSelecter() {
		addStyleName("positionSelecter");

		final MenuBar menu = new MenuBar(true);
		menu.setAnimationEnabled(true);
		menu.addItem(new MenuItem("ページトップ", commandPositionTop));
		menu.addItem(new MenuItem("チャット", commandPositionChat));
		menu.addItem(new MenuItem("ページ下", commandPositionBottom));

		final MenuBar menuBar = new MenuBar(true);
		menu.setAnimationEnabled(true);
		menuBar.addItem("表示位置", menu);

		setWidget(menuBar);
	}

	private Command commandPositionTop = new Command() {
		public void execute() {
			Controller.getInstance().scrollToTop();
		}
	};

	private Command commandPositionChat = new Command() {
		public void execute() {
			Controller.getInstance().scrollToChat();
		}
	};

	private Command commandPositionBottom = new Command() {
		public void execute() {
			Controller.getInstance().scrollToBottom();
		}
	};
}
