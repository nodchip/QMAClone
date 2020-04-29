package tv.dyndns.kishibe.qmaclone.client.ranking;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.report.SafeHtmlColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class CellTableRanking extends CellTable<PacketRankingData> {

	interface Factory {
		CellTableRanking create(String dataLabel);
	}

	interface CellTableRankingTemplates extends SafeHtmlTemplates {
		@Template("<img src=\"{0}\" style=\"width: 48px; height: 48px;\">")
		SafeHtml icon(SafeUri fileName);
	}

	private static final CellTableRankingTemplates TEMPLATES = GWT
			.create(CellTableRankingTemplates.class);

	private final ListDataProvider<PacketRankingData> rankingProvider = new ListDataProvider<PacketRankingData>();

	@Inject
	public CellTableRanking(@Assisted String dataLabel) {
		super(0, GWT.<CellTable.BasicResources> create(CellTable.BasicResources.class),
				new ProvidesKey<PacketRankingData>() {
					@Override
					public Object getKey(PacketRankingData item) {
						return item.imageFileName;
					}
				});

		rankingProvider.addDataDisplay(this);

		// 順位
		addColumn(new SafeHtmlColumn<PacketRankingData>() {
			@Override
			public SafeHtml getValue(PacketRankingData object) {
				return SafeHtmlUtils.fromString(String.valueOf(object.ranking));
			}
		}, "順位");

		// アイコン
		addColumn(new SafeHtmlColumn<PacketRankingData>() {
			@Override
			public SafeHtml getValue(PacketRankingData object) {
				return TEMPLATES.icon(UriUtils.fromString("http://kishibe.dyndns.tv/qmaclone/icon/"
						+ object.imageFileName));
			}
		}, "");

		// プレイヤー
		addColumn(new SafeHtmlColumn<PacketRankingData>() {
			@Override
			public SafeHtml getValue(PacketRankingData object) {
				return SafeHtmlUtils.fromString(object.name);
			}
		}, "プレイヤー");

		// データ
		addColumn(new SafeHtmlColumn<PacketRankingData>() {
			@Override
			public SafeHtml getValue(PacketRankingData object) {
				return SafeHtmlUtils.fromString(object.data);
			}
		}, dataLabel);
	}

	public void setRanking(List<PacketRankingData> ranking) {
		setPageSize(ranking.size());
		rankingProvider.setList(ranking);
		rankingProvider.refresh();
	}

}
