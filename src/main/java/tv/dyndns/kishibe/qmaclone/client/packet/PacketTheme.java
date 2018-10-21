package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketTheme implements IsSerializable {

	private String name;
	private int numberOfProblems;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumberOfProblems() {
		return numberOfProblems;
	}

	public void setNumberOfProblems(int numberOfProblems) {
		this.numberOfProblems = numberOfProblems;
	}

}
