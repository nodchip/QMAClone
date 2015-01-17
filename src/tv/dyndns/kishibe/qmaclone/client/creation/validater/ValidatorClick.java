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
package tv.dyndns.kishibe.qmaclone.client.creation.validater;

import java.util.List;

import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;
import tv.dyndns.kishibe.qmaclone.client.geom.Polygon;
import tv.dyndns.kishibe.qmaclone.client.geom.PolygonException;
import tv.dyndns.kishibe.qmaclone.client.packet.PacketProblem;

import com.google.common.base.Preconditions;

public class ValidatorClick extends Validator {
  public Evaluation check(PacketProblem problem) {
    Preconditions.checkArgument(problem.type == ProblemType.Click);

    Evaluation eval = super.check(problem);
    List<String> warn = eval.warn;

    String url = problem.choices[0];
    if (!isUrl(url)) {
      warn.add("選択肢に正しいURLが入力されていません");
      return eval;
    }

    String urlLower = url.toLowerCase();
    if (!urlLower.endsWith(".bmp") && !urlLower.endsWith(".png") && !urlLower.endsWith(".gif")
        && !urlLower.endsWith(".jpg") && !urlLower.endsWith(".jpeg")) {
      warn.add("使用可能な画像形式はBMP・PNG・GIF・JPGのみです");
      return eval;
    }

    List<String> answerList = problem.getAnswerList();
    if (answerList.isEmpty()) {
      warn.add("解答が入力されていません");
      return eval;
    }

    for (int i = 0; i < answerList.size(); ++i) {
      try {
        validate(answerList.get(i), warn);
      } catch (PolygonException e) {
        warn.add((i + 1) + "番目の解答が領域を表現した文字列になっていません: " + e.getMessage());
      }
    }

    return eval;
  }

  private void validate(String polygonDescription, List<String> warn) throws PolygonException {
    Polygon.fromString(polygonDescription).isCompleted();
  }
}
