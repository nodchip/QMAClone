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

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsResponse;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsThread;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelBuildThread extends VerticalPanel implements ClickHandler {
  private static final Logger logger = Logger.getLogger(PanelBuildThread.class.getName());
  private final Button buttonToSecondState = new Button("新規スレッド作成", this);
  private final Button buttonBuild = new Button("スレッドを立てる", this);
  private final TextBox textBoxThreadTitle = new TextBox();
  private final PanelResponseForm responseForm = new PanelResponseForm(false);
  private final PanelBbs panelBbs;

  public PanelBuildThread(PanelBbs panelBbs) {
    this.panelBbs = panelBbs;

    displayFirstState();
  }

  private void displayFirstState() {
    clear();
    add(buttonToSecondState);
  }

  private void displaySecondState() {
    clear();
    add(new HTML("<b>新規スレッド作成</b>"));

    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(buttonBuild);

      textBoxThreadTitle.setWidth("400px");
      panel.add(textBoxThreadTitle);

      add(panel);
    }

    add(responseForm);
  }

  private void build() {
    String threadName = textBoxThreadTitle.getText();
    if (Strings.isNullOrEmpty(threadName)) {
      return;
    }

    PacketBbsThread thread = new PacketBbsThread();
    thread.title = threadName;

    Optional<PacketBbsResponse> response = responseForm.getBbsResponse();
    if (!response.isPresent()) {
      return;
    }

    setEnabled(false);

    Service.Util.getInstance().buildBbsThread(panelBbs.getBbsId(), thread, response.get(),
        callbackBuildBbsThread);
  }

  private final AsyncCallback<Void> callbackBuildBbsThread = new AsyncCallback<Void>() {
    public void onSuccess(Void result) {
      setEnabled(true);
      displayFirstState();
      clearForm();
      panelBbs.reload();
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "スレッドの設置に失敗しました", caught);
    }
  };

  private void setEnabled(boolean enabled) {
    buttonToSecondState.setEnabled(enabled);
    buttonBuild.setEnabled(enabled);
    textBoxThreadTitle.setEnabled(enabled);
    responseForm.setEnabled(enabled);
  }

  private void clearForm() {
    textBoxThreadTitle.setText("");
    responseForm.clearForm();
  }

  @Override
  public void onClick(ClickEvent event) {
    if (event.getSource() == buttonToSecondState) {
      displaySecondState();
    } else if (event.getSource() == buttonBuild) {
      build();
    }
  }
}
