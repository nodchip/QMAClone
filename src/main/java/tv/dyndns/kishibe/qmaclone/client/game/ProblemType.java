package tv.dyndns.kishibe.qmaclone.client.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.creation.validater.Evaluation;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.Validator;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.Validator4Taku;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorClick;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorEffect;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorFlash;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorGroup;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorHayaimono;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorJunban;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorMarubatsu;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorMojiPanel;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorNarabekae;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorNull;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorSenmusubi;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorSlot;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorTato;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorTegaki;
import tv.dyndns.kishibe.qmaclone.client.creation.validater.ValidatorTyping;
import tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizable;
import tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizerDefault;
import tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizerMarubatsu;
import tv.dyndns.kishibe.qmaclone.client.game.accuracyrate.AccuracyRateNormalizerYontaku;
import tv.dyndns.kishibe.qmaclone.client.game.judge.Judge;
import tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeClick;
import tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeDefault;
import tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeJunban;
import tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeSenmusubi;
import tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeSlot;
import tv.dyndns.kishibe.qmaclone.client.game.judge.JudgeTato;
import tv.dyndns.kishibe.qmaclone.client.game.shuffler.Shuffleable;
import tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerDefault;
import tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerJunban;
import tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerMojiPanel;
import tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerSenmusubi;
import tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerTato;
import tv.dyndns.kishibe.qmaclone.client.game.shuffler.ShufflerYontaku;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblemMinimum;
import tv.dyndns.kishibe.qmaclone.client.util.HasIndex;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.IsSerializable;

