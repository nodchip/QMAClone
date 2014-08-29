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

import tv.dyndns.kishibe.qmaclone.client.Controller;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.Utility;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSettingChat extends VerticalPanel implements ClickHandler {
	private static final String CHAT = "CHAT";
	private static PanelSettingChat instance = new PanelSettingChat();
	private final FlowPanel panelIgnoreUserCodes = new FlowPanel();
	private final Map<Button, Integer> buttonToIgnoreUserCode = new HashMap<Button, Integer>();
	private final RadioButton radioButtonChatOn = new RadioButton(CHAT, "オン");
	private final RadioButton radioButtonChatOff = new RadioButton(CHAT, "オフ");

	public static PanelSettingChat getInstance() {
		return instance;
	}

	private PanelSettingChat() {
		setHorizontalAlignment(ALIGN_CENTER);
		{
			final VerticalPanel panel = new VerticalPanel();
			if (UserData.get().isChatEnabled()) {
				radioButtonChatOn.setValue(true);
			} else {
				radioButtonChatOff.setValue(true);
			}

			radioButtonChatOn.addClickHandler(this);
			radioButtonChatOff.addClickHandler(this);

			panel.add(new HTML("▼チャットの表示"));
			panel.add(radioButtonChatOn);
			panel.add(radioButtonChatOff);
			add(panel);

		}
		{
			final VerticalPanel panel = new VerticalPanel();
			panel.add(new HTML("▼ボタンを押すと発言非表示を解除します。"));
			panel.add(panelIgnoreUserCodes);
			add(panel);
		}
	}

	public void addIgnoreUserCodeButton(int userCode) {
		// すでに追加されていたら何もしない
		if (buttonToIgnoreUserCode.containsValue(userCode)) {
			return;
		}

		final Button button = new Button(Utility.makeTrip(userCode), this);
		panelIgnoreUserCodes.add(button);
		buttonToIgnoreUserCode.put(button, userCode);
	}

	// private void removeIgnoreUserCodeButton(Button button) {
	// final int ignoreUserCode = buttonToIgnoreUserCode.get(button);
	// panelIgnoreUserCodes.remove(button);
	// buttonToIgnoreUserCode.remove(button);
	// }

	private void changeChatEnabled(boolean enabled) {
		Controller.getInstance().setChatEnabled(enabled);
		UserData.get().setChatEnabled(enabled);
	}

	@Override
	public void onClick(ClickEvent event) {
		final Object sender = event.getSource();
		// if (buttonToIgnoreUserCode.containsKey(sender)) {
		// removeIgnoreUserCodeButton((Button) sender);
		// } else
		if (sender == radioButtonChatOn) {
			changeChatEnabled(true);
		} else if (sender == radioButtonChatOff) {
			changeChatEnabled(false);
		}
	}
}
