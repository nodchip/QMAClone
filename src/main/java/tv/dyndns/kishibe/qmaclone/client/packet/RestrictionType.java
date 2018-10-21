package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.gwt.user.client.rpc.IsSerializable;

// ////////////////////////////////////////////////////////////////////////
// 制限ユーザー
// ////////////////////////////////////////////////////////////////////////
public enum RestrictionType implements IsSerializable {
	/**
	 * チャット
	 * 
	 * 自分の発言が他の人から見えなくなる
	 */
	CHAT(0),
	/**
	 * 問題投稿
	 * 
	 * 新規問題投稿時に偽の投稿完了メッセージが表示されるが、実際には問題が記録されない
	 * 
	 * 問題変更は通常通り可能
	 */
	PROBLEM_SUBMITTION(1),
	/**
	 * 問題指摘
	 * 
	 * 指摘時にエラーメッセージが表示される
	 */
	INDICATION(2),
	/**
	 * 対戦
	 * 
	 * レーティング更新時に最下位として計算する
	 */
	MATCH(3),
	/**
	 * 掲示板
	 * 
	 * 掲示板に書き込めなくなる
	 */
	BBS(4),

	/**
	 * 投票
	 * 
	 * 投票できなくなる
	 */
	VOTE(5);
	private final int value;

	private RestrictionType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}