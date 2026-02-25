package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class WrongAnswerViewImpl extends Composite implements WrongAnswerView {

	private static WrongAnswerViewImplUiBinder uiBinder = GWT
			.create(WrongAnswerViewImplUiBinder.class);

	interface WrongAnswerViewImplUiBinder extends UiBinder<Widget, WrongAnswerViewImpl> {
	}

	@UiField
	HTML html;

	public WrongAnswerViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setAnswer(List<PacketWrongAnswer> wrongAnswers) {
		setHtml(wrongAnswers);
	}

	private void setHtml(List<PacketWrongAnswer> wrongAnswers) {
		StringBuilder builder = new StringBuilder();
		if (wrongAnswers.isEmpty()) {
			builder.append("<div class='creationWrongAnswerEmpty'>まだ誤答例はありません。</div>");
			html.setHTML(builder.toString());
			return;
		}

		builder.append("<div class='creationWrongAnswerBody'>");
		builder.append("<table class='creationWrongAnswerTable'>");
		builder.append(
				"<thead><tr><th class='creationWrongAnswerRank'>順位</th><th>誤答</th><th class='creationWrongAnswerCount'>件数</th></tr></thead>");
		builder.append("<tbody>");
		for (int i = 0; i < wrongAnswers.size(); ++i) {
			PacketWrongAnswer wrongAnswer = wrongAnswers.get(i);
			builder.append("<tr>");
			builder.append("<td class='creationWrongAnswerRank'>");
			builder.append(i + 1);
			builder.append("</td>");
			builder.append("<td class='creationWrongAnswerAnswer'>");
			builder.append(SafeHtmlUtils.htmlEscape(wrongAnswer.answer));
			builder.append("</td>");
			builder.append("<td class='creationWrongAnswerCount'>");
			builder.append(wrongAnswer.count);
			builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</tbody></table></div>");
		html.setHTML(builder.toString());
	}

}
