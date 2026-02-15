package tv.dyndns.kishibe.qmaclone.client.report;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Controller;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

/**
 * 問題一覧の共通テーブル。
 */
public class CellTableProblem extends CellTable<ProblemReportRow> {
  private static final long RESOLVED_CHECK_PERIOD = 7L * 24 * 60 * 60 * 1000;
  private static final long INDICATION_PERIOD = 30L * 24 * 60 * 60 * 1000;
  private static final Logger logger = Logger.getLogger(CellTableProblem.class.getName());
  private static final String STYLE_RATE_BADGE_BASE = "problemRateBadge";
  private static final String STYLE_RATE_BADGE_NEW = "problemRateBadge--new";
  private static final String STYLE_RATE_BADGE_L1 = "problemRateBadge--l1";
  private static final String STYLE_RATE_BADGE_L2 = "problemRateBadge--l2";
  private static final String STYLE_RATE_BADGE_L3 = "problemRateBadge--l3";
  private static final String STYLE_RATE_BADGE_L4 = "problemRateBadge--l4";
  private static final String STYLE_RATE_BADGE_L5 = "problemRateBadge--l5";
  private static final String SAFE_VALUE_NA = "-";
  private final ListDataProvider<ProblemReportRow> dataProvider = new ListDataProvider<ProblemReportRow>();
  private final ListHandler<ProblemReportRow> columnSortHandler;

  interface CellTableProblemTemplates extends SafeHtmlTemplates {
    @Template("<div class='gridFontSmall'>{0}</div>")
    SafeHtml smallFont(String text);

    @Template("<div class='problemReportSentenceText'>{0}</div>")
    SafeHtml sentenceFont(String text);

    @Template("<span class='{0} {1}'>{2}</span>")
    SafeHtml rateBadge(String baseClass, String levelClass, String text);

    @Template("")
    SafeHtml empty();

    @Template("<img src=\"{0}\" title=\"{1}\">")
    SafeHtml indication(SafeUri fileName, String title);
  }

  private static final CellTableProblemTemplates TEMPLATES =
      GWT.create(CellTableProblemTemplates.class);
  private static final SafeHtml SAFE_HTML_EXISTS = TEMPLATES.smallFont("有");

