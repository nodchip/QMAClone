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
package tv.dyndns.kishibe.qmaclone.client.game.sentence;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

public class WidgetProblemSentenceCube extends WidgetProblemSentenceNormal {
	private final Grid grid = new Grid(1, 1);
	private final Label label = new Label("　");
	private String answer = "";
	private int array[];
	private final RepeatingCommand commandUpdate = new RepeatingCommand() {
		@Override
		public boolean execute() {
			changeLabel();
			return isAttached();
		}
	};

	public WidgetProblemSentenceCube(PacketProblem problem) {
		super(problem);

		answer = problem.shuffledAnswers[0].trim();

		if ((answer.length() & 1) != 0) {
			answer += "■";
		}
		array = new int[answer.length()];
		for (int i = 0; i < answer.length(); ++i) {
			array[i] = i;
		}

		label.addStyleDependentName("cubeHint");
		grid.setWidget(0, 0, label);
		add(grid);
		setCellHorizontalAlignment(grid, ALIGN_CENTER);
		setCellWidth(grid, "600px");

		changeLabel();
	}

	private void changeLabel() {
		for (int i = 0; i < array.length; ++i) {
			int index = Random.nextInt(array.length);
			int temp = array[i];
			array[i] = array[index];
			array[index] = temp;
		}

		int length = answer.length() / 2;
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; ++i) {
			int index = array[i];
			sb.append(answer.charAt(index));
		}
		label.setText(sb.toString());
	}

	protected void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleFixedPeriod(commandUpdate, 600);
	}
}