public enum ProblemType implements IsSerializable, HasIndex {
  // ランダム
  Random(0, "ランダム", "ラ", null, 0, 0, false, false, false, new ValidatorNull(), new JudgeDefault(),
      new ShufflerDefault(), false, new AccuracyRateNormalizerDefault()),
  // ○×
  Marubatsu(1, "○×", "○", "【○×】\n" + "\n" + "　「○」と「×」（および「Ａ」と「Ｂ」）の二者択一による問題形式\n" + "\n"
      + "◆問題文の書式\n" + "「～である」や「～だった」あるいはそれに類する文末にしてください。\n" + "選択肢に画像が使われていない問題では、文末に「？」はつきません。\n"
      + "問題文にに画像は併用できません。\n" + "選択肢を画像にする際の文末は「～はどっち？」としてください。\n"
      + "選択肢は「○」と「×」の２つあるいは２つの静止画および動画を使用。画像の場合は画像URLを入力してください。\n" + "「o」「x」ではありません\n" + "\n"
      + "◆分岐\n" + "「～ですが」による分岐はできませんが、文末付近のみの分岐は可能です。（いわゆる「<wait>分岐」）\n"
      + "その際は分岐を２つとし、それぞれの解答を「○」と「×」が１つずつになるようにしてください。", 1, 2, false, true, false,
      new ValidatorMarubatsu(), new JudgeDefault(), new ShufflerDefault(), false,
      new AccuracyRateNormalizerMarubatsu()),
  // 四択
  YonTaku(2, "四択", "択", "【四択】\n" + "\n" + "　その名の通り４つの選択肢による四者択一問題\n" + "\n" + "◆問題文の書式\n"
      + "文末は「～は（何・どれ・誰）？」などのように言い切りで疑問符をつけるようにしてください。\n" + "１枚の静止画あるいは動画を併用することが可能です。\n" + "\n"
      + "◆選択肢\n" + "解答欄の一番上に正答、それ以外にダミー解答を入力してください\n" + "項目を４枚の静止画で構成させることも可能です。\n"
      + "なお正答選択肢以外のダミー選択肢は、３つよりも多いダミー群の中から無作為に３つ選ぶ方式、\n" + "いわゆる「可変式選択肢」にすることも可能です。\n" + "\n"
      + "◆分岐\n" + "「～ですが」などによる分岐が可能です。\n"
      + "選択肢は同じ項目にし、分岐した際の解答は原則として分岐前の解答や他の分岐の解答と同じにしないようにしてください。", 0, 8, false, true, false,
      new Validator4Taku(false), new JudgeDefault(), new ShufflerYontaku(), false,
      new AccuracyRateNormalizerYontaku()),
  // 連想
  Rensou(3, "連想", "連", "【連想】\n" + "\n" + "　４つの項目（ヒント）を時間をずらして表示する方法による四者択一問題形式\n" + "\n"
      + "◆問題文の書式\n" + "４つの項目を１行に一つずつ書いて下さい。\n" + "ヒント文中に伏字（○）を利用することも可能です。\n"
      + "３行目までの各行の末尾に%w等で適宜ウェイトを入れてくだい。\n" + "\n" + "◆選択肢\n" + "解答欄の一番上に正答、それ以外にダミー解答を入力してください。\n"
      + "選択肢を画像で構成させることも可能です。その場合は選択肢に画像URLを入力してください。\n"
      + "なお正答選択肢以外のダミー選択肢は、３つよりも多いダミー群の中から無作為に３つ選ぶ方式、\n" + "いわゆる「可変式選択肢」にすることも可能です。\n" + "\n"
      + "◆分岐\n" + "【四択】【タイピング】などの形式のいわゆる「ですが分岐」とは趣が異なりますが、\n"
      + "選択肢およびヒント項目の一部を全く同じものにして、途中までに解答を断定させないような「分岐」は可能です。\n"
      + "最終第４項目までにひとつのものに正答が特定できないような作問でないのであれば、特に問題はありません。", 0, 8, false, true, false,
      new Validator4Taku(true), new JudgeDefault(), new ShufflerYontaku(), false,
      new AccuracyRateNormalizerYontaku()),
  // 並べ替え
  Narabekae(4, "並べ替え", "並", "【並べ替え】\n" + "\n" + "　ランダムに入れ替わった単語や文章を正しく並べ替えて解答させる問題形式\n" + "\n"
      + "◆問題文の書式\n" + "文末は「～は？」などのように言い切りで疑問符をつけるようにしてください。\n"
      + "（注意：【フラッシュ】および【エフェクト】のような「です・ます調」あるいは体言止めの語尾にはできません。）\n"
      + "【タイピング】のような伏せ字（○）を利用した問題文も可能です。\n" + "また、１枚の静止画あるいは動画を併用することが可能です。\n" + "\n" + "◆解答の制限\n"
      + "文字数は３文字以上、９文字以下。\n" + "異種の文字を混在可能で、漢字や記号等も使用できます。\n" + "\n" + "◆分岐\n"
      + "不可能ですが、全く同じ文字列を用いて複数の（それぞれ別の）問題を作成することは可能です。\n" + "\n" + "◆複数解答\n"
      + "「表記のゆらぎ」による複数解答に限り可能です。\n" + "具体的には、長音、促音などの位置を入れ替えたり、\n"
      + "特定の文字列の位置を前後で入れ替えても問題に対する解答として成立する場合などです。", 8, 0, false, false, false,
      new ValidatorNarabekae(), new JudgeDefault(), new ShufflerDefault(), false,
      new AccuracyRateNormalizerDefault()),
  // 文字パネル
  MojiPanel(5, "文字パネル", "パ", "【文字パネル】\n" + "\n"
      + "　６種類または８種類の文字から３つ、あるいは１０種類の文字から４つ～６つを選択し、指定する３文字～６文字の言葉を解答させる問題形式\n" + "\n" + "◆問題文の書式\n"
      + "文末は「～は？」などのように言い切りで疑問符をつけるようにしてください。\n"
      + "伏せ字（○）を使った形式では、【タイピング】で用いられている「○を答えなさい」というような脚注は不要です。\n" + "１枚の静止画あるいは動画を併用することが可能です。\n"
      + "\n" + "◆選択肢\n" + "解答の文字数が３文字の時は８種類、４～６文字の時は１０種類の異なる文字を用意。同じ文字を複数使用する解答も可能です。\n"
      + "異種の文字を混在可能で、漢字や記号なども使用できます。\n" + "\n" + "◆分岐\n" + "「～ですが」などによる分岐が可能。分岐数に上限はありません。\n"
      + "分岐した際の解答は分岐前の解答や他の分岐の解答と同じにしないようにしてください。\n" + "\n"
      + "なお、分岐前の解答の文字数と分岐後の解答の文字数は必ず同じとなります。\n" + "\n" + "◆複数解答\n" + "「表記のゆらぎ」による複数解答に限り可能です。", 8,
      1, false, false, false, new ValidatorMojiPanel(), new JudgeDefault(),
      new ShufflerMojiPanel(), false, new AccuracyRateNormalizerDefault()),
  // タイピング
  Typing(6, "タイピング", "タ", "【タイピング】\n" + "\n" + "　画面上に表示される、「ひらがな」「カタカナ」「英数字（英字は大文字のみ）」の\n"
      + "　いずれか１つのキーボードを使って解答を直接入力させる問題形式\n" + "\n" + "◆問題文の書式\n"
      + "最も基本とされるのは伏字（○）を用い、脚注をつける形式です。\n" + "例：\n" + "（１）伏字部分が漢字の場合はひらがなで答えさせます。\n"
      + "・1964年にはアジアで初めて夏季オリンピックが開催された日本の現在の首都は○○？○の漢字をひらがなで答えなさい\n"
      + "・2001年に就任し、「感動した！」などの名言でも話題となった日本の元首相は小泉○○○？○の名前をひらがなで答えなさい\n"
      + "・元ラッツ＆スターのメンバーで「マーシー」の愛称で親しまれた元タレントは○○まさし？○の苗字をひらがなで答えなさい\n" + "\n"
      + "※この場合の脚注は原則として「○の漢字をひらがなで答えなさい」となります。\n" + "　ただし、伏せられている部分が苗字のみの場合は「○の苗字を～」に、\n"
      + "　名前（＝ファーストネーム）のみの場合は「○の名前を～」に変化します。\n" + "\n"
      + "（２）伏字部分に漢字とひらがなが混在している場合は、文字数を指定するなどしてひらがなで答えさせます。\n" + "\n"
      + "・コント集団としても有名なバンド　ザ・ドリフターズのメンバーで現時点で最年少なのは○○○○？ひらがな５文字で答えなさい\n" + "\n"
      + "※この場合の脚注は原則として「ひらがなＸ文字で答えなさい」となります。\n" + "\n"
      + "（３）伏字部分に漢字が含まれていない場合（ひらがな・カタカナ・英数字いずれも可）はそのまま答えさせます。\n" + "\n"
      + "・1969年に放送が開始された長谷川町子原作の国民的アニメのタイトルは『○○○さん』？○を答えなさい\n" + "\n"
      + "※この場合の脚注は原則として「○を答えなさい」となります。\n" + "\n"
      + "上記の形式以外にも、○による伏字を使わずに「ひらがな(Ｘ文字)で答えなさい」「カタカナ(Ｘ文字)で答えなさい」\n"
      + "「数字で答えなさい」「アルファベットで答えなさい」「フルネームで答えなさい」などという脚注をつける方法での出題も可能です。\n"
      + "いずれの場合も問題本文の文末は疑問符にし、脚注文末に句読点をつける必要はありません。\n" + "\n"
      + "なお、画像が使用されていない問題における問題文で、「？」とその後に続く脚注との間に改行がない場合には、\n"
      + "そこに全角一つ分のスペースが挿入されていることがあります。（文字数が制限ギリギリの場合はない場合もあります。）\n" + "\n"
      + "１枚の静止画あるいは動画を併用できますが、その際の文末は「～は（どれ・何）？」などという疑問形となります（伏字（○）の使用も可能です）。\n" + "\n"
      + "◆解答の制限\n" + "１文字以上８文字以下。\n" + "使える文字種は「ひらがな」「カタカナ」「英数字（英字は大文字のみ）」のいずれか１種。\n"
      + "ただし、キーボードに用意されていないひらがな・カタカナ（「ゐ」「ゑ」「ゎ」「ヰ」「ヱ」「ヮ」）や、ひらがなの「う」に濁点（う゛）は用いることができません。\n" + "\n"
      + "◆分岐\n" + "「～ですが」などによる分岐が可能です。分岐数に上限はありません。\n" + "分岐した際の解答は原則として分岐前の解答と同じにしないようにしてください。\n"
      + "分岐前と分岐後における解答の文字の種類は必ずしも同じにする必要はありませんが、\n"
      + "画面上のキーボードの表示により分岐の有無が推測できてしまいますのでその点の注意が必要です。\n" + "\n" + "◆複数解答\n"
      + "可能。ただしこれは「表記のゆらぎ」や「別称」に使うべきもので、\n" + "解答をひとつのものに特定できない設問にすることはできません。\n" + "\n"
      + "【複数解タイピング】\n" + "\n" + "　画面上に表示される、「ひらがな」「カタカナ」「英数字（英字は大文字のみ）」の\n"
      + "　いずれか１つのキーボードを使って解答を直接入力させる問題形式\n" + "　そしてその中でも正答対象が複数存在するもの（出題上の形式としては通常の【タイピング】に内包）\n"
      + "\n" + "◆問題文の書式\n" + "「～を１つ（１人、１個など）答えなさい」という表現が基本となります。\n"
      + "ただし、通常の【タイピング】のように伏字（○）と、「疑問形+脚注」の文末表現を用いた問題文も少し存在します。\n" + "例：日本の三大和牛といえば○○牛？　１つ答えなさい\n"
      + "解答の条件を設定する表現は「１つ（１人、１個など）」の直前に組み込まれていることが多いようです。\n"
      + "例：「～をフルネームで１人答えなさい」「～をファーストネームで１人答えなさい」など\n"
      + "ただし、解答に漢字が含まれる場合であっても「～をひらがなで１つ答えなさい」などの問題文に文字種を指定する記述は必要ありません。\n" + "\n" + "◆解答の制限\n"
      + "１文字以上８文字以下。\n" + "使える文字種は「ひらがな」「カタカナ」「英数字（英字は大文字のみ）」のいずれか１種。\n"
      + "ただし、キーボードに用意されていないひらがな・カタカナ（「ゐ」「ゑ」「ゎ」「ヰ」「ヱ」「ヮ」）や、ひらがなの「う」に濁点（う゛）は用いることができません。\n"
      + "また、特殊な操作をすることによってタイプすることが可能な記号（「＞」「（」「）」「→」など）も、現状では問題の解答には用いられていないようです。\n" + "\n" + "\n"
      + "以下に、この問題形式ならではの制限事項・注意点を記述していきます。\n" + "\n" + "　出題の題意から、正答となる対象には最低２つのものが含まれている必要があります。\n"
      + "正答となる対象の数に明確な上限はありませんが、範囲があまりにも広すぎたり、多すぎたり、\n"
      + "あるいは漠然としすぎたものが対象となる問題の作成は避けたほうが望ましいでしょう。\n"
      + "また、題意を満たす対象の文字数が８文字を超えるがために正答から除外されてしまうような問題も、\n"
      + "問題文で制限を設けるなどといった回避のために必要な手順を踏まえない限りは、作成することは避けるべきでしょう（次で詳述）。\n" + "\n"
      + "　QMACloneのタイピング系問題における仕様上８文字を超える文字列は入力することが不可能ですから、\n"
      + "８文字を超える正答が発生してしまう問題は、たとえそれの他に８文字以内で答えることが\n"
      + "可能な別の正答が存在したとしても、問題自体を作成することがはじめからできません。\n"
      + "そのため問題作成の際には、８文字を超える解が発生してしまわないように注意する必要があります。\n"
      + "ただし、問題文で「○文字以下の」というように回答文字数を指定したり、対象となる範囲を時期・場所など何らかの条件で限定したり\n"
      + "といった形で８文字を超える正答候補を除外するための制限を設けた場合は、この限りではありません。\n" + "\n"
      + "　QMACloneの【タイピング】で回答に用いることができるキーボードは\n" + "「ひらがな」「カタカナ」「英数字」の“いずれか１種”ですから、\n"
      + "用意されたキーボードの種類と異なる文字種で答えるべき対象が正答となる問題を作成することは原則としてできません。\n"
      + "そのため問題作成の際には、複数の文字種にまたがった正答群や、\n" + "あるいは複数の文字種が混在した解が発生してしまわないように注意する必要があります。\n"
      + "（※ただし、「～のうちひらがなだけで表されるものを１つ」などのように問題文で制限を設けて出題することは可能です。）\n" + "\n"
      + "　正答となる対象に、有名な別称・慣用表現が存在する場合や、\n" + "常識的な範囲の付加表現・省略表現が認められる場合は、それらも正答となるようにケアする必要があります。\n"
      + "（※ただし、当然ながら正答となる文字列は８文字以内であることが必須条件です。）\n" + "また、通常の【タイピング】問題と同じように表記揺れに対してのケアも必要となります。\n"
      + "題意上、回答文字数の指定を設けることができない場合が多いため、\n" + "普段にもまして表記揺れや別称の存在に対して注意を払う必要があるでしょう。\n" + "\n"
      + "　問題を作成するにあたって「当初解答（群）として想定された文字列」が、\n" + "用意されたキーボードの文字種類で表記することが可能なものである場合は、\n"
      + "（想定された解答＋付加表現）の文字列に用意されたキーボード以外の文字で打つべき文字が含まれていても\n"
      + "その他の条件（＝「付加を含めても８文字以内」など）を満たしていれば、別解として正解扱い判定にすることが可能です。）\n" + "\n" + "◆分岐\n" + "不可能。", 8,
      0, false, false, false, new ValidatorTyping(), new JudgeDefault(), new ShufflerDefault(),
      false, new AccuracyRateNormalizerDefault()),
  // フラッシュ
  Flash(7, "フラッシュ", "フ",
      "【フラッシュ】\n" + "\n" + "　問題文とともに、解答に含まれる文字がランダムに表示\n" + "　【タイピング】と同様のキーボードを用い、解答を直接入力させる問題形式\n"
          + "\n" + "◆問題文の書式\n" + "文末は「です・ます調」、あるいは体言止めにするか、「～は？」などのように言い切りで疑問符をつけるようにしてください。\n"
          + "伏字（○）を利用した問題文も可能。画像は併用できません。\n" + "\n" + "◆解答の制限\n"
          + "なお、解答文字列が３文字、５文字、７文字のものはそれぞれマスが１つ分余ることになります。\n"
          + "使える文字種は「ひらがな」「カタカナ」「英数字（英字は大文字のみ）」のいずれか１種。\n"
          + "ただし、キーボードに用意されていないひらがな・カタカナ（「ゐ」「ゑ」「ゎ」「ヰ」「ヱ」「ヮ」）や、ひらがなの「う」に濁点（う゛）は用いることができません。\n"
          + "\n" + "◆分岐\n" + "不可能。\n" + "\n" + "◆複数解答\n" + "原則として不可能です。\n"
          + "ただし、表記ゆれのある言葉について例外的に複数解答を認めます。", 8, 0, false, false, false, new ValidatorFlash(),
      new JudgeDefault(), new ShufflerDefault(), false, new AccuracyRateNormalizerDefault()),
  // エフェクト
  Effect(8, "エフェクト", "エ", "【エフェクト】\n" + "\n" + "　問題文とともに、ヒントとなる文字あるいは画像をエフェクトをかけて表示し\n"
      + "　【タイピング】と同様のキーボードを用い、解答を直接入力させる問題形式\n" + "\n" + "◆問題文の書式\n"
      + "文末は「です・ます調」、あるいは体言止めにするか、「～は？」などのように言い切りで疑問符をつけるようにしてください。\n"
      + "伏字（○）を利用した問題文も可能です。なお、この場合の○の個数は、「○の個数と同じ文字数で答えなさい」という意味ではなく、\n"
      + "ヒントとしてエフェクトされている文字の個数であることをご留意ください。\n" + "\n" + "◆ヒントの制限\n" + "文字を使用。\n"
      + "文字の種類は問わず、漢字、かな、記号、英数字など使用可能で、漢字やかなが混在した文字群も出題できます。\n"
      + "なお、この【エフェクト】問のヒント文字に限っては、問題文などでは使用できない「環境依存文字」の使用が可能です。\n"
      + "(ただし、一般的なブラウザにおいて表示可能であることを確認して使用して下さい。)\n" + "\n" + "◆解答の制限\n" + "１文字以上８文字以下。\n"
      + "使える文字種は「ひらがな」「カタカナ」「英数字（英字は大文字のみ）」のいずれか１種。\n"
      + "ただし、キーボードに用意されていないひらがな・カタカナ（「ゐ」「ゑ」「ゎ」「ヰ」「ヱ」「ヮ」）や、ひらがなの「う」に濁点（う゛）は用いることができません。\n" + "\n"
      + "◆分岐\n" + "不可能。\n" + "\n" + "◆複数解答\n" + "可能ですが、これは「表記のゆらぎ」や「別称」に使うべきものであって、\n"
      + "正答をひとつのものに特定できない設問にすることはできません。\n" + "また逆に、問題文で適宜制限を設けることで複数解答を除外することが可能です。", 8, 1, false,
      false, false, new ValidatorEffect(), new JudgeDefault(), new ShufflerDefault(), false,
      new AccuracyRateNormalizerDefault()),
  // 一問多答
  Tato(9, "一問多答", "多", "【一問多答】\n" + "\n" + "　３つあるいは４つの選択肢を用意し、問題文で指定した該当項目を全て選ばせる問題形式\n" + "\n"
      + "◆問題文の書式\n" + "「次のうち～を全て選びなさい」「次の***のうち～を全て選びなさい」という書式が基本となります。\n"
      + "選択肢に画像が使用されている場合は「次の画像(から連想される***)のうち～（のもの）を全て選びなさい」などとなっているようです。\n" + "画像は併用できません。\n"
      + "\n" + "◆選択肢\n" + "１つ以上の正答を含んだ１～３の３項目あるいは１～４の４項目を作成。\n"
      + "４つを超える項目を用意し、ランダムで出力させるという方式による出題も可能です。\n"
      + "また、選択肢を画像で構成させることも可能です。この場合は画像URLを入力してください。\n" + "\n" + "◆分岐\n"
      + "不可能ですが、全く同じ選択肢を用いてそれぞれ別の問題を作成することは可能です。", 8, 8, false, true, false, new ValidatorTato(),
      new JudgeTato(), new ShufflerTato(), true, new AccuracyRateNormalizerDefault()),
  // 順番当て
  Junban(10, "順番当て", "順", "【順番当て】\n" + "\n" + "　３つあるいは４つの選択肢を用意し、問題文で指定した順番通りに選ばせる問題形式\n" + "\n"
      + "◆問題文の書式\n" + "（１）「次の***を（***が）大きい順or小さい順etc.に選びなさい」　序列\n"
      + "（２）「次の***を順に選んで***に（なるように）しなさい」　序列・アナグラム\n"
      + "上記の２パターンが基本となります。（注意：「並べなさい」「並べ替えなさい」のような語尾は存在しません。）\n"
      + "アナグラムの場合は「次の文字(列)を順に選んで」という書きだしが基本となります。\n" + "画像は併用できません。\n" + "\n" + "◆選択肢\n"
      + "Ａ～Ｃの３項目あるいはＡ～Ｄの４項目を作成。１項目あたりの最大文字数は全角１８文字。\n"
      + "アナグラム系の問題を除いては、４つを超える項目を用意しランダムで出力させるという方式による出題も可能です。\n"
      + "また、選択肢の３（４）項目を３（４）枚の画像で構成させることも可能です。\n" + "正答は必ず１パターン（1/6or1/24）だけになるようにしてください。\n" + "\n"
      + "◆分岐\n" + "不可能ですが、全く同じ選択肢を用いてそれぞれ別の問題を作成することは可能です。", 8, 0, true, false, false,
      new ValidatorJunban(), new JudgeJunban(), new ShufflerJunban(), true,
      new AccuracyRateNormalizerDefault()),
  // 線結び
  Senmusubi(11, "線結び", "線", "【線結び】\n" + "\n"
      + "　左右に分かれた選択肢同士を繋ぎ合わせて、意味の通じる文章・語句や正しい組み合わせを作らせる問題形式\n" + "\n" + "◆問題文の書式\n"
      + "（１）語句・文章作成系の問題\n" + "「次の文字列を組み合わせて***にしなさい」\n" + "（２）選択肢の左側と右側が同意の関係あるいは主述の関係になっている問題\n"
      + "「次の***（選択肢左側）と***（選択肢右側）の正しい組み合わせを選びなさい」\n" + "（３）選択肢の左側と右側が並列の関係になっている問題\n"
      + "「次の***（選択肢）を※※同士で組み合わせなさい」\n" + "以上の３パターンが基本となります。画像は併用できません。\n" + "\n" + "◆選択肢\n"
      + "左右それぞれ３組あるいは４組の選択肢を用意します。\n" + "選択肢左側の見出しはＡ～Ｄ、右側の見出しは１～４を使用します。\n"
      + "画像を使用することができます。画像使用の問題の場合は画像URLを入力してください\n"
      + "また、４組を超える選択肢項目を用意し、それをランダムで出力させるという方式による出題も可能です。\n"
      + "正答は必ず１パターン（1/6or1/24）だけになるようにしてください。\n" + "なお、用意されている選択肢のすべての部分が本来はカギカッコ（「 」『 』）で囲むべき\n"
      + "固有名詞で構成されている場合は、例外的にその表記が省略されているようです。\n" + "\n" + "◆分岐\n"
      + "不可能ですが、全く同じ選択肢を用いてそれぞれ別の問題を作成することは可能です。", 8, 8, true, true, false,
      new ValidatorSenmusubi(), new JudgeSenmusubi(), new ShufflerSenmusubi(4), true,
      new AccuracyRateNormalizerDefault()),
  // スロット
  Slot(12, "スロット", "ス", "【スロット】\n" + "\n" + "　リールに用意された文字の中から正しい組み合わせの言葉を選んで揃え、解答させる問題方式\n" + "\n"
      + "◆問題文の書式\n" + "文末は「～は？」などのように、言い切り形で疑問符をつけるようにしてください。\n"
      + "○を用いた伏字による出題も可能。その際は【タイピング】で用いられている「○を答えなさい」というような脚注は不要です。\n"
      + "また、１枚の静止画あるいは動画を併用することが可能です。\n" + "\n" + "◆選択肢\n"
      + "解答となる文字数は２文字以上、６文字以下。解答文字列を作るためのリールを同数個用意。\n"
      + "ひとつのリールには、正答を構成する文字を含む４個の異なる文字を用意してください。\n" + "文字には、ひらがな、カタカナ、英数字、漢字、記号などが使用可能です。\n"
      + "複数の文字種を混在させた文字列を正答として揃えさせる問題を作成することも可能です。\n" + "\n" + "◆分岐\n"
      + "新しく作成する問題の分岐は禁止です。既存の問題の分岐は保持して下さい。ただし、全く同じ構成・配列のリールを用意してそれぞれ異なる問題を作成することは可能です。\n" + "\n"
      + "◆複数解答\n" + "不可能。複数の正答が作れてしまうような文字配置のリールは作らないようにしてください。", 4, 0, false, false, false,
      new ValidatorSlot(), new JudgeSlot(), new ShufflerDefault(), false,
      new AccuracyRateNormalizerDefault()),
  // クリック
  Click(13, "画像クリック", "画", "【画像タッチ】\n" + "\n" + "　回答用に１枚の画像を用意し、その画像の一部分を直接クリックして答えさせる問題形式\n"
      + "\n" + "◆問題文の書式\n" + "「～は（どれ・何・どこ）？」という単純な疑問文形の書式が基本となります。\n" + "画像は併用できません。\n" + "\n"
      + "◆回答用の画像について\n" + "選択肢に画像URLを入力してください。\n"
      + "正解となる領域を入力するには解答欄の右側のボタンを押して表示されるインターフェースを使ってください\n"
      + "正答となる範囲は、クリックして回答するための充分な面積が確保されていることが望まれます。\n" + "\n" + "◆分岐\n"
      + "不可能ですが、同じ画像を用意してそれぞれ別の問題を作成することは可能です。\n" + "\n" + "◆複数解答\n"
      + "可能。ひとつの画像の中で正答となる箇所を複数指定しても構いません。\n" + "例：【理系学問】で二酸化炭素の分子モデルの画像が出て「酸素原子はどれ？」とクリックさせる問題",
      8, 1, false, false, true, new ValidatorClick(), new JudgeClick(), new ShufflerDefault(),
      false, new AccuracyRateNormalizerDefault()),
  // 手書き
  Tegaki(14, "手書き", "手", "【手書き】\n" + "\n" + "　※この出題形式の使用は推奨しておりません\n"
      + "　画面に表示されるキャンバスに文字を書いて答えさせる問題形式\n" + "\n" + "◆問題文の書式\n" + "【タイピング】に準拠します\n" + "\n"
      + "◆回答について\n" + "１文字以上８文字以下。ただし入力文字数が少なくなるようにして下さい。\n"
      + "使える文字種は日本語内のほとんどの文字ですが、一部使用できない文字もあります。\n" + "プレビュー画面において実際に候補が現れることを確認して下さい。\n"
      + "\"口(くち)\"と\"□(しかく)\"等、紛らわしい文字は避けてください\n" + "\n" + "◆分岐\n" + "【タイピング】に準拠します\n" + "\n"
      + "◆複数解答\n" + "【タイピング】に準拠します", 8, 0, false, false, false, new ValidatorTegaki(),
      new JudgeDefault(), new ShufflerDefault(), false, new AccuracyRateNormalizerDefault()), //
  // 早い者勝ち
  Hayaimono(15, "早い者勝ち", "早", "【早い者勝ち】\n" + "\n"
      + "　用意された６つまたは８つの選択肢の中から、問題文に該当するものを１つ、「早い者勝ち」方式で選択させる問題形式\n" + "\n" + "◆問題文の書式\n"
      + "「次のうち～を１つ選びなさい」「次の***のうち～を１つ選びなさい」という書式が基本となります。\n" + "画像は併用できません。\n" + "\n" + "◆選択肢\n"
      + "１～６あるいは１～８の選択肢を用意。\n" + "【線結び】の選択肢と同じように、固有名詞を囲むためのカギカッコ（「」『』）は省略されています。\n"
      + "実際の画面上で表示される選択肢の数は６つまたは８つの２パターンで、\n" + "６つの時は正解選択肢が２つ、８つの時は３つか４つとなります。\n"
      + "【一問多答】や【線結び】などの場合と同じく、それよりも多い数の正解選択肢およびダミー選択肢を用意してランダムで選抜し表示させる方式が可能です。\n"
      + "画像は使用できません。\n" + "\n" + "◆分岐\n" + "不可能ですが、全く同じ選択肢を用いてそれぞれ別の問題を作成することは可能です。", 4, 8, false,
      false, false, new ValidatorHayaimono(), new JudgeDefault(), new ShufflerDefault(), false,
      new AccuracyRateNormalizerDefault()), //
  // グループ分け
  Group(16, "グループ分け", "グ", "【グループ分け】\n" + "\n" + "　用意された３つ～５つの選択肢を指定されたグループに分類させる問題形式\n" + "\n"
      + "◆問題文の書式\n" + "「次の***を、～ごとにグループ分けしなさい」という書式が基本となります。\n" + "画像は併用できません。\n" + "\n" + "◆選択肢\n"
      + "３つ～５つの選択肢を書き、対応するグループを解答欄に書いて下さい。\n" + "グループは２種類または３種類入力することができます。\n"
      + "【線結び】の選択肢と同じように、固有名詞を囲むためのカギカッコ（「」『』）は省略されています。\n" + "画像は使用できません。\n" + "\n" + "◆分岐\n"
      + "不可能。\n", 5, 5, false, false, false, new ValidatorGroup(), new JudgeSenmusubi(),
      new ShufflerSenmusubi(5), false, new AccuracyRateNormalizerDefault()), //
  Random1(17, "ランダム１", "１", null, 0, 0, false, false, false, new ValidatorNull(),
      new JudgeDefault(), new ShufflerDefault(), false, new AccuracyRateNormalizerDefault()), //
  Random2(18, "ランダム２", "２", null, 0, 0, false, false, false, new ValidatorNull(),
      new JudgeDefault(), new ShufflerDefault(), false, new AccuracyRateNormalizerDefault()), //
  Random3(19, "ランダム３", "３", null, 0, 0, false, false, false, new ValidatorNull(),
      new JudgeDefault(), new ShufflerDefault(), false, new AccuracyRateNormalizerDefault()), //
  Random4(20, "ランダム４", "４", null, 0, 0, false, false, false, new ValidatorNull(),
      new JudgeDefault(), new ShufflerDefault(), false, new AccuracyRateNormalizerDefault()), //
  Ranodm5(21, "ランダム未分類", "未", null, 0, 0, false, false, false, new ValidatorNull(),
      new JudgeDefault(), new ShufflerDefault(), false, new AccuracyRateNormalizerDefault());
  /**
   * ランダムフラグオフセット(1-based)
   */
  public static final int randomOffset = Random1.index - 1;
  public static final int numberOfRandoms = 5;
  public static final int numberOfTypesWithoutRandom = Random1.index;
  public static final ProblemType[] valuesWithoutRandom = Arrays.asList(values())
      .subList(0, numberOfTypesWithoutRandom).toArray(new ProblemType[0]);

