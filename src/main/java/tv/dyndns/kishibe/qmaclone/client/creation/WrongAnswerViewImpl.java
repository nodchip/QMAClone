package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.ComboChart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;

public class WrongAnswerViewImpl extends Composite implements WrongAnswerView {

	private static WrongAnswerViewImplUiBinder uiBinder = GWT
			.create(WrongAnswerViewImplUiBinder.class);

	interface WrongAnswerViewImplUiBinder extends UiBinder<Widget, WrongAnswerViewImpl> {
	}

	@UiField
	SimplePanel panel;
	@UiField
	HTML html;

	public WrongAnswerViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setAnswer(List<PacketWrongAnswer> wrongAnswers) {
		setChart(wrongAnswers);
		setHtml(wrongAnswers);
	}

	private void setChart(List<PacketWrongAnswer> wrongAnswers) {
		DataTable data = DataTable.create();
		data.addColumn(ColumnType.STRING, "誤解答");
		data.addColumn(ColumnType.NUMBER, "回答数");
		data.addRows(wrongAnswers.size());
		for (int row = 0; row < wrongAnswers.size(); ++row) {
			PacketWrongAnswer wrongAnswer = wrongAnswers.get(row);
			data.setValue(row, 0, wrongAnswer.answer);
			data.setValue(row, 1, wrongAnswer.count);
		}

		Options options = Options.create();
		options.setWidth(780);
		options.setHeight(400);
		options.setTitle("誤解答例");

		panel.setWidget(new PieChart(data, options));
	}

	private void setHtml(List<PacketWrongAnswer> wrongAnswers) {
		// 文字列化
		List<String> formatteds = Lists.newArrayList();
		for (PacketWrongAnswer wrongAnswer : wrongAnswers) {
			formatteds.add(wrongAnswer.answer + "(" + wrongAnswer.count + ")");
		}

		String joined = Joiner.on(' ').join(formatteds);
		html.setHTML(SafeHtmlUtils.fromString(joined));
	}

}
