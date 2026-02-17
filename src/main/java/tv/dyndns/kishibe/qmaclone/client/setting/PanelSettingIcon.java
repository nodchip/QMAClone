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
package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingIcon extends VerticalPanel implements SubmitCompleteHandler, ClickHandler {

  private static final Logger logger = Logger.getLogger(PanelSettingIcon.class.toString());
  private static final int UPDATE_DURATION = 1000;
  private final Image image = new Image(Constant.ICON_URL_PREFIX
      + UserData.get().getImageFileName());
  private final FileUpload fileUpload = new FileUpload();
  private final FormPanel form = new FormPanel();
  private final Button buttonSubmit = new Button("送信", this);
  private final HTML htmlMessage = new HTML();
  private boolean sending = false;
  private final RepeatingCommand commandUpdate = new RepeatingCommand() {
    @Override
    public boolean execute() {
      if (!sending) {
        checkForm();
      }
      return isAttached();
    }
  };

  public PanelSettingIcon() {
    setWidth("100%");
    setHorizontalAlignment(ALIGN_CENTER);
    addStyleName("settingIconPanel");

    HTML lead = new HTML("オリジナルアイコンをアップロードできます。<br/>"
        + "ファイルサイズは64KBまで、画像形式はブラウザで表示可能なものに対応しています。<br/>"
        + "画像は自動的に正方形に圧縮されて表示されます。公序良俗に反する画像の使用はお止めください。");
    lead.addStyleName("settingIconLead");
    add(lead);

    VerticalPanel card = new VerticalPanel();
    card.setWidth("100%");
    card.addStyleName("settingIconCard");

    image.setPixelSize(96, 96);
    image.addStyleName("settingIconPreview");
    card.add(image);

    VerticalPanel panelForm = new VerticalPanel();
    panelForm.setWidth("100%");
    panelForm.addStyleName("settingIconForm");

    Hidden hiddenUserCode = new Hidden();
    hiddenUserCode.setName(Constant.FORM_NAME_USER_CODE);
    hiddenUserCode.setValue(UserData.get().getUserCode() + "");
    panelForm.add(hiddenUserCode);

    fileUpload.setName(Constant.FORM_NAME_ICON);
    fileUpload.addStyleName("settingIconFileInput");
    panelForm.add(fileUpload);

    form.setAction("icon");
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);
    form.addSubmitCompleteHandler(this);
    form.setWidget(panelForm);
    form.addStyleName("settingIconUploadForm");

    buttonSubmit.setText("アイコンをアップロード");
    buttonSubmit.addStyleName("creationButtonPrimary");
    buttonSubmit.addStyleName("settingIconSubmitButton");

    HorizontalPanel actionRow = new HorizontalPanel();
    actionRow.addStyleName("settingIconActionRow");
    actionRow.setWidth("100%");
    actionRow.setHorizontalAlignment(ALIGN_CENTER);
    actionRow.add(buttonSubmit);

    card.add(form);
    card.add(actionRow);
    add(card);

    htmlMessage.addStyleName("settingMessage");
    htmlMessage.addStyleName("settingIconMessage");
    add(htmlMessage);

    checkForm();
  }

  private void submit() {
    buttonSubmit.setEnabled(false);

    if (!checkForm()) {
      return;
    }

    sending = true;
    form.submit();
  }

  private boolean checkForm() {
    htmlMessage.setHTML("");

    if (fileUpload.getFilename().length() == 0) {
      buttonSubmit.setEnabled(false);
      htmlMessage.setHTML("アップロードする画像を選択してください。");
      return false;
    }

    buttonSubmit.setEnabled(true);
    return true;
  }

  protected void onLoad() {
    super.onLoad();
    Scheduler.get().scheduleFixedDelay(commandUpdate, UPDATE_DURATION);
  }

  @Override
  public void onSubmitComplete(SubmitCompleteEvent event) {
    sending = false;
    String result = event.getResults();
    htmlMessage.setHTML("");

    if (Strings.isNullOrEmpty(result)) {
      htmlMessage.setHTML("アップロード中にエラーが発生しました。頻繁に発生する場合は管理人にお知らせ下さい。");
    } else if (result.contains(Constant.ICON_UPLOAD_RESPONSE_OK)) {
      htmlMessage.setHTML("アイコンのアップロードに成功しました。反映まで若干時間がかかります。");
      UserData.get().load();
      SettingSaveToast.showSaved("アイコン");
    } else if (result.contains(Constant.ICON_UPLOAD_RESPONSE_FAILED_TO_DETECT_IMAGE_FILE_TYPE)) {
      htmlMessage.setHTML("画像ファイルを認識できませんでした。正しい画像ファイルであることを確認して再度アップロードしてください。");
    } else if (result.contains(Constant.ICON_UPLOAD_RESPONSE_FAILED_TO_PARSE_REQUEST)) {
      htmlMessage.setHTML("送信されたデータが不正です。ファイルサイズ等を確認して再度アップロードしてください。");
    } else if (result.contains(Constant.ICON_UPLOAD_RESPONSE_IMAGE_FILE_NAME_FORMAT_ERROR)) {
      htmlMessage.setHTML("画像ファイルのファイル名に'.'が含まれていません。正しいファイルを再度アップロードしてください。");
    } else if (result.contains(Constant.ICON_UPLOAD_RESPONSE_REQUEST_FORMAT_ERROR)) {
      htmlMessage.setHTML("不正なパラメータが送信されました。正規のインタフェースから再度操作をしてください");
    } else {
      htmlMessage.setHTML("原因不明のエラーが発生しました。管理者にご連絡ください。");
      logger.warning("アイコンのアップロードに失敗しました。\n" + result);
    }

    buttonSubmit.setEnabled(true);
  }

  @Override
  public void onClick(ClickEvent event) {
    Object sender = event.getSource();
    if (sender == buttonSubmit) {
      submit();
    }
  }
}
