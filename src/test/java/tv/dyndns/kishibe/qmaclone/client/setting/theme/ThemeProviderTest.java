package tv.dyndns.kishibe.qmaclone.client.setting.theme;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketTheme;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingThemeQuery;

import com.google.gwt.view.client.HasData;

/**
 * Tests for {@link ThemeProvider}.
 * 
 * @author nodchip
 */
public class ThemeProviderTest {

	@Mock
	private PanelSettingThemeQuery mockPresenter;
	@Mock
	private HasData<PacketTheme> mockHasData;
	private AutoCloseable closeableMocks;
	private ThemeProvider themeProvider;

	@BeforeEach
	public void setUp() throws Exception {
		closeableMocks = MockitoAnnotations.openMocks(this);
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

	@org.junit.jupiter.api.AfterEach
	public void tearDown() throws Exception {
		closeableMocks.close();
	}

}
