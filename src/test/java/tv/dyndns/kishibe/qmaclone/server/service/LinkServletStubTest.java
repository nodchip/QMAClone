package tv.dyndns.kishibe.qmaclone.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider.getLinkData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tv.dyndns.kishibe.qmaclone.client.service.ServiceException;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LinkServletStubTest {

	private static final int FAKE_START = 123;
	private static final int FAKE_COUNT = 234;
	private static final int FAKE_NUMBER_OF_LINK_DATA = 345;
	private static final int FAKE_LINK_DATA_ID = 456;

	@Mock
	private Database mockDatabase;
	private LinkServletStub service;

	@BeforeEach
	public void setUp() throws Exception {
		service = new LinkServletStub(mockDatabase);
	}

	@Test
	public void getShouldReturnListOfLinkData() throws Exception {
		when(mockDatabase.getLinkDatas(FAKE_START, FAKE_COUNT)).thenReturn(
				ImmutableList.of(getLinkData()));

		assertEquals(ImmutableList.of(getLinkData()), service.get(FAKE_START, FAKE_COUNT));
	}

	@Test
	public void getShouldThrowServiceExceptionOnDatabaseException() throws Exception {
		when(mockDatabase.getLinkDatas(FAKE_START, FAKE_COUNT)).thenThrow(new DatabaseException());

		assertThrows(ServiceException.class, () -> service.get(FAKE_START, FAKE_COUNT));
	}

	@Test
	public void getNumberOfLinkDataShouldReturnResult() throws Exception {
		when(mockDatabase.getNumberOfLinkDatas()).thenReturn(FAKE_NUMBER_OF_LINK_DATA);

		assertEquals(FAKE_NUMBER_OF_LINK_DATA, service.getNumberOfLinkData());
	}

	@Test
	public void getNumberOfLinkDataShouldServiceExceptionOnDatabaseException() throws Exception {
		when(mockDatabase.getNumberOfLinkDatas()).thenThrow(new DatabaseException());

		assertThrows(ServiceException.class, () -> service.getNumberOfLinkData());
	}

	@Test
	public void addShouldCallAdd() throws Exception {
		service.add(getLinkData());

		verify(mockDatabase).addLinkData(getLinkData());
	}

	@Test
	public void addShouldServiceExceptionOnDatabaseException() throws Exception {
		doThrow(new DatabaseException()).when(mockDatabase).addLinkData(getLinkData());

		assertThrows(ServiceException.class, () -> service.add(getLinkData()));
	}

	@Test
	public void updateShouldCallAdd() throws Exception {
		service.update(getLinkData());

		verify(mockDatabase).updateLinkData(getLinkData());
	}

	@Test
	public void updateShouldServiceExceptionOnDatabaseException() throws Exception {
		doThrow(new DatabaseException()).when(mockDatabase).updateLinkData(getLinkData());

		assertThrows(ServiceException.class, () -> service.update(getLinkData()));
	}

	@Test
	public void removeShouldCallAdd() throws Exception {
		service.remove(FAKE_LINK_DATA_ID);

		verify(mockDatabase).removeLinkData(FAKE_LINK_DATA_ID);
	}

	@Test
	public void removeShouldServiceExceptionOnDatabaseException() throws Exception {
		doThrow(new DatabaseException()).when(mockDatabase).removeLinkData(FAKE_LINK_DATA_ID);

		assertThrows(ServiceException.class, () -> service.remove(FAKE_LINK_DATA_ID));
	}

}
