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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Label;

public class ClickableLabel extends Label implements MouseOverHandler, MouseOutHandler {
	private static final String CLICKABLE = "clickable";
	private static final String CLICKABLE_ENTER = "clickableEnter";
	private boolean enabled = true;

	public ClickableLabel(String text, ClickHandler clickHandler) {
		super(text, true);
		addDomHandler(clickHandler, ClickEvent.getType());
		addMouseOverHandler(this);
		addMouseOutHandler(this);
		setEnabled(true);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;

		if (enabled) {
			addStyleDependentName(CLICKABLE);
		} else {
			removeStyleDependentName(CLICKABLE);
		}
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		if (enabled) {
			addStyleDependentName(CLICKABLE_ENTER);

		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		if (enabled) {
			removeStyleDependentName(CLICKABLE_ENTER);
		}
	}
}
