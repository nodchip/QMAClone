package tv.dyndns.kishibe.qmaclone.client.chat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import tv.dyndns.kishibe.qmaclone.client.Controller;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.StatusUpdater;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessage;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketChatMessages;
import tv.dyndns.kishibe.qmaclone.client.packet.RestrictionType;

public class PanelRealtime extends Composite implements KeyDownHandler {
  private static final Logger logger = Logger.getLogger(PanelRealtime.class.getName());
  public static final int TIMER_INTERVAL = 5000;
  private static PanelRealtimeUiBinder uiBinder = GWT.create(PanelRealtimeUiBinder.class);
  private static final List<String> NG_WORDS = ImmutableList.of("エロ動画", "おっぱい", "オッパイ", "調教", "御奉仕",
      "なまぽ", "ナマポ", "ざーめん", "ザーメン", "おなにー", "オナニー", "ふぇら", "フェラ", "変態", "エロアニメ", "きんたま", "キンタマ",
      "金玉", "ちんちん", "チンチン", "れいぷ", "レイプ", "くぱぁ", "クパァ", "せんずり", "精液", "ちんぽ", "チンポ", "ケツ", "けつ",
      "Fuck", "ファック", "セックス", "オナホ", "シコシコ", "亀頭", "エロブログ", "えっち", "エッチ", "セクロス", "せくろす", "女体",
      "ゲイ", "死ね", "殺す", "ウンコ", "うんこ", "ㄘんㄘん", "淫夢");
  private static final int MIN_PLAY_COUNT_TO_SEND_MESSAGE = 10;

  interface PanelRealtimeUiBinder extends UiBinder<Widget, PanelRealtime> {
  }

  @UiField
  TextBox textBoxName;
  @UiField
  TextBox textBoxBody;
  @UiField
  Button buttonSend;
  @UiField(provided = true)
  CellListChatLog cellListChatLog;

  private int nextArrayIndex = 1;
  private final StatusUpdater<PacketChatMessages> updater = new StatusUpdater<PacketChatMessages>(
      PacketChatMessages.class.getName(), TIMER_INTERVAL) {
    @Override
    protected void request(AsyncCallback<PacketChatMessages> callback) {
      Service.Util.getInstance().receiveMessageFromChat(nextArrayIndex, callback);
    }

    @Override
    protected PacketChatMessages parse(String json) {
      return PacketChatMessages.Json.READER.read(json);
    }

    @Override
    protected void onReceived(PacketChatMessages status) {
      callbackRecieveMessage.onSuccess(status);
    }
  };
  private boolean isRecieving = false;
  private final LinkedList<PacketChatMessage> messages = new LinkedList<PacketChatMessage>();
  private final ListDataProvider<PacketChatMessage> dataProvider = new ListDataProvider<PacketChatMessage>(
      messages);
  private final Set<Integer> restrictedUserCodes = Sets.newHashSet();

  public PanelRealtime() {
    cellListChatLog = new CellListChatLog(dataProvider);
    cellListChatLog.setPageSize(Constant.CHAT_MAX_RESPONSES);
    initWidget(uiBinder.createAndBindUi(this));
    textBoxName.setText(UserData.get().getPlayerName());
    textBoxBody.addKeyDownHandler(this);

    Service.Util.getInstance().getRestrictedUserCodes(RestrictionType.CHAT,
        callbackGetRestrictedUserCodes);
  }

