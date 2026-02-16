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
	private static final String DESCRIPTION = "<div class='statisticsTopIntro'>左メニューから表示したい統計項目を選んでください。</div>"
			+ "<div class='statisticsTopTiles'>"
			+ "<section class='statisticsTopTile'><h3 class='statisticsTopTileTitle'>問題数</h3>"
			+ "<p class='statisticsTopTileDesc'>投稿された問題数をジャンル・出題形式別に表示します。</p>"
			+ "<p class='statisticsTopTileUse'>使いどころ: 問題在庫と出題形式の偏り確認</p></section>"
			+ "<section class='statisticsTopTile'><h3 class='statisticsTopTileTitle'>ジャンル別正解率</h3>"
			+ "<p class='statisticsTopTileDesc'>ジャンルごとの正解率分布を確認できます。</p>"
			+ "<p class='statisticsTopTileUse'>使いどころ: 難易度バランスの把握</p></section>"
			+ "<section class='statisticsTopTile'><h3 class='statisticsTopTileTitle'>プレイヤー正解率</h3>"
			+ "<p class='statisticsTopTileDesc'>プレイヤーが解いた問題の正解率を集計表示します。</p>"
			+ "<p class='statisticsTopTileUse'>使いどころ: 得意/苦手ジャンル分析</p></section>"
			+ "<section class='statisticsTopTile'><h3 class='statisticsTopTileTitle'>県別平均トップレーティング</h3>"
			+ "<p class='statisticsTopTileDesc'>各県トップ層の平均レーティングを比較できます。</p>"
			+ "<p class='statisticsTopTileUse'>使いどころ: 地域別の競技レベル把握</p></section>"
			+ "<section class='statisticsTopTile'><h3 class='statisticsTopTileTitle'>レーティング履歴</h3>"
			+ "<p class='statisticsTopTileDesc'>プレイヤーのレーティング推移を時系列で確認できます。</p>"
			+ "<p class='statisticsTopTileUse'>使いどころ: 直近パフォーマンスの推移確認</p></section>"
			+ "<section class='statisticsTopTile'><h3 class='statisticsTopTileTitle'>レーティング分布</h3>"
			+ "<p class='statisticsTopTileDesc'>全体のレーティング分布を確認できます。</p>"
			+ "<p class='statisticsTopTileUse'>使いどころ: 現在の実力帯の位置確認</p></section>"
			+ "</div>";

	public PanelStatisticsTop() {
		super(SafeHtmlUtils.fromSafeConstant(DESCRIPTION));
		addStyleName("statisticsCard");
		addStyleName("statisticsTopCard");
		setWidth("100%");
	}
}
