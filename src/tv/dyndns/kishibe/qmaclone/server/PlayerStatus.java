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

import java.util.ArrayList;
import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerAction;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketPlayerSummary;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketResult;

public class PlayerStatus {
  private static final String LEVEL_DROP = "(落)";
  private PacketPlayerSummary playerSummary;
  private String answer;
  private boolean answered = false;
  private int score;
  private int playerId;
  private int playerListId;
  private int sessionId;
  private int timeRemain;
  private boolean requestStartingGame = false;
  private int rank;
  private int rating;
  private boolean human;
  private int skipCount;
  private String greeting;
  private String imageFileName;
  private List<PacketPlayerAction> list = new ArrayList<PacketPlayerAction>();
  private int tempRanking;
  private int classLevel;
  private int userCode;
  private int volatility;
  private int newRating;
  private int newVolatility;
  private int humanRank;
  private int playCount;
  private int numberOfConsecutiveTimeUp;

  public PlayerStatus(PacketPlayerSummary playerSummary, int playerId, int playerListId,
      int sessionId, boolean isHuman, String greeting, String imageFileName, int classLevel,
      int rating, int userCode, int volatility, int playCount) {
    this.playerSummary = playerSummary;
    this.playerId = playerId;
    this.playerListId = playerListId;
    this.sessionId = sessionId;
    this.skipCount = 3;
    this.human = isHuman;
    this.greeting = greeting;
    this.imageFileName = imageFileName;
    this.classLevel = classLevel;
    this.rating = rating;
    this.userCode = userCode;
    this.setVolatility(volatility);
    this.playCount = playCount;
  }

  public void clearAnswer() {
    answer = null;
    answered = false;
    ++numberOfConsecutiveTimeUp;
  }

  public void setAnswer(String answer, int timeRemain) {
    if (answered) {
      return;
    }

    this.answer = answer;
    this.answered = true;
    this.timeRemain = timeRemain;
    this.numberOfConsecutiveTimeUp = 0;
  }

  public boolean isAnswered() {
    return answered;
  }

  public String getAnswer() {
    return answer;
  }

  public boolean isCorrect(String answer[]) {
    if (this.answer == null) {
      return false;
    }
    for (String tempAnswer : answer) {
      if (this.answer.equals(tempAnswer)) {
        return true;
      }
    }
    return false;
  }

  public int getTimeRemain() {
    return timeRemain;
  }

  public void pushPlayerAction(PacketPlayerAction playerAction) {
    if (list == null) {
      list = new ArrayList<PacketPlayerAction>();
    }
    list.add(playerAction);
  }

  public List<PacketPlayerAction> popPlayerAction() {
    List<PacketPlayerAction> playerAction = list;
    list = null;
    return playerAction;
  }

  public void setRequestStartingGame() {
    requestStartingGame = true;
  }

  public boolean isRequestStartingGame() {
    return requestStartingGame;
  }

  public void incSkipCount() {
    ++skipCount;
  }

  public void clearSkipCount() {
    skipCount = 0;
  }

  public boolean shouldBeDropped() {
    return skipCount >= 1 || numberOfConsecutiveTimeUp >= 4;
  }

  public int getSessionId() {
    return sessionId;
  }

  public int getPlayerId() {
    return playerId;
  }

  public int getPlayerListId() {
    return playerListId;
  }

  public PacketPlayerSummary getPlayerSummary() {
    return playerSummary;
  }

  /**
   * 回線落ち扱いにする
   */
  public void drop() {
    human = false;
    playerSummary.level = LEVEL_DROP;
  }

  public boolean isHuman() {
    return human;
  }

  public void addScore(int score) {
    this.score += score;
  }

  public int getScore() {
    return score;
  }

  public void setRating(int rating) {
    this.rating = rating;
  }

  public int getRating() {
    return rating;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public PacketResult toResult() {
    PacketResult result = new PacketResult();
    result.playerSummary = playerSummary;
    result.playerId = playerId;
    result.playerListId = playerListId;
    result.rank = rank;
    result.score = score;
    result.sessionId = sessionId;
    result.imageFileName = imageFileName;
    result.newRating = newRating;
    result.newVolatility = newVolatility;
    return result;
  }

  public String getGreeting() {
    return greeting;
  }

  public String getImageFileName() {
    return imageFileName;
  }

  public void setTempRanking(int tempRanking) {
    this.tempRanking = tempRanking;
  }

  public int getTempRanking() {
    return tempRanking;
  }

  public int getClassLevel() {
    return classLevel;
  }

  public int getUserCode() {
    return userCode;
  }

  public int getVolatility() {
    return volatility;
  }

  public void setVolatility(int volatility) {
    this.volatility = volatility;
  }

  public int getNewRating() {
    return newRating;
  }

  public void setNewRating(int newRating) {
    this.newRating = newRating;
  }

  public int getNewVolatility() {
    return newVolatility;
  }

  public void setNewVolatility(int newVolatility) {
    this.newVolatility = newVolatility;
  }

  public int getHumanRank() {
    return humanRank;
  }

  public void setHumanRank(int humanRank) {
    this.humanRank = humanRank;
  }

  public int getPlayCount() {
    return playCount;
  }
}