  private final AsyncCallback<Set<Integer>> callbackGetRestrictedUserCodes = new AsyncCallback<Set<Integer>>() {
    @Override
    public void onSuccess(Set<Integer> result) {
      restrictedUserCodes.addAll(result);
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "制限ユーザーの取得に失敗しました", caught);
    }
  };

  private void sendMessage() {
    if (!checkContents()) {
      return;
    }

    if (textBoxName.getText().equals("未初期化です")) {
      Controller.getInstance().log("「未初期化です」名義で発言することはできません。名前を変更して下さい。");
      return;
    }

    setEnabled(false);

    UserData record = UserData.get();
    PacketChatMessage message = new PacketChatMessage();
    message.name = textBoxName.getText();
    message.body = textBoxBody.getText();
    message.imageFileName = record.getImageFileName();
    message.classLevel = record.getClassLevel();
    message.userCode = record.getUserCode();

    // 制限ユーザーからの発言をサーバーに送信しない
    if (restrictedUserCodes.contains(message.userCode)) {
      if (!messages.isEmpty()) {
        message.resId = messages.getFirst().resId + 1;
      }
      addMessageToPanel(PacketChatMessages.fromMessage(message));
      setEnabled(true);
      return;
    }

    Service.Util.getInstance().sendMessageToChat(message, callbackSendMessage);
  }

  private final AsyncCallback<Void> callbackSendMessage = new AsyncCallback<Void>() {
    public void onSuccess(Void result) {
      textBoxBody.setText("");
      setEnabled(true);
      textBoxBody.setFocus(false);
      textBoxBody.setFocus(true);
      recieveMessage();
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "チャットメッセージの送信中にエラーが発生しました", caught);
    }
  };

  private boolean checkContents() {
    if (textBoxName.getText().trim().isEmpty()) {
      return false;
    }

    if (textBoxBody.getText().trim().isEmpty()) {
      return false;
    }

    return true;
  }

  private void recieveMessage() {
    if (isRecieving) {
      return;
    }
    isRecieving = true;

    Service.Util.getInstance().receiveMessageFromChat(nextArrayIndex, callbackRecieveMessage);
  }

  private final AsyncCallback<PacketChatMessages> callbackRecieveMessage = new AsyncCallback<PacketChatMessages>() {
    public void onSuccess(PacketChatMessages result) {
      if (result != null && result.list != null && !result.list.isEmpty()) {
        List<PacketChatMessage> incomingMessages = result.list;
        nextArrayIndex = incomingMessages.get(incomingMessages.size() - 1).resId + 1;
        addMessageToPanel(result);
      }

      isRecieving = false;
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "チャットメッセージの取得中にエラーが発生しました", caught);
    }
  };

  private void addMessageToPanel(PacketChatMessages incomingMessages) {
    boolean updated = false;
    for (PacketChatMessage message : incomingMessages.list) {
      if (message == null) {
        logger.log(Level.WARNING, "nullのチャットデータが渡されました"
            + Arrays.deepToString(incomingMessages.list.toArray(new PacketChatMessage[0])));
        continue;
      }

      PacketChatMessage first = messages.isEmpty() ? null : messages.getFirst();
      if (!shouldShow(message, first)) {
        continue;
      }

      messages.addFirst(message);
      updated = true;
    }

    while (messages.size() > Constant.CHAT_MAX_RESPONSES) {
      messages.removeLast();
    }

    if (updated) {
      dataProvider.refresh();
    }
  }

  @VisibleForTesting
  boolean shouldShow(PacketChatMessage message, PacketChatMessage lastMessage) {
    // チャット履歴が空なら表示する
    if (lastMessage == null) {
      return true;
    }

    // 最後の投稿と同じメッセージなら表示しない
    // 自分の投稿が2回表示されるバグへの対処
    if (Objects.equal(message.body, lastMessage.body) && message.userCode == lastMessage.userCode
        && Objects.equal(message.remoteAddress, lastMessage.remoteAddress)) {
      return false;
    }

    // このユーザーから投稿されたものなら表示する
    if (message.userCode == UserData.get().getUserCode()) {
      return true;
    }

    // 制限ユーザーからの投稿は表示しない
    if (message.restricted) {
      return false;
    }

    // idが最後に表示されたものと同じか古ければ表示しない
    if (message.resId <= lastMessage.resId) {
      return false;
    }

    // NGワードが含まれているものは表示しない
    for (String word : NG_WORDS) {
      if (message.body.contains(word)) {
        return false;
      }
    }

    return true;
  }

  public void setEnabled(boolean enabled) {
    textBoxName.setEnabled(enabled);
    textBoxBody.setEnabled(enabled);
    buttonSend.setEnabled(enabled);
  }

  protected void onLoad() {
    super.onLoad();
    recieveMessage();
    updater.start();
  }

  @Override
  protected void onUnload() {
    updater.stop();
    super.onUnload();
  }

  @Override
  public void onKeyDown(KeyDownEvent event) {
    if (event.getSource() == textBoxBody) {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        sendMessage();
      }
    }
  }

  @UiHandler("buttonSend")
  void onButtonSend(ClickEvent e) {
    sendMessage();
  }
}
