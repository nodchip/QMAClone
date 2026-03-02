package tv.dyndns.kishibe.qmaclone.client.setting;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Facebook連携更新用の管理者パネル。
 */
public class PanelSettingFacebookAuth extends VerticalPanel {
  public PanelSettingFacebookAuth() {
    setStyleName("settingAdminRoot");
    add(new HTML("<h3>Facebook連携</h3>"));
    add(new HTML("<p>Facebookアクセストークンを再認可して更新します。</p>"));

    Button buttonAuthorize = new Button("Facebook連携を更新");
    buttonAuthorize.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Window.open("admin/facebook/auth/start", "_blank", "");
      }
    });
    add(buttonAuthorize);
  }
}

