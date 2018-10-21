package tv.dyndns.kishibe.qmaclone.server;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class StatsServletStub extends HttpServlet {

	private static final String MODE_CURRENT_PLAYERS = "current_players";
	private static final String MODE_TOTAL_PLAYERS = "total_players";
	private static final String MODE_CURRENT_SESSIONS = "current_sessions";
	private static final String MODE_TOTAL_SESSIONS = "total_sessions";
	private static final String MODE_PAGE_VIEW = "page_view";
	private static final String MODE_PROBLEMS = "problems";
	private static final String MODE_LOGIN_PLAYERS = "login_players";
	private static final String MODE_ACTIVE_PLAYERS = "active_players";
	private static final String MODE_PLAYERS_IN_WHOLE = "players_in_whole";
	private final ServerStatusManager serverStatusManager;

	@Inject
	public StatsServletStub(ServerStatusManager serverStatusManager) {
		this.serverStatusManager = Preconditions.checkNotNull(serverStatusManager);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		resp.setContentType("text/plain");

		PacketServerStatus status = serverStatusManager.getServerStatus();

		Map<String, Integer> results = ImmutableMap.<String, Integer> builder()
				.put(MODE_CURRENT_PLAYERS, status.numberOfCurrentPlayers)
				.put(MODE_TOTAL_PLAYERS, status.numberOfTotalPlayers)
				.put(MODE_CURRENT_SESSIONS, status.numberOfCurrentSessions)
				.put(MODE_TOTAL_SESSIONS, status.numberOfTotalSessions)
				.put(MODE_PAGE_VIEW, status.numberOfPageView)
				.put(MODE_PROBLEMS, status.numberOfProblems)
				.put(MODE_LOGIN_PLAYERS, status.numberOfLoginPlayers)
				.put(MODE_ACTIVE_PLAYERS, status.numberOfActivePlayers)
				.put(MODE_PLAYERS_IN_WHOLE, status.numberOfPlayersInWhole).build();

		try (PrintStream ps = new PrintStream(resp.getOutputStream())) {
			for (Entry<String, Integer> entry : results.entrySet()) {
				if (!entry.getKey().equals(req.getParameter("mode"))) {
					continue;
				}

				ps.printf("%s.value %d\n", entry.getKey(), entry.getValue());
			}
		}
	}

}
