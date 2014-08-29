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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.ui.HTML;

public class PanelAdvertisement extends HTML {
	private static final int UPDATE_DURATION = 300 * 1000;
	private static final String AD = "<iframe src=\"http://kishibe.dyndns.tv/qmaclone/ad.html\" height=\"90\" width=\"728\" frameborder=\"0\" scrolling=\"no\"></iframe>";
	private RepeatingCommand commandUpdate = new RepeatingCommand() {
		@Override
		public boolean execute() {
			update();
			return isAttached();
		}
	};

	public PanelAdvertisement() {
		super(AD);
		setHeight("90px");
	}

	private void update() {
		if (SharedData.get().getIsPlaying()) {
			return;
		}

		setHTML(AD);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleFixedDelay(commandUpdate, UPDATE_DURATION);
	}
}
