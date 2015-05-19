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
package tv.dyndns.kishibe.qmaclone.client.game;

import tv.dyndns.kishibe.qmaclone.client.Utility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WidgetTimeProgressBar extends VerticalPanel {
	public interface BarTemplate extends SafeHtmlTemplates {
		@Template("<p style='position:relative;'><span class='timerLabel'>{0}</span>"
				+ "<span style='{1}'></span></p>")
		SafeHtml bar(String message, SafeStyles styles);
	}

	private static final BarTemplate TEMPLATE = GWT.create(BarTemplate.class);
	private static final int WIDTH = 480;
	private static final int UPDATE_DURATION = 250;
	private static final int MAX_TIME = 30 * 1000;
	private final HTML html = new HTML();
	private int second = 30;
	private final long startTime = System.currentTimeMillis();
	private final RepeatingCommand commandUpdate = new RepeatingCommand() {
		@Override
		public boolean execute() {
			update();
			return isAttached();
		}
	};

	public WidgetTimeProgressBar() {
		setHorizontalAlignment(ALIGN_CENTER);
		setVerticalAlignment(ALIGN_MIDDLE);

		add(html);
		html.setPixelSize(WIDTH, 40);
		update();
	}

	public void setTime(int second) {
		this.second = Math.max(0, second);
	}

	private void update() {
		long currentTime = System.currentTimeMillis();
		int time = (int) (currentTime - startTime);

		int left = time * WIDTH / MAX_TIME;
		int width = WIDTH - left;
		String message = second == 0 ? "時間切れ" : ("残り時間約" + second + "秒");
		String color = Utility.createBackgroundColorString((double) width / (double) WIDTH);
		SafeStyles styles = new SafeStylesBuilder()
				.position(Position.ABSOLUTE)
				.top(0, Unit.PX)
				.left(left, Unit.PX)
				.width(width, Unit.PX)
				.height(32, Unit.PX)
				.trustedBackgroundColor(color)
				.zIndex(-5)
				.toSafeStyles();
		SafeHtml safeHtml = TEMPLATE.bar(message, styles);
		html.setHTML(safeHtml);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleFixedPeriod(commandUpdate, UPDATE_DURATION);
	}
}
