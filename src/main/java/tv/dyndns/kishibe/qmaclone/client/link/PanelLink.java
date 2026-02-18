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
package tv.dyndns.kishibe.qmaclone.client.link;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;
import tv.dyndns.kishibe.qmaclone.client.service.LinkService;
import tv.dyndns.kishibe.qmaclone.client.ui.PageSelectable;
import tv.dyndns.kishibe.qmaclone.client.ui.WidgetPageSelector;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelLink extends VerticalPanel implements PageSelectable, LinkDataUpdateListener {
	private static final Logger logger = Logger.getLogger(PanelLink.class.getName());
	private final VerticalPanel displayPanel = new VerticalPanel();
	private final PanelAddLink panelAddLink = new PanelAddLink(this);
	private final PanelLink instance = this;
	private final HTML title = new HTML("リンク");
	private final HTML lead = new HTML("登録されたリンク集です。更新・追加は下部フォームから行えます。");
	private final HorizontalPanel header = new HorizontalPanel();

	public PanelLink() {
		setWidth("800px");
		addStyleName("linkRoot");
		title.addStyleName("linkPageTitle");
		lead.addStyleName("linkPageLead");
		header.addStyleName("linkHeaderCard");
		header.setWidth("100%");
		header.add(title);
		header.setCellHorizontalAlignment(title, ALIGN_LEFT);
		displayPanel.setWidth("100%");
		displayPanel.addStyleName("linkList");
		panelAddLink.addStyleName("linkEditorCard");
		add(header);
		add(lead);
		displayPanel.setHorizontalAlignment(ALIGN_CENTER);
		reload();
	}

	public void selectPage(int page) {
		LinkService.Util.get().get(page * Constant.LINK_DATA_PER_PAGE, Constant.LINK_DATA_PER_PAGE,
				callbackGetLinkDatas);
	}

	public void reload() {
		LinkService.Util.get().getNumberOfLinkData(callbackGetNumberOfLinkDatas);
	}

	private final AsyncCallback<List<PacketLinkData>> callbackGetLinkDatas = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<List<PacketLinkData>>() {
		public void onSuccess(List<PacketLinkData> result) {
			displayPanel.clear();

			for (PacketLinkData link : result) {
				displayPanel.add(new WidgetLinkData(link, instance));

			}
		}

		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "リンクの取得に失敗しました", caught);
		}
	};

	private final AsyncCallback<Integer> callbackGetNumberOfLinkDatas = new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<Integer>() {
		public void onSuccess(Integer result) {
			clear();

			add(header);
			add(lead);

			final int numberOflinkDatas = result;

			final WidgetPageSelector widgetPageSelector = new WidgetPageSelector(instance,
					(numberOflinkDatas - 1) / Constant.LINK_DATA_PER_PAGE + 1);
			widgetPageSelector.addStyleName("linkPager");
			add(widgetPageSelector);
			widgetPageSelector.setPage(0);
			setCellHorizontalAlignment(widgetPageSelector, ALIGN_CENTER);

			add(displayPanel);

			add(panelAddLink);

			panelAddLink.switchMode(false);
		}

		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "リンク件数の取得に失敗しました", caught);
		}
	};

	public void onClickUpdate(PacketLinkData linkData) {
		displayPanel.clear();
		panelAddLink.setLinkData(linkData);
		panelAddLink.switchMode(true);
	}
}

