package tv.dyndns.kishibe.qmaclone.client.testing;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.game.RandomFlag;
import tv.dyndns.kishibe.qmaclone.client.game.SessionData;
import tv.dyndns.kishibe.qmaclone.client.packet.NewAndOldProblems;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketLinkData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketUserData.WebSocketUsage;

public class TestDataProvider {
  private TestDataProvider() {
    throw new AssertionError();
  }

  public static PacketProblem getProblem() {
    PacketProblem problem = new PacketProblem();
    problem.id = 12345;
    problem.genre = ProblemGenre.Anige;
    problem.type = ProblemType.Marubatsu;
    problem.good = 123;
    problem.bad = 12;
    problem.randomFlag = RandomFlag.Random1;
    problem.creatorHash = 123456789;
    problem.userCode = 12345678;
    problem.sentence = "問題文";
    problem.answers = new String[] { "１２３４５６７８", "２３４５６７８９", "３４５６７８９０", "４５６７８９０１" };
    problem.choices = new String[] { "ＱＷＥＲＴＹＵＩ", "ＷＥＲＴＹＵＩＯ", "ＥＲＴＹＵＩＯＰ", "ＲＴＹＵＩＯＰＡ" };
    problem.creator = "作成者";
    problem.note = "ノート";
    problem.shuffledAnswers = new String[] { "２３４５６７８９", "３４５６７８９０", "４５６７８９０１", "１２３４５６７８" };
    problem.shuffledChoices = new String[] { "ＷＥＲＴＹＵＩＯ", "ＥＲＴＹＵＩＯＰ", "ＲＴＹＵＩＯＰＡ", "ＱＷＥＲＴＹＵＩ" };
    problem.imageAnswer = false;
    problem.imageChoice = false;
    problem.voteBad = 1234;
    problem.voteGood = 12345;
    problem.imageUrl = "http://image.url.com/image.jpg";
    problem.movieUrl = "http://movie.url.com/movie.html";
    problem.indication = new Date();
    problem.numberOfDisplayedChoices = 4;
    return problem;
  }

  public static PacketUserData getUserData() {
    PacketUserData data = new PacketUserData();
    data.playerName = "プレイヤー";
    data.greeting = "よろしく";
    data.highScore = 1234;
    data.averageScore = 123;
    data.playCount = 234;
    data.rating = 2222;
    data.levelName = 1;
    data.levelNumber = 2;
    data.prefecture = 3;
    data.averageRank = 4.5f;
    data.genres = EnumSet.of(ProblemGenre.Anige);
    data.types = EnumSet.of(ProblemType.Marubatsu);
    data.classLevel = 1;
    data.userCode = 7;
    data.imageFileName = "image.jpg";
    data.correctCount = new int[ProblemGenre.values().length][ProblemType.values().length][2];
    data.playSound = false;
    data.multiGenre = false;
    data.multiType = false;
    data.difficultSelect = 1;
    data.rankingMove = true;
    data.bbsDispInfo = 0;
    data.bbsAge = true;
    data.chat = true;
    data.newAndOldProblems = NewAndOldProblems.Both;
    data.ignoreUserCodes = Collections.emptySet();
    data.timerMode = 0;
    data.publicEvent = true;
    data.hideAnswer = false;
    data.showInfo = true;
    data.reflectEventResult = true;
    data.webSocketUsage = WebSocketUsage.Default;
    data.volatility = 345;
    data.qwertyHiragana = true;
    data.qwertyKatakana = true;
    data.qwertyAlphabet = true;
    data.theme = "ガンダム";
    return data;
  }

  public static SessionData getSessionData() {
    return new SessionData(0, 1, false, false, false);
  }

  public static PacketLinkData getLinkData() {
    PacketLinkData linkData = new PacketLinkData();
    linkData.id = 123;
    linkData.homePageName = "foo";
    linkData.authorName = "bar";
    linkData.url = "buzz";
    linkData.bannerUrl = "hoge";
    linkData.description = "fuga";
    linkData.userCode = 123456789;
    linkData.lastUpdate = 1360983938000L;
    return linkData;
  }
}
