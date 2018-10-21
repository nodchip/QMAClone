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
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketWrongAnswer implements IsSerializable, Comparable<PacketWrongAnswer> {

  public String answer;
  public int count;

  public PacketWrongAnswer setAnswer(String answer) {
    this.answer = answer;
    return this;
  }

  public PacketWrongAnswer setCount(int count) {
    this.count = count;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(answer, count);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PacketWrongAnswer)) {
      return false;
    }
    PacketWrongAnswer rh = (PacketWrongAnswer) obj;
    return Objects.equal(answer, rh.answer) && count == rh.count;
  }

  @Override
  public int compareTo(PacketWrongAnswer o) {
    return ComparisonChain.start().compare(answer, o.answer).compare(count, o.count).result();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("answer", answer).add("count", count).toString();
  }
}
