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
package tv.dyndns.kishibe.qmaclone.client;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.report.ProblemReportUi;
import tv.dyndns.kishibe.qmaclone.client.ui.WidgetMultiItemSelector;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PanelSearchProblem extends VerticalPanel implements ClickHandler, KeyDownHandler {
  private static final Logger logger = Logger.getLogger(PanelSearchProblem.class.getName());
  private final Button buttonSearch = new Button("検索", this);
  private final TextBox textBoxQuery = new TextBox();
  private final TextBox textBoxCreator = new TextBox();
  private final ListBox listBoxCreatorMatching = new ListBox();
  private final WidgetMultiItemSelector<ProblemGenre> multiItemSelectorGenre = new WidgetMultiItemSelector<ProblemGenre>(
      "ジャンル", ProblemGenre.values(), 3);
  private final WidgetMultiItemSelector<ProblemType> multiItemSelectorType = new WidgetMultiItemSelector<ProblemType>(
      "出題形式", ProblemType.valuesWithoutRandom, 5);
  private final WidgetMultiItemSelector<RandomFlag> multiItemSelectorRandomFlag = new WidgetMultiItemSelector<RandomFlag>(
      "ランダム", RandomFlag.values(), 3);;
  private final ListBox listBoxMaxProblemsPerPage = new ListBox();
  private final SimplePanel panelGrid = new SimplePanel();
  private int maxProblemsPerPage;

  public PanelSearchProblem() {
    setWidth("800px");
    setHorizontalAlignment(ALIGN_CENTER);

    add(new Label("問題の検索を行います"));

    {
      textBoxQuery.addKeyDownHandler(this);
      textBoxCreator.addKeyDownHandler(this);

      textBoxQuery.setWidth("200px");
      textBoxCreator.setWidth("200px");
      listBoxMaxProblemsPerPage.setWidth("200px");

      listBoxMaxProblemsPerPage.addItem("10");
      listBoxMaxProblemsPerPage.addItem("100");
      listBoxMaxProblemsPerPage.addItem("1000");
      listBoxMaxProblemsPerPage.setSelectedIndex(1);

      final Grid grid = new Grid(3, 2);
      grid.addStyleName("gridFrame");
      grid.addStyleName("gridFontNormal");
      grid.setHTML(0, 0, "問題文");
      grid.setWidget(0, 1, textBoxQuery);
      grid.setHTML(1, 0, "問題作成者");
      grid.setWidget(1, 1, textBoxCreator);
      grid.setHTML(2, 0, "1ページあたりの表示問題数");
      grid.setWidget(2, 1, listBoxMaxProblemsPerPage);
      add(grid);
    }
    {
      listBoxCreatorMatching.addItem("完全一致");
      listBoxCreatorMatching.addItem("部分一致");

      final HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("問題作成者の検索方法"));
      panel.add(listBoxCreatorMatching);
      add(panel);
    }
    add(multiItemSelectorGenre);
    add(multiItemSelectorType);
    add(multiItemSelectorRandomFlag);

    add(new HTML(new SafeHtmlBuilder().appendEscapedLines(
        "複数の単語を空白で区切るとAND検索となります。\n" + "複数の単語を「OR」で区切るとOR検索となります。「OR」は大文字で入力してください。\n"
            + "単語の先頭に「-」(ハイフン)を加えると除外検索となります。").toSafeHtml()));

    add(buttonSearch);
    add(panelGrid);
  }

  private void search() {
    if (SharedData.get().getIsPlaying()) {
      return;
    }

    String query = textBoxQuery.getText();
    if (query == null) {
      query = "";
    }
    query = query.replaceAll("  ", " ");

    String creator = textBoxCreator.getText();
    if (creator == null) {
      creator = "";
    }
    creator = creator.replaceAll("　", " ");

    maxProblemsPerPage = Integer.parseInt(listBoxMaxProblemsPerPage
        .getItemText(listBoxMaxProblemsPerPage.getSelectedIndex()));

    setEnabled(false);

    final Set<ProblemGenre> genres = multiItemSelectorGenre.get();
    final Set<ProblemType> types = multiItemSelectorType.get();
    final Set<RandomFlag> randomFlags = multiItemSelectorRandomFlag.get();
    final boolean creatorPerfectMatching = listBoxCreatorMatching.getSelectedIndex() == 0;

    Service.Util.getInstance().searchProblem(query, creator, creatorPerfectMatching, genres, types,
        randomFlags, callbackSearchProblem);
  }

  private final AsyncCallback<List<PacketProblem>> callbackSearchProblem = new AsyncCallback<List<PacketProblem>>() {
    public void onSuccess(List<PacketProblem> result) {
      panelGrid.setWidget(new ProblemReportUi(result, true, true, maxProblemsPerPage));
      setEnabled(true);
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "問題の検索に失敗しました", caught);
      setEnabled(true);
    }
  };

  private void setEnabled(boolean enabled) {
    buttonSearch.setEnabled(enabled);
    textBoxQuery.setEnabled(enabled);
    textBoxCreator.setEnabled(enabled);
    multiItemSelectorGenre.setEnabled(enabled);
    multiItemSelectorType.setEnabled(enabled);
    multiItemSelectorRandomFlag.setEnabled(enabled);
    listBoxMaxProblemsPerPage.setEnabled(enabled);
    listBoxCreatorMatching.setEnabled(enabled);
  }

  @Override
  public void onClick(ClickEvent event) {
    final Object sender = event.getSource();
    if (sender == buttonSearch) {
      search();
    }
  }

  @Override
  public void onKeyDown(KeyDownEvent event) {
    final Object sender = event.getSource();
    if (sender == textBoxQuery || sender == textBoxCreator) {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        search();
      }
    }
  }
}
