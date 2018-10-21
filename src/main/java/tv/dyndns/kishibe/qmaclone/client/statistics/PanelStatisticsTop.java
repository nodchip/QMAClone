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
package tv.dyndns.kishibe.qmaclone.client.statistics;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;

public class PanelStatisticsTop extends HTML {
	private static final String DESCRIPTION = "←のメニューから設定したい項目を選んでください"
			+ "<dl>"
			+ "<dt>問題数</dt><dd>プレイヤーの皆様から投稿された問題の問題数をジャンル・出題形式別に表示します</dd>"
			+ "<dt>ジャンル別正解率</dt><dd>ジャンル別に問題正解率の分布を表示します</dd>"
			+ "<dt>プレイヤー正解率</dt><dd>プレイヤーが解いた問題の正解率をジャンル・出題形式別に表示します</dd>"
			+ "<dt>県別平均トップレーティング</dt><dd>各県のトッププレイヤーたちの平均レーティングを表示します</dd>"
			+ "<dt>レーティング履歴</dt><dd>プレイヤーのレーティング遷移を表示します</dd>"
			+ "<dt>レーティング分布</dt><dd>レーティングの分布状況を表示します</dd>" + "</dl>";

	public PanelStatisticsTop() {
		super(SafeHtmlUtils.fromSafeConstant(DESCRIPTION));
		setWidth("600px");
	}
}
