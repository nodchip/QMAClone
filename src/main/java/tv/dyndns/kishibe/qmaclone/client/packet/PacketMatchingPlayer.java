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

import name.pehl.piriti.json.client.JsonReader;

import com.google.common.base.MoreObjects;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketMatchingPlayer implements IsSerializable {
  public static class Json {
    public interface PacketMatchingPlayerReader extends JsonReader<PacketMatchingPlayer> {
    }

    public static final PacketMatchingPlayerReader READER = GWT
        .create(PacketMatchingPlayerReader.class);
  }

  public PacketPlayerSummary playerSummary;
  public boolean isRequestSkip;
  public String greeting;
  public String imageFileName;

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("playerSummary", playerSummary)
        .add("isRequestSkip", isRequestSkip).add("greeting", greeting)
        .add("imageFileName", imageFileName).toString();
  }
}
