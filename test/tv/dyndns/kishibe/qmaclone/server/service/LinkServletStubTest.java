package tv.dyndns.kishibe.qmaclone.server.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tv.dyndns.kishibe.qmaclone.client.testing.TestDataProvider.getLinkData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.service.ServiceException;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class LinkServletStubTest {

	private static final int FAKE_START = 123;
	private static final int FAKE_COUNT = 234;
	private static final int FAKE_NUMBER_OF_LINK_DATA = 345;
	private static final int FAKE_LINK_DATA_ID = 456;

	@Mock
	private Database mockDatabase;
	private LinkServletStub service;

	@Before
	public void setUp() throws Exception {
		service = new LinkServletStub(mockDatabase);
	}

	@Test
	public void getShouldReturnListOfLinkData() throws Exception {
		when(mockDatabase.getLinkDatas(FAKE_START, FAKE_COUNT)).thenReturn(
				ImmutableList.of(getLinkData()));

		assertEquals(ImmutableList.of(getLinkData()), service.get(FAKE_START, FAKE_COUNT));
	}

	@Test(expected = ServiceException.class)
	public void getShouldThrowServiceExceptionOnDatabaseException() throws Exception {
		when(mockDatabase.getLinkDatas(FAKE_START, FAKE_COUNT)).thenThrow(new DatabaseException());

		service.get(FAKE_START, FAKE_COUNT);
	}

	@Test
	public void getNumberOfLinkDataShouldReturnResult() throws Exception {
		when(mockDatabase.getNumberOfLinkDatas()).thenReturn(FAKE_NUMBER_OF_LINK_DATA);

		assertEquals(FAKE_NUMBER_OF_LINK_DATA, service.getNumberOfLinkData());
	}

	@Test(expected = ServiceException.class)
	public void getNumberOfLinkDataShouldServiceExceptionOnDatabaseException() throws Exception {
		when(mockDatabase.getNumberOfLinkDatas()).thenThrow(new DatabaseException());

		service.getNumberOfLinkData();
	}

	@Test
	public void addShouldCallAdd() throws Exception {
		service.add(getLinkData());

		verify(mockDatabase).addLinkData(getLinkData());
	}

	@Test(expected = ServiceException.class)
	public void addShouldServiceExceptionOnDatabaseException() throws Exception {
		doThrow(new DatabaseException()).when(mockDatabase).addLinkData(getLinkData());

		service.add(getLinkData());
	}

	@Test
	public void updateShouldCallAdd() throws Exception {
		service.update(getLinkData());

		verify(mockDatabase).updateLinkData(getLinkData());
	}

	@Test(expected = ServiceException.class)
	public void updateShouldServiceExceptionOnDatabaseException() throws Exception {
		doThrow(new DatabaseException()).when(mockDatabase).updateLinkData(getLinkData());

		service.update(getLinkData());
	}

	@Test
	public void removeShouldCallAdd() throws Exception {
		service.remove(FAKE_LINK_DATA_ID);

		verify(mockDatabase).removeLinkData(FAKE_LINK_DATA_ID);
	}

	@Test(expected = ServiceException.class)
	public void removeShouldServiceExceptionOnDatabaseException() throws Exception {
		doThrow(new DatabaseException()).when(mockDatabase).removeLinkData(FAKE_LINK_DATA_ID);

		service.remove(FAKE_LINK_DATA_ID);
	}

}
