package tv.dyndns.kishibe.qmaclone.client.service;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath(ServiceConstants.LINK_SERVICE_PATH)
public interface LinkService extends RemoteService {

	public static class Util {
		public static LinkServiceAsync get() {
			return GWT.create(LinkService.class);
		}
	}

	List<PacketLinkData> get(int start, int count) throws ServiceException;

	int getNumberOfLinkData() throws ServiceException;

	void add(PacketLinkData linkData) throws ServiceException;

	void update(PacketLinkData linkData) throws ServiceException;

	void remove(int id) throws ServiceException;

}
