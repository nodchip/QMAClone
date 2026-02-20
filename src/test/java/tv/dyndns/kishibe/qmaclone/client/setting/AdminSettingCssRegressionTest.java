package tv.dyndns.kishibe.qmaclone.client.setting;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class AdminSettingCssRegressionTest {

	@Test
	public void themeModeEditorActionButtonsShouldTargetRenderedTableCellButtons() throws IOException {
		String css = readQmaCloneCss();

		assertTrue(css.contains(".settingAdminThemeModeTable td button,"));
		assertTrue(css.contains(".settingAdminThemeModeTable td button:hover,"));
	}

	@Test
	public void restrictedUserInputRowShouldAlignTextboxAndButtonsWithSameHeight() throws IOException {
		String css = readQmaCloneCss();

		assertTrue(css.contains(".settingAdminRestrictedInputRow .gwt-TextBox,"));
		assertTrue(css.contains(".settingAdminRestrictedInputRow .gwt-Button {"));
		assertTrue(css.contains("height: 34px;"));
		assertTrue(css.contains("box-sizing: border-box;"));
	}

	private String readQmaCloneCss() throws IOException {
		Path cssPath = Paths.get("src", "main", "webapp", "QMAClone.css");
		return Files.readString(cssPath, StandardCharsets.UTF_8);
	}
}
