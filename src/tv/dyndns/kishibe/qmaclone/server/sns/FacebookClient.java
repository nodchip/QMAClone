package tv.dyndns.kishibe.qmaclone.server.sns;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;

public class FacebookClient implements SnsClient {
	private static final Logger logger = Logger.getLogger(FacebookClient.class.getName());
	private final Database database;
	private final HttpClient httpClient;

	@Inject
	public FacebookClient(Database database, HttpClient httpClient) {
		this.database = Preconditions.checkNotNull(database);
		this.httpClient = Preconditions.checkNotNull(httpClient);
	}

	@Override
	public void postProblem(PacketProblem problem) {
		String problemReportSentence = problem.getProblemReportSentence();
		String message = "問題番号" + problem.id + ":" + problemReportSentence
				+ " http://kishibe.dyndns.tv/qmaclone/";
		post(message);
	}

	@Override
	public void postThemeModeUpdate(String theme) {
		String message = "テーマモード 「" + theme + "」 が更新されました" + " http://kishibe.dyndns.tv/qmaclone/";
		post(message);
	}

	private void post(String message) {
		String accessToken = getPageAccessToken();
		com.restfb.FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
		facebookClient.publish("me/feed", FacebookType.class, Parameter.with("message", message));
	}

	@Override
	public void followBack() {
		throw new UnsupportedOperationException();
	}

	@VisibleForTesting
	String getPageAccessToken() {
		String accessToken;
		try {
			accessToken = database.getPassword("facebook_access_token");
		} catch (DatabaseException e) {
			logger.log(Level.WARNING, "Failed to get the access token from the database.", e);
			return null;
		}

		HttpGet httpGet = new HttpGet("https://graph.facebook.com/1060467615/accounts?access_token="
				+ accessToken);
		HttpEntity entity = null;
		String json;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			entity = response.getEntity();
			json = EntityUtils.toString(entity, "utf-8");

		} catch (ClientProtocolException e) {
			logger.log(Level.WARNING, "Failed to get an access token from facebook.", e);
			return null;
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to get an access token from facebook.", e);
			return null;
		} finally {
			try {
				EntityUtils.consume(entity);
			} catch (IOException e) {
				logger.log(Level.WARNING, "Failed to get an access token from facebook.", e);
				return null;
			}
		}

		List<String> elements = ImmutableList.copyOf(json.replaceAll("\\s", "").split("\""));
		int accessTokenIndex = elements.indexOf("access_token");
		if (accessTokenIndex == -1) {
			return null;
		}
		return elements.get(accessTokenIndex + 2);
	}
}
