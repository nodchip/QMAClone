package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Tests for {@link ThemeCell}.
 * 
 * @author nodchip
 */
public class ThemeCellTest {

	private static final String FAKE_THEME = "AAA";
	private static final int FAKE_NUMBER_OF_PROBLEMS = 111;
	private PacketTheme fakeTheme;
	private ThemeCell themeCell;

	@BeforeEach
	public void setUp() throws Exception {
		fakeTheme = new PacketTheme();
		fakeTheme.setName(FAKE_THEME);
		fakeTheme.setNumberOfProblems(FAKE_NUMBER_OF_PROBLEMS);
		themeCell = new ThemeCell();
	}

	@Test
	public void renderShouldAddThemeAndNumberOfProblems() {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		themeCell.render(null, fakeTheme, sb);

		assertThat(sb.toSafeHtml().asString(), containsString(FAKE_THEME));
		assertThat(sb.toSafeHtml().asString(),
				containsString(String.valueOf(FAKE_NUMBER_OF_PROBLEMS)));
	}

}
