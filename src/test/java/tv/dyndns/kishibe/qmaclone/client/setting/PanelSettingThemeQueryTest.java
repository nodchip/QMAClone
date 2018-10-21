package tv.dyndns.kishibe.qmaclone.client.setting;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tv.dyndns.kishibe.qmaclone.client.ServiceAsync;
import tv.dyndns.kishibe.qmaclone.client.UserData;

import com.google.gwt.core.client.Scheduler;

@RunWith(MockitoJUnitRunner.class)
public class PanelSettingThemeQueryTest {

	private static final int FAKE_USER_CODE = 111;
	private static final int FAKE_NUMBER_OF_THEME_QUERIES = 222;
	private static final String FAKE_THEME_1 = "fake THEME 1";
	private static final String FAKE_THEME_2 = "fake THEME 2";
	private static final String FAKE_QUERY_1 = "fake query 1";
	private static final String FAKE_QUERY_2 = "fake query 2";
	private static final String FAKE_NOTE = "fake note";
	private static final int FAKE_START = 333;
	private static final int FAKE_LENGTH = 444;

	@Mock
	private PanelSettingThemeQuery.View mockView;
	@Mock
	private ServiceAsync mockService;
	@Mock
	private Scheduler mockScheduler;

	private PanelSettingThemeQuery panel;

	@Before
	public void setUp() throws Exception {
		UserData.get().setUserCode(FAKE_USER_CODE);
		panel = new PanelSettingThemeQuery(mockView, mockService, mockScheduler);
	}

	@After
	public void tearDown() throws Exception {
		verify(mockService).isThemeModeEditor(FAKE_USER_CODE, panel.callbackIsThemeModeEditor);
	}

	@Test
	public void commandUpdateFormShouldUpdateForm() {
		when(mockView.getTheme()).thenReturn("");
		when(mockView.isAttached()).thenReturn(true);

		assertTrue(panel.commandUpdateForm.execute());

		verify(mockView).enableForm(false);
	}

	@Test
	public void callbackIsThemeModeEditorShouldCallGetNumberOfThemeQueriesIfAccepted() {
		panel.callbackIsThemeModeEditor.onSuccess(true);

		verify(mockService).getNumberofThemeQueries(panel.callbackGetNumberofThemeQueries);
	}

	@Test
	public void callbackIsThemeModeEditorShouldShowHaveRightAndCheckApplying() {
		UserData.get().setPlayCount(1000);
		UserData.get().setUserCode(FAKE_USER_CODE);

		panel.callbackIsThemeModeEditor.onSuccess(false);

		verify(mockView).showHaveRight();
		verify(mockService).isApplyingThemeModeEditor(FAKE_USER_CODE,
				panel.callbackIsApplyingThemeModeEditor);
	}

	@Test
	public void callbackIsThemeModeEditorShouldShowNoRight() {
		UserData.get().setPlayCount(0);

		panel.callbackIsThemeModeEditor.onSuccess(false);

		verify(mockView).showNoRight();
	}

	@Test
	public void callbackIsApplyingThemeModeEditorShouldShowApplying() {
		panel.callbackIsApplyingThemeModeEditor.onSuccess(true);

		verify(mockView).showApplyingRight();
	}

	@Test
	public void callbackIsApplyingThemeModeEditorShouldShowApplyRightForm() {
		panel.callbackIsApplyingThemeModeEditor.onSuccess(false);

		verify(mockView).showApplyRightForm();
	}

	@Test
	public void callbackGetNumberofThemeQueriesShouldShowEditForm() {
		panel.callbackGetNumberofThemeQueries.onSuccess(FAKE_NUMBER_OF_THEME_QUERIES);

		verify(mockView).showEditForm(FAKE_NUMBER_OF_THEME_QUERIES);
	}

	@Test
	public void onAddButtonClickedShouldAddThemeModeQuery() {
		UserData.get().setUserCode(FAKE_USER_CODE);
		when(mockView.getTheme()).thenReturn(FAKE_THEME_1);
		when(mockView.getQuery()).thenReturn(FAKE_QUERY_1);

		panel.onAddButtonClicked();

		verify(mockService).addThemeModeQuery(FAKE_THEME_1, FAKE_QUERY_1, FAKE_USER_CODE,
				panel.callbackUpdateThemeModeQuery);
	}

	@Test
	public void onRemoveButtonClickedShouldRemoveThemeModeQuery() {
		UserData.get().setUserCode(FAKE_USER_CODE);
		when(mockView.getTheme()).thenReturn(FAKE_THEME_1);
		when(mockView.getQuery()).thenReturn(FAKE_QUERY_1);

		panel.onRemoveButtonClicked();

		verify(mockService).removeThemeModeQuery(FAKE_THEME_1, FAKE_QUERY_1, FAKE_USER_CODE,
				panel.callbackUpdateThemeModeQuery);
	}

	@Test
	public void onApplyButtonClickedShouldDoNothingIfNotConfirmed() {
		when(mockView.confirmToApply()).thenReturn(false);

		panel.onApplyButtonClicked();

		verify(mockView, never()).notifyApplying();
	}

	@Test
	public void onApplyButtonClickedShouldApplyThemeModeEditor() {
		UserData.get().setUserCode(FAKE_USER_CODE);
		when(mockView.confirmToApply()).thenReturn(true);
		when(mockView.getNote()).thenReturn(FAKE_NOTE);

		panel.onApplyButtonClicked();

		verify(mockService).applyThemeModeEditor(FAKE_USER_CODE, FAKE_NOTE,
				panel.callbackApplyThemeModeEditor);
		verify(mockView).notifyApplying();
	}

	@Test
	public void callbackApplyThemeModeEditorShouldDoNothing() {
		panel.callbackApplyThemeModeEditor.onSuccess(null);
	}

	@Test
	public void onViewLoadedShouldShouldScheduleCommandUpdateForm() {
		panel.onViewLoaded();

		verify(mockScheduler).scheduleFixedDelay(panel.commandUpdateForm,
				PanelSettingThemeQuery.UPDATE_PERIOD);
	}

	@Test
	public void onThemeAndQueryCopiedShouldSetThemeAndQuery() {
		panel.onThemeQuerySelected(FAKE_THEME_1, FAKE_QUERY_1);

		verify(mockView).setTheme(FAKE_THEME_1);
		verify(mockView).setQuery(FAKE_QUERY_1);
	}

}
