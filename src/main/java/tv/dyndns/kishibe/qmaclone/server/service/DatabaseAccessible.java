package tv.dyndns.kishibe.qmaclone.server.service;

import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;

public interface DatabaseAccessible<T> {
	T access() throws DatabaseException;
}
