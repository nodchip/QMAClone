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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;
import tv.dyndns.kishibe.qmaclone.client.service.LinkService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WidgetLinkData extends VerticalPanel implements ClickHandler {
	private static final Logger logger = Logger.getLogger(WidgetLinkData.class.getName());

	interface LinkDataTemplate extends SafeHtmlTemplates {
		@Template("<a href='{0}' target='_blank'>{1}</a> / {2} - 最終更新:{3}")
		SafeHtml messageWithLink(SafeUri url, String homePageName, String authorName,
				String lastUpdate);

		@Template("<a href='{0}' target='_black'><img src='{1]' /></a>")
		SafeHtml image(SafeUri url, String bannerUrl);
	}

	private static final LinkDataTemplate TEMPLATE = GWT.create(LinkDataTemplate.class);

	private final Button buttonUpdate = new Button("修正", this);
	private final Button buttonRemove = new Button("削除", this);
	private final PacketLinkData linkData;
	private final PanelLink panelLink;

	public WidgetLinkData(PacketLinkData linkData, PanelLink panelLink) {
		this.linkData = linkData;
		this.panelLink = panelLink;
		add(new HTML(TEMPLATE.messageWithLink(UriUtils.fromString(linkData.url),
				linkData.homePageName, linkData.authorName,
				Utility.toDateFormat(new Date(linkData.lastUpdate)))));

		{
			final HorizontalPanel panel = new HorizontalPanel();
			panel.setVerticalAlignment(ALIGN_MIDDLE);
			panel.add(new HTML(
					TEMPLATE.image(UriUtils.fromString(linkData.url), linkData.bannerUrl)));
			panel.add(new HTML(SafeHtmlUtils.fromString(linkData.description)));

			{
				final VerticalPanel panelButtons = new VerticalPanel();
				panelButtons.add(buttonUpdate);
				panelButtons.add(buttonRemove);
				panel.add(panelButtons);
			}

			add(panel);
		}
	}

	private final AsyncCallback<Void> callbackRemoveLinkData = new AsyncCallback<Void>() {
		public void onSuccess(Void result) {
			panelLink.reload();
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "リンクの削除に失敗しました", caught);
		}
	};

	private void setEnabled(boolean enabled) {
		buttonUpdate.setEnabled(enabled);
		buttonRemove.setEnabled(enabled);
	}

	@Override
	public void onClick(ClickEvent event) {
		final Widget sender = (Widget) event.getSource();
		if (sender == buttonUpdate) {
			panelLink.onClickUpdate(linkData);
		} else if (sender == buttonRemove) {
			if (Window.confirm("このリンクを削除しますか？")) {
				setEnabled(false);
				LinkService.Util.get().remove(linkData.id, callbackRemoveLinkData);
			}
		}
	}
}
