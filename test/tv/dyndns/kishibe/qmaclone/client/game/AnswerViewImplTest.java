package tv.dyndns.kishibe.qmaclone.client.game;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;

public class AnswerViewImplTest extends QMACloneGWTTestCaseBase {
	private AnswerViewImpl view;

	@Test
	public void testSetGetWithoutFrame() {
		view = new AnswerViewImpl(2, 4, false);
		assertEquals("", view.get());
		assertEquals("", view.getText());

		view.set("あ", false);
		assertEquals("あ", view.get());
		assertEquals("あ", view.getText());
	}

	@Test
	public void testSetGetWithFrame() {
		view = new AnswerViewImpl(2, 4, true);
		assertEquals("", view.get());
		assertEquals("□□", view.getText());

		view.set("あ", true);
		assertEquals("あ", view.get());
		assertEquals("あ□", view.getText());
	}

	@Test
	public void testSetGetLongString() {
		view = new AnswerViewImpl(2, 4, false);

		view.set("あいう", false);
		assertEquals("あい", view.get());
		assertEquals("あいう", view.getText());

		view.set("あいうえお", false);
		assertEquals("あい", view.get());
		assertEquals("あいうえ", view.getText());
	}

	@Test
	public void testGetRaw() {
		view = new AnswerViewImpl(2, 4, false);

		view.set("あいう", false);
		assertEquals("あい", view.get());
		assertEquals("あいう", view.getText());
		assertEquals("あいう", view.getRaw());

		view.set("あいうえお", false);
		assertEquals("あい", view.get());
		assertEquals("あいうえ", view.getRaw());
	}
}
