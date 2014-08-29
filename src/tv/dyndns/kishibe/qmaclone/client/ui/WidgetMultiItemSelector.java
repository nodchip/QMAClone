//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.client.ui;

import java.util.List;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.util.HasIndex;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link Set} を生成する {@link Widget}. 指定する {@link Enum}
 * はtoString()をオーバーライドして表示に使用されるラベルを返すようにしなければならない。
 * 
 * @author nodchip
 * @param <T>
 *            列挙型
 */
public class WidgetMultiItemSelector<T extends Enum<T> & HasIndex> extends VerticalPanel implements
		ClickHandler {
	@VisibleForTesting
	final ListBox listBox = new ListBox();
	@VisibleForTesting
	final CheckBox checkBoxMultiSelect = new CheckBox("複数選択");
	@VisibleForTesting
	CheckBox[] checkBoxs;
	@VisibleForTesting
	final HorizontalPanel panelMultiSelect = new HorizontalPanel();
	private T[] items;

	public WidgetMultiItemSelector(String title, T[] items, int columns) {
		this.items = items;

		setHorizontalAlignment(ALIGN_CENTER);

		// 上段
		{
			listBox.setWidth("150px");
			for (T item : items) {
				listBox.addItem(item.toString());
			}

			checkBoxMultiSelect.addClickHandler(this);

			final HorizontalPanel panel = new HorizontalPanel();
			panel.setVerticalAlignment(ALIGN_MIDDLE);
			panel.add(listBox);
			panel.add(checkBoxMultiSelect);
			add(panel);
		}

		// 下段
		{
			final VerticalPanel[] panels = new VerticalPanel[columns];
			for (int i = 0; i < panels.length; ++i) {
				panels[i] = new VerticalPanel();
				panelMultiSelect.add(panels[i]);
			}

			checkBoxs = new CheckBox[items.length];
			for (int itemIndex = 0; itemIndex < items.length; ++itemIndex) {
				checkBoxs[itemIndex] = new CheckBox(items[itemIndex].toString());
				panels[itemIndex / ((items.length + columns - 1) / columns)]
						.add(checkBoxs[itemIndex]);
			}
			add(panelMultiSelect);
		}

		checkBoxMultiSelect.setValue(false);
		listBox.setSelectedIndex(0);

		set(ImmutableSet.of(items[0]));
	}

	public void set(Set<T> set) {
		if (set.isEmpty()) {
			// 空集合なら0個目を選択する
			checkBoxMultiSelect.setValue(false);
			listBox.setSelectedIndex(0);

		} else if (set.size() == 1) {
			// 1つのみならそれを選択する
			checkBoxMultiSelect.setValue(false);
			listBox.setSelectedIndex(set.iterator().next().getIndex());

		} else {
			checkBoxMultiSelect.setValue(true);
			for (CheckBox checkBox : checkBoxs) {
				checkBox.setValue(false);
			}
			for (T element : set) {
				checkBoxs[element.getIndex()].setValue(true);
			}
		}

		updateWidget();
	}

	public Set<T> get() {
		final boolean multiSelect = isMultiSelect();

		if (!multiSelect) {
			Set<T> set = Sets.newHashSet();
			set.add(items[listBox.getSelectedIndex()]);
			return set;
		}

		List<T> list = Lists.newArrayList();
		for (int checkBoxIndex = 0; checkBoxIndex < checkBoxs.length; ++checkBoxIndex) {
			if (checkBoxs[checkBoxIndex].getValue()) {
				list.add(items[checkBoxIndex]);
			}
		}
		return Sets.newHashSet(list);
	}

	private void updateWidget() {
		final boolean multiSelect = isMultiSelect();

		listBox.setVisible(!multiSelect);
		panelMultiSelect.setVisible(multiSelect);
	}

	public boolean isMultiSelect() {
		return checkBoxMultiSelect.getValue();
	}

	public void setEnabled(boolean enabled) {
		listBox.setEnabled(enabled);
		checkBoxMultiSelect.setEnabled(enabled);
		for (CheckBox checkBox : checkBoxs) {
			checkBox.setEnabled(enabled);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		final Widget sender = (Widget) event.getSource();
		if (sender == checkBoxMultiSelect) {
			updateWidget();
		}
	}
}
