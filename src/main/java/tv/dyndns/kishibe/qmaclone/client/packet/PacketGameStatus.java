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

import tv.dyndns.kishibe.qmaclone.client.game.Transition;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketGameStatus implements IsSerializable {
	public static class GamePlayerStatus implements IsSerializable {
		public int score;
		public int rank;
		public String answer;

		public static GamePlayerStatus fromJsonObject(JSONObject object) {
			GamePlayerStatus status = new GamePlayerStatus();
			status.score = PacketJsonParser.getInt(object, "score");
			status.rank = PacketJsonParser.getInt(object, "rank");
			status.answer = PacketJsonParser.getString(object, "answer");
			return status;
		}
	}

	public Transition transition;
	public int problemCounter;
	public int restMs;
	public int numberOfPlayingHumans;
	public GamePlayerStatus[] status;

	public static PacketGameStatus fromJson(String json) {
		JSONObject object = PacketJsonParser.parseObject(json);
		PacketGameStatus gameStatus = new PacketGameStatus();
		String transitionName = PacketJsonParser.getString(object, "transition");
		if (transitionName != null) {
			gameStatus.transition = Transition.valueOf(transitionName);
		}
		gameStatus.problemCounter = PacketJsonParser.getInt(object, "problemCounter");
		gameStatus.restMs = PacketJsonParser.getInt(object, "restMs");
		gameStatus.numberOfPlayingHumans = PacketJsonParser.getInt(object, "numberOfPlayingHumans");

		JSONArray array = PacketJsonParser.getArray(object, "status");
		if (array != null) {
			gameStatus.status = new GamePlayerStatus[array.size()];
			for (int i = 0; i < array.size(); ++i) {
				JSONValue value = array.get(i);
				JSONObject playerStatus = value == null ? null : value.isObject();
				if (playerStatus != null) {
					gameStatus.status[i] = GamePlayerStatus.fromJsonObject(playerStatus);
				}
			}
		}
		return gameStatus;
	}
}
