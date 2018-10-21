package tv.dyndns.kishibe.qmaclone.server.database;

/**
 * データベースから送出される例外
 * 
 * @author nodchip
 */
@SuppressWarnings("serial")
public class DatabaseException extends Exception {
	public DatabaseException() {
	}

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(Throwable cause) {
		super(cause);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
