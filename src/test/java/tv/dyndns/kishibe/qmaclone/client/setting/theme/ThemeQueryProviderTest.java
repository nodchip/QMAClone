package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingThemeQuery;

import com.google.gwt.view.client.HasData;

@RunWith(MockitoJUnitRunner.class)
public class ThemeQueryProviderTest {

	private static final String FAKE_THEME = "AAA";
	@Mock
	private PanelSettingThemeQuery mockPresenter;
	@Mock
	private HasData<PacketThemeQuery> mockHasData;
	private ThemeQueryProvider themeQueryProvider;

	@Before
	public void setUp() throws Exception {
		themeQueryProvider = new ThemeQueryProvider(mockPresenter, FAKE_THEME);
	}

	@Test
	public void onRangeChangedShouldCallPresenter() {
		themeQueryProvider.onRangeChanged(mockHasData);

		verify(mockPresenter).onThemeQueryRequested(FAKE_THEME, mockHasData);
	}

	@Test
	public void testRefresh() {
		themeQueryProvider.onRangeChanged(mockHasData);

		verify(mockPresenter).onThemeQueryRequested(FAKE_THEME, mockHasData);
	}

}
