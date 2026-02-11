package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketThemeQuery;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingThemeQuery;

import com.google.gwt.view.client.HasData;

public class ThemeQueryProviderTest {

	private static final String FAKE_THEME = "AAA";
	@Mock
	private PanelSettingThemeQuery mockPresenter;
	@Mock
	private HasData<PacketThemeQuery> mockHasData;
	private AutoCloseable closeableMocks;
	private ThemeQueryProvider themeQueryProvider;

	@BeforeEach
	public void setUp() throws Exception {
		closeableMocks = MockitoAnnotations.openMocks(this);
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

	@org.junit.jupiter.api.AfterEach
	public void tearDown() throws Exception {
		closeableMocks.close();
	}

}
