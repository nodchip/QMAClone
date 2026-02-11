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

public class GeneralRankingPresenterTest {

	private static final List<List<PacketRankingData>> FAKE_RANKINGS = ImmutableList
			.<List<PacketRankingData>> of(ImmutableList.of(new PacketRankingData(111, 222,
					"fake name", "fake image file name", "fake data")));

	@Mock
	private ServiceAsync mockService;
	@Mock
	private GeneralRankingPresenter.View mockView;
	private AutoCloseable closeableMocks;
	private GeneralRankingPresenter presenter;

	@BeforeEach
	public void setUp() throws Exception {
		closeableMocks = MockitoAnnotations.openMocks(this);
		presenter = new GeneralRankingPresenter(mockService);
		presenter.setView(mockView);
	}

	@AfterEach
	public void tearDown() throws Exception {
		verify(mockService).getGeneralRanking(presenter.callbackGetRankingData);
		closeableMocks.close();
	}

	@Test
	public void setView() {
		presenter.callbackGetRankingData.onSuccess(FAKE_RANKINGS);

		verify(mockView).setRanking(FAKE_RANKINGS);
	}

}
