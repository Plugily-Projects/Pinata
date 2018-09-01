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

package pl.plajer.pinata.pinata;

import org.bukkit.Location;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;

/**
 * @author Plajer
 * <p>
 * Created at 28 lis 2017
 */
public class LivingPinata {

  private Player player;
  private Location fenceLocation;
  private LeashHitch leash;
  private Pinata data;

  public LivingPinata(Player player, Location fenceLocation, LeashHitch leash, Pinata data) {
    this.player = player;
    this.fenceLocation = fenceLocation;
    this.leash = leash;
    this.data = data;
  }

  public LivingPinata(Location f, LeashHitch l, Pinata data) {
    fenceLocation = f;
    leash = l;
    this.data = data;
  }

  public Player getPlayer() {
    return player;
  }

  public Location getFence() {
    return fenceLocation;
  }

  public LeashHitch getLeash() {
    return leash;
  }

  public Pinata getData() {
    return data;
  }
}
