//The MIT License
//
//Copyright (c) 2009 nodchip
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package tv.dyndns.kishibe.qmaclone.client.packet;

import java.util.List;

import name.pehl.piriti.json.client.JsonReader;

import com.google.common.base.Objects;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketServerStatus implements IsSerializable, Cloneable {
	public static class Json {
		public interface PacketServerStatusReader extends JsonReader<PacketServerStatus> {
		}

		public static final PacketServerStatusReader READER = GWT
				.create(PacketServerStatusReader.class);
	}

	public int numberOfCurrentPlayers;
	public int numberOfTotalPlayers;
	public int numberOfCurrentSessions;
	public int numberOfTotalSessions;
	public int numberOfPageView;
	public int numberOfProblems;
	public int numberOfLoginPlayers;
	public int numberOfActivePlayers;
	public List<PacketPlayerSummary> lastestPlayers;
	public int numberOfPlayersInWhole;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PacketServerStatus)) {
			return false;
		}
		PacketServerStatus rh = (PacketServerStatus) obj;
		return numberOfCurrentPlayers == rh.numberOfCurrentPlayers
				&& numberOfTotalPlayers == rh.numberOfTotalPlayers
				&& numberOfCurrentSessions == rh.numberOfCurrentSessions
				&& numberOfTotalSessions == rh.numberOfTotalSessions
				&& numberOfPageView == rh.numberOfPageView
				&& numberOfProblems == rh.numberOfProblems
				&& numberOfLoginPlayers == rh.numberOfLoginPlayers
				&& numberOfActivePlayers == rh.numberOfActivePlayers
				&& Objects.equal(lastestPlayers, rh.lastestPlayers)
				&& numberOfPlayersInWhole == rh.numberOfPlayersInWhole;
	}

	@Override
	public int hashCode() {
		return Objects
				.hashCode(numberOfCurrentPlayers, numberOfTotalPlayers, numberOfCurrentSessions,
						numberOfTotalSessions, numberOfPageView, numberOfProblems,
						numberOfLoginPlayers, numberOfActivePlayers, lastestPlayers,
						numberOfPlayersInWhole);
	}
}
