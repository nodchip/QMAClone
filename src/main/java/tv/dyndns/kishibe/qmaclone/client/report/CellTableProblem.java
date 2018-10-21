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
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

public class CellTableProblem extends CellTable<PacketProblem> {
	private static final long RESOLVED_CHECK_PERIOD = 7L * 24 * 60 * 60 * 1000;
	private static final long INDICATION_PERIOD = 30L * 24 * 60 * 60 * 1000;
	private static final Logger logger = Logger.getLogger(CellTableProblem.class.getName());
	private static final String STYLE_ACCURACY_RATE = "accuracyRate";
	private static final String STYLE_NEW_PROBLEM = "newProblem";
	private final ListDataProvider<PacketProblem> dataProvider = new ListDataProvider<PacketProblem>();

	public interface CellTableProblemTemplates extends SafeHtmlTemplates {
		@Template("<div class='gridFontSmall'>{0}</div>")
		SafeHtml smallFont(String text);

		@Template("")
		SafeHtml empty();

		@Template("<img src=\"{0}\" title=\"{1}\">")
		SafeHtml indication(SafeUri fileName, String title);
	}

	private static final CellTableProblemTemplates TEMPLATES = GWT
			.create(CellTableProblemTemplates.class);
	private static final SafeHtml SAFE_HTML_EXISTS = TEMPLATES.smallFont("有");
	private final ListHandler<PacketProblem> columnSortHandler;

