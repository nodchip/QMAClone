package tv.dyndns.kishibe.qmaclone.client.lobby;

import static java.lang.String.valueOf;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.PopupPanelEventRooms;
import tv.dyndns.kishibe.qmaclone.client.Service;
import tv.dyndns.kishibe.qmaclone.client.UserData;
import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRankingData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketRoomKey;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketServerStatus;
import tv.dyndns.kishibe.qmaclone.client.ui.WidgetMultiItemSelector;
import tv.dyndns.kishibe.qmaclone.client.util.CommandRunner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LobbyUi extends Composite {
  private static final Logger logger = Logger.getLogger(LobbyUi.class.getName());
  private static final LobbyUiUiBinder uiBinder = GWT.create(LobbyUiUiBinder.class);
  private static LobbyUi instance = null;
  private final SceneLobby scene;
  // BugTrack-QMAClone/388 - QMAClone wiki
  // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack%2DQMAClone%2F388#1322751108
  // BugTrack-QMAClone/397
  // http://kishibe.dyndns.tv/qmaclone/wiki/wiki.cgi?page=BugTrack-QMAClone%2F397
  @VisibleForTesting
  static final List<String> LEVEL_NAMES = ImmutableList.of("修練", "見習", "初級", "中級", "上級", "魔導",
      "大魔導", "賢者", "大賢者", "青銅", "白銀", "黄金", "白金", "金剛", "天青", "紅玉", "翡翠", "黄玉", "紫宝", "琥珀", "瑠璃");
  private static final String[][] DIFFICULTIES = {
      { "全難易度から出題する", valueOf(Constant.DIFFICULT_SELECT_NORMAL) },
      { "難問を出題する", valueOf(Constant.DIFFICULT_SELECT_DIFFICULT) },
      { "やや難問を出題する", valueOf(Constant.DIFFICULT_SELECT_LITTLE_DIFFICULT) },
      { "やや易問を出題する", valueOf(Constant.DIFFICULT_SELECT_LITTLE_EASY) },
      { "易問を出題する", valueOf(Constant.DIFFICULT_SELECT_EASY) }, };
  @UiField
  ListBox listBoxLevelNumber;
  @UiField
  ListBox listBoxLevelName;
  @UiField
  ListBox listBoxPrefecture;
  @UiField
  TextBox textBoxPlayerName;
  @UiField(provided = true)
  WidgetMultiItemSelector<ProblemGenre> multiItemSelectorGenre;
  @UiField(provided = true)
  WidgetMultiItemSelector<ProblemType> multiItemSelectorType;
  @UiField
  TextBox textBoxGreeting;
  @UiField
  ListBox listBoxDifficultSelect;
  @UiField
  ListBox listBoxNewProblem;
  @UiField
  Button buttonGameVsCom;
  @UiField
  Button buttonGameAllClass;
  @UiField
  Button buttonGameEvent;
  @UiField
  Button buttonGameTheme;
  @UiField
  TextBox textBoxEventName;
  @UiField
  Button buttonShowEventRooms;
  @UiField
  CheckBox checkBoxPublicEvent;
  @UiField
  ListBox listBoxTheme;
  @UiField
  SpanElement spanTopPageCount;
  @UiField
  SpanElement spanProblems;
  @UiField
  SpanElement spanTotalSessions;
  @UiField
  SpanElement spanCurrentSessions;
  @UiField
  SpanElement spanTotalPlayers;
  @UiField
  SpanElement spanCurrentPlayers;
  @UiField
  SpanElement spanLoginPlayers;
  @UiField
  SpanElement spanActivePlayers;
  @UiField
  SpanElement spanWaiting;
  @UiField
  SpanElement spanPlayCount;
  @UiField
  SpanElement spanHighScore;
  @UiField
  SpanElement spanAverageScore;
  @UiField
  SpanElement spanRating;
  @UiField
  SpanElement spanAverageRank;
  @UiField
  SpanElement spanClass;
  @UiField
  SpanElement spanUserCode;
  @UiField
  Button buttonShowUserCode;
  @UiField
  HTMLPanel panelInformation;
  @UiField
  SpanElement spanPlayerHistory;
  @VisibleForTesting
  boolean specialLevelName = false;
  private final CommandRunner initializers = new CommandRunner(Arrays.asList(new Runnable() {
    @Override
    public void run() {
      Service.Util.getInstance().getGeneralRanking(callbackGetRankingData);
    }
  }, new Runnable() {
    public void run() {
      Service.Util.getInstance().getThemeModeThemes(callbackGetThemeModeThemes);
    }
  }));

  interface LobbyUiUiBinder extends UiBinder<Widget, LobbyUi> {
  }

  public LobbyUi(SceneLobby sceneRegistration) {
    multiItemSelectorGenre = new WidgetMultiItemSelector<ProblemGenre>("ジャンル",
        ProblemGenre.values(), 3);
    multiItemSelectorType = new WidgetMultiItemSelector<ProblemType>("出題形式", ProblemType.values(),
        4);
    initWidget(uiBinder.createAndBindUi(this));

    instance = this;

    if (Constant.FIXED_CLASS_LEVEL >= 0) {
      UserData.get().setClassLevel(Constant.FIXED_CLASS_LEVEL);
    }

    this.scene = sceneRegistration;

    UserData record = UserData.get();

    // 階級名
    for (String levelName : LEVEL_NAMES) {
      listBoxLevelName.addItem(levelName);
    }
    listBoxLevelName.addChangeHandler(levelNameChangeHandler);

    int levelName = record.getLevelName();
    if (levelName == Integer.MAX_VALUE) {
      levelName = 0;
    }
    listBoxLevelName.setSelectedIndex(Math.min(levelName, listBoxLevelName.getItemCount() - 1));

    // 階級の数字
    for (int i = 1; i <= 10; ++i) {
      listBoxLevelNumber.addItem(valueOf(i));
    }

    int levelNumber = record.getLevelNumber();
    if (levelNumber == Integer.MAX_VALUE) {
      levelNumber = 0;
    }
    listBoxLevelNumber.setSelectedIndex(levelNumber);

    // 地域
    for (String prefectureName : Constant.PREFECTURE_NAMES) {
      listBoxPrefecture.addItem(prefectureName);
    }
    int prefecture = record.getPrefecture();
    listBoxPrefecture.setSelectedIndex(prefecture);

    // プレイヤー名入力
    textBoxPlayerName.setText(record.getPlayerName());

    // ジャンル選択
    multiItemSelectorGenre.set(UserData.get().getGenre());

    // 出題形式選択
    multiItemSelectorType.set(UserData.get().getTypes());

    // 挨拶
    if (!record.getGreeting().isEmpty()) {
      textBoxGreeting.setText(record.getGreeting());
    }

    // 難問の出題
    for (String[] difficulty : DIFFICULTIES) {
      String item = difficulty[0];
      String value = difficulty[1];
      listBoxDifficultSelect.addItem(item, value);
    }
    int difficultSelect = UserData.get().getDifficultSelect();
    for (int i = 0; i < listBoxDifficultSelect.getItemCount(); ++i) {
      if (difficultSelect == Integer.parseInt(listBoxDifficultSelect.getValue(i))) {
        listBoxDifficultSelect.setSelectedIndex(i);
        break;
      }
    }

    // 新問の出題
    listBoxNewProblem.setSelectedIndex(UserData.get().getNewAndOldProblems().ordinal());

    // イベント
    checkBoxPublicEvent.setValue(UserData.get().isPublicEvent());

    setPlayerRecord();

    // 情報パネル
    updateInfomationPanel();

    initializers.run();
  }

  private static final int[] SPECIAL_LEVEL_NAME_RANGE = { 0, 1, 5, 21, 50 };
  private static final String[] SPECIAL_LEVEL_NAMES = { "賢神", "賢帝", "賢王", "賢将" };
  private final AsyncCallback<List<List<PacketRankingData>>> callbackGetRankingData = new AsyncCallback<List<List<PacketRankingData>>>() {
    @Override
    public void onSuccess(List<List<PacketRankingData>> rankingData) {
      updateSpecialLevelName(rankingData);
      initializers.run();
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "ランキングの取得に失敗しました", caught);

      initializers.run();
    }
  };

  @VisibleForTesting
  void updateSpecialLevelName(List<List<PacketRankingData>> rankingData) {
    if (rankingData == null || rankingData.size() < 4) {
      logger.log(Level.WARNING, "取得したランキング情報が壊れています");
      return;
    }

    int userCode = UserData.get().getUserCode();
    List<PacketRankingData> ratingRanking = rankingData.get(3);

    for (int nameIndex = 0; !specialLevelName && nameIndex < 4; ++nameIndex) {
      int begin = SPECIAL_LEVEL_NAME_RANGE[nameIndex];
      int end = SPECIAL_LEVEL_NAME_RANGE[nameIndex + 1];
      for (int i = begin; i < end; ++i) {
        if (ratingRanking.size() <= i) {
          continue;
        }

        if (userCode != ratingRanking.get(i).userCode) {
          continue;
        }

        Preconditions.checkState(listBoxLevelName.getItemCount() == LEVEL_NAMES.size());
        listBoxLevelName.addItem(SPECIAL_LEVEL_NAMES[nameIndex]);
        listBoxLevelName.setSelectedIndex(listBoxLevelName.getItemCount() - 1);
        listBoxLevelNumber.setEnabled(false);
        listBoxLevelName.addChangeHandler(levelNameChangeHandler);
        specialLevelName = true;
        return;
      }
    }
  }

  @VisibleForTesting
  final ChangeHandler levelNameChangeHandler = new ChangeHandler() {
    @Override
    public void onChange(ChangeEvent event) {
      listBoxLevelNumber.setEnabled(listBoxLevelName.getSelectedIndex() < LEVEL_NAMES.size());
    }
  };

  public static LobbyUi getInstance() {
    return instance;
  }

  private final AsyncCallback<List<List<String>>> callbackGetThemeModeThemes = new AsyncCallback<List<List<String>>>() {
    public void onSuccess(List<List<String>> result) {
      updateThemeMode(result);
      initializers.run();
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "テーマモード一覧の取得に失敗しました", caught);

      initializers.run();
    }
  };

  @VisibleForTesting
  void updateThemeMode(List<List<String>> themeMode) {
    listBoxTheme.clear();
    listBoxTheme.addItem("テーマを選んでください", "");

    int index = 1;
    for (ProblemGenre genre : ProblemGenre.values()) {
      listBoxTheme.addItem("", "");
      listBoxTheme.addItem(genre.toString(), "");

      List<String> themes = themeMode.get(genre.getIndex());
      for (String theme : themes) {
        String item = index++ + " " + theme;
        String value = theme;
        listBoxTheme.addItem(item, value);
      }
    }

    for (int i = 0; i < listBoxTheme.getItemCount(); ++i) {
      String value = listBoxTheme.getValue(i);
      String theme = UserData.get().getTheme();
      if (value.equals(theme)) {
        listBoxTheme.setSelectedIndex(i);
        break;
      }
    }
  }

  public void setPlayerRecord() {
    UserData record = UserData.get();

    int playCount = record.getPlayCount();
    int highScore = record.getHighScore();
    int averageScore = record.getAverageScore();
    int rating = record.getRating();
    double averageRank = record.getAverageRank();
    int classLevel = record.getClassLevel();
    int classNameIndex = classLevel / Constant.STEP_PER_CLASS_LEVEL;
    if (classNameIndex >= Constant.MAX_CLASS_LEVEL) {
      classNameIndex = Constant.MAX_CLASS_LEVEL;
    }

    if (record.getPlayCount() == Integer.MAX_VALUE) {
      record.setPlayCount(0);
      record.setHighScore(0);
      record.setAverageScore(0);
      record.setRating(0);

      playCount = 0;
      highScore = 0;
      averageScore = 0;
      rating = 0;
    }

    String averageRankString;
    if (averageRank > 0.9) {
      averageRankString = valueOf(averageRank);
      if (averageRankString.length() > 4) {
        averageRankString = averageRankString.substring(0, 4);
      }
    } else {
      averageRankString = "未プレイ";
    }

    spanPlayCount.setInnerText(valueOf(playCount));
    spanHighScore.setInnerText(valueOf(highScore));
    spanAverageScore.setInnerText(valueOf(averageScore));
    spanRating.setInnerText(valueOf(rating));
    spanAverageRank.setInnerText(averageRankString);
    spanClass.setInnerText("(" + classLevel + ")" + Constant.getClassName(classNameIndex) + "組");
  }

  @UiHandler("buttonGameVsCom")
  void onButtonGameVsCom(ClickEvent e) {
    register(e.getSource());
  }

  @UiHandler("buttonGameAllClass")
  void onButtonGameAllClass(ClickEvent e) {
    register(e.getSource());
  }

  @UiHandler("buttonGameEvent")
  void onButtonGameEvent(ClickEvent e) {
    register(e.getSource());
  }

  @UiHandler("buttonGameTheme")
  void onButtonGameTheme(ClickEvent e) {
    register(e.getSource());
  }

  @UiHandler("buttonShowEventRooms")
  void onButtonShowEventRooms(ClickEvent e) {
    showEventRooms();
  }

  @UiHandler("buttonShowUserCode")
  void onButtonShowUserCode(ClickEvent e) {
    spanUserCode.setInnerText(valueOf(UserData.get().getUserCode()));
    buttonShowUserCode.setVisible(false);
  }

  public void updateInfomationPanel() {
    panelInformation.setVisible(UserData.get().isShowInfo());
  }

  private void setEnabled(boolean enabled) {
    FocusWidget[] widgets = { listBoxLevelNumber, listBoxLevelName, listBoxPrefecture,
        textBoxPlayerName, textBoxGreeting, listBoxDifficultSelect, listBoxNewProblem,
        buttonGameVsCom, buttonGameAllClass, buttonGameEvent, buttonGameTheme, textBoxEventName,
        buttonShowEventRooms, checkBoxPublicEvent, listBoxTheme, buttonShowUserCode, };
    for (FocusWidget widget : widgets) {
      widget.setEnabled(enabled);
    }
    multiItemSelectorGenre.setEnabled(enabled);
    multiItemSelectorType.setEnabled(enabled);
  }

  public void setLastestPlayers(List<PacketPlayerSummary> playerSummaries) {
    StringBuilder sb = new StringBuilder();
    for (PacketPlayerSummary playerSummary : playerSummaries) {
      if (sb.length() != 0) {
        sb.append('\n');
      }
      sb.append(playerSummary.level).append(' ').append(playerSummary.name);
    }
    spanPlayerHistory.setInnerHTML(new SafeHtmlBuilder().appendEscapedLines(sb.toString())
        .toSafeHtml().asString());
  }

  private boolean checkContents() {
    if (textBoxPlayerName.getText().trim().length() == 0) {
      return false;
    }

    if (textBoxGreeting.getText().trim().length() == 0) {
      return false;
    }

    if (multiItemSelectorGenre.get().isEmpty()) {
      return false;
    }

    if (multiItemSelectorType.get().isEmpty()) {
      return false;
    }

    return true;
  }

  public void setServerStatus(PacketServerStatus serverStatus) {
    spanTotalSessions.setInnerText(valueOf(serverStatus.numberOfTotalSessions));
    spanCurrentSessions.setInnerText(valueOf(serverStatus.numberOfCurrentSessions));
    spanTotalPlayers.setInnerText(valueOf(serverStatus.numberOfTotalPlayers));
    spanCurrentPlayers.setInnerText(valueOf(serverStatus.numberOfCurrentPlayers));
    spanLoginPlayers.setInnerText(valueOf(serverStatus.numberOfLoginPlayers));
    spanTopPageCount.setInnerText(valueOf(serverStatus.numberOfPageView));
    spanProblems.setInnerText(valueOf(serverStatus.numberOfProblems));
    spanActivePlayers.setInnerText(valueOf(serverStatus.numberOfActivePlayers));
    spanWaiting.setInnerText(valueOf(serverStatus.numberOfPlayersInWhole));

    setPlayerRecord();
  }

  public PacketPlayerSummary getPlayerSummary() {
    PacketPlayerSummary player = new PacketPlayerSummary();

    // 階級
    player.level = listBoxLevelName.getItemText(listBoxLevelName.getSelectedIndex());
    if (listBoxLevelName.getSelectedIndex() != LEVEL_NAMES.size()) {
      player.level += listBoxLevelNumber.getItemText(listBoxLevelNumber.getSelectedIndex());
    }

    // プレイヤー名
    player.name = textBoxPlayerName.getText();
    if (player.name.length() > Constant.MAX_PLAYER_NAME_LENGTH) {
      player.name = player.name.substring(0, Constant.MAX_PLAYER_NAME_LENGTH);
    }

    // 県
    player.prefecture = Constant.PREFECTURE_NAMES[listBoxPrefecture.getSelectedIndex()];

    // レーティング
    player.rating = UserData.get().getRating();

    return player;
  }

  public Set<ProblemGenre> getGenres() {
    return multiItemSelectorGenre.get();
  }

  public void setGenres(Set<ProblemGenre> genres) {
    multiItemSelectorGenre.set(genres);
  }

  public Set<ProblemType> getTypes() {
    return multiItemSelectorType.get();
  }

  public void setTypes(Set<ProblemType> types) {
    multiItemSelectorType.set(types);
  }

  public boolean getMultiGenre() {
    return multiItemSelectorGenre.isMultiSelect();
  }

  public boolean getMultiType() {
    return multiItemSelectorType.isMultiSelect();
  }

  public String getGreeting() {
    return textBoxGreeting.getText();
  }

  public String getEventName() {
    return textBoxEventName.getText();
  }

  public void setEventName(String eventName) {
    textBoxEventName.setText(eventName);
  }

  public boolean getPublicEvent() {
    return checkBoxPublicEvent.getValue();
  }

  public void setPublicEvent(boolean publicEvent) {
    checkBoxPublicEvent.setValue(publicEvent);
  }

  public int getDifficultSelect() {
    int selectedIndex = listBoxDifficultSelect.getSelectedIndex();
    String selectedItem = listBoxDifficultSelect.getValue(selectedIndex);
    return Integer.parseInt(selectedItem);
  }

  public NewAndOldProblems getNewAndOldProblems() {
    return NewAndOldProblems.values()[listBoxNewProblem.getSelectedIndex()];
  }

  public void setNewProblem(NewAndOldProblems newProblem) {
    listBoxNewProblem.setSelectedIndex(newProblem.ordinal());
  }

  public String getThemeModeTheme() {
    int index = listBoxTheme.getSelectedIndex();
    if (index == -1) {
      return null;
    }

    return listBoxTheme.getValue(index);
  }

  private void showEventRooms() {
    Service.Util.getInstance().getEventRooms(callbackGetEventRooms);
  }

  private final AsyncCallback<List<PacketRoomKey>> callbackGetEventRooms = new AsyncCallback<List<PacketRoomKey>>() {
    public void onSuccess(List<PacketRoomKey> result) {
      showEventRooms(result);
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "イベント部屋一覧の取得に失敗しました", caught);
    }
  };

  private void showEventRooms(List<PacketRoomKey> eventRooms) {
    PopupPanelEventRooms popup = new PopupPanelEventRooms(this, eventRooms);
    popup.setPopupPosition(buttonShowEventRooms.getAbsoluteLeft(),
        buttonShowEventRooms.getAbsoluteTop());
    popup.show();
  }

  private void register(Object sender) {
    // プレイヤー登録
    if (!checkContents()) {
      return;
    }

    if (sender == buttonGameEvent) {
      StringBuilder sb = new StringBuilder();
      sb.append("イベント名 ： ").append(getEventName()).append("\n\nジャンル ：");

      for (ProblemGenre genre : getGenres()) {
        sb.append(" ").append(genre.toString());
      }

      sb.append("\n\n形式 ：");

      for (ProblemType type : getTypes()) {
        sb.append(" ").append(type.toString());
      }

      sb.append("\n\nこの条件でイベント戦を行ないます。\n同じ条件を指定したプレイヤーとのみマッチングします。\nよろしいですか？");

      if (!Window.confirm(sb.toString())) {
        return;
      }
    }

    if (sender == buttonGameTheme) {
      String themeModeTheme = getThemeModeTheme();
      if (Strings.isNullOrEmpty(themeModeTheme)) {
        return;
      }
    }

    setEnabled(false);

    saveUserData();

    int sessionType;
    if (sender == buttonGameVsCom) {
      sessionType = SceneLobby.SESSION_TYPE_VS_COM;
    } else if (sender == buttonGameAllClass) {
      sessionType = SceneLobby.SESSION_TYPE_WHOLE;
    } else if (sender == buttonGameEvent) {
      sessionType = SceneLobby.SESSION_TYPE_EVENT;
    } else if (sender == buttonGameTheme) {
      sessionType = SceneLobby.SESSION_TYPE_THEME;
    } else {
      sessionType = SceneLobby.SESSION_TYPE_VS_COM;
    }

    // 全体戦では手書きクイズを出さない
    Set<ProblemType> types = getTypes();
    if (sessionType == SceneLobby.SESSION_TYPE_WHOLE) {
      types.remove(ProblemType.Tegaki);
    }

    if (types.isEmpty()) {
      Window.alert("手書きクイズはβ版のため全体対戦ではお使いいただけません");
      setEnabled(true);
      return;
    }
    setTypes(types);

    scene.register(sessionType);
  }

  @VisibleForTesting
  void saveUserData() {
    UserData record = UserData.get();
    record.setPlayerName(textBoxPlayerName.getText().trim());
    record.setLevelName(listBoxLevelName.getSelectedIndex());
    record.setLevelNumber(listBoxLevelNumber.getSelectedIndex());
    record.setPrefecture(listBoxPrefecture.getSelectedIndex());
    record.setGenres(getGenres());
    record.setTypes(getTypes());
    record.setGreeting(getGreeting());
    record.setMultiGenre(getMultiGenre());
    record.setMultiType(getMultiType());
    record.setDifficultSelect(getDifficultSelect());
    record.setNewAndOldProblems(getNewAndOldProblems());
    record.setPublicEvent(getPublicEvent());
    record.setTheme(getThemeModeTheme());
    record.save();
  }
}
