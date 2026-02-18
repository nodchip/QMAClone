package tv.dyndns.kishibe.qmaclone.client.game.input;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * 画像タト選択時のスタイル名定義を検証する。
 */
public class InputWidgetTatoStyleNameTest {
	/**
	 * 選択状態のスタイル名は非選択と異なる専用クラスを参照する。
	 */
	@Test
	public void imageSelectedStyleNameUsesDedicatedClass() throws Exception {
		String source = Files.readString(Paths.get(
				"src/main/java/tv/dyndns/kishibe/qmaclone/client/game/input/InputWidgetTato.java"),
				StandardCharsets.UTF_8);
		assertTrue(source.contains(
				"private static final String STYLE_NAME_IMAGE_SELECTED = \"gwt-Button-tatoImageSelected\";"));
	}
}
