package tv.dyndns.kishibe.qmaclone.client.game;

import com.google.gwt.user.client.ui.IsWidget;

public interface AnswerView extends IsWidget {
	/**
	 * 解答文字列を設定する
	 * 
	 * @param s
	 *            解答
	 * @param fillWithFrame
	 *            □で埋める場合は{@code true}
	 */
	void set(String s, boolean fillWithFrame);

	/**
	 * 解答文字列を取得する。文字列は解答欄の長さで切られる。
	 * 
	 * @return　解答文字列
	 */
	String get();

	/**
	 * 解答文字列を取得する。文字列の長さは切られない。
	 * 
	 * @return
	 */
	String getRaw();
}
