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

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;
import tv.dyndns.kishibe.qmaclone.client.service.LinkService;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelAddLink extends VerticalPanel implements ClickHandler {
	private static final Logger logger = Logger.getLogger(PanelAddLink.class.getName());
	private final PanelLink panelLink;
	private final Button buttonAddLink = new Button("リンクを追加する", this);
	private final Button buttonSubmit = new Button("送信", this);
	private final Button buttonCancel = new Button("キャンセル", this);
	private final WidgetLinkDataForm widgetLinkDataForm = new WidgetLinkDataForm();
	private final RepeatingCommand commandCheckValid = new RepeatingCommand() {
		@Override
		public boolean execute() {
			final boolean valid = widgetLinkDataForm.isValid();
			buttonSubmit.setEnabled(valid);
			return isAttached();
		}
	};

	public PanelAddLink(PanelLink panelLink) {
		this.panelLink = panelLink;

		{
			final HorizontalPanel panel = new HorizontalPanel();
			panel.add(buttonAddLink);
			panel.add(buttonSubmit);
			panel.add(buttonCancel);
			add(panel);
		}

		add(widgetLinkDataForm);

		switchMode(false);
	}

	private final AsyncCallback<Void> callbackAddLinkData = new AsyncCallback<Void>() {
		public void onSuccess(Void result) {
			setEnabled(true);
			switchMode(false);
			widgetLinkDataForm.clearFrom();
			panelLink.reload();
		}

		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "リンクの追加に失敗しました", caught);
		}
	};

	public void switchMode(boolean showForm) {
		buttonAddLink.setVisible(!showForm);
		buttonSubmit.setVisible(showForm);
		buttonCancel.setVisible(showForm);
		widgetLinkDataForm.setVisible(showForm);
	}

	public void setLinkData(PacketLinkData linkData) {
		widgetLinkDataForm.setLinkData(linkData);
	}

	public void setEnabled(boolean enabled) {
		buttonAddLink.setEnabled(enabled);
		buttonSubmit.setEnabled(enabled);
		buttonCancel.setEnabled(enabled);
		widgetLinkDataForm.setEnabled(enabled);
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		commandCheckValid.execute();
		Scheduler.get().scheduleFixedDelay(commandCheckValid, 1000);
	}

	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource() == buttonAddLink) {
			widgetLinkDataForm.clearFrom();
			switchMode(true);
		} else if (event.getSource() == buttonSubmit) {
			if (widgetLinkDataForm.isValid()) {
				final PacketLinkData linkData = widgetLinkDataForm.getLinkData();
				if (linkData.id < 0) {
					LinkService.Util.get().add(linkData, callbackAddLinkData);
				} else {
					LinkService.Util.get().update(linkData, callbackAddLinkData);
				}
			}
		} else if (event.getSource() == buttonCancel) {
			panelLink.reload();
		}
	}
}
