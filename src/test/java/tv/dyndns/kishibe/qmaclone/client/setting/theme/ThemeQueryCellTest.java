package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Tests for {@link ThemeQueryCell}
 * 
 * @author nodchip
 */
public class ThemeQueryCellTest {

	private static final String FAKE_THEME = "AAA";
	private static final String FAKE_QUERY = "BBB";
	private PacketThemeQuery fakeThemeQuery;
	private ThemeQueryCell themeQueryCell;

	@BeforeEach
	public void setUp() throws Exception {
		fakeThemeQuery = new PacketThemeQuery();
		fakeThemeQuery.setTheme(FAKE_THEME);
		fakeThemeQuery.setQuery(FAKE_QUERY);
		themeQueryCell = new ThemeQueryCell();
	}

	@Test
	public void renderShouldRenderTheme() {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		themeQueryCell.render(null, fakeThemeQuery, sb);

		assertThat(sb.toSafeHtml().asString(), containsString(FAKE_QUERY));
	}

}
