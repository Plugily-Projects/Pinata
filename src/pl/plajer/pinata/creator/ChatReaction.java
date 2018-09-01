/*
 * Pinata plugin - spawn pinata mob and kill it to get drops
 * Copyright (C)2018 Plajer
 *
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pl.plajer.pinata.creator;

import pl.plajer.pinata.pinata.Pinata;

/**
 * @author Plajer
 * <p>
 * Created at 09.06.2018
 */
public class ChatReaction {

  private ReactionType reactionType;
  private Pinata pinata;

  @java.beans.ConstructorProperties({"reactionType", "pinata"})
  public ChatReaction(ReactionType reactionType, Pinata pinata) {
    this.reactionType = reactionType;
    this.pinata = pinata;
  }

  public ReactionType getReactionType() {
    return this.reactionType;
  }

  public void setReactionType(ReactionType reactionType) {
    this.reactionType = reactionType;
  }

  public Pinata getPinata() {
    return this.pinata;
  }

  public void setPinata(Pinata pinata) {
    this.pinata = pinata;
  }

  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof ChatReaction)) return false;
    final ChatReaction other = (ChatReaction) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$reactionType = this.getReactionType();
    final Object other$reactionType = other.getReactionType();
    if (this$reactionType == null ? other$reactionType != null : !this$reactionType.equals(other$reactionType)) return false;
    final Object this$pinata = this.getPinata();
    final Object other$pinata = other.getPinata();
    if (this$pinata == null ? other$pinata != null : !this$pinata.equals(other$pinata)) return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $reactionType = this.getReactionType();
    result = result * PRIME + ($reactionType == null ? 43 : $reactionType.hashCode());
    final Object $pinata = this.getPinata();
    result = result * PRIME + ($pinata == null ? 43 : $pinata.hashCode());
    return result;
  }

  protected boolean canEqual(Object other) {
    return other instanceof ChatReaction;
  }

  public String toString() {
    return "ChatReaction(reactionType=" + this.getReactionType() + ", pinata=" + this.getPinata() + ")";
  }

  public enum ReactionType {
    SET_MOB_TYPE, SET_HEALTH, SET_CRATE_TIME, SET_DROP_VIEW_TIME, SET_BLINDNESS_DURATION, SET_BLINDNESS, SET_FULL_BLINDNESS
  }

}
