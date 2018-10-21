package tv.dyndns.kishibe.qmaclone.client.game;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum Transition implements IsSerializable {
	Matching, Ready, Problem, Answer, Result, Finished
}
