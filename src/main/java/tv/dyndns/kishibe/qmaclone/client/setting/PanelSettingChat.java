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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Controller;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.Utility;
import tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 設定画面のチャット関連設定を表示するパネル。
 */
public class PanelSettingChat extends VerticalPanel implements ClickHandler {
	private static final Logger logger = Logger.getLogger(PanelSettingChat.class.getName());
	private static final String CHAT = "CHAT";
	private static PanelSettingChat instance = new PanelSettingChat();
	private final FlowPanel panelIgnoreUserCodes = new FlowPanel();
	private final HTML htmlIgnoreUserCodeEmpty = new HTML("現在、発言を非表示にしているユーザーはいません。");
	private final Map<Button, Integer> buttonToIgnoreUserCode = new HashMap<Button, Integer>();
	private final RadioButton radioButtonChatOn = new RadioButton(CHAT, "オン");
	private final RadioButton radioButtonChatOff = new RadioButton(CHAT, "オフ");

	public static PanelSettingChat getInstance() {
		return instance;
	}

	/**
	 * チャット設定UIを初期化する。
	 */
	private PanelSettingChat() {
		setWidth("100%");
		setStyleName("settingChatRoot");
		{
			final VerticalPanel panel = new VerticalPanel();
			panel.setWidth("100%");
			panel.setStyleName("settingChatCard");
			if (UserData.get().isChatEnabled()) {
				radioButtonChatOn.setValue(true);
			} else {
				radioButtonChatOff.setValue(true);
			}

			radioButtonChatOn.addClickHandler(this);
			radioButtonChatOff.addClickHandler(this);

			panel.add(new HTML("<h3 class='settingChatCardTitle'>チャット表示</h3>"));
			panel.add(new HTML(
					"<p class='settingChatLead'>チャット欄を表示するかどうかを切り替えます。必要に応じて表示をオフにしてください。</p>"));
			radioButtonChatOn.addStyleName("settingChatOption");
			radioButtonChatOff.addStyleName("settingChatOption");
			final FlowPanel options = new FlowPanel();
			options.setStyleName("settingChatOptionGroup");
			options.add(radioButtonChatOn);
			options.add(radioButtonChatOff);
			panel.add(options);
			add(panel);

		}
		{
			final VerticalPanel panel = new VerticalPanel();
			panel.setWidth("100%");
			panel.setStyleName("settingChatCard");
			panel.add(new HTML("<h3 class='settingChatCardTitle'>発言非表示の解除</h3>"));
			panel.add(new HTML(
					"<p class='settingChatLead'>ユーザーコードのボタンを押すと、そのユーザーの発言非表示を解除します。</p>"));
			panelIgnoreUserCodes.setStyleName("settingChatIgnoredList");
			htmlIgnoreUserCodeEmpty.setStyleName("settingChatEmptyMessage");
			panelIgnoreUserCodes.add(htmlIgnoreUserCodeEmpty);
			panel.add(panelIgnoreUserCodes);
			add(panel);
		}
	}

	/**
	 * 発言非表示を解除するためのユーザーコードボタンを追加する。
	 */
	public void addIgnoreUserCodeButton(int userCode) {
		// すでに追加されていたら何もしない
		if (buttonToIgnoreUserCode.containsValue(userCode)) {
			return;
		}

		final Button button = new Button(Utility.makeTrip(userCode), this);
		button.setStyleName("settingChatIgnoredUserButton");
		buttonToIgnoreUserCode.put(button, userCode);
		panelIgnoreUserCodes.add(button);
		updateIgnoreUserCodeEmptyMessage();
	}

	// private void removeIgnoreUserCodeButton(Button button) {
	// final int ignoreUserCode = buttonToIgnoreUserCode.get(button);
	// panelIgnoreUserCodes.remove(button);
	// buttonToIgnoreUserCode.remove(button);
	// }

	/**
	 * 発言非表示ユーザーの登録を解除する。
	 */
	private void removeIgnoreUserCodeButton(final Button button) {
		final Integer ignoreUserCode = buttonToIgnoreUserCode.get(button);
		if (ignoreUserCode == null) {
			return;
		}

		Service.Util.getInstance().removeIgnoreUserCode(UserData.get().getUserCode(), ignoreUserCode,
				new RpcAsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						panelIgnoreUserCodes.remove(button);
						buttonToIgnoreUserCode.remove(button);
						updateIgnoreUserCodeEmptyMessage();
						SettingSaveToast.showSaved("発言非表示の解除");
					}

					@Override
					public void onFailureRpc(Throwable caught) {
						logger.log(Level.WARNING, "発言非表示の解除に失敗しました", caught);
					}
				});
	}

	private void changeChatEnabled(boolean enabled) {
		Controller.getInstance().setChatEnabled(enabled);
		UserData.get().setChatEnabled(enabled);
		UserData.get().save(callbackSaveChatEnabled);
	}

	/**
	 * 発言非表示ユーザー未登録時メッセージの表示状態を更新する。
	 */
	private void updateIgnoreUserCodeEmptyMessage() {
		htmlIgnoreUserCodeEmpty.setVisible(buttonToIgnoreUserCode.isEmpty());
	}

	private final AsyncCallback<Void> callbackSaveChatEnabled = new RpcAsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			SettingSaveToast.showSaved("チャット表示");
		}

		@Override
		public void onFailureRpc(Throwable caught) {
			logger.log(Level.WARNING, "チャット表示設定の保存に失敗しました", caught);
		}
	};

	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();
		if (buttonToIgnoreUserCode.containsKey(sender)) {
			removeIgnoreUserCodeButton((Button) sender);
		} else if (sender == radioButtonChatOn) {
			changeChatEnabled(true);
		} else if (sender == radioButtonChatOff) {
			changeChatEnabled(false);
		}
	}
}
