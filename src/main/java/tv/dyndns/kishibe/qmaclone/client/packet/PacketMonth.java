package tv.dyndns.kishibe.qmaclone.client.packet;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketMonth implements IsSerializable, Comparable<PacketMonth> {

	public int year;
	public int month;

	public PacketMonth() {
	}

	public PacketMonth(int year, int month) {
		this.year = year;
		this.month = month;
	}

	@Override
	public int compareTo(PacketMonth o) {
		return ComparisonChain.start().compare(year, o.year).compare(month, o.month).result();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(year, month);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PacketMonth)) {
			return false;
		}
		PacketMonth rh = (PacketMonth) obj;
		return year == rh.year && month == rh.month;
	}

}
