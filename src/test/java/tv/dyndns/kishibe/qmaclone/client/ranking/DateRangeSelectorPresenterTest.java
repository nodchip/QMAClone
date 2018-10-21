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
import tv.dyndns.kishibe.qmaclone.client.packet.PacketMonth;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class DateRangeSelectorPresenterTest {

	private static final int FAKE_YEAR = 2112;
	private static final int FAKE_MONTH = 9;
	private static final List<PacketMonth> FAKE_MONTHS = ImmutableList.of(new PacketMonth(
			FAKE_YEAR, FAKE_MONTH));

	@Mock
	private ServiceAsync mockService;
	@Mock
	private ThemeRankingPresenter mockThemeRankingPresenter;
	@Mock
	private DateRangeSelectorPresenter.View mockView;
	private DateRangeSelectorPresenter presenter;

	@Before
	public void setUp() throws Exception {
		presenter = new DateRangeSelectorPresenter(mockService, mockThemeRankingPresenter);
		presenter.setView(mockView);
	}

	@After
	public void tearDown() throws Exception {
		verify(mockService).getThemeRankingDateRanges(presenter.callbackGetThemeRankingDateRanges);
	}

	@Test
	public void callbackGetThemeRankingDateRangesShouldSetDateRangeToView() {
		presenter.callbackGetThemeRankingDateRanges.onSuccess(FAKE_MONTHS);

		verify(mockView).setDateRange(FAKE_MONTHS);
	}

	@Test
	public void onAllSelectedShouldDelegateToThemeRankingPresenter() {
		presenter.onAllSelected();

		verify(mockThemeRankingPresenter).onAllSelected();
	}

	@Test
	public void onOldSelectedShouldDelegateToThemeRankingPresenter() {
		presenter.onOldSelected();

		verify(mockThemeRankingPresenter).onOldSelected();
	}

	@Test
	public void testOnYearSelected() {
		presenter.onYearSelected(FAKE_YEAR);

		verify(mockThemeRankingPresenter).onYearSelected(FAKE_YEAR);
	}

	@Test
	public void testOnMonthSelected() {
		presenter.onMonthSelected(FAKE_YEAR, FAKE_MONTH);

		verify(mockThemeRankingPresenter).onMonthSelected(FAKE_YEAR, FAKE_MONTH);
	}

}
