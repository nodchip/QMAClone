package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor.ThemeModeEditorStatus;

final class ThemeModeEditorUiSupport {

	private ThemeModeEditorUiSupport() {
	}

	static List<PacketThemeModeEditor> sortEditors(List<PacketThemeModeEditor> editors) {
		Collections.sort(editors, new Comparator<PacketThemeModeEditor>() {
			@Override
			public int compare(PacketThemeModeEditor left, PacketThemeModeEditor right) {
				int statusCompare = left.themeModeEditorStatus.compareTo(right.themeModeEditorStatus);
				if (statusCompare != 0) {
					return statusCompare;
				}
				return Integer.compare(left.userCode, right.userCode);
			}
		});
		return editors;
	}

	static String toStatusStyle(ThemeModeEditorStatus status) {
		String suffix = "unknown";
		if (status == ThemeModeEditorStatus.Applying) {
			suffix = "applying";
		} else if (status == ThemeModeEditorStatus.Accepted) {
			suffix = "accepted";
		} else if (status == ThemeModeEditorStatus.Refected) {
			suffix = "rejected";
		}
		return "settingAdminStatus settingAdminStatus--" + suffix;
	}

	static String toStatusLabel(ThemeModeEditorStatus status) {
		if (status == ThemeModeEditorStatus.Applying) {
			return "申請中";
		}
		if (status == ThemeModeEditorStatus.Accepted) {
			return "承認";
		}
		if (status == ThemeModeEditorStatus.Refected) {
			return "却下";
		}
		return "不明";
	}
}

