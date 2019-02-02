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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.Transition;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKey;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.exception.GameNotFoundException;

public class GameManager {
	private static final Logger logger = Logger.getLogger(GameManager.class.toString());
	private final Game.Factory gameFactory;
	private final Cache<Integer, Game> sessions = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES)
			.build();
	private final Map<PacketRoomKey, Game> matchingSessions = Maps.newHashMap();
	private volatile List<PacketRoomKey> publicMatchingEventRooms = Lists.newArrayList();
	private volatile Game gameWhole = null;
	private final Set<Integer> usedSessionIds = Sets.newHashSet();
	private final Random random = new Random();
	private final RestrictedUserUtils restrictedUserUtils;

	@Inject
	public GameManager(Game.Factory gameFactory, RestrictedUserUtils restrictedUserUtils) {
		this.gameFactory = Preconditions.checkNotNull(gameFactory);
		this.restrictedUserUtils = Preconditions.checkNotNull(restrictedUserUtils);
	}

	/**
	 * マッチング状態にあるセッションを返すか、なければ新たに作成する
	 * 
	 * @param gameMode
	 * @param roomName
	 * @param classLevel
	 * @param THEME
	 * @param genres
	 * @param types
	 * @param publicEvent
	 * @param serverStatusManager
	 * @param userCode
	 * @param remoteAddress
	 * @param badUserManager
	 * @return
	 */
	public synchronized Game getOrCreateMatchingSession(GameMode gameMode, String roomName, int classLevel,
			String theme, Set<ProblemGenre> genres, Set<ProblemType> types, boolean publicEvent,
			ServerStatusManager serverStatusManager, int userCode, String remoteAddress) {
		// 隔離部屋は使用しない。通常のプレイヤーよりランクを下げることで対処する。
		// http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack-QMAClone%2F490
		try {
			restrictedUserUtils.checkAndUpdateRestrictedUser(userCode, remoteAddress, RestrictionType.MATCH);
		} catch (DatabaseException e) {
			logger.log(Level.WARNING, "制限ユーザーのチェックに失敗しました。処理を続行します。", e);
		}

		PacketRoomKey roomKey = new PacketRoomKey(gameMode, gameMode == GameMode.THEME ? theme : roomName, genres,
				types);

		Game result = matchingSessions.get(roomKey);

		// ゲームセッションが見つからない場合やマッチング中でない場合は新たにゲームセッションを作成する
		if (result == null || result.getTransition() != Transition.Matching) {
			boolean alone;
			boolean event;

			switch (gameMode) {
			case VS_COM:
				alone = true;
				event = false;
				break;
			case WHOLE:
				alone = false;
				event = false;
				break;
			case EVENT:
				alone = false;
				event = true;
				break;
			case THEME:
				alone = false;
				event = false;
				break;
			case LIMITED:
				alone = false;
				event = false;
				break;
			default:
				alone = false;
				event = false;
				break;
			}

			int sessionId;
			synchronized (random) {
				do {
					sessionId = random.nextInt(Integer.MAX_VALUE);
				} while (usedSessionIds.contains(sessionId));
				usedSessionIds.add(sessionId);
			}

			Game game = gameFactory.create(sessionId, classLevel, event, alone, theme, publicEvent, gameMode);
			matchingSessions.put(roomKey, game);
			sessions.put(sessionId, game);
			updateEventRooms();
			result = game;
		}

		if (result == null) {
			String message = "ゲームセッションの作成または検索に失敗しました ";
			message += MoreObjects.toStringHelper(this).add("gameMode", gameMode).add("roomName", roomName)
					.add("classLevel", classLevel).add("THEME", theme).add("genres", genres).add("types", types)
					.add("publicEvent", publicEvent).add("serverStatusManager", serverStatusManager)
					.add("userCode", userCode).add("remoteAddress", remoteAddress).toString();
			logger.log(Level.SEVERE, message);
		}

		if (gameMode == GameMode.WHOLE) {
			gameWhole = result;
		}

		return result;
	}

	/**
	 * セッションを返す
	 * 
	 * @param sessionId セッションID
	 * @return セッション
	 */
	public Game getSession(int sessionId) throws GameNotFoundException {
		Game game = sessions.getIfPresent(sessionId);
		if (game == null) {
			throw new GameNotFoundException(
					String.format("ゲームセッションが見つかりませんでした sessionId=%d sessions=%s", sessionId, sessions.toString()));
		}
		return game;
	}

	public void notifyMatchingCompleted() {
		updateEventRooms();
	}

	public List<PacketRoomKey> getPublicMatchingEventRooms() {
		return publicMatchingEventRooms;
	}

	private synchronized void updateEventRooms() {
		List<PacketRoomKey> eventRooms = Lists.newArrayList();
		for (Entry<PacketRoomKey, Game> entry : matchingSessions.entrySet()) {
			Game game = entry.getValue();
			if (game.isEvent() && game.isPublicEvent() && game.getTransition() == Transition.Matching) {
				PacketRoomKey key = entry.getKey();
				eventRooms.add(key);
			}
		}
		this.publicMatchingEventRooms = eventRooms;
	}

	public int getNumberOfPlayersInWhole() {
		Game game = gameWhole;

		if (game == null || game.getTransition() != Transition.Matching) {
			return 0;
		} else {
			return game.getNumberOfHumanPlayer();
		}
	}

	public int getNumberOfSessions() {
		return (int) sessions.size();
	}

	public int getNumberOfPlayers() {
		int numberOfPlayers = 0;
		for (Game game : sessions.asMap().values()) {
			if (game.getTransition() == Transition.Result || game.getTransition() == Transition.Finished) {
				continue;
			}
			numberOfPlayers += game.getNumberOfHumanPlayer();
		}
		return numberOfPlayers;
	}

	public Set<Integer> getTestingProblemIds() {
		Set<Integer> problemIds = Sets.newHashSet();
		for (Game game : sessions.asMap().values()) {
			problemIds.addAll(game.getTestingProblemIds());
		}
		return problemIds;
	}
}
