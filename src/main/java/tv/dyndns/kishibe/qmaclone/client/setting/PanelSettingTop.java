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

import com.google.gwt.user.client.ui.HTML;

public class PanelSettingTop extends HTML {
	public PanelSettingTop() {
		super("<div class='settingTopRoot'>"
				+ "<div class='settingTopHero'>"
				+ "<h2 class='settingTopTitle'>設定ガイド</h2>"
				+ "<p class='settingTopLead'>左メニューから、変更したい項目を選んでください。"
				+ "保存できる設定と、管理系の設定をここからまとめて確認できます。</p>"
				+ "</div>"
				+ "<div class='settingTopGrid'>"
				+ "<div class='settingTopItem'><span class='settingTopName'>アイコン</span>"
				+ "<span class='settingTopDesc'>プロフィール用アイコンのアップロードを行います。</span></div>"
				+ "<div class='settingTopItem'><span class='settingTopName'>ユーザーコード</span>"
				+ "<span class='settingTopDesc'>ユーザーコードの切り替えと連携設定を行います。</span></div>"
				+ "<div class='settingTopItem'><span class='settingTopName'>正答率統計</span>"
				+ "<span class='settingTopDesc'>登録問題一覧にある問題の登録解除を行います。</span></div>"
				+ "<div class='settingTopItem'><span class='settingTopName'>チャット</span>"
				+ "<span class='settingTopDesc'>無視設定した発言者の解除を行います。</span></div>"
				+ "<div class='settingTopItem'><span class='settingTopName'>その他</span>"
				+ "<span class='settingTopDesc'>効果音など、プレイ環境に関する設定を変更します。</span></div>"
				+ "</div>"
				+ "<p class='settingTopHint'>必要な項目を選ぶと、右側に設定内容が表示されます。</p>"
				+ "</div>", true);
		addStyleName("settingTopPanel");
	}
}
