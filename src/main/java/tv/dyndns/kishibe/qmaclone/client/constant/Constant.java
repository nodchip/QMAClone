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
package tv.dyndns.kishibe.qmaclone.client.constant;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window.Location;

public class Constant {
  private Constant() {
  }

  public static final int MAX_PROBLEMS_PER_PLAYER = 2;
  public static final int MAX_PLAYER_PER_SESSION = 8;
  public static final int MAX_PROBLEMS_PER_SESSION = MAX_PLAYER_PER_SESSION
      * MAX_PROBLEMS_PER_PLAYER;
  public static final int MAX_PERFECT_BORDER_TIME = 0;
  public static final double MAX_POINT_COMPRESS = 0.5;
  public static final double MIN_POINT_COMPRESS = 0.5;
  public static final int MAX_POINT = 10000;
  // public static final int WAIT_SECOND_FOR_MATCHING = 10;
  public static final int WAIT_SECOND_FOR_MATCHING = 100;
  public static final int SECONDS_FROM_READY_TO_PROBLEM = 10;
  public static final int MAX_TIMING_DELAY = 200;
  private static final String[] CLASS_NAMES = { "ヴァンパイア", "トロール", "ユニコーン", "ホムンクルス", "ミノタウロス",
      "ガーゴイル", "クラーケン", "リザードマン", "スフィンクス", "デュラハン", "グレムリン", "ケンタウロス", "ゾンビ", "ヘルハウンド", "カーバンクル",
      "マンドレイク", "ヒドラ", "ドッペルゲンガー", "サラマンダー", "ゴーレム", "グリフォン", "ロック", "フェンリル", "ワルキューレ", "ケルベロス",
      "ワーウルフ", "サイクロプス", "キマイラ", "イフリート", "バジリスク", "マンティコア", "マーメイド", "オーク", "バンシー", "バルログ",
      "ジャバウォック", "フェニックス", "ケットシー", "ケツァルコアトル", "メドゥーサ", "リバイアサン", "ワイバーン", "鵺", "ケルピー", "マミー",
      "スキュラ", "ペガサス", "カロン", "ヒポグリフ", "ベヒモス", "サンダーバード", "シーサーペント", "麒麟", "イエティ", "インキュバス", "サキュバス",
      "スライム", "エント", "コカトリス", "ブラウニー", "バンダースナッチ", "ペリュトン", "パビルサグ", "ミミック", "ジャック・オ・ランタン",
      "ポルターガイスト", "ノーム", "クネヒト・ルプレヒト", "ドラゴン", "エルフ", "ドワーフ", "天狗", "龍", "ウンディーネ", "ウィル・オ・ザ・ウィスプ",
      "ゴブリン", "リャナンシー", "オーガ", "ホビット", "スレイプニル", "スマウグ", "ネメアンライオン", "アルゴス", "エキドナ", "ムスッペル",
      "ミルメコレオ", "パズズ", "タロス", "カクス", "アモン", "ヘカトンケイル", "エコー", "メフィストフェレス", "ギガース", "シルフ", "エンジェル",
      "キューピッド", "チョンチョン", "開明獣", "九尾の狐", "コロポックル", "河童", "ガルーダ", "ナーガ", "バロン", "ギリメカラ", "クサントス",
      "バリオス", "八岐大蛇", "酒呑童子", "蚩尤", "八咫烏", "セルキー", "ハンプティ・ダンプティ", "フンババ", "リー・バン", "リッチ" };
  public static final int NUMBER_OF_CLASSES = CLASS_NAMES.length;
  private static final double ACCURACY_RATE_UPPER_START = 1.0;
  private static final double ACCURACY_RATE_LOWER_START = 0.8;
  private static final double ACCURACY_RATE_UPPER_END = 0.25;
  private static final double ACCURACY_RATE_LOWER_END = 0.0;
  private static final double[] ACCURACY_RATE_LOWER_BOUND;
  private static final double[] ACCURACY_RATE_UPPER_BOUND;
  public static final int DIFFICULT_SELECT_NORMAL = 0;
  public static final int DIFFICULT_SELECT_DIFFICULT = 1;
  public static final int DIFFICULT_SELECT_EASY = 2;
  public static final int DIFFICULT_SELECT_LITTLE_DIFFICULT = 3;
  public static final int DIFFICULT_SELECT_LITTLE_EASY = 4;
  public static final int MAX_CLASS_LEVEL = CLASS_NAMES.length - 1;
  public static final int CLASS_LEVEL_NORMAL = MAX_CLASS_LEVEL + 1;
  public static final int CLASS_LEVEL_DIFFICULT = MAX_CLASS_LEVEL + 2;
  public static final int CLASS_LEVEL_EASY = MAX_CLASS_LEVEL + 3;
  public static final int CLASS_LEVEL_LITTLE_DIFFICULT = MAX_CLASS_LEVEL + 4;
  public static final int CLASS_LEVEL_LITTLE_EASY = MAX_CLASS_LEVEL + 5;
  public static final int STEP_PER_CLASS_LEVEL = 4;
  public static final int RETRY_DELAY = 1000;
  public static final String DEPROY_DIRECTORY = "/QMAClone";
  public static final int REPORT_NOT_YET = 10;
  public static final int MAX_RATIO_CALCULATING = 10;
  public static final String WAIT_SPACE = "                              ";
  public static final int FIXED_CLASS_LEVEL = -1;
  public static final String DELIMITER_GENERAL = "\n";
  // public static final String DELIMITER_JUNBAN = ">";
  public static final String DELIMITER_KUMIAWASE_PAIR = "<--->";
  public static final String FORM_NAME_USER_CODE = "user_code";
  public static final String FORM_NAME_ICON = "icon";
  public static final String ICON_UPLOAD_RESPONSE_OK = "--OK--";
  public static final String ICON_UPLOAD_RESPONSE_FAILED_TO_PARSE_REQUEST = "--FAILED_TO_PARSE_REQUEST--";
  public static final String ICON_UPLOAD_RESPONSE_FAILED_TO_DETECT_IMAGE_FILE_TYPE = "--FAILED_TO_DETECT_IMAGE_FILE_TYPE--";
  public static final String ICON_UPLOAD_RESPONSE_REQUEST_FORMAT_ERROR = "--REQUEST_FORMAT_ERROR--";
  public static final String ICON_UPLOAD_RESPONSE_IMAGE_FILE_NAME_FORMAT_ERROR = "--IMAGE_FILE_NAME_FORMAT_ERROR--";
  public static final String ICON_URL_PREFIX = "http://kishibe.dyndns.tv/qmaclone/icon/";
  public static final String FILE_PATH_BASE = "/var/www/html/";
  public static final String ICON_FOLDER_PATH = FILE_PATH_BASE + "qmaclone/icon/";
  public static final int ICON_SIZE = 48;
  public static final int ICON_SIZE_BIG = 64;
  public static final String ICON_NO_IMAGE = "noimage.jpg";
  public static final String SOUND_URL_PREFIX = "http://kishibe.dyndns.tv/qmaclone/sound/";
  public static final String SOUND_URL_GOOD = SOUND_URL_PREFIX + "chime00.wav";
  public static final String SOUND_URL_BAD = SOUND_URL_PREFIX + "beep14.wav";
  public static final String SOUND_URL_TIME_UP = SOUND_URL_PREFIX + "bell02.wav";
  public static final String SOUND_URL_BUTTON_OK = SOUND_URL_PREFIX + "weapon01.wav";
  public static final String SOUND_URL_BUTTON_PUSH = SOUND_URL_PREFIX + "wood07.wav";
  public static final String SOUND_URL_READY_FOR_GAME = SOUND_URL_PREFIX + "bell00.wav";
  public static final int RANKING_DISPLAY_DAY = 30;
  public static final int NUMBER_OF_RANKING_DATA = 100;
  public static final int RANKING_HIGH_SCORE = 0;
  public static final int RANKING_AVERAGE_SCORE = 1;
  public static final int RANKING_PLAY_COUNT = 2;
  public static final int RANKING_VICTORY_POINT = 3;
  public static final int RANKING_AVERAGE_RANK = 4;
  public static final int RANKING_CLASS = 5;
  public static final int RANKING_CORRECT_RATIO = 6;
  public static final int NUMBER_OF_RANKING_TYPE = 7;
  public static final int BBS_THREADS_PER_PAGE = 5;
  public static final int BBS_INITIAL_RESPONSE_PER_THREAD = 10;
  public static final int BBS_DISPLAY_INFO_ANONYMOUS = 0;
  public static final int BBS_DISPLAY_INFO_NAME_ONLY = 1;
  public static final int BBS_DISPLAY_INFO_ALL_DATA = 2;
  // public static final String NAMES_OF_GENRE[] = { "ノンジャンル", "アニメ＆ゲーム", "スポーツ", "芸能", "ライフスタイル",
  // "社会", "文系学問", "理系学問", "未分類" };
  // public static final String INITIAL_OF_GENRE[] = { "ノ", "ア", "ス", "芸", "ラ", "社", "文", "理", "未"
  // };
  public static final int LINK_DATA_PER_PAGE = 10;
  public static final String[] PREFECTURE_NAMES = { "無所属", "北海道", "青森", "岩手", "宮城", "秋田", "山形",
      "福島", "茨城", "栃木", "群馬", "埼玉", "千葉", "東京", "神奈川", "新潟", "富山", "石川", "福井", "山梨", "長野", "岐阜",
      "静岡", "愛知", "三重", "滋賀", "京都", "大阪", "兵庫", "奈良", "和歌山", "鳥取", "島根", "岡山", "広島", "山口", "徳島",
      "香川", "愛媛", "高知", "福岡", "佐賀", "長崎", "熊本", "大分", "宮崎", "鹿児島", "沖縄", };
  public static final int MAX_RATING_HISTORY = 100;
  public static final int RATING_DISTRIBUTION_WIDTH = 20;
  public static final int CLICK_IMAGE_WIDTH = 512;
  public static final int CLICK_IMAGE_HEIGHT = 384;
  public static final int CHAT_MAX_RESPONSES = 100;
  public static final int NEW_PROBLEM_ID = -1;
  public static final int MIN_NUMBER_OF_THEME_MODE_PROBLEMS = 100;
  public static final int MAX_NUMBER_OF_CREATION_PER_HOUR = 3;
  public static final int WEB_SOCKET_PORT = 60080;
  public static final String WEB_SOCKET_URL = getWebSocketUrl();
  public static final String KEY_GAME_SESSION_ID = "game_session_id";

