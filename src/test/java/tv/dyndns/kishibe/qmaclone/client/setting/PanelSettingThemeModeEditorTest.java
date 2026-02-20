package tv.dyndns.kishibe.qmaclone.client.setting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeModeEditor.ThemeModeEditorStatus;

public class PanelSettingThemeModeEditorTest {

	@Test
	public void sortEditorsShouldSortByStatusAndUserCode() {
		List<PacketThemeModeEditor> input = Lists.newArrayList(
				createEditor(300, "gamma", ThemeModeEditorStatus.Refected),
				createEditor(200, "beta", ThemeModeEditorStatus.Applying),
				createEditor(100, "alpha", ThemeModeEditorStatus.Accepted),
				createEditor(150, "delta", ThemeModeEditorStatus.Applying));

		List<PacketThemeModeEditor> sorted = ThemeModeEditorUiSupport.sortEditors(input);

		assertEquals(ThemeModeEditorStatus.Applying, sorted.get(0).themeModeEditorStatus);
		assertEquals(150, sorted.get(0).userCode);
		assertEquals(ThemeModeEditorStatus.Applying, sorted.get(1).themeModeEditorStatus);
		assertEquals(200, sorted.get(1).userCode);
		assertEquals(ThemeModeEditorStatus.Accepted, sorted.get(2).themeModeEditorStatus);
		assertEquals(ThemeModeEditorStatus.Refected, sorted.get(3).themeModeEditorStatus);
	}

	@Test
	public void toStatusStyleShouldMapEachStatus() {
		assertEquals("settingAdminStatus settingAdminStatus--applying",
				ThemeModeEditorUiSupport.toStatusStyle(ThemeModeEditorStatus.Applying));
		assertEquals("settingAdminStatus settingAdminStatus--accepted",
				ThemeModeEditorUiSupport.toStatusStyle(ThemeModeEditorStatus.Accepted));
		assertEquals("settingAdminStatus settingAdminStatus--rejected",
				ThemeModeEditorUiSupport.toStatusStyle(ThemeModeEditorStatus.Refected));
	}

	private static PacketThemeModeEditor createEditor(int userCode, String name,
			ThemeModeEditorStatus status) {
		PacketThemeModeEditor editor = new PacketThemeModeEditor();
		editor.userCode = userCode;
		editor.name = name;
		editor.themeModeEditorStatus = status;
		return editor;
	}
}
