package tv.dyndns.kishibe.qmaclone.server.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.service.ServiceException;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

import com.google.common.base.Throwables;

public class DatabaseUtil {

	private static final Logger logger = Logger.getLogger(DatabaseUtil.class.toString());

	private DatabaseUtil() {
		throw new UnsupportedOperationException();
	}

	public static <T> T wrap(String message, DatabaseAccessible<T> accessor)
			throws ServiceException {
		try {
			return accessor.access();
		} catch (DatabaseException e) {
			logger.log(Level.WARNING, message, e);
			throw new ServiceException(Throwables.getStackTraceAsString(e));
		}
	}

}