  private static String getWebSocketUrl() {
    if (GWT.isClient() && Location.getHost().contains(":8888")) {
      return "ws://localhost:" + WEB_SOCKET_PORT + "/QMAClone/websocket/";
    } else if (GWT.isClient() && Location.getHost().contains(":8080")) {
      return "ws://localhost:" + WEB_SOCKET_PORT + "/QMAClone/websocket/";
    } else {
      return "ws://kishibe.dyndns.tv/QMAClone/websocket/";
    }
  }

  public static final int MAX_NUMBER_OF_ANSWERS = 8;
  public static final int MAX_NUMBER_OF_CHOICES = 8;
  public static final String WEBSOCKET_PROTOCOL_SEPARATOR = "/";
  public static final int GENERIC_BBS_ID = -1;
  public static final int MAX_PLAYER_NAME_LENGTH = 8;

  public static final int MAX_NUMBER_OF_POLYGON_VERTICES = 8;

  static {
    int numberOfClasses = CLASS_NAMES.length;
    ACCURACY_RATE_LOWER_BOUND = new double[numberOfClasses + 5];
    ACCURACY_RATE_UPPER_BOUND = new double[numberOfClasses + 5];
    // 全難易度から出題する
    ACCURACY_RATE_LOWER_BOUND[numberOfClasses] = 0.0;
    ACCURACY_RATE_UPPER_BOUND[numberOfClasses] = 1.0;
    // 難問を出題する
    ACCURACY_RATE_LOWER_BOUND[numberOfClasses + 1] = 0.0;
    ACCURACY_RATE_UPPER_BOUND[numberOfClasses + 1] = 0.3;
    // やや難問を出題する
    ACCURACY_RATE_LOWER_BOUND[numberOfClasses + 2] = 0.7;
    ACCURACY_RATE_UPPER_BOUND[numberOfClasses + 2] = 1.0;
    // やや易問を出題する
    ACCURACY_RATE_LOWER_BOUND[numberOfClasses + 3] = 0.2;
    ACCURACY_RATE_UPPER_BOUND[numberOfClasses + 3] = 0.5;
    // 易問を出題する
    ACCURACY_RATE_LOWER_BOUND[numberOfClasses + 4] = 0.5;
    ACCURACY_RATE_UPPER_BOUND[numberOfClasses + 4] = 0.8;

    for (int i = 0; i < numberOfClasses; ++i) {
      ACCURACY_RATE_LOWER_BOUND[i] = (ACCURACY_RATE_LOWER_START - ACCURACY_RATE_LOWER_END)
          * (numberOfClasses - i - 1) / numberOfClasses + ACCURACY_RATE_LOWER_END;
      ACCURACY_RATE_UPPER_BOUND[i] = (ACCURACY_RATE_UPPER_START - ACCURACY_RATE_UPPER_END)
          * (numberOfClasses - i - 1) / numberOfClasses + ACCURACY_RATE_UPPER_END;
    }
  }

  public static String getClassName(int classLevel) {
    return CLASS_NAMES[classLevel];
  }

  public static double getAccuracyRateLowerBound(int classLevel) {
    return ACCURACY_RATE_LOWER_BOUND[classLevel];
  }

  public static double getAccuracyRateUpperBound(int classLevel) {
    return ACCURACY_RATE_UPPER_BOUND[classLevel];
  }
}
