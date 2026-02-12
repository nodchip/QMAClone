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

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketServerStatus implements IsSerializable, Cloneable {
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

	public static PacketServerStatus fromJson(String json) {
		JSONObject object = PacketJsonParser.parseObject(json);
		PacketServerStatus status = new PacketServerStatus();
		status.numberOfCurrentPlayers = PacketJsonParser.getInt(object, "numberOfCurrentPlayers");
		status.numberOfTotalPlayers = PacketJsonParser.getInt(object, "numberOfTotalPlayers");
		status.numberOfCurrentSessions = PacketJsonParser.getInt(object, "numberOfCurrentSessions");
		status.numberOfTotalSessions = PacketJsonParser.getInt(object, "numberOfTotalSessions");
		status.numberOfPageView = PacketJsonParser.getInt(object, "numberOfPageView");
		status.numberOfProblems = PacketJsonParser.getInt(object, "numberOfProblems");
		status.numberOfLoginPlayers = PacketJsonParser.getInt(object, "numberOfLoginPlayers");
		status.numberOfActivePlayers = PacketJsonParser.getInt(object, "numberOfActivePlayers");
		status.numberOfPlayersInWhole = PacketJsonParser.getInt(object, "numberOfPlayersInWhole");
		JSONArray players = PacketJsonParser.getArray(object, "lastestPlayers");
		if (players != null) {
			status.lastestPlayers = Lists.newArrayList();
			for (int i = 0; i < players.size(); ++i) {
				JSONValue value = players.get(i);
				JSONObject playerObject = value == null ? null : value.isObject();
				if (playerObject != null) {
					status.lastestPlayers.add(PacketPlayerSummary.fromJsonObject(playerObject));
				}
			}
		}
		return status;
	}

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
