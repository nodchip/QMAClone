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
package tv.dyndns.kishibe.qmaclone.client.packet;

import java.util.Date;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketProblemMinimum implements IsSerializable {
	/**
	 * 問題番号
	 */
	public int id;
	/**
	 * ジャンル
	 */
	public ProblemGenre genre;
	/**
	 * 出題形式
	 */
	public ProblemType type;
	/**
	 * 正答数
	 */
	public int good;
	/**
	 * 誤答数
	 */
	public int bad;
	/**
	 * ランダムフラグ
	 */
	public RandomFlag randomFlag;
	/**
	 * 作問者文字列ハッシュ
	 */
	public int creatorHash;
	/**
	 * 作問者ユーザーコード
	 */
	public int userCode;
	/**
	 * 指摘日時。指摘されていない場合はnull。
	 */
	public Date indication;

	public int getAccuracyRate() {
		if (good == 0 && bad == 0) {
			return -1;
		}
		return (100 * good) / (good + bad);
	}

	public double getNormalizedAccuracyRate() {
		return type.getNormalizedAccuracyRate(this);
	}

	public boolean isNew() {
		return good + bad < Constant.MAX_RATIO_CALCULATING;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PacketProblemMinimum)) {
			return false;
		}
		PacketProblemMinimum rh = (PacketProblemMinimum) obj;
		return Objects.equal(id, rh.id) && Objects.equal(genre, rh.genre)
				&& Objects.equal(type, rh.type) && Objects.equal(good, rh.good)
				&& Objects.equal(bad, rh.bad) && Objects.equal(randomFlag, rh.randomFlag)
				&& Objects.equal(creatorHash, rh.creatorHash)
				&& Objects.equal(userCode, rh.userCode) && Objects.equal(indication, rh.indication);
	}

	@Override
	public int hashCode() {
		// [userCode, creatorHash, shuffledAnswers,
		// shuffledChoices]はハッシュコードに含めない
		return Objects.hashCode(id, genre, type, good, bad, randomFlag);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", id).add("genre", genre).add("type", type)
				.add("good", good).add("bad", bad).add("randomFlag", randomFlag)
				.add("creatorHash", creatorHash).add("userCode", userCode)
				.add("indication", indication).toString();
	}
}
