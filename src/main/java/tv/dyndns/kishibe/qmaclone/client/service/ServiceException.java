package tv.dyndns.kishibe.qmaclone.client.service;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * RPCで返される例外
 * 
 * @author nodchip
 */
@SuppressWarnings("serial")
public class ServiceException extends Exception implements IsSerializable {
	public ServiceException() {
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
