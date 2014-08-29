package tv.dyndns.kishibe.qmaclone.client.ui;

import org.junit.Test;

import tv.dyndns.kishibe.qmaclone.client.QMACloneGWTTestCaseBase;
import tv.dyndns.kishibe.qmaclone.client.util.HasIndex;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WidgetMultiItemSelectorTest extends QMACloneGWTTestCaseBase {
	private static enum Example implements HasIndex {
		A, B, C;
		@Override
		public int getIndex() {
			return ordinal();
		}
	}

	private WidgetMultiItemSelector<Example> selector;

	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		selector = new WidgetMultiItemSelector<WidgetMultiItemSelectorTest.Example>("タイトル",
				Example.values(), 2);
	}

	@Test
	public void testWidgetMultiItemSelector() {
		assertEquals(VerticalPanel.ALIGN_CENTER, selector.getHorizontalAlignment());
		assertEquals(3, selector.listBox.getItemCount());
		assertEquals("A", selector.listBox.getItemText(0));
		assertEquals("B", selector.listBox.getItemText(1));
		assertEquals("C", selector.listBox.getItemText(2));
		assertEquals(0, selector.listBox.getSelectedIndex());
		assertEquals("複数選択", selector.checkBoxMultiSelect.getText());
		assertFalse(selector.isMultiSelect());
		assertTrue(selector.listBox.isVisible());
		assertFalse(selector.panelMultiSelect.isVisible());
	}

	@Test
	public void testSetOneItem() {
		selector.set(ImmutableSet.of(Example.B));
		assertEquals(1, selector.listBox.getSelectedIndex());
		assertFalse(selector.isMultiSelect());
		assertTrue(selector.listBox.isVisible());
		assertFalse(selector.panelMultiSelect.isVisible());
	}

	@Test
	public void testSetMultipleItems() {
		selector.set(ImmutableSet.of(Example.B, Example.C));
		assertFalse(selector.listBox.isVisible());
		assertTrue(selector.panelMultiSelect.isVisible());
		assertFalse(selector.checkBoxs[0].getValue());
		assertTrue(selector.checkBoxs[1].getValue());
		assertTrue(selector.checkBoxs[1].getValue());
		assertTrue(selector.isMultiSelect());
	}

	@Test
	public void testGet() {
		ImmutableSet<Example> set = ImmutableSet.of(Example.A, Example.C);
		selector.set(set);
		assertEquals(set, selector.get());
	}

	@Test
	public void testSetEnabled() {
		selector.setEnabled(false);
		assertFalse(selector.listBox.isEnabled());
		assertFalse(selector.checkBoxMultiSelect.isEnabled());
		assertFalse(selector.checkBoxs[0].isEnabled());
		assertFalse(selector.checkBoxs[1].isEnabled());
		assertFalse(selector.checkBoxs[2].isEnabled());

		selector.setEnabled(true);
		assertTrue(selector.listBox.isEnabled());
		assertTrue(selector.checkBoxMultiSelect.isEnabled());
		assertTrue(selector.checkBoxs[0].isEnabled());
		assertTrue(selector.checkBoxs[1].isEnabled());
		assertTrue(selector.checkBoxs[2].isEnabled());

	}
}
