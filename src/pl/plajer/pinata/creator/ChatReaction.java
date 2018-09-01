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

  public enum ReactionType {
    SET_MOB_TYPE, SET_HEALTH, SET_CRATE_TIME, SET_DROP_VIEW_TIME, SET_BLINDNESS_DURATION, SET_BLINDNESS, SET_FULL_BLINDNESS
  }

}
