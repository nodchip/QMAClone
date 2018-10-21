package tv.dyndns.kishibe.qmaclone.server.sns;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.util.DevelopmentUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class SnsClients implements SnsClient {

	private final List<SnsClient> clients = Lists.newArrayList();
	private final DevelopmentUtil developmentUtil;

	@Inject
	public SnsClients(TwitterClient twitterClient, FacebookClient facebookClient,
			DevelopmentUtil developmentUtil) {
		clients.add(twitterClient);
		clients.add(facebookClient);
		this.developmentUtil = Preconditions.checkNotNull(developmentUtil);
	}

	@Override
	public void postProblem(PacketProblem problem) {
		if (developmentUtil.isDev()) {
			return;
		}

		for (SnsClient client : clients) {
			client.postProblem(problem);
		}
	}

	@Override
	public void postThemeModeUpdate(String theme) {
		if (developmentUtil.isDev()) {
			return;
		}

		for (SnsClient client : clients) {
			client.postThemeModeUpdate(theme);
		}
	}

	@Override
	public void followBack() {
		if (developmentUtil.isDev()) {
			return;
		}

		for (SnsClient client : clients) {
			client.followBack();
		}
	}

}
