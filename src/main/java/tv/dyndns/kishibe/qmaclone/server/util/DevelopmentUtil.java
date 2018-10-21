package tv.dyndns.kishibe.qmaclone.server.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DevelopmentUtil {

	private static final Logger logger = Logger.getLogger(DevelopmentUtil.class.getName());

	public boolean isDev() {
		try {
			return InetAddress.getLocalHost().toString().startsWith("doutanuki/");
		} catch (UnknownHostException e) {
			logger.log(Level.WARNING, "自IPの取得に失敗しました", e);
		}
		return false;
	}

}
