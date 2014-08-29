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
package tv.dyndns.kishibe.qmaclone.client.game.panel;

import java.util.Map;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.game.WidgetTimeProgressBar;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.collect.ImmutableMap;

public class QuestionPanelFactory {
	private static interface Builder {
		QuestionPanel build(PacketProblem problem, SessionData sessionData);
	}

	private static final Map<ProblemType, Builder> problemTypeToBuilder = new ImmutableMap.Builder<ProblemType, QuestionPanelFactory.Builder>()
			.put(ProblemType.Marubatsu, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelMaruBatu(problem, sessionData);
				}
			}).put(ProblemType.YonTaku, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanel4Taku(problem, sessionData);
				}
			}).put(ProblemType.Rensou, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelRensou(problem, sessionData);
				}
			}).put(ProblemType.Narabekae, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelNarabekae(problem, sessionData);
				}
			}).put(ProblemType.MojiPanel, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelMojiPanel(problem, sessionData);
				}
			}).put(ProblemType.Typing, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelTyping(problem, sessionData);
				}
			}).put(ProblemType.Flash, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelCube(problem, sessionData);
				}
			}).put(ProblemType.Effect, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelEffect(problem, sessionData);
				}
			}).put(ProblemType.Tato, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelTato(problem, sessionData);
				}
			}).put(ProblemType.Junban, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelJunban(problem, sessionData);
				}
			}).put(ProblemType.Senmusubi, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelSenmusubi(problem, sessionData);
				}
			}).put(ProblemType.Slot, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelSlot(problem, sessionData);
				}
			}).put(ProblemType.Click, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelClick(problem, sessionData);
				}
			}).put(ProblemType.Tegaki, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelTegaki(problem, sessionData);
				}
			}).put(ProblemType.Hayaimono, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelHayaimono(problem, sessionData);
				}
			}).put(ProblemType.Group, new Builder() {
				@Override
				public QuestionPanel build(PacketProblem problem, SessionData sessionData) {
					return new QuestionPanelGroup(problem, sessionData);
				}
			}).build();

	private QuestionPanelFactory() {
	}

	public static QuestionPanel create(PacketProblem problem,
			WidgetTimeProgressBar widgetTimeProgressBar, SessionData sessionData) {
		QuestionPanel panelQuestion = problemTypeToBuilder.get(problem.type).build(problem,
				sessionData);

		if (widgetTimeProgressBar != null) {
			panelQuestion.setWidgetTimeProgressBar(widgetTimeProgressBar);
		}

		return panelQuestion;
	}

}
