package tv.dyndns.kishibe.qmaclone.client.ranking;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.common.collect.ImmutableList;

public class ThemeRankingPresenterTest {

	private static final String FAKE_THEME = "fake THEME";
	private static final int FAKE_YEAR = 2112;
	private static final int FAKE_MONTH = 9;
	private static final List<PacketRankingData> FAKE_RANKING = ImmutableList
			.of(new PacketRankingData());

	@Mock
	private ThemeRankingPresenter.View mockView;
	@Mock
	private ServiceAsync mockService;
	private AutoCloseable closeableMocks;
	private ThemeRankingPresenter presenter;

	@BeforeEach
	public void setUp() throws Exception {
		closeableMocks = MockitoAnnotations.openMocks(this);
		presenter = new ThemeRankingPresenter(mockService);
		presenter.setView(mockView);
	}

	@Test
	public void onOldSelectedShouldTriggerRpc() {
		presenter.onThemeSelected(FAKE_THEME);
		presenter.onOldSelected();

		verify(mockService).getThemeRankingOld(FAKE_THEME, presenter.callbackGetThemeRanking);
	}

	@Test
	public void onAllSelectedShouldTriggerRpc() {
		presenter.onThemeSelected(FAKE_THEME);
		presenter.onAllSelected();

		verify(mockService).getThemeRankingAll(FAKE_THEME, presenter.callbackGetThemeRanking);
	}

	@Test
	public void onYearSelectedShouldTriggerRpc() {
		presenter.onThemeSelected(FAKE_THEME);
		presenter.onYearSelected(FAKE_YEAR);

		verify(mockService).getThemeRanking(FAKE_THEME, FAKE_YEAR,
				presenter.callbackGetThemeRanking);
	}

	@Test
	public void onMonthSelectedShouldTriggerRpc() {
		presenter.onThemeSelected(FAKE_THEME);
		presenter.onMonthSelected(FAKE_YEAR, FAKE_MONTH);

		verify(mockService).getThemeRanking(FAKE_THEME, FAKE_YEAR, FAKE_MONTH,
				presenter.callbackGetThemeRanking);
	}

	@Test
	public void callbackGetThemeRankingShouldSetRankingToView() {
		presenter.callbackGetThemeRanking.onSuccess(FAKE_RANKING);

		verify(mockView).setRanking(FAKE_RANKING);
	}

	@AfterEach
	public void tearDown() throws Exception {
		closeableMocks.close();
	}

}