  /**
   * 共通問題テーブルを生成する。
   *
   * @param rows 問題行データ
   * @param regist 登録列の動作（追加/削除）を切り替えるフラグ
   * @param pageSize ページサイズ
   */
  public CellTableProblem(List<ProblemReportRow> rows, final boolean regist, int pageSize) {
    super(pageSize, CellTableProblemResources.Factory.get(), new ProvidesKey<ProblemReportRow>() {
      @Override
      public Object getKey(ProblemReportRow item) {
        if (item == null || item.problem == null) {
          return null;
        }
        return item.problem.id;
      }
    });

    dataProvider.setList(rows);
    dataProvider.addDataDisplay(this);
    columnSortHandler = new ListHandler<ProblemReportRow>(dataProvider.getList());
    addColumnSortHandler(columnSortHandler);

    // 問題番号
    addColumn("問題番号", new Comparator<ProblemReportRow>() {
      @Override
      public int compare(ProblemReportRow left, ProblemReportRow right) {
        return safeProblemId(left) - safeProblemId(right);
      }
    }, new LinkColumn<ProblemReportRow>() {
      @Override
      public String getValue(ProblemReportRow row) {
        PacketProblem problem = row.problem;
        return problem.testing ? "(出題中)" : Integer.toString(problem.id);
      }
    }, new FieldUpdater<ProblemReportRow, String>() {
      @Override
      public void update(int index, ProblemReportRow row, String value) {
        PacketProblem problem = row.problem;
        if (problem.testing) {
          return;
        }
        Controller.getInstance().showCreationProblem(problem.id);
      }
    });

    // 類似度
    addColumn("類似度", new Comparator<ProblemReportRow>() {
      @Override
      public int compare(ProblemReportRow left, ProblemReportRow right) {
        return Float.compare(safeSimilarity(left), safeSimilarity(right));
      }
    }, new SafeHtmlColumn<ProblemReportRow>() {
      @Override
      public SafeHtml getValue(ProblemReportRow row) {
        if (row.similarityScore == null) {
          return TEMPLATES.smallFont(SAFE_VALUE_NA);
        }
        return TEMPLATES.smallFont(formatSimilarity(row.similarityScore));
      }
    }, null);

    // ジャンル
    addColumn("ジャンル", new Comparator<ProblemReportRow>() {
      @Override
      public int compare(ProblemReportRow left, ProblemReportRow right) {
        return safeGenre(left).compareTo(safeGenre(right));
      }
    }, new SafeHtmlColumn<ProblemReportRow>() {
      @Override
      public SafeHtml getValue(ProblemReportRow row) {
        return TEMPLATES.smallFont(safeGenre(row));
      }
    }, null);

    // 出題形式
    addColumn("出題形式", new Comparator<ProblemReportRow>() {
      @Override
      public int compare(ProblemReportRow left, ProblemReportRow right) {
        return safeType(left).compareTo(safeType(right));
      }
    }, new SafeHtmlColumn<ProblemReportRow>() {
      @Override
      public SafeHtml getValue(ProblemReportRow row) {
        return TEMPLATES.smallFont(safeType(row));
      }
    }, null);

    // 問題文
    addColumn("問題文", new Comparator<ProblemReportRow>() {
      @Override
      public int compare(ProblemReportRow left, ProblemReportRow right) {
        return safeSentence(left).compareTo(safeSentence(right));
      }
    }, new SafeHtmlColumn<ProblemReportRow>() {
      @Override
      public SafeHtml getValue(ProblemReportRow row) {
        return TEMPLATES.sentenceFont(row.problem.getProblemReportSentence());
      }
    }, null);

    // 作問者
    addColumn("作問者", new Comparator<ProblemReportRow>() {
      @Override
      public int compare(ProblemReportRow left, ProblemReportRow right) {
        return safeCreator(left).compareTo(safeCreator(right));
      }
    }, new SafeHtmlColumn<ProblemReportRow>() {
      @Override
      public SafeHtml getValue(ProblemReportRow row) {
        String creator = safeCreator(row);
        if (Strings.isNullOrEmpty(creator)) {
          return TEMPLATES.smallFont(SAFE_VALUE_NA);
        }
        return TEMPLATES.smallFont(creator);
      }
    }, null);

    // 正答率
    addColumn("正答率", new Comparator<ProblemReportRow>() {
      @Override
      public int compare(ProblemReportRow left, ProblemReportRow right) {
        return safeProblem(left).getAccuracyRate() - safeProblem(right).getAccuracyRate();
      }
    }, new SafeHtmlColumn<ProblemReportRow>() {
      @Override
      public SafeHtml getValue(ProblemReportRow row) {
        int ratio = safeProblem(row).getAccuracyRate();
        if (ratio < 0) {
          return TEMPLATES.rateBadge(STYLE_RATE_BADGE_BASE, STYLE_RATE_BADGE_NEW, "NEW");
        }
        String ratioText = ratio + "%";
        return TEMPLATES.rateBadge(STYLE_RATE_BADGE_BASE, getAccuracyBadgeLevelClass(ratio), ratioText);
      }
    }, null);

    // 指摘
    addColumn("指摘", new Comparator<ProblemReportRow>() {
      @Override
      public int compare(ProblemReportRow left, ProblemReportRow right) {
        PacketProblem l = safeProblem(left);
        PacketProblem r = safeProblem(right);
        return ComparisonChain.start()
            .compare(l.indication, r.indication, Ordering.natural().nullsLast())
            .compare(l.indicationResolved, r.indicationResolved, Ordering.natural().nullsLast())
            .result();
      }
    }, new SafeHtmlColumn<ProblemReportRow>() {
      @Override
      public SafeHtml getValue(ProblemReportRow row) {
        PacketProblem problem = safeProblem(row);
        if (problem.indication != null) {
          if (problem.indication.getTime() + INDICATION_PERIOD < System.currentTimeMillis()) {
            return TEMPLATES.indication(UriUtils.fromString("notification_error.png"),
                "指摘から30日以上経過しています。\n問題作成者以外の方も書き換えることができます。");
          }
          return TEMPLATES.indication(UriUtils.fromString("notification_warning.png"),
              "他の問題作成者により指摘がありました。\n速やかに内容を確認してください。");
        } else if (problem.indicationResolved != null
            && System.currentTimeMillis() < problem.indicationResolved.getTime() + RESOLVED_CHECK_PERIOD) {
          return TEMPLATES.indication(UriUtils.fromString("notification_resolved.png"),
              "問題が修正されました。\n指摘した方は内容を確認して下さい。");
        }
        return TEMPLATES.empty();
      }
    }, null);

    // 登録
    addColumn("登録", null, new LinkColumn<ProblemReportRow>() {
      @Override
      public String getValue(ProblemReportRow row) {
        return regist ? "追加" : "削除";
      }
    }, new FieldUpdater<ProblemReportRow, String>() {
      @Override
      public void update(int index, ProblemReportRow row, String value) {
        final int problemId = safeProblemId(row);
        final int userCode = UserData.get().getUserCode();
        if (regist) {
          final List<Integer> problemIds = new ArrayList<Integer>();
          problemIds.add(problemId);
          Service.Util.getInstance().addProblemIdsToReport(userCode, problemIds, callbackReport);
        } else {
          Service.Util.getInstance().removeProblemIDFromReport(userCode, problemId, callbackReport);
        }
      }
    });

    // 操作（詳細）
    addColumn("操作", null, new LinkColumn<ProblemReportRow>() {
      @Override
      public String getValue(ProblemReportRow row) {
        return "詳細";
      }
    }, new FieldUpdater<ProblemReportRow, String>() {
      @Override
      public void update(int index, ProblemReportRow row, String value) {
        showDetailDialog(safeProblem(row));
      }
    });

  }

