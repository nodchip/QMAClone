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
package tv.dyndns.kishibe.qmaclone.client.game.left;

import tv.dyndns.kishibe.qmaclone.client.game.left.AnswerPopup.ChoiceMarkType;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.user.client.ui.AbsolutePanel;

public class AnswerPopupFactory {
	private final AbsolutePanel absolutePanel;

	public AnswerPopupFactory(AbsolutePanel absolutePanel) {
		this.absolutePanel = absolutePanel;
	}

	public AnswerPopup get(PacketProblem problem) {
		switch (problem.type) {
		case Marubatsu: {
			if (problem.imageChoice) {
				return new AnswerPopupMark(absolutePanel, problem, ChoiceMarkType.Alpha);
			} else {
				return new AnswerPopupCommon(absolutePanel);
			}
		}
		case YonTaku: {
			return new AnswerPopupMark(absolutePanel, problem, ChoiceMarkType.Digit);
		}
		case Rensou: {
			return new AnswerPopupMark(absolutePanel, problem, ChoiceMarkType.Alpha);
		}
		case Hayaimono:
		case Narabekae:
		case MojiPanel:
		case Typing:
		case Flash:
		case Effect:
		case Slot:
		case Tegaki: {
			return new AnswerPopupCommon(absolutePanel);
		}
		case Tato: {
			return new AnswerPopupTato(absolutePanel, problem);
		}
		case Junban: {
			return new AnswerPopupJunban(absolutePanel, problem);
		}
		case Senmusubi: {
			return new AnswerPopupSenmusubi(absolutePanel, problem);
		}
		case Click: {
			return new AnswerPopupClick(absolutePanel);
		}
		case Group: {
			return new AnswerPopupGroup(absolutePanel, problem);
		}
		default:
			break;
		}

		return null;
	}
}
