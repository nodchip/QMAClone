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
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
		Service.Util.getInstance().getWrongImageLinks(new AsyncCallback<List<PacketImageLink>>() {
			@Override
			public void onSuccess(List<PacketImageLink> result) {
				dataProvider.setList(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				logger.log(Level.WARNING, "画像リンク一覧の取得に失敗しました", caught);
			}
		});

		dataProvider.addDataDisplay(this);

		// 問題番号
		addColumn(new TextHeader("問題番号"), null, new LinkColumn<PacketImageLink>() {
			@Override
			public String getValue(PacketImageLink object) {
				return Integer.toString(object.problemId);
			}
		}, new FieldUpdater<PacketImageLink, String>() {
			@Override
			public void update(int index, PacketImageLink object, String value) {
				Controller.getInstance().showCreationProblem(object.problemId);
			}
		});

		// 画像リンク
		addColumn(new TextHeader("画像リンク"), null, new SafeHtmlColumn<PacketImageLink>() {
			@Override
			public SafeHtml getValue(PacketImageLink object) {
				String urlToShow = object.url;
				if (urlToShow.length() > 50) {
					urlToShow = urlToShow.substring(0, 20) + " ... "
							+ urlToShow.substring(urlToShow.length() - 25, urlToShow.length());
				}
				return TEMPLATES.link(UriUtils.fromString(object.url), urlToShow);
			}
		}, null);

		// ステータスコード
		addColumn(new TextHeader("ステータスコード"), null, new SafeHtmlColumn<PacketImageLink>() {
			@Override
			public SafeHtml getValue(PacketImageLink object) {
				return SafeHtmlUtils.fromString(Integer.toString(object.statusCode));
			}
		}, null);
	}

	private <C, S> void addColumn(Header<C> header, ValueUpdater<C> valueUpdater,
			Column<PacketImageLink, S> column, FieldUpdater<PacketImageLink, S> fieldUpdater) {
		header.setUpdater(valueUpdater);
		column.setFieldUpdater(fieldUpdater);
		addColumn(column, header);
	}
}
