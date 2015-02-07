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

import java.util.Set;

import tv.dyndns.kishibe.qmaclone.client.game.GameMode;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemGenre;
import tv.dyndns.kishibe.qmaclone.client.game.ProblemType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.gwt.user.client.rpc.IsSerializable;

public class PacketRoomKey implements IsSerializable {
  private GameMode gameMode;
  private String name;
  private Set<ProblemGenre> genres;
  private Set<ProblemType> types;

  public PacketRoomKey() {
  }

  public PacketRoomKey(GameMode gameMode, String name, Set<ProblemGenre> genres,
      Set<ProblemType> types) {
    genres = gameMode == GameMode.EVENT ? genres : ImmutableSet.of(ProblemGenre.Random);
    types = gameMode == GameMode.EVENT ? types : ImmutableSet.of(ProblemType.Random);

    this.gameMode = gameMode;
    this.name = name;
    this.genres = ImmutableSet.copyOf(genres);
    this.types = ImmutableSet.copyOf(types);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof PacketRoomKey && Objects.equal(gameMode, ((PacketRoomKey) obj).gameMode)
        && Objects.equal(name, ((PacketRoomKey) obj).name)
        && Objects.equal(genres, ((PacketRoomKey) obj).genres)
        && Objects.equal(types, ((PacketRoomKey) obj).types);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(gameMode, name, genres, types);
  }

  @Override
  public String toString() {
    StringBuilder genreInitials = new StringBuilder();
    for (ProblemGenre genre : genres) {
      genreInitials.append(genre.getInitial());
    }

    StringBuilder typeInitials = new StringBuilder();
    for (ProblemType type : types) {
      typeInitials.append(type.getInitial());
    }

    return MoreObjects.toStringHelper(this).add("gameMode", gameMode).add("name", name)
        .add("genres", genreInitials).add("types", typeInitials).toString();
  }

  public String getName() {
    return name;
  }

  public Set<ProblemGenre> getGenres() {
    return genres;
  }

  public Set<ProblemType> getTypes() {
    return types;
  }
}
