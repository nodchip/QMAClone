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
package tv.dyndns.kishibe.qmaclone.client.chat;

import tv.dyndns.kishibe.qmaclone.client.SharedData;

import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class PanelChat extends VerticalPanel {
	public PanelChat() {
		setWidth("100%");
		setHeight("100%");
		addStyleName("app-chat-content");

		if (SharedData.get().isAdministoratorMode()) {
			final TabPanel panel = new TabPanel();
			panel.addStyleName("app-chat-tabs");
			panel.setAnimationEnabled(true);
			panel.setWidth("100%");
			panel.setHeight("100%");
			panel.add(new LazyPanel() {
				@Override
				protected Widget createWidget() {
					PanelRealtime widget = new PanelRealtime();
					widget.addStyleName("app-chat-pane");
					widget.addStyleName("app-chat-pane-realtime");
					widget.setWidth("100%");
					widget.setHeight("100%");
					return widget;
				}
			}, "リアルタイム");
			panel.add(new LazyPanel() {
				@Override
				protected Widget createWidget() {
					PanelPast widget = new PanelPast();
					widget.addStyleName("app-chat-pane");
					widget.addStyleName("app-chat-pane-past");
					widget.setWidth("100%");
					widget.setHeight("100%");
					return widget;
				}
			}, "過去ログ");
			panel.selectTab(0);
			add(panel);
			setCellWidth(panel, "100%");
			setCellHeight(panel, "100%");
		} else {
			PanelRealtime panelRealtime = new PanelRealtime();
			panelRealtime.addStyleName("app-chat-pane");
			panelRealtime.addStyleName("app-chat-pane-realtime");
			panelRealtime.setWidth("100%");
			panelRealtime.setHeight("100%");
			add(panelRealtime);
			setCellWidth(panelRealtime, "100%");
			setCellHeight(panelRealtime, "100%");
		}
	}
}
