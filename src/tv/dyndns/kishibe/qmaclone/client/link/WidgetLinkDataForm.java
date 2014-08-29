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
package tv.dyndns.kishibe.qmaclone.client.link;

import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WidgetLinkDataForm extends VerticalPanel {
	private static final String TEXT_NEW_LINK = "新規リンク情報入力中";
	private static final String TEXT_HOME_PAGE_NAME = "(ここにホームページ名を入力してください)";
	private static final String TEXT_AUTHOR_NAME = "(ここにホームページの管理者名を入力してください)";
	private static final String TEXT_URL = "(ここにホームページのURLを入力してください)";
	private static final String TEXT_BANNER_URL = "(ここにホームページのバナー画像のURLを入力してください)";
	private static final String TEXT_DESCRIPTION = "(ここにホームページの説明文を入力してください)";
	private int id = Integer.MIN_VALUE;
	private final Label labelId = new Label(TEXT_NEW_LINK);
	private final TextBox textBoxHomePageName = new TextBox();
	private final TextBox textBoxAuthorName = new TextBox();
	private final TextBox textBoxUrl = new TextBox();
	private final TextBox textBoxBannerUrl = new TextBox();
	private final TextArea textAreaDescription = new TextArea();
	private final VerticalPanel panelWarning = new VerticalPanel();
	private final ClickHandler textBoxClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			final Widget sender = (Widget) event.getSource();
			if (sender instanceof TextBoxBase) {
				TextBoxBase textBox = (TextBoxBase) sender;
				textBox.selectAll();
			}
		}
	};

	public WidgetLinkDataForm() {
		add(labelId);

		final Grid grid = new Grid(5, 2);
		grid.addStyleName("gridFrame");
		grid.addStyleName("gridFontNormal");
		add(grid);
		final CellFormatter cellFormatter = grid.getCellFormatter();
		for (int row = 0; row < 5; ++row) {
			cellFormatter.setHorizontalAlignment(row, 0, ALIGN_RIGHT);
		}

		grid.setText(0, 0, "ホームページ名");
		grid.setText(1, 0, "管理者名");
		grid.setText(2, 0, "ホームページURL");
		grid.setText(3, 0, "バナー画像URL");
		grid.setText(4, 0, "説明文");

		textBoxHomePageName.setMaxLength(32);
		textBoxHomePageName.setWidth("400px");
		textBoxHomePageName.addClickHandler(textBoxClickHandler);

		textBoxAuthorName.setMaxLength(16);
		textBoxAuthorName.setWidth("400px");
		textBoxAuthorName.addClickHandler(textBoxClickHandler);

		textBoxUrl.setMaxLength(256);
		textBoxUrl.setWidth("400px");
		textBoxUrl.addClickHandler(textBoxClickHandler);

		textBoxBannerUrl.setMaxLength(256);
		textBoxBannerUrl.setWidth("400px");
		textBoxBannerUrl.addClickHandler(textBoxClickHandler);

		textAreaDescription.setCharacterWidth(60);
		textAreaDescription.addClickHandler(textBoxClickHandler);

		grid.setWidget(0, 1, textBoxHomePageName);
		grid.setWidget(1, 1, textBoxAuthorName);
		grid.setWidget(2, 1, textBoxUrl);
		grid.setWidget(3, 1, textBoxBannerUrl);
		grid.setWidget(4, 1, textAreaDescription);

		add(panelWarning);

		clearFrom();
	}

	public boolean isValid() {
		boolean result = true;
		panelWarning.clear();

		final String textHomePageName = textBoxHomePageName.getText();
		if (textHomePageName.length() == 0 || textHomePageName.equals(TEXT_HOME_PAGE_NAME)) {
			result = false;
			addWarningMessage("ホームページ名を入力してください");
		}

		final String textAuthorName = textBoxAuthorName.getText();
		if (textAuthorName.length() == 0 || textAuthorName.equals(TEXT_AUTHOR_NAME)) {
			result = false;
			addWarningMessage("ホームページの管理者名を入力してください");
		}

		final String textUrl = textBoxUrl.getText();
		if (textUrl.length() == 0 || textUrl.equals(TEXT_URL)) {
			result = false;
			addWarningMessage("ホームページのURLを入力してください");
		}

		final String textBannerUrl = textBoxBannerUrl.getText();
		if (textBannerUrl.length() == 0 || textBannerUrl.equals(TEXT_BANNER_URL)) {
			result = false;
			addWarningMessage("ホームページのバナー画像URLを入力してください");
		}

		final String textDescription = textAreaDescription.getText();
		if (textDescription.length() == 0 || textDescription.equals(TEXT_DESCRIPTION)) {
			result = false;
			addWarningMessage("ホームページの説明文を入力してください");
		}

		return result;
	}

	private void addWarningMessage(final String message) {
		final Label label = new Label(message);
		label.addStyleDependentName("linkFormWarning");
		panelWarning.add(label);
	}

	public PacketLinkData getLinkData() {
		if (!isValid()) {
			return null;
		}

		final PacketLinkData linkData = new PacketLinkData();
		linkData.id = id;
		linkData.homePageName = textBoxHomePageName.getText();
		linkData.authorName = textBoxAuthorName.getText();
		linkData.url = textBoxUrl.getText();
		linkData.bannerUrl = textBoxBannerUrl.getText();
		linkData.description = textAreaDescription.getText();
		linkData.userCode = UserData.get().getUserCode();
		return linkData;
	}

	public void setLinkData(PacketLinkData linkData) {
		id = linkData.id;
		labelId.setText("ID" + id + "のリンクを修正中");
		textBoxHomePageName.setText(linkData.homePageName);
		textBoxAuthorName.setText(linkData.authorName);
		textBoxUrl.setText(linkData.url);
		textBoxBannerUrl.setText(linkData.bannerUrl);
		textAreaDescription.setText(linkData.description);
	}

	public void clearFrom() {
		id = Integer.MIN_VALUE;
		labelId.setText(TEXT_NEW_LINK);
		textBoxHomePageName.setText(TEXT_HOME_PAGE_NAME);
		textBoxAuthorName.setText(TEXT_AUTHOR_NAME);
		textBoxUrl.setText(TEXT_URL);
		textBoxBannerUrl.setText(TEXT_BANNER_URL);
		textAreaDescription.setText(TEXT_DESCRIPTION);
	}

	public void setEnabled(boolean enabled) {
		textBoxHomePageName.setEnabled(enabled);
		textBoxAuthorName.setEnabled(enabled);
		textBoxUrl.setEnabled(enabled);
		textBoxBannerUrl.setEnabled(enabled);
		textAreaDescription.setEnabled(enabled);
	}
}
