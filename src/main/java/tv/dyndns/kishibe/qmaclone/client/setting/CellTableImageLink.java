package tv.dyndns.kishibe.qmaclone.client.setting;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.Controller;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketImageLink;
import tv.dyndns.kishibe.qmaclone.client.report.LinkColumn;
import tv.dyndns.kishibe.qmaclone.client.report.SafeHtmlColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.ListDataProvider;

public class CellTableImageLink extends CellTable<PacketImageLink> {
	private static final Logger logger = Logger.getLogger(CellTableImageLink.class.getName());
	private final ListDataProvider<PacketImageLink> dataProvider = new ListDataProvider<PacketImageLink>();

	public interface CellTableImageLinkTemplates extends SafeHtmlTemplates {
		@Template("<a href='{0}'>{1}</a>")
		SafeHtml link(SafeUri url, String urlToShow);
	}

	private static final CellTableImageLinkTemplates TEMPLATES = GWT
			.create(CellTableImageLinkTemplates.class);

	public CellTableImageLink() {
		super(100, GWT.<CellTable.BasicResources> create(CellTable.BasicResources.class));
		setPageSize(100);
		setWidth("100%");
		setEmptyTableWidget(new HTML("<div class='settingImageLinkEmpty'>リンク切れは見つかりませんでした。</div>"));
		setLoadingIndicator(new HTML("<div class='settingImageLinkLoading'>画像リンク情報を読み込み中です...</div>"));
		Service.Util.getInstance().getWrongImageLinks(new tv.dyndns.kishibe.qmaclone.client.RpcAsyncCallback<List<PacketImageLink>>() {
			@Override
			public void onSuccess(List<PacketImageLink> result) {
				dataProvider.setList(result);
			}

			@Override
			public void onFailureRpc(Throwable caught) {
				logger.log(Level.WARNING, "画像リンク一覧の取得に失敗しました", caught);
			}
		});

		dataProvider.addDataDisplay(this);

		// 問題番号
		Column<PacketImageLink, String> problemIdColumn = new LinkColumn<PacketImageLink>() {
			@Override
			public String getValue(PacketImageLink object) {
				return Integer.toString(object.problemId);
			}
		};
		addColumn(new TextHeader("問題番号"), null, problemIdColumn, new FieldUpdater<PacketImageLink, String>() {
			@Override
			public void update(int index, PacketImageLink object, String value) {
				Controller.getInstance().showCreationProblem(object.problemId);
			}
		});
		setColumnWidth(problemIdColumn, 18, Unit.PCT);

		// 画像リンク
		Column<PacketImageLink, SafeHtml> imageLinkColumn = new SafeHtmlColumn<PacketImageLink>() {
			@Override
			public SafeHtml getValue(PacketImageLink object) {
				String urlToShow = makeCompactUrl(object.url);
				return TEMPLATES.link(UriUtils.fromString(object.url), urlToShow);
			}
		};
		addColumn(new TextHeader("画像リンク"), null, imageLinkColumn, null);
		setColumnWidth(imageLinkColumn, 62, Unit.PCT);

		// ステータスコード
		Column<PacketImageLink, SafeHtml> statusCodeColumn = new SafeHtmlColumn<PacketImageLink>() {
			@Override
			public SafeHtml getValue(PacketImageLink object) {
				return renderStatusBadge(object.statusCode);
			}
		};
		addColumn(new TextHeader("ステータス"), null, statusCodeColumn, null);
		setColumnWidth(statusCodeColumn, 20, Unit.PCT);
	}

	/**
	 * 長いURLは先頭と末尾を残して省略し、一覧可読性を維持する。
	 */
	private static String makeCompactUrl(String rawUrl) {
		if (rawUrl == null) {
			return "";
		}
		if (rawUrl.length() <= 72) {
			return rawUrl;
		}
		return rawUrl.substring(0, 30) + " ... " + rawUrl.substring(rawUrl.length() - 32);
	}

	/**
	 * HTTPステータスを種別ごとのバッジ表示にする。
	 */
	private static SafeHtml renderStatusBadge(int statusCode) {
		String categoryClass = "settingImageLinkStatus--ok";
		if (statusCode >= 500) {
			categoryClass = "settingImageLinkStatus--server";
		} else if (statusCode >= 400) {
			categoryClass = "settingImageLinkStatus--client";
		} else if (statusCode >= 300) {
			categoryClass = "settingImageLinkStatus--redirect";
		}
		SafeHtmlBuilder builder = new SafeHtmlBuilder();
		builder.appendHtmlConstant("<span class='settingImageLinkStatus ")
				.appendEscaped(categoryClass).appendHtmlConstant("'>")
				.appendEscaped(Integer.toString(statusCode)).appendHtmlConstant("</span>");
		return builder.toSafeHtml();
	}

	private <C, S> void addColumn(Header<C> header, ValueUpdater<C> valueUpdater,
			Column<PacketImageLink, S> column, FieldUpdater<PacketImageLink, S> fieldUpdater) {
		header.setUpdater(valueUpdater);
		column.setFieldUpdater(fieldUpdater);
		addColumn(column, header);
	}
}

