package tv.dyndns.kishibe.qmaclone.client.ranking;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
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
	private ThemeSelectorPresenter presenter;

	@Before
	public void setUp() throws Exception {
		presenter = new ThemeSelectorPresenter(mockService, mockThemeRankingPresenter);
		presenter.setView(mockView);
	}

	@After
	public void tearDown() throws Exception {
		verify(mockService).getThemeModeThemes(presenter.callbackGetThemeModeThemes);
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
