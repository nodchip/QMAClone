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
package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ValidatorTegaki extends Validator {
	private static final Logger logger = Logger.getLogger(ValidatorTegaki.class.getName());

	// サーバー側でRPCが呼ばれるのを防ぐため、staticな内部クラスにする
	public static class AvailableCharacters {
		public static AvailableCharacters INSTANCE;
		private Set<String> availableCharacters;

		public static AvailableCharacters get() {
			if (INSTANCE == null) {
				INSTANCE = new AvailableCharacters();
			}
			return INSTANCE;
		}

		public AvailableCharacters() {
			initialize();
		}

		public void initialize() {
			Service.Util.getInstance().getAvailableChalactersForHandwriting(
					callbackGetAvailableChalacters);
		}

		private final AsyncCallback<String> callbackGetAvailableChalacters = new AsyncCallback<String>() {
			public void onSuccess(String result) {
				availableCharacters = Sets.newHashSet();
				for (int i = 0; i < result.length(); ++i) {
					availableCharacters.add(result.substring(i, i + 1));
				}
			}

			public void onFailure(Throwable caught) {
				logger.log(Level.WARNING, "手書きクイズで使用可能な文字の取得に失敗しました", caught);
			}
		};

		public boolean isInitialized() {
			return availableCharacters != null;
		}

		private boolean isAvailable(String ch) {
			Preconditions.checkState(isInitialized());
			return availableCharacters.contains(ch);
		}
	}

	public Evaluation check(PacketProblem problem) {
		Evaluation eval = super.check(problem);
		List<String> warn = eval.warn;

		if (!AvailableCharacters.get().isInitialized()) {
			warn.add("使用可能な文字リストが取得されていません。しばらくお待ちください。");
			return eval;
		}

		int numberOfAnswers = problem.getNumberOfAnswers();
		if (numberOfAnswers == 0) {
			warn.add("解答を入力してください");
		}

		for (int answerIndex = 0; answerIndex < numberOfAnswers; ++answerIndex) {
			String answer = problem.answers[answerIndex];
			for (int characterIndex = 0; characterIndex < answer.length(); ++characterIndex) {
				String s = answer.substring(characterIndex, characterIndex + 1);

				if (!AvailableCharacters.get().isAvailable(s)) {
					warn.add((answerIndex + 1) + "番目の解答" + (characterIndex + 1) + "文字目「" + s
							+ "」は使用できません。");
				}
			}
		}

		return eval;
	}
}
