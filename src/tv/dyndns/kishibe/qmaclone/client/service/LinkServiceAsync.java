package tv.dyndns.kishibe.qmaclone.client.service;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LinkServiceAsync {

	void get(int start, int count, AsyncCallback<List<PacketLinkData>> callback);

	void getNumberOfLinkData(AsyncCallback<Integer> callback);

	void add(PacketLinkData linkData, AsyncCallback<Void> callback);

	void update(PacketLinkData linkData, AsyncCallback<Void> callback);

	void remove(int id, AsyncCallback<Void> callback);

}
