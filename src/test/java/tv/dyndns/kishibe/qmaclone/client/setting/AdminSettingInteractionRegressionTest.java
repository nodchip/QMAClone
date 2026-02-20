package tv.dyndns.kishibe.qmaclone.client.setting;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class AdminSettingInteractionRegressionTest {

	@Test
	public void themeModeEditorTableShouldDisableKeyboardSelectionHighlight() throws IOException {
		String source = readSource("CellTableThemeModeEditor.java");

		assertTrue(source.contains("setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);"));
	}

	@Test
	public void restrictedUserTableShouldReflectSelectionToInputBoxes() throws IOException {
		String source = readSource("PanelSettingRestrictedUserView.java");

		assertTrue(source.contains("userCodeTable.setSelectionModel(userCodeSelectionModel);"));
		assertTrue(source
				.contains("remoteAddressTable.setSelectionModel(remoteAddressSelectionModel);"));
		assertTrue(source.contains("userCodeSelectionModel.addSelectionChangeHandler"));
		assertTrue(source.contains("remoteAddressSelectionModel.addSelectionChangeHandler"));
	}

	private String readSource(String fileName) throws IOException {
		Path sourcePath = Paths.get("src", "main", "java", "tv", "dyndns", "kishibe", "qmaclone",
				"client", "setting", fileName);
		return Files.readString(sourcePath, StandardCharsets.UTF_8);
	}
}
