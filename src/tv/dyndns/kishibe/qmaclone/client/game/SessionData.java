package tv.dyndns.kishibe.qmaclone.client.game;

public class SessionData {

	private final int sessionId;
	private final int playerListIndex;
	private final boolean addPenalty;
	private final boolean event;
	private final boolean themeMode;

	public SessionData(int sessionId, int playerListIndex, boolean addPenalty, boolean event,
			boolean themeMode) {
		this.sessionId = sessionId;
		this.playerListIndex = playerListIndex;
		this.addPenalty = addPenalty;
		this.event = event;
		this.themeMode = themeMode;
	}

	public int getSessionId() {
		return sessionId;
	}

	public int getPlayerListIndex() {
		return playerListIndex;
	}

	public boolean isAddPenalty() {
		return addPenalty;
	}

	public boolean isEvent() {
		return event;
	}

	public boolean isThemeMode() {
		return themeMode;
	}

}
