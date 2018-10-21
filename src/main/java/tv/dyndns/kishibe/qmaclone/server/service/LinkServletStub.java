package tv.dyndns.kishibe.qmaclone.server.service;

import static tv.dyndns.kishibe.qmaclone.server.service.DatabaseUtil.wrap;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;
import tv.dyndns.kishibe.qmaclone.client.service.LinkService;
import tv.dyndns.kishibe.qmaclone.client.service.ServiceException;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.base.Preconditions;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class LinkServletStub extends RemoteServiceServlet implements LinkService {

	private final Database database;

	@Inject
	public LinkServletStub(Database database) {
		this.database = Preconditions.checkNotNull(database);
	}

	@Override
	public List<PacketLinkData> get(final int start, final int count) throws ServiceException {
		return wrap("リンクデータの取得に失敗しました", new DatabaseAccessible<List<PacketLinkData>>() {
			@Override
			public List<PacketLinkData> access() throws DatabaseException {
				return database.getLinkDatas(start, count);
			}
		});
	}

	@Override
	public int getNumberOfLinkData() throws ServiceException {
		return wrap("リンクデータ数の取得に失敗しました", new DatabaseAccessible<Integer>() {
			@Override
			public Integer access() throws DatabaseException {
				return database.getNumberOfLinkDatas();
			}
		});
	}

	@Override
	public void add(final PacketLinkData linkData) throws ServiceException {
		wrap("リンクデータの取得に失敗しました", new DatabaseAccessible<Void>() {
			@Override
			public Void access() throws DatabaseException {
				database.addLinkData(linkData);
				return null;
			}
		});
	}

	@Override
	public void update(final PacketLinkData linkData) throws ServiceException {
		wrap("リンクデータの更新に失敗しました", new DatabaseAccessible<Void>() {
			@Override
			public Void access() throws DatabaseException {
				database.updateLinkData(linkData);
				return null;
			}
		});
	}

	@Override
	public void remove(final int id) throws ServiceException {
		wrap("リンクデータの削除に失敗しました", new DatabaseAccessible<Void>() {
			@Override
			public Void access() throws DatabaseException {
				database.removeLinkData(id);
				return null;
			}
		});
	}

}
