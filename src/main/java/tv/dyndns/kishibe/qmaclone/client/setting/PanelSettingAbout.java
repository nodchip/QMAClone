package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import tv.dyndns.kishibe.qmaclone.client.sound.SoundAsset;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundCatalog;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundCreditsCatalog;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundCreditsCatalog.SoundCreditEntry;
import tv.dyndns.kishibe.qmaclone.client.sound.SoundEvent;

/**
 * Help/Aboutパネル。
 */
public class PanelSettingAbout extends Composite {

  interface PanelSettingAboutUiBinder extends UiBinder<Widget, PanelSettingAbout> {}

  private static final PanelSettingAboutUiBinder UI_BINDER = GWT.create(PanelSettingAboutUiBinder.class);

  @UiField
  FlowPanel panelSoundCredits;

  public PanelSettingAbout() {
    initWidget(UI_BINDER.createAndBindUi(this));
    buildSoundCredits();
  }

  private void buildSoundCredits() {
    panelSoundCredits.clear();
    Map<SoundEvent, SoundAsset> assets = SoundCatalog.getAssets();
    for (SoundCreditEntry entry : SoundCreditsCatalog.getEntries()) {
      HTML item = new HTML();
      item.setStyleName("settingAboutCreditItem");
      item.setHTML(
          "<div class='settingAboutCreditTitle'>"
              + SafeHtmlUtils.htmlEscape(entry.getTitle())
              + "</div>"
              + "<div class='settingAboutCreditMeta'>assetId: "
              + SafeHtmlUtils.htmlEscape(entry.getAssetId())
              + " / source: "
              + SafeHtmlUtils.htmlEscape(entry.getSource())
              + " / license: "
              + SafeHtmlUtils.htmlEscape(entry.getLicense())
              + "</div>"
              + "<div class='settingAboutCreditMeta'>mapped: "
              + SafeHtmlUtils.htmlEscape(findMappedEvents(assets, entry.getAssetId()))
              + "</div>");
      panelSoundCredits.add(item);
    }
  }

  private String findMappedEvents(Map<SoundEvent, SoundAsset> assets, String assetId) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<SoundEvent, SoundAsset> e : assets.entrySet()) {
      if (!assetId.equals(e.getValue().getAssetId())) {
        continue;
      }
      if (sb.length() != 0) {
        sb.append(", ");
      }
      sb.append(e.getKey().name());
    }
    return sb.length() == 0 ? "-" : sb.toString();
  }
}