  private final int index;
  private final String name;
  private final String initial;
  private final String description;
  private final int numberOfAnswers;
  private final int numberOfChoices;
  private final boolean imageAnswer;
  private final boolean imageChoice;
  private final boolean polygonCreation;
  private final Validator validator;
  private final Judge judge;
  private final Shuffleable shuffler;
  private final boolean numberOfDisplayedChoicesChangeable;
  private final AccuracyRateNormalizable accuracyRateNormalizer;

  private ProblemType(int index, String name, String initial, String description,
      int numberOfAnswers, int numberOfChoices, boolean imageAnswer, boolean imageChoice,
      boolean polygonCreation, Validator validator, Judge judge, Shuffleable shuffler,
      boolean numberOfDisplayedChoicesChangeable, AccuracyRateNormalizable accuracyRateNormalizer) {
    this.index = index;
    this.name = name;
    this.initial = initial;
    this.description = description;
    this.numberOfAnswers = numberOfAnswers;
    this.numberOfChoices = numberOfChoices;
    this.imageAnswer = imageAnswer;
    this.imageChoice = imageChoice;
    this.polygonCreation = polygonCreation;
    this.validator = Preconditions.checkNotNull(validator);
    this.judge = Preconditions.checkNotNull(judge);
    this.shuffler = Preconditions.checkNotNull(shuffler);
    this.numberOfDisplayedChoicesChangeable = numberOfDisplayedChoicesChangeable;
    this.accuracyRateNormalizer = Preconditions.checkNotNull(accuracyRateNormalizer);
  }

