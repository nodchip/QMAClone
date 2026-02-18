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
		@Template("<p class='timerMeter'><span class='timerLabel'>{0}</span>"
				+ "<span class='timerFill' style='{1}'></span></p>")
		SafeHtml bar(String message, SafeStyles styles);
	}

	private static final BarTemplate TEMPLATE = GWT.create(BarTemplate.class);
	private static final int UPDATE_DURATION = 33;
	private static final int MAX_TIME = 30 * 1000;
	private final HTML html = new HTML();
	private int syncedRemainingMs = MAX_TIME;
	private long syncedAtMs = System.currentTimeMillis();
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
		setStyleName("gameTimerWidget");

		add(html);
		html.setWidth("100%");
		html.setHeight("40px");
		html.setStyleName("gameTimerTrack");
		update();
	}

	public void setTime(int second) {
		int clampedSecond = Math.max(0, second);
		syncedRemainingMs = clampedSecond * 1000;
		syncedAtMs = System.currentTimeMillis();
	}

	private void update() {
		long now = System.currentTimeMillis();
		int elapsedSinceSync = (int) (now - syncedAtMs);
		int remainingMs = Math.max(0, syncedRemainingMs - elapsedSinceSync);
		double remainingRate = TimerGaugeStyle.remainingRate(MAX_TIME - remainingMs, MAX_TIME);
		double left = (1.0 - remainingRate) * 100.0;
		double width = remainingRate * 100.0;
		int displaySecond = (remainingMs + 999) / 1000;
		String message = displaySecond == 0 ? "時間切れ" : ("残り時間約" + displaySecond + "秒");
		String color = TimerGaugeStyle.fillColor(remainingRate);
		SafeStyles styles = new SafeStylesBuilder()
				.position(Position.ABSOLUTE)
				.top(2, Unit.PX)
				.left(left, Unit.PCT)
				.width(width, Unit.PCT)
				.height(32, Unit.PX)
				.trustedBackgroundColor(color)
				.zIndex(1)
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
