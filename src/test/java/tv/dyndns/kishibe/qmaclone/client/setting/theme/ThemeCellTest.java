package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Tests for {@link ThemeCell}.
 * 
 * @author nodchip
 */
@RunWith(JUnit4.class)
public class ThemeCellTest {

	private static final String FAKE_THEME = "AAA";
	private static final int FAKE_NUMBER_OF_PROBLEMS = 111;
	private PacketTheme fakeTheme;
	private ThemeCell themeCell;

	@Before
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
