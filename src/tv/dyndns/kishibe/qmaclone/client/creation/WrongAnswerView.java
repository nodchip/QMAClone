package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketWrongAnswer;

import com.google.gwt.user.client.ui.IsWidget;

interface WrongAnswerView extends IsWidget {

	void setAnswer(List<PacketWrongAnswer> wrongAnswers);

}