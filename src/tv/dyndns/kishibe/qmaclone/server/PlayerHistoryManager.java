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
package tv.dyndns.kishibe.qmaclone.server;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;

import com.google.common.collect.Lists;

public class PlayerHistoryManager {
	private static final int MAX_PLAYERS = 8;
	private final Queue<PacketPlayerSummary> deque = new ConcurrentLinkedQueue<PacketPlayerSummary>();

	public void push(PacketPlayerSummary player) {
		deque.offer(player);
		while (deque.size() > MAX_PLAYERS) {
			deque.poll();
		}
	}

	public List<PacketPlayerSummary> get() {
		final List<PacketPlayerSummary> list = Lists.newArrayList(deque);
		Collections.reverse(list);
		return list;
	}
}