  private final AsyncCallback<Void> callbackReport =
      new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          dataProvider.refresh();
        }

        @Override
        public void onFailureRpc(Throwable caught) {
          logger.log(Level.WARNING, "問題の追加・削除に失敗しました", caught);
        }
      };

  /**
   * 詳細ダイアログを表示する。
   *
   * @param problem 問題
   */
  private void showDetailDialog(PacketProblem problem) {
    DialogBox dialog = new DialogBox(true, true);
    dialog.setStyleName("problemReportDetailDialog");
    dialog.setText("問題詳細: " + problem.id);
    HTML content = new HTML(buildDetailHtml(problem));
    content.setStyleName("problemReportDetailBody");
    dialog.setWidget(content);
    dialog.center();
  }

  /**
   * 詳細表示用HTMLを生成する。
   *
   * @param problem 問題
   * @return 安全なHTML
   */
  private SafeHtml buildDetailHtml(PacketProblem problem) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    builder.appendHtmlConstant("<table class='problemReportDetailTable'>");
    appendDetailRow(builder, "ランダム", Integer.toString(problem.randomFlag.getIndex()));
    appendDetailRow(builder, "問題ノート", blankToNa(problem.note));
    appendDetailRow(builder, "画像", problem.hasImage() ? "有" : "無");
    appendDetailRow(builder, "動画", problem.hasMovie() ? "有" : "無");
    appendDetailRow(builder, "正解数", Integer.toString(problem.good));
    appendDetailRow(builder, "誤答数", Integer.toString(problem.bad));
    appendDetailRow(builder, "回答数", Integer.toString(problem.good + problem.bad));
    appendDetailRow(builder, "良問", Integer.toString(problem.voteGood));
    builder.appendHtmlConstant("</table>");
    return builder.toSafeHtml();
  }

  /**
   * 詳細行を追加する。
   *
   * @param builder 出力先
   * @param label ラベル
   * @param value 値
   */
  private static void appendDetailRow(SafeHtmlBuilder builder, String label, String value) {
    builder.appendHtmlConstant("<tr><th>");
    builder.appendEscaped(label);
    builder.appendHtmlConstant("</th><td>");
    builder.appendEscaped(value);
    builder.appendHtmlConstant("</td></tr>");
  }

  private static String blankToNa(String value) {
    return Strings.isNullOrEmpty(value) ? SAFE_VALUE_NA : value;
  }

  private static String formatSimilarity(float value) {
    int scaled = Math.round(value * 1000f);
    int integerPart = scaled / 1000;
    int decimalPart = Math.abs(scaled % 1000);
    String decimal = Integer.toString(decimalPart);
    while (decimal.length() < 3) {
      decimal = "0" + decimal;
    }
    return integerPart + "." + decimal;
  }

  private static PacketProblem safeProblem(ProblemReportRow row) {
    return row == null || row.problem == null ? new PacketProblem() : row.problem;
  }

  private static int safeProblemId(ProblemReportRow row) {
    return row == null || row.problem == null ? Integer.MIN_VALUE : row.problem.id;
  }

  private static float safeSimilarity(ProblemReportRow row) {
    return row == null || row.similarityScore == null ? Float.NEGATIVE_INFINITY : row.similarityScore;
  }

  private static String safeGenre(ProblemReportRow row) {
    return row == null || row.problem == null || row.problem.genre == null ? SAFE_VALUE_NA
        : row.problem.genre.toString();
  }

  private static String safeType(ProblemReportRow row) {
    return row == null || row.problem == null || row.problem.type == null ? SAFE_VALUE_NA
        : row.problem.type.toString();
  }

  private static String safeSentence(ProblemReportRow row) {
    return row == null || row.problem == null ? "" : row.problem.getProblemReportSentence();
  }

  private static String safeCreator(ProblemReportRow row) {
    return row == null || row.problem == null ? SAFE_VALUE_NA : blankToNa(row.problem.creator);
  }

  /**
   * 正答率からバッジ色クラスを決定する。
   *
   * @param ratio 正答率（0-100想定）
   * @return バッジ色クラス
   */
  private static String getAccuracyBadgeLevelClass(int ratio) {
    int safeRatio = Math.max(0, Math.min(100, ratio));
    if (safeRatio < 20) {
      return STYLE_RATE_BADGE_L1;
    }
    if (safeRatio < 40) {
      return STYLE_RATE_BADGE_L2;
    }
    if (safeRatio < 60) {
      return STYLE_RATE_BADGE_L3;
    }
    if (safeRatio < 80) {
      return STYLE_RATE_BADGE_L4;
    }
    return STYLE_RATE_BADGE_L5;
  }

  private <S> void addColumn(String header, Comparator<ProblemReportRow> comparator,
      Column<ProblemReportRow, S> column, FieldUpdater<ProblemReportRow, S> fieldUpdater) {
    if (comparator != null) {
      column.setSortable(true);
      columnSortHandler.setComparator(column, comparator);
    }
    if (fieldUpdater != null) {
      column.setFieldUpdater(fieldUpdater);
    }
    addColumn(column, header);
  }
}
