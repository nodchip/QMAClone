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

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Optional;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.SharedData;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsResponse;

public class PanelThread extends VerticalPanel implements ClickHandler {
  private static final Logger logger = Logger.getLogger(PanelThread.class.getName());
  private final int threadId;
  private final String title;
  private final VerticalPanel bodyPanel = new VerticalPanel();
  private final Button buttonAll = new Button("全部読む", this);
  private final Button buttonWrite = new Button("書き込む", this);
  private final PanelResponseForm responseForm = new PanelResponseForm(true);

  public PanelThread(int threadId, String title) {
    this.threadId = threadId;
    this.title = title;
    setHorizontalAlignment(ALIGN_CENTER);
    setEnabled(false);
    Service.Util.getInstance().getBbsResponses(threadId, Constant.BBS_INITIAL_RESPONSE_PER_THREAD,
        callbackGetBbsResponses);
  }

  private void setPanels() {
    HTML titleHtml = new HTML(SafeHtmlUtils.fromString(title));
    titleHtml.addStyleDependentName("bbsThreadTitle");
    add(titleHtml);

    add(bodyPanel);

    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(buttonAll);
      panel.add(buttonWrite);
      add(panel);
    }

    add(responseForm);
  }

  private final AsyncCallback<List<PacketBbsResponse>> callbackGetBbsResponses = new AsyncCallback<List<PacketBbsResponse>>() {
    public void onSuccess(List<PacketBbsResponse> result) {
      setResponses(result);
      setEnabled(true);
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "BBSのレスの取得に失敗しました", caught);
    }
  };

  private void setResponses(List<PacketBbsResponse> responses) {
    bodyPanel.clear();

    if (!bodyPanel.isAttached()) {
      setPanels();
    }

    for (PacketBbsResponse response : responses) {
      VerticalPanel panel = new VerticalPanel();

      {
        String upper = "";
        if (SharedData.get().isAdministoratorMode()) {
          upper = response.id + ": " + response.name
              + Utility.makeTrip(response.userCode, response.remoteAddress) + " "
              + Utility.toDateFormat(new Date(response.postTime));
        } else {
          switch (response.dispInfo) {
          case Constant.BBS_DISPLAY_INFO_ANONYMOUS:
            upper = response.id + ": " + Utility.toDateFormat(new Date(response.postTime));
            break;
          case Constant.BBS_DISPLAY_INFO_NAME_ONLY:
            upper = response.id + ": " + response.name + " "
                + Utility.toDateFormat(new Date(response.postTime));
            break;
          case Constant.BBS_DISPLAY_INFO_ALL_DATA:
            upper = response.id + ": " + response.name
                + Utility.makeTrip(response.userCode, response.remoteAddress) + " "
                + Utility.toDateFormat(new Date(response.postTime));
            break;
          }
        }

        HTML upperHtml = new HTML(SafeHtmlUtils.fromString(upper));
        upperHtml.addStyleDependentName("bbsResponseHeader");
        panel.add(upperHtml);
      }

      HTML bodyHtml = new HTML(response.body);
      bodyHtml.addStyleDependentName("bbsResponseBody");
      panel.add(bodyHtml);
      bodyPanel.add(panel);
    }

    setEnabled(true);
  }

  private void showAll() {
    setEnabled(false);
    Service.Util.getInstance().getBbsResponses(threadId, Integer.MAX_VALUE,
        callbackGetBbsResponses);
  }

  private void write() {
    Optional<PacketBbsResponse> response = responseForm.getBbsResponse();
    if (!response.isPresent()) {
      return;
    }

    setEnabled(false);

    response.get().threadId = threadId;

    boolean age = responseForm.isAgeChecked();

    UserData.get().setBbsDispInfo(response.get().dispInfo);
    UserData.get().setBbsAge(age);

    Service.Util.getInstance().writeToBbs(response.get(), age, callbackWriteToBbs);
  }

  private final AsyncCallback<Void> callbackWriteToBbs = new AsyncCallback<Void>() {
    public void onSuccess(Void result) {
      responseForm.clearForm();
      Service.Util.getInstance().getBbsResponses(threadId, Constant.BBS_INITIAL_RESPONSE_PER_THREAD,
          callbackGetBbsResponses);
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "BBSスレッドへのレスの書き込みに失敗しました", caught);
    }
  };

  private void setEnabled(boolean enabled) {
    buttonAll.setEnabled(enabled);
    buttonWrite.setEnabled(enabled);
    responseForm.setEnabled(enabled);
  }

  @Override
  public void onClick(ClickEvent event) {
    Object sender = event.getSource();
    if (sender == buttonAll) {
      showAll();
    } else if (sender == buttonWrite) {
      write();
    }
  }
}
