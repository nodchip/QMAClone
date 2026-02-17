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
package tv.dyndns.kishibe.qmaclone.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TwoColumnSelectionPanel extends DockPanel implements ClickHandler {
	private static final String MENU_ITEM_STYLE_SELECTED = "settingLeftPanelItemSelected";
	private final Map<HTML, Widget> leftToRight = new HashMap<HTML, Widget>();
	private final SimplePanel westPanel = new SimplePanel();
	private final VerticalPanel panel = new VerticalPanel();
	private final SimplePanel centerPanel = new SimplePanel();
	private boolean forceContentWidgetWidth = false;

	public TwoColumnSelectionPanel(int menuWidth) {
		westPanel.setWidth(menuWidth + "px");
		westPanel.setWidget(panel);
		panel.setWidth("100%");
		panel.addStyleName("settingLeftPanel");
		add(westPanel, WEST);
		add(centerPanel, CENTER);
	}

	public void add(String item, final Widget widget) {
		final HTML html = new HTML(item);
		html.addStyleName("settingLeftPanelItem");
		html.addClickHandler(this);
		panel.add(html);
		leftToRight.put(html, widget);

		if (centerPanel.getWidget() == null) {
			onClick(new ClickEvent() {
				@Override
				public Object getSource() {
					return html;
				}
			});
		}
	}

	public void addSeparator(String item) {
		panel.add(new Label(item));
	}

	public void setCenterPanelWidth(int widthPx) {
		centerPanel.setWidth(widthPx + "px");
	}

	public void setForceContentWidgetWidth(boolean force) {
		this.forceContentWidgetWidth = force;
	}

	@Override
	public void onClick(ClickEvent event) {
		final Widget sender = (Widget) event.getSource();
		if (leftToRight.containsKey(sender)) {
			// 左メニューの選択状態を単一選択にそろえる。
			for (HTML menuItem : leftToRight.keySet()) {
				menuItem.removeStyleName(MENU_ITEM_STYLE_SELECTED);
			}
			sender.addStyleName(MENU_ITEM_STYLE_SELECTED);
			final Widget w = leftToRight.get(sender);
			if (forceContentWidgetWidth) {
				w.setWidth("100%");
			}
			centerPanel.setWidget(w);
			// LazyPanelはsetWidget()のみでは本体が表示されないため、setVisible()を呼び出して表示させる
			w.setVisible(true);
		}
	}
}
