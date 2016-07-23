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
package tv.dyndns.kishibe.qmaclone.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData.WebSocketUsage;
import tv.dyndns.kishibe.qmaclone.client.setting.PanelSettingChat;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserData implements CloseHandler<Window> {
  private static final Logger logger = Logger.getLogger(UserData.class.getName());
  private static final UserData INSTANCE = new UserData();
  private final RepeatingCommand commandLoadFromServer = new RepeatingCommand() {
    @Override
    public boolean execute() {
      loadFromServer();
      return false;
    }
  };

  public static UserData get() {
    return INSTANCE;
  }

  private UserData() {
    if (GWT.isClient()) {
      Window.addCloseHandler(this);
      load();
    }
  }

  private PacketUserData data = new PacketUserData();
  private final List<UserDataLoadListener> loadListeners = new ArrayList<UserDataLoadListener>();
  private boolean ready = false;
  private static final String KEY_USER_CODE = "user_code";

  public void load() {
    String s = Cookies.getCookie(KEY_USER_CODE);
    if (Strings.isNullOrEmpty(s) || 8 < s.length() || s.equals("0")) {
      createUserCode();
      return;
    }
    data.userCode = Integer.parseInt(s);

    loadFromServer();
  }

  private void createUserCode() {
    Service.Util.getInstance().getNewUserCode(callbackCreateUserCode);
  }

  private final AsyncCallback<Integer> callbackCreateUserCode = new AsyncCallback<Integer>() {
    public void onSuccess(Integer result) {
      data.userCode = result;
      Cookies.setCookie(KEY_USER_CODE, Integer.toString(data.userCode), getExpireTime());
      save();
      callLoadListeners();
    }

    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "新規ユーザーコードの取得に失敗しました", caught);
    }
  };

  private void loadFromServer() {
    Service.Util.getInstance().loadUserData(data.userCode, callbackLoadFromServer);
  }

  private final AsyncCallback<PacketUserData> callbackLoadFromServer = new AsyncCallback<PacketUserData>() {
    public void onSuccess(PacketUserData result) {
      data = result;
      Cookies.setCookie(KEY_USER_CODE, Integer.toString(data.userCode), getExpireTime());
      save();

      callLoadListeners();

      for (int ignoreUserCode : data.ignoreUserCodes) {
        PanelSettingChat.getInstance().addIgnoreUserCodeButton(ignoreUserCode);
      }
    }

    public void onFailure(Throwable caught) {
      Scheduler.get().scheduleFixedDelay(commandLoadFromServer, 5000);
      logger.log(Level.WARNING, "ユーザー情報の取得中にエラーが発生しました。パケットを再送します。", caught);
    }
  };

  private void callLoadListeners() {
    ready = true;
    for (UserDataLoadListener userDataLoadListener : loadListeners) {
      userDataLoadListener.onLoad();
    }
    loadListeners.clear();
  }

  public void addLoadListener(UserDataLoadListener listener) {
    if (ready) {
      listener.onLoad();
    } else {
      loadListeners.add(listener);
    }
  }

  public void save() {
    Cookies.setCookie(KEY_USER_CODE, Integer.toString(data.userCode), getExpireTime());
    Service.Util.getInstance().saveUserData(data, callbackSaveUserData);
  }

  private final AsyncCallback<Void> callbackSaveUserData = new AsyncCallback<Void>() {
    @Override
    public void onSuccess(Void result) {
    }

    @Override
    public void onFailure(Throwable caught) {
      logger.log(Level.WARNING, "ユーザーデータの保存に失敗しました", caught);
    }
  };

  private Date getExpireTime() {
    long currentTime = System.currentTimeMillis();
    currentTime += 90L * 24L * 60L * 60L * 1000L;
    return new Date(currentTime);
  }

  public void setPlayerName(String playerName) {
    data.playerName = playerName;
  }

  public String getPlayerName() {
    return data.playerName;
  }

  public void setHighScore(int score) {
    data.highScore = score;
  }

  public int getHighScore() {
    return data.highScore;
  }

  public void setAverageScore(int score) {
    data.averageScore = score;
  }

  public int getAverageScore() {
    return data.averageScore;
  }

  public void setPlayCount(int playCount) {
    data.playCount = playCount;
  }

  public int getPlayCount() {
    return data.playCount;
  }

  public void setRating(int rating) {
    data.rating = rating;
  }

  public int getRating() {
    return data.rating;
  }

  public void setLevelName(int levelName) {
    data.levelName = levelName;
  }

  public int getLevelName() {
    return data.levelName;
  }

  public void setLevelNumber(int levelNumber) {
    data.levelNumber = Math.max(0, levelNumber);
  }

  public int getLevelNumber() {
    return data.levelNumber;
  }

  public void setAvarageRank(float averageRank) {
    data.averageRank = averageRank;
  }

  public float getAverageRank() {
    return data.averageRank;
  }

  public void setGenres(Set<ProblemGenre> genres) {
    data.genres = genres;
  }

  public Set<ProblemGenre> getGenre() {
    return data.genres;
  }

  public void setTypes(Set<ProblemType> types) {
    data.types = types;
  }

  public Set<ProblemType> getTypes() {
    return data.types;
  }

  public void setGreeting(String greeting) {
    data.greeting = greeting;
  }

  public String getGreeting() {
    return data.greeting;
  }

  public void setClassLevel(int classLevel) {
    data.classLevel = classLevel;
  }

  public int getClassLevel() {
    return data.classLevel;
  }

  public void setUserCode(int userCode) {
    data.userCode = userCode;
    if (GWT.isClient()) {
      Cookies.setCookie(KEY_USER_CODE, "" + data.userCode, getExpireTime());
    }
  }

  public int getUserCode() {
    return data.userCode;
  }

  public void setImageFileName(String imageFileName) {
    data.imageFileName = imageFileName;
  }

  public String getImageFileName() {
    return data.imageFileName;
  }

  public int[][][] getCorrectCount() {
    return data.correctCount;
  }

  public void setPlaySound(boolean playSound) {
    data.playSound = playSound;
  }

  public boolean isPlaySound() {
    return data.playSound;
  }

  public boolean isMultiGenre() {
    return data.multiGenre;
  }

  public void setMultiGenre(boolean multiGenre) {
    data.multiGenre = multiGenre;
  }

  public boolean isMultiType() {
    return data.multiType;
  }

  public void setMultiType(boolean multiType) {
    data.multiType = multiType;
  }

  public int getDifficultSelect() {
    return data.difficultSelect;
  }

  public void setDifficultSelect(int difficultSelect) {
    data.difficultSelect = difficultSelect;
  }

  public NewAndOldProblems getNewAndOldProblems() {
    return data.newAndOldProblems;
  }

  public void setNewAndOldProblems(NewAndOldProblems newAndOldProblems) {
    data.newAndOldProblems = newAndOldProblems;
  }

  public boolean isRankingMove() {
    return data.rankingMove;
  }

  public void setRankingMove(boolean rankingMove) {
    data.rankingMove = rankingMove;
  }

  public int getBbsDispInfo() {
    return data.bbsDispInfo;
  }

  public void setBbsDispInfo(int dispInfo) {
    data.bbsDispInfo = dispInfo;
  }

  public boolean isBbsAge() {
    return data.bbsAge;
  }

  public void setBbsAge(boolean age) {
    data.bbsAge = age;
  }

  public int getPrefecture() {
    return data.prefecture;
  }

  public void setPrefecture(int prefecture) {
    data.prefecture = prefecture;
  }

  public boolean isChatEnabled() {
    return data.chat;
  }

  public void setChatEnabled(boolean chatEnabled) {
    data.chat = chatEnabled;
  }

  public boolean isPublicEvent() {
    return data.publicEvent;
  }

  public void setPublicEvent(boolean publicEvent) {
    data.publicEvent = publicEvent;
  }

  public boolean isHideAnswer() {
    return data.hideAnswer;
  }

  public void setHideAnswer(boolean hideAnswer) {
    data.hideAnswer = hideAnswer;
  }

  public boolean isShowInfo() {
    return data.showInfo;
  }

  public void setShowInfo(boolean showInfo) {
    data.showInfo = showInfo;
  }

  public void setReflectEventResult(boolean reflectEventResult) {
    data.reflectEventResult = reflectEventResult;
  }

  public boolean isReflectEventResult() {
    return data.reflectEventResult;
  }

  public void setWebSocketUsage(WebSocketUsage webSocketUsage) {
    data.webSocketUsage = webSocketUsage;
  }

  public WebSocketUsage getWebSocketUsage() {
    return data.webSocketUsage;
  }

  public void setVolatility(int volatility) {
    data.volatility = volatility;
  }

  public int getVolatility() {
    return data.volatility;
  }

  public void setQwertyHiragana(boolean qwertyHiragana) {
    data.qwertyHiragana = qwertyHiragana;
  }

  public boolean isQwertyHiragana() {
    return data.qwertyHiragana;
  }

  public void setQwertyKatakana(boolean qwertyKatakana) {
    data.qwertyKatakana = qwertyKatakana;
  }

  public boolean isQwertyKatakana() {
    return data.qwertyKatakana;
  }

  public void setQwertyAlphabet(boolean qwertyAlphabet) {
    data.qwertyAlphabet = qwertyAlphabet;
  }

  public boolean isQwertyAlphabet() {
    return data.qwertyAlphabet;
  }

  public boolean isRegisterCreatedProblem() {
    return data.registerCreatedProblem;
  }

  public void setRegisterCreatedProblem(boolean registerCreatedProblem) {
    data.registerCreatedProblem = registerCreatedProblem;
  }

  public boolean isRegisterIndicatedProblem() {
    return data.registerIndicatedProblem;
  }

  public void setRegisterIndicatedProblem(boolean registerIndicatedProblem) {
    data.registerIndicatedProblem = registerIndicatedProblem;
  }

  public String getGooglePlusId() {
    return data.googlePlusId;
  }

  public void setGooglePlusId(String googlePlusId) {
    data.googlePlusId = googlePlusId;
  }

  public String getTheme() {
    return data.theme;
  }

  public void setTheme(String theme) {
    data.theme = theme;
  }

  @Override
  public void onClose(CloseEvent<Window> event) {
    // save();
  }
}
