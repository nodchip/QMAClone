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

import com.google.common.base.MoreObjects;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketMatchingPlayer implements IsSerializable {
  public PacketPlayerSummary playerSummary;
  public boolean isRequestSkip;
  public String greeting;
  public String imageFileName;

  public static PacketMatchingPlayer fromJsonObject(JSONObject object) {
    PacketMatchingPlayer player = new PacketMatchingPlayer();
    JSONObject summaryObject = PacketJsonParser.getObject(object, "playerSummary");
    if (summaryObject != null) {
      player.playerSummary = PacketPlayerSummary.fromJsonObject(summaryObject);
    }
    player.isRequestSkip = PacketJsonParser.getBoolean(object, "isRequestSkip");
    player.greeting = PacketJsonParser.getString(object, "greeting");
    player.imageFileName = PacketJsonParser.getString(object, "imageFileName");
    return player;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("playerSummary", playerSummary)
        .add("isRequestSkip", isRequestSkip).add("greeting", greeting)
        .add("imageFileName", imageFileName).toString();
  }
}