	public CellTableProblem(List<PacketProblem> problems, final boolean regist, int pageSize) {
		super(pageSize, CellTableProblemResources.Factory.get(), new ProvidesKey<PacketProblem>() {
			@Override
			public Object getKey(PacketProblem item) {
				return item.id;
			}
		});

		dataProvider.setList(problems);
		dataProvider.addDataDisplay(this);
		columnSortHandler = new ListHandler<PacketProblem>(dataProvider.getList());
		addColumnSortHandler(columnSortHandler);

		// 問題番号
		addColumn("問題番号", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.id - o2.id;
			}
		}, new LinkColumn<PacketProblem>() {
			@Override
			public String getValue(PacketProblem object) {
				return object.testing ? "(出題中)" : Integer.toString(object.id);
			}
		}, new FieldUpdater<PacketProblem, String>() {
			@Override
			public void update(int index, PacketProblem object, String value) {
				if (object.testing) {
					return;
				}
				Controller.getInstance().showCreationProblem(object.id);
			}
		});

		// ジャンル
		addColumn("ジャンル", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.genre.compareTo(o2.genre);
			}
		}, new SafeHtmlColumn<PacketProblem>() {
			@Override
			public SafeHtml getValue(PacketProblem object) {
				return TEMPLATES.smallFont(object.genre.toString());
			}
		}, null);

		// 出題形式
		addColumn("出題形式", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.type.compareTo(o2.type);
			}
		}, new SafeHtmlColumn<PacketProblem>() {
			@Override
			public SafeHtml getValue(PacketProblem object) {
				return TEMPLATES.smallFont(object.type.toString());
			}
		}, null);

		// ランダムフラグ
		addColumn("ランダム", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.randomFlag.compareTo(o2.randomFlag);
			}
		}, new TextColumn<PacketProblem>() {
			@Override
			public String getValue(PacketProblem object) {
				return Integer.toString(object.randomFlag.getIndex());
			}
		}, null);

		// 問題文
		addColumn("問題文", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.sentence.compareTo(o2.sentence);
			}
		}, new SafeHtmlColumn<PacketProblem>() {
			@Override
			public SafeHtml getValue(PacketProblem object) {
				return TEMPLATES.smallFont(object.getProblemReportSentence());
			}
		}, null);

		// 問題ノート
		addColumn("問題ノート", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.note.compareTo(o2.note);
			}
		}, new SafeHtmlColumn<PacketProblem>() {
			@Override
			public SafeHtml getValue(PacketProblem object) {
				if (Strings.isNullOrEmpty(object.note)) {
					return null;
				}
				return TEMPLATES.smallFont(object.note);
			}
		}, null);

		// 作問者
		addColumn("作問者", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.creator.compareTo(o2.creator);
			}
		}, new SafeHtmlColumn<PacketProblem>() {
			@Override
			public SafeHtml getValue(PacketProblem object) {
				if (Strings.isNullOrEmpty(object.creator)) {
					return null;
				}
				return TEMPLATES.smallFont(object.creator);
			}
		}, null);

		// 画像
		addColumn("画像", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return -((Boolean) o1.hasImage()).compareTo(o2.hasImage());
			}
		}, new SafeHtmlColumn<PacketProblem>() {
			@Override
			public SafeHtml getValue(PacketProblem object) {
				if (object.hasImage()) {
					return SAFE_HTML_EXISTS;
				} else {
					return null;
				}
			}
		}, null);

		// 動画
		addColumn("動画", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return -((Boolean) o1.hasMovie()).compareTo(o2.hasMovie());
			}
		}, new SafeHtmlColumn<PacketProblem>() {
			@Override
			public SafeHtml getValue(PacketProblem object) {
				if (object.hasMovie()) {
					return SAFE_HTML_EXISTS;
				} else {
					return null;
				}
			}
		}, null);

		// 正解数
		addColumn("正解数", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.good - o2.good;
			}
		}, new TextColumn<PacketProblem>() {
			@Override
			public String getValue(PacketProblem object) {
				return Integer.toString(object.good);
			}
		}, null);

		// 誤答数
		addColumn("誤答数", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.bad - o2.bad;
			}
		}, new TextColumn<PacketProblem>() {
			@Override
			public String getValue(PacketProblem object) {
				return Integer.toString(object.bad);
			}
		}, null);

		// 回答数
		addColumn("回答数", new Comparator<PacketProblem>() {

			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return (o1.good + o1.bad) - (o2.good + o2.bad);
			}
		}, new TextColumn<PacketProblem>() {
			@Override
			public String getValue(PacketProblem object) {
				return Integer.toString(object.good + object.bad);
			}
		}, null);

		// 正答率
		addColumn("正答率", new Comparator<PacketProblem>() {
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.getAccuracyRate() - o2.getAccuracyRate();
			}
		}, new TextColumn<PacketProblem>() {
			@Override
			public String getValue(PacketProblem object) {
				int ratio = object.getAccuracyRate();
				if (ratio == -1) {
					return "-%";
				} else {
					return ratio + "%";
				}
			}
		}, null);

		// // +1ボタン
		// addColumn("+1", null, new SafeHtmlColumn<PacketProblem>() {
		// @Override
		// public SafeHtml getValue(PacketProblem object) {
		// return PlusOne.getButton(object.id, false);
		// }
		// }, null);

		// 良問
		addColumn("良問", new Comparator<PacketProblem>() {
			@Override
			public int compare(PacketProblem o1, PacketProblem o2) {
				return o1.voteGood - o2.voteGood;
			}
		}, new GwtButtonColumn<PacketProblem>() {
			@Override
			public String getValue(PacketProblem object) {
				return Integer.toString(object.voteGood);
			}
		}, new FieldUpdater<PacketProblem, String>() {
			@Override
			public void update(int index, final PacketProblem object, String value) {
				new ProblemFeedbackDialogBox(dataProvider, object).center();
			}
		});

		// 指摘
		addColumn("指摘", new Comparator<PacketProblem>() {
			public int compare(PacketProblem o1, PacketProblem o2) {
				return ComparisonChain
						.start()
						.compare(o1.indication, o2.indication, Ordering.natural().nullsLast())
						.compare(o1.indicationResolved, o2.indicationResolved,
								Ordering.natural().nullsLast()).result();
			}
		}, new SafeHtmlColumn<PacketProblem>() {
			@Override
			public SafeHtml getValue(PacketProblem object) {
				if (object.indication != null) {
					if (object.indication.getTime() + INDICATION_PERIOD < System
							.currentTimeMillis()) {
						return TEMPLATES.indication(UriUtils.fromString("notification_error.png"),
								"指摘から30日以上経過しています。\n問題作成者以外の方も書き換えることができます。");
					} else {
						return TEMPLATES.indication(
								UriUtils.fromString("notification_warning.png"),
								"他の問題作成者により指摘がありました。\n速やかに内容を確認してください。");
					}
				} else if (object.indicationResolved != null) {
					if (System.currentTimeMillis() < object.indicationResolved.getTime()
							+ RESOLVED_CHECK_PERIOD) {
						return TEMPLATES.indication(
								UriUtils.fromString("notification_resolved.png"),
								"問題が修正されました。\n指摘した方は内容を確認して下さい。");
					}
				}
				return TEMPLATES.empty();
			}
		}, null);

		// // 悪問
		// addColumn("悪問", new Comparator<PacketProblem>() {
		// @Override
		// public int compare(PacketProblem o1, PacketProblem o2) {
		// return o1.voteBad - o2.voteBad;
		// }
		// }, new GwtButtonColumn<PacketProblem>() {
		// @Override
		// public String getValue(PacketProblem object) {
		// return Integer.toString(object.voteBad);
		// }
		// }, new FieldUpdater<PacketProblem, String>() {
		// @Override
		// public void update(int index, final PacketProblem object, String value) {
		// String feedback = Window.prompt("投票理由をお書き下さい", "");
		// if (Strings.isNullOrEmpty(feedback)) {
		// return;
		// }
		//
		// int userCode = UserData.get().getUserCode();
		// String playerName = UserData.get().getPlayerName();
		// Service.Util.getInstance().voteToProblem(userCode, object.id, false, feedback,
		// playerName, new AsyncCallback<Void>() {
		// @Override
		// public void onSuccess(Void result) {
		// ++object.voteBad;
		// dataProvider.refresh();
		// }
		//
		// @Override
		// public void onFailure(Throwable caught) {
		// logger.log(Level.WARNING, "問題への投票に失敗しました", caught);
		// }
		// });
		// }
		// });

		// 問題登録・解除
		addColumn("登録", null, new LinkColumn<PacketProblem>() {
			@Override
			public String getValue(PacketProblem object) {
				return regist ? "追加" : "削除";
			}
		}, new FieldUpdater<PacketProblem, String>() {
			public void update(int index, PacketProblem object, String value) {
				final int problemId = object.id;
				final int userCode = UserData.get().getUserCode();
				if (regist) {
					final List<Integer> problemIds = new ArrayList<Integer>();
					problemIds.add(problemId);
					Service.Util.getInstance().addProblemIdsToReport(userCode, problemIds,
							callbackReport);
				} else {
					Service.Util.getInstance().removeProblemIDFromReport(userCode, problemId,
							callbackReport);
				}
			}
		});

		// 各行の表示色設定
		final RowStyles<PacketProblem> rowStyles = new RowStyles<PacketProblem>() {
			@Override
			public String getStyleNames(PacketProblem row, int rowIndex) {
				if (row == null) {
					return null;
				}

				if (row.isNew()) {
					return STYLE_NEW_PROBLEM;
				} else {
					return STYLE_ACCURACY_RATE + row.getAccuracyRate();
				}
			}
		};
		setRowStyles(rowStyles);
	}

	private final AsyncCallback<Void> callbackReport = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			dataProvider.refresh();
		}

		@Override
		public void onFailure(Throwable caught) {
			logger.log(Level.WARNING, "問題の追加・削除に失敗しました", caught);
		}
	};

	private <C, S> void addColumn(String header, Comparator<PacketProblem> comparator,
			Column<PacketProblem, S> column, FieldUpdater<PacketProblem, S> fieldUpdater) {
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
