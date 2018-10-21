package tv.dyndns.kishibe.qmaclone.server.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.handlers.AbstractListHandler;

/**
 * {@link org.apache.commons.dbutils.handlers.ColumnListHandler} よりコピー
 * 
 * @author nodchip
 * 
 * @param <T>
 */
public class ColumnListHandler<T> extends AbstractListHandler<T> {
	private final Class<T> c;

	public ColumnListHandler(Class<T> c) {
		this.c = c;
	}

	protected T handleRow(ResultSet rs) throws SQLException {
		Object object = rs.getObject(1);
		if (c == object.getClass()) {
			return c.cast(object);
		} else if (c == Integer.class && object.getClass() == Long.class) {
			// SQL の int(10) 型を Java の Long　型で返すため、特別処理
			return c.cast((Integer) (int) (long) (Long) object);
		} else {
			throw new IllegalArgumentException(String.format("%s から %s にキャストできません", object
					.getClass().toString(), c.toString()));
		}
	}
}
