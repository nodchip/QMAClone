package tv.dyndns.kishibe.qmaclone.server;

import java.util.logging.Logger;

public class GameLogger {
	private static final Logger logger = Logger.getLogger(GameLogger.class.getName());

	public void write(String s) {
		logger.info(s);
	}
}