  /**
   * ランダムフラグ(1-based)から{@link ProblemType}を生成する
   * 
   * @param randomFlag
   *          ランダムフラグ
   * @return {@link ProblemType}
   */
  public static ProblemType fromRandomFlag(int randomFlag) {
    return ProblemType.values()[randomOffset + randomFlag];
  }

  /**
   * 出題形式の頭文字を返す
   * 
   * @return 出題形式の頭文字
   */
  public String getInitial() {
    return initial;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getNumberOfAnswers() {
    return numberOfAnswers;
  }

  public int getNumberOfChoices() {
    return numberOfChoices;
  }

  public boolean isImageAnswer() {
    return imageAnswer;
  }

  public boolean isImageChoice() {
    return imageChoice;
  }

  public boolean isPolygonCreation() {
    return polygonCreation;
  }

  public Evaluation validate(PacketProblem problem) {
    return validator.check(problem);
  }

  public boolean judge(PacketProblem problem, String playerAnswer) {
    return judge.judge(problem, playerAnswer);
  }

  public void shuffleAnswersAndChoices(PacketProblem problem, int[] answerOrder, int[] choiceOrder) {
    shuffler.shuffle(problem, answerOrder, choiceOrder);
  }

  /**
   * ジャンルの集合をビットフラグへ変換する
   * 
   * @param genres
   *          ジャンルの集合
   * @return ビットフラグ
   */
  public static int toBitFlag(Set<ProblemType> types) {
    int bitFlag = 0;
    for (ProblemType type : types) {
      bitFlag |= (1 << type.index);
    }
    return bitFlag;
  }

  /**
   * ビットフラグをジャンルの集合へ変換する
   * 
   * @param bitFlag
   *          ビットフラグ
   * @return ジャンルの集合
   */
  public static Set<ProblemType> fromBitFlag(int bitFlag) {
    if (bitFlag == 0) {
      return Collections.emptySet();
    }

    List<ProblemType> types = Lists.newArrayList();
    for (ProblemType type : values()) {
      if ((bitFlag & (1 << type.index)) == 0) {
        continue;
      }
      types.add(type);
    }
    return Sets.newHashSet(types);
  }

  /**
   * 形式名から {@link ProblemType} を返す
   * 
   * @param name
   *          形式名
   * @return {@link ProblemType}
   */
  public static ProblemType fromName(String name) {
    for (ProblemType type : ProblemType.values()) {
      if (type.name.equals(name)) {
        return type;
      }
    }
    return null;
  }

  public boolean isNumberOfDisplayedChoicesChangeable() {
    return numberOfDisplayedChoicesChangeable;
  }

  public double getNormalizedAccuracyRate(PacketProblemMinimum problem) {
    return accuracyRateNormalizer.normalize(problem);
  }

}
