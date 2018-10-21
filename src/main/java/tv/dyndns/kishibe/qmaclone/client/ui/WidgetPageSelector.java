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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WidgetPageSelector extends HorizontalPanel implements ClickHandler {
	private static final String STYLE = "pageSelectButton";
	private final PageSelectable selectable;
	private final Map<Button, Integer> buttonToPageNumber = new HashMap<Button, Integer>();
	private final int numberOfPage;

	public WidgetPageSelector(PageSelectable selectable, int numberOfPage) {
		this.selectable = selectable;
		this.numberOfPage = numberOfPage;
		if (numberOfPage <= 1) {
			setVisible(false);
		}
	}

	/**
	 * ページ番号をセットする
	 * 
	 * @param page
	 *            現在のページ番号(0ベース)
	 * @param numberOfPage
	 *            総ページ数
	 */
	public void setPage(int page) {
		clear();
		buttonToPageNumber.clear();

		// 先頭ページ
		if (page > 5) {
			Button button = new Button("&lt;&lt;", this);
			button.setTitle("先頭ページへ");
			button.addStyleName(STYLE);
			buttonToPageNumber.put(button, 0);
			add(button);
		}

		// 5ページ前
		if (page > 4) {
			Button button = new Button("&lt;", this);
			button.setTitle("5ページ前へ");
			button.addStyleName(STYLE);
			buttonToPageNumber.put(button, page - 5);
			add(button);
		}

		// 番号つき
		{
			int beginPage = Math.max(0, page - 4);
			int endPage = Math.min(numberOfPage, page + 5);
			for (int p = beginPage; p < endPage; ++p) {
				Button button = new Button("" + (p + 1), this);
				button.addStyleName(STYLE);
				buttonToPageNumber.put(button, p);
				add(button);

				if (p == page) {
					button.setEnabled(false);
				}
			}
		}

		// 5ページ後
		if (page < numberOfPage - 5) {
			Button button = new Button("&gt;", this);
			button.setTitle("5ページ後へ");
			button.addStyleName(STYLE);
			buttonToPageNumber.put(button, page + 5);
			add(button);
		}

		// 先頭ページ
		if (page < numberOfPage - 6) {
			Button button = new Button("&gt;&gt;", this);
			button.setTitle("末尾ページへ");
			button.addStyleName(STYLE);
			buttonToPageNumber.put(button, numberOfPage - 1);
			add(button);
		}

		selectable.selectPage(page);
	}

	public void deselect() {
		for (Button button : buttonToPageNumber.keySet()) {
			button.setEnabled(true);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		Widget sender = (Widget) event.getSource();
		if (buttonToPageNumber.containsKey(sender)) {
			int page = buttonToPageNumber.get(sender);
			setPage(page);
			selectable.selectPage(page);
		}
	}
}
