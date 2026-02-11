package tv.dyndns.kishibe.qmaclone.client.ranking;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;

import com.google.common.collect.ImmutableList;

public class ThemeSelectorPresenterTest {

	private static final String FAKE_THEME = "fake THEME";
	private static final List<List<String>> FAKE_THEMES = ImmutableList
			.<List<String>> of(ImmutableList.of(FAKE_THEME));
	@Mock
	private ServiceAsync mockService;
	@Mock
	private ThemeRankingPresenter mockThemeRankingPresenter;
	@Mock
	private ThemeSelectorPresenter.View mockView;
	private AutoCloseable closeableMocks;
	private ThemeSelectorPresenter presenter;

	@BeforeEach
	public void setUp() throws Exception {
		closeableMocks = MockitoAnnotations.openMocks(this);
		presenter = new ThemeSelectorPresenter(mockService, mockThemeRankingPresenter);
		presenter.setView(mockView);
	}

	@AfterEach
	public void tearDown() throws Exception {
		verify(mockService).getThemeModeThemes(presenter.callbackGetThemeModeThemes);
		closeableMocks.close();
	}

	@Test
	public void onThemeSelectedShouldDelegateToThemeRankingPresenter() {
		presenter.onThemeSelected(FAKE_THEME);

		verify(mockThemeRankingPresenter).onThemeSelected(FAKE_THEME);
	}

	@Test
	public void callbackGetThemeModeThemesShouldSetThemeToView() {
		presenter.callbackGetThemeModeThemes.onSuccess(FAKE_THEMES);

		verify(mockView).setTheme(FAKE_THEMES);
	}

}
