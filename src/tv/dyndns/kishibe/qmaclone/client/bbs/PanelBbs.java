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
package tv.dyndns.kishibe.qmaclone.client.bbs;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsThread;
import tv.dyndns.kishibe.qmaclone.client.ui.PageSelectable;
import tv.dyndns.kishibe.qmaclone.client.ui.WidgetPageSelector;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class PanelBbs extends VerticalPanel implements PageSelectable, ClickHandler {
	private static final Logger logger = Logger.getLogger(PanelBbs.class.getName());
	private final PanelBbs instance = this;
	private final VerticalPanel displayPanel = new VerticalPanel();
	private final PanelBuildThread buildThread = new PanelBuildThread(this);
	private final LazyPanel threadListPanel = new LazyPanel() {
		@Override
		protected Widget createWidget() {
			return new PanelThreadList(bbsId);
		}
	};
	private final Button threadListButton = new Button("スレッドリスト", this);
	private WidgetPageSelector pageSelector;
	private final int bbsId;

	public PanelBbs(int bbsId) {
		this.bbsId = bbsId;
		setWidth("800px");
		displayPanel.setHorizontalAlignment(ALIGN_CENTER);
		reload();
	}

	public void reload() {
		Service.Util.getInstance().getNumberOfBbsThreads(bbsId, callbackGetNumberOfBbsThreads);
	}

	private final AsyncCallback<Integer> callbackGetNumberOfBbsThreads = new AsyncCallback<Integer>() {
		public void onSuccess(Integer result) {
			clear();

			{
				int numberOfBbsThreads = result;
				int numberOfPage = (numberOfBbsThreads - 1) / Constant.BBS_THREADS_PER_PAGE + 1;
				pageSelector = new WidgetPageSelector(instance, numberOfPage);
				pageSelector.setPage(0);

				HorizontalPanel panel = new HorizontalPanel();
				panel.add(pageSelector);
				panel.add(threadListButton);
				add(panel);
				setCellHorizontalAlignment(panel, ALIGN_CENTER);
			}

			add(displayPanel);

			add(buildThread);
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "BBSスレッド数の取得に失敗しました", caught);
		}
	};

	@Override
	public void selectPage(int page) {
		Service.Util.getInstance().getBbsThreads(bbsId, page * Constant.BBS_THREADS_PER_PAGE,
				Constant.BBS_THREADS_PER_PAGE, callbackGetBbsThread);
	}

	private final AsyncCallback<List<PacketBbsThread>> callbackGetBbsThread = new AsyncCallback<List<PacketBbsThread>>() {
		public void onSuccess(List<PacketBbsThread> result) {
			displayPanel.clear();

			for (PacketBbsThread thread : result) {
				DecoratorPanel decoratorPanel = new DecoratorPanel();
				decoratorPanel.setWidget(new PanelThread((int) thread.id, thread.title));
				displayPanel.add(decoratorPanel);
			}
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "BBSスレッドの取得に失敗しました", caught);
		}
	};

	@Override
	public void onClick(ClickEvent event) {
		Object source = event.getSource();
		if (source == threadListButton) {
			pageSelector.deselect();
			displayPanel.clear();
			displayPanel.add(threadListPanel);
			threadListPanel.ensureWidget();
		}
	}

	public int getBbsId() {
		return bbsId;
	}
}
