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

import static com.google.common.base.Objects.equal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.constant.Constant;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.util.HasIndex;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketUserData implements IsSerializable {
  public static enum WebSocketUsage implements IsSerializable, HasIndex {
    Default(0), On(1), Off(2);
    private final int index;

    private WebSocketUsage(int index) {
      this.index = index;
    }

    @Override
    public int getIndex() {
      return index;
    }
  }

  public String playerName;
  public String greeting;
  public int highScore;
  public int averageScore;
  public int playCount;
  public int rating;
  public int levelName;
  public int levelNumber;
  public int prefecture;
  public float averageRank;
  public Set<ProblemGenre> genres;
  public Set<ProblemType> types;
  public int classLevel;
  public int userCode;
  public String imageFileName;
  public int[][][] correctCount;
  public boolean playSound;
  public boolean multiGenre;
  public boolean multiType;
  public int difficultSelect;
  public boolean rankingMove;
  public int bbsDispInfo;
  public boolean bbsAge;
  public boolean chat;
  public NewAndOldProblems newAndOldProblems;
  public Set<Integer> ignoreUserCodes;
  public int timerMode;
  public boolean publicEvent;
  public boolean hideAnswer;
  public boolean showInfo;
  public boolean reflectEventResult;
  public WebSocketUsage webSocketUsage;
  public int volatility;
  public boolean qwertyHiragana;
  public boolean qwertyKatakana;
  public boolean qwertyAlphabet;
  public boolean registerCreatedProblem;
  public boolean registerIndicatedProblem;
  public String googlePlusId;
  public String theme;

  public PacketUserData() {
    playerName = "未初期化です";
    greeting = "よろしく～！";
    imageFileName = "noimage.jpg";
    correctCount = new int[ProblemGenre.values().length][ProblemType.values().length][2];
    playSound = false;
    rankingMove = true;
    rating = 1300;
    ignoreUserCodes = new HashSet<Integer>();
    bbsDispInfo = Constant.BBS_DISPLAY_INFO_ALL_DATA;
    bbsAge = true;
    timerMode = 0;
    prefecture = 0;
    genres = Collections.emptySet();
    types = Collections.emptySet();
    chat = true;
    publicEvent = true;
    showInfo = true;
    newAndOldProblems = NewAndOldProblems.Both;
    reflectEventResult = false;
    webSocketUsage = WebSocketUsage.Default;
    difficultSelect = Constant.DIFFICULT_SELECT_EASY;
    theme = "";
  }

  public void setReflectEventResult(boolean reflectEventResult) {
    this.reflectEventResult = reflectEventResult;
  }

  public boolean isReflectEventResult() {
    return reflectEventResult;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("playerName", playerName).add("greeting", greeting)
        .add("highScore", highScore).add("averageScore", averageScore).add("playCount", playCount)
        .add("rating", rating).add("levelName", levelName).add("levelNumber", levelNumber)
        .add("prefecture", prefecture).add("averageRank", averageRank).add("genres", genres)
        .add("types", types).add("classLevel", classLevel).add("userCode", userCode)
        .add("imageFileName", imageFileName).add("correctCount", correctCount)
        .add("playSound", playSound).add("multiGenre", multiGenre).add("multiType", multiType)
        .add("difficultSelect", difficultSelect).add("rankingMove", rankingMove)
        .add("bbsDispInfo", bbsDispInfo).add("bbsAge", bbsAge).add("chat", chat)
        .add("newAndOldProblems", newAndOldProblems).add("ignoreUserCodes", ignoreUserCodes)
        .add("timerMode", timerMode).add("publicEvent", publicEvent).add("hideAnswer", hideAnswer)
        .add("showInfo", showInfo).add("reflectEventResult", reflectEventResult)
        .add("webSocketUsage", webSocketUsage).add("volatility", volatility)
        .add("qwertyHiragana", qwertyHiragana).add("qwertyKatakana", qwertyKatakana)
        .add("qwertyAlphabet", qwertyAlphabet)
        .add("registerCreatedProblem", registerCreatedProblem)
        .add("registerIndicatedProblem", registerIndicatedProblem)
        .add("googlePlusId", googlePlusId).add("THEME", theme).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PacketUserData)) {
      return false;
    }
    PacketUserData rh = (PacketUserData) obj;
    return equal(playerName, rh.playerName) && equal(greeting, rh.greeting)
        && highScore == rh.highScore && averageScore == rh.averageScore
        && playCount == rh.playCount && rating == rh.rating && levelName == rh.levelName
        && levelNumber == rh.levelNumber && prefecture == rh.prefecture
        && averageRank == rh.averageRank && equal(genres, rh.genres) && equal(types, rh.types)
        && classLevel == rh.classLevel && userCode == rh.userCode
        && equal(imageFileName, rh.imageFileName)
        && Arrays.deepEquals(correctCount, rh.correctCount) && playSound == rh.playSound
        && multiGenre == rh.multiGenre && multiType == rh.multiType
        && difficultSelect == rh.difficultSelect && rankingMove == rh.rankingMove
        && bbsDispInfo == rh.bbsDispInfo && bbsAge == rh.bbsAge && chat == rh.chat
        && newAndOldProblems == rh.newAndOldProblems && equal(ignoreUserCodes, rh.ignoreUserCodes)
        && timerMode == rh.timerMode && publicEvent == rh.publicEvent
        && hideAnswer == rh.hideAnswer && showInfo == rh.showInfo
        && reflectEventResult == rh.reflectEventResult && webSocketUsage == rh.webSocketUsage
        && volatility == rh.volatility && qwertyHiragana == rh.qwertyHiragana
        && qwertyKatakana == rh.qwertyKatakana && qwertyAlphabet == rh.qwertyAlphabet
        && registerCreatedProblem == rh.registerCreatedProblem
        && registerIndicatedProblem == rh.registerIndicatedProblem
        && equal(googlePlusId, rh.googlePlusId) && equal(theme, rh.theme);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(playerName, greeting, highScore, averageScore, playCount, rating,
        levelName, levelNumber, prefecture, averageRank, genres, types, classLevel, userCode,
        imageFileName, correctCount, playSound, multiGenre, multiType, difficultSelect,
        rankingMove, bbsDispInfo, bbsAge, chat, newAndOldProblems, ignoreUserCodes, timerMode,
        publicEvent, hideAnswer, showInfo, reflectEventResult, webSocketUsage, volatility,
        qwertyHiragana, qwertyKatakana, qwertyAlphabet, registerCreatedProblem,
        registerIndicatedProblem, googlePlusId, theme);
  }
}
