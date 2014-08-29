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

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.lib.text.RichTextToolbar;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketBbsResponse;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelResponseForm extends VerticalPanel {
  private final ListBox listBoxAnonymous = new ListBox();
  private final CheckBox checkBoxAge = new CheckBox("スレッド順位を上げる");
  private final RichTextArea richTextArea = new RichTextArea();
  private final RichTextToolbar toolbar = new RichTextToolbar(richTextArea);

  public PanelResponseForm(boolean displayAge) {
    {
      HorizontalPanel panel = new HorizontalPanel();
      listBoxAnonymous.addItem("匿名", "0");
      listBoxAnonymous.addItem("名前のみ表示", "1");
      listBoxAnonymous.addItem("全て表示", "2");
      listBoxAnonymous.setSelectedIndex(UserData.get().getBbsDispInfo());
      listBoxAnonymous.setWidth("200px");
      panel.add(listBoxAnonymous);

      if (displayAge) {
        panel.add(checkBoxAge);
        checkBoxAge.setValue(UserData.get().isBbsAge());
      }

      add(panel);
    }

    richTextArea.ensureDebugId("cwRichText-area");
    richTextArea.setWidth("600px");
    toolbar.ensureDebugId("cwRichText-toolbar");
    toolbar.setWidth("100%");

    add(toolbar);
    add(richTextArea);
  }

  public Optional<PacketBbsResponse> getBbsResponse() {
    String body = richTextArea.getHTML();
    if (Strings.isNullOrEmpty(body)) {
      return Optional.absent();
    }

    PacketBbsResponse response = new PacketBbsResponse();
    response.name = UserData.get().getPlayerName();
    response.userCode = UserData.get().getUserCode();
    response.dispInfo = Integer.parseInt(listBoxAnonymous.getValue(listBoxAnonymous
        .getSelectedIndex()));
    response.body = body;

    return Optional.of(response);
  }

  public boolean isAgeChecked() {
    return checkBoxAge.getValue();
  }

  public void setEnabled(boolean enabled) {
    listBoxAnonymous.setEnabled(enabled);
    checkBoxAge.setEnabled(enabled);
    richTextArea.setEnabled(enabled);
  }

  public void clearForm() {
    richTextArea.setHTML("");
  }
}
