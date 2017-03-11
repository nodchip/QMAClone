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

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;
import tv.dyndns.kishibe.qmaclone.server.database.Database;
import tv.dyndns.kishibe.qmaclone.server.database.DatabaseException;
import tv.dyndns.kishibe.qmaclone.server.websocket.MessageSender;

public class ChatManager {
  private static final Logger logger = Logger.getLogger(ChatManager.class.getName());
  private static final Object STATIC_KEY = new Object();
  private final LoadingCache<Object, NavigableMap<Integer, PacketChatMessage>> data = CacheBuilder
      .newBuilder().concurrencyLevel(1)
      .build(new CacheLoader<Object, NavigableMap<Integer, PacketChatMessage>>() {
        @Override
        public NavigableMap<Integer, PacketChatMessage> load(Object arg0) throws Exception {
          return loadData();
        }
      });
  private final Object writeLock = new Object();
  private final MessageSender<PacketChatMessages> chatMessagesMessageSender;
  private final Database database;
  private final ThreadPool threadPool;
  private final RestrictedUserUtils restrictedUserUtils;
  private final ChatPostCounter chatPostCounter;

  @Inject
  public ChatManager(Database database, ThreadPool threadPool,
      RestrictedUserUtils restrictedUserUtils, ChatPostCounter chatPostCounter,
      ThreadPool threadPool2, MessageSender<PacketChatMessages> chatMessagesMessageSender) {
    this.database = Preconditions.checkNotNull(database);
    this.threadPool = Preconditions.checkNotNull(threadPool);
    this.restrictedUserUtils = Preconditions.checkNotNull(restrictedUserUtils);
    this.chatMessagesMessageSender = Preconditions.checkNotNull(chatMessagesMessageSender);
    this.chatPostCounter = Preconditions.checkNotNull(chatPostCounter);
    threadPool.addMinuteTasks(chatPostCounter);
  }

  private NavigableMap<Integer, PacketChatMessage> loadData() {
    NavigableMap<Integer, PacketChatMessage> data = new ConcurrentSkipListMap<Integer, PacketChatMessage>();

    try {
      data.putAll(database.getLatestChatData());
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "チャットデータの読み込みに失敗しました", e);
    }

    return data;
  }

  private NavigableMap<Integer, PacketChatMessage> getData() {
    try {
      return data.get(STATIC_KEY);
    } catch (ExecutionException e) {
      logger.log(Level.WARNING, "チャットデータの取得に失敗しました", e);
      return null;
    }
  }

  /**
   * 発言を追加する
   * 
   * @param message
   *          発言
   * @param remoteAddress
   *          クライアント側IPアドレス
   */
  public void write(final PacketChatMessage message, String remoteAddress) {
    chatPostCounter.add(message.userCode, remoteAddress);
    if (!chatPostCounter.isAbleToPost(message.userCode, remoteAddress)) {
      try {
        database.addRestrictedUserCode(message.userCode, RestrictionType.CHAT);
        if (!remoteAddress.equals("127.0.0.1")) {
          database.addRestrictedRemoteAddress(remoteAddress, RestrictionType.CHAT);
        }
      } catch (DatabaseException e) {
        logger.log(Level.WARNING, "制限ユーザーの追加に失敗しました。処理を続行します。", e);
      }
    }

    NavigableMap<Integer, PacketChatMessage> data = getData();

    message.remoteAddress = remoteAddress;
    message.date = System.currentTimeMillis();
    synchronized (writeLock) {
      message.resId = data.isEmpty() ? 1 : (data.lastEntry().getValue().resId + 1);
      data.put(message.resId, message);
      chatMessagesMessageSender.send(PacketChatMessages.fromMessage(message));
    }

    data.remove(message.resId - Constant.CHAT_MAX_RESPONSES - 1);

    threadPool.execute(new Runnable() {
      public void run() {
        try {
          database.addChatLog(message);
        } catch (DatabaseException e) {
          logger.log(Level.WARNING, "チャットデータの保存に失敗しました", e);
        }
      }
    });
  }

  /**
   * 発言を読み込む
   * 
   * @param nextResponseId
   *          次に読み込むレスポンス番号
   * @return 発言リスト
   */
  public PacketChatMessages read(int nextResponseId) {
    NavigableMap<Integer, PacketChatMessage> data = getData();
    for (Entry<Integer, PacketChatMessage> entry : data.entrySet()) {
      PacketChatMessage chatData = entry.getValue();
      int userCode = chatData.userCode;
      String remoteAddress = chatData.remoteAddress;
      try {
        chatData.restricted = restrictedUserUtils.checkAndUpdateRestrictedUser(userCode,
            remoteAddress, RestrictionType.CHAT);
      } catch (DatabaseException e) {
        logger.log(Level.WARNING, "制限ユーザーの取得に失敗しました。処理を続行します。", entry);
      }
    }
    return PacketChatMessages.fromMessages(data.tailMap(nextResponseId).values());
  }

  /**
   * チャットメッセージ送信インスタンスを返す
   * 
   * @return チャットメッセージ送信インスタンス
   */
  public MessageSender<PacketChatMessages> getChatMessagesMessageSender() {
    return chatMessagesMessageSender;
  }
}
