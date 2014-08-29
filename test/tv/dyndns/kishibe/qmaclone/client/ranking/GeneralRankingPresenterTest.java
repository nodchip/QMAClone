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
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class GeneralRankingPresenterTest {

	private static final List<List<PacketRankingData>> FAKE_RANKINGS = ImmutableList
			.<List<PacketRankingData>> of(ImmutableList.of(new PacketRankingData(111, 222,
					"fake name", "fake image file name", "fake data")));

	@Mock
	private ServiceAsync mockService;
	@Mock
	private GeneralRankingPresenter.View mockView;
	private GeneralRankingPresenter presenter;

	@Before
	public void setUp() throws Exception {
		presenter = new GeneralRankingPresenter(mockService);
		presenter.setView(mockView);
	}

	@After
	public void tearDown() throws Exception {
		verify(mockService).getGeneralRanking(presenter.callbackGetRankingData);
	}

	@Test
	public void setView() {
		presenter.callbackGetRankingData.onSuccess(FAKE_RANKINGS);

		verify(mockView).setRanking(FAKE_RANKINGS);
	}

}
