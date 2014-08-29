package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingThemeQuery;

import com.google.gwt.view.client.HasData;

/**
 * Tests for {@link ThemeProvider}.
 * 
 * @author nodchip
 */
@RunWith(MockitoJUnitRunner.class)
public class ThemeProviderTest {

	@Mock
	private PanelSettingThemeQuery mockPresenter;
	@Mock
	private HasData<PacketTheme> mockHasData;
	private ThemeProvider themeProvider;

	@Before
	public void setUp() throws Exception {
		themeProvider = new ThemeProvider(mockPresenter);
	}

	@Test
	public void onRangeChangedShouldCallPresenter() {
		themeProvider.onRangeChanged(mockHasData);

		verify(mockPresenter).onThemeRequested(mockHasData);
	}

	@Test
	public void refreshShouldCallPresenter() {
		themeProvider.onRangeChanged(mockHasData);

		verify(mockPresenter).onThemeRequested(mockHasData);
	}

}
