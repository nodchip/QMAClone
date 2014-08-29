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

import name.pehl.piriti.json.client.JsonReader;
import tv.dyndns.kishibe.qmaclone.client.game.Transition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketGameStatus implements IsSerializable {
	public static class Json {
		public interface PacketGameStatusReader extends JsonReader<PacketGameStatus> {
		}

		public static final PacketGameStatusReader READER = GWT
				.create(PacketGameStatusReader.class);
	}

	public static class GamePlayerStatus implements IsSerializable {
		public static class Json {
			public interface PlayerStatusReader extends JsonReader<GamePlayerStatus> {
			}

			public static final PlayerStatusReader READER = GWT.create(PlayerStatusReader.class);
		}

		public int score;
		public int rank;
		public String answer;
	}

	public Transition transition;
	public int problemCounter;
	public int restMs;
	public int numberOfPlayingHumans;
	public GamePlayerStatus[] status;
}
