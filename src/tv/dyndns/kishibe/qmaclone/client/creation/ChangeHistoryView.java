package tv.dyndns.kishibe.qmaclone.client.creation;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemCreationLog;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * 問題変更履歴表示用のビューインターフェース
 * 
 * @author nodchip
 */
public interface ChangeHistoryView extends IsWidget {

	interface ChangeHistoryPresenter {
		/**
		 * 差分元または差分先が変更された際に呼ばれる
		 * 
		 * @param before
		 * @param after
		 */
		void onUpdateDiffTarget(PacketProblemCreationLog before, PacketProblemCreationLog after);
	}

	/**
	 * 問題作成ログを表示する
	 * 
	 * @param creationLog
	 */
	void setCreationLog(List<PacketProblemCreationLog> creationLog);

	/**
	 * 差分を表示する
	 * 
	 * @param html
	 */
	void setDiffHtml(SafeHtml html);

}
