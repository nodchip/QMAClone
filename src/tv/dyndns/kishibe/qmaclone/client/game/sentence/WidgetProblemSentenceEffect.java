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
package tv.dyndns.kishibe.qmaclone.client.game.sentence;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.util.Rand;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;

public class WidgetProblemSentenceEffect extends WidgetProblemSentenceNormal {
	public interface HintTemplate extends SafeHtmlTemplates {
		@Template("<p style='position:relative;'>"
				+ "<span style='width:600px;height:64px;font-size:56px;text-align:center;vertical-align:middle;'>{0}</span>")
		SafeHtml prefix(String hint);

		@Template("<span style='{0}'></span>")
		SafeHtml line(SafeStyles styles);
	}

	private static final HintTemplate HINT_TEMPLATE = GWT.create(HintTemplate.class);
	private static final SafeHtml SURFIX_TEMPLATE = SafeHtmlUtils.fromSafeConstant("</p>");
	private static final int WIDTH = 600;
	private static final int NUMBER_OF_LINE = 10;
	private static final int INITIALI_WIDTH = 100;
	private static final int UPDATE_PERIOD_MS = 200;
	private static final int SHOW_ALL_TIMING_MS = 25 * 1000;

	@VisibleForTesting
	final HTML html = new HTML();
	private final SafeHtml prefix;
	private final int velocities[] = new int[NUMBER_OF_LINE];
	private final int pos[] = new int[NUMBER_OF_LINE];
	private int count = 0;
	private static final String[] COLORS = { "FFFFFF", "F8F8F8", "F0F0F0", "E8E8E8", "E0E0E0",
			"D8D8D8", "D0D0D0", "C8C8C8", "C0C0C0", "B8B8B8", };
	private final RepeatingCommand commandUpdate = new RepeatingCommand() {
		@Override
		public boolean execute() {
			update();
			return isAttached();
		}
	};

	public WidgetProblemSentenceEffect(PacketProblem problem) {
		super(problem);
		setWidth("600px");

		String hint = problem.choices[0];
		prefix = HINT_TEMPLATE.prefix(hint);

		html.setPixelSize(600, 64);

		add(html);
		setCellHorizontalAlignment(html, ALIGN_CENTER);
		setCellVerticalAlignment(html, ALIGN_MIDDLE);
		setCellWidth(html, "600px");
		setCellHeight(html, "64px");

		Rand rand = new Rand(Objects.hashCode(problem.id, problem.shuffledChoices,
				problem.shuffledAnswers));
		for (int i = 0; i < NUMBER_OF_LINE; ++i) {
			velocities[i] = rand.get(10) + 6;
			if (rand.get(2) == 0) {
				velocities[i] *= -1;
			}
			pos[i] = rand.get(WIDTH - INITIALI_WIDTH);
		}

		update();
	}

	@VisibleForTesting
	void update() {
		// SHOW_ALL_TIMING_MS以降は目隠しを表示しない
		int showAllCount = SHOW_ALL_TIMING_MS / UPDATE_PERIOD_MS;
		if (++count > showAllCount) {
			html.setHTML(new SafeHtmlBuilder().append(prefix).append(SURFIX_TEMPLATE).toSafeHtml());
			return;
		}

		// 目隠しの位置を更新する
		int lineWidth = INITIALI_WIDTH * (showAllCount - count) / showAllCount;
		for (int i = 0; i < NUMBER_OF_LINE; ++i) {
			pos[i] += velocities[i];
			if (pos[i] < 0) {
				velocities[i] = Math.abs(velocities[i]);
			} else if (pos[i] > WIDTH - lineWidth) {
				velocities[i] = -Math.abs(velocities[i]);
			}
		}

		// 目隠しを表示する
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.append(prefix);

		for (int i = 0; i < NUMBER_OF_LINE; ++i) {
			SafeStyles styles = new SafeStylesBuilder().position(Position.ABSOLUTE)
					.left(pos[i], Unit.PX).top(0, Unit.PX).trustedBackgroundColor(COLORS[i])
					.width(lineWidth, Unit.PX).height(64, Unit.PX).toSafeStyles();
			builder.append(HINT_TEMPLATE.line(styles));
		}
		builder.append(SURFIX_TEMPLATE);

		html.setHTML(builder.toSafeHtml());
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleFixedPeriod(commandUpdate, UPDATE_PERIOD_MS);
	}
}
