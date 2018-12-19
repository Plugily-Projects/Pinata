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

package pl.plajer.pinata.api;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinata.PinataItem;

/**
 * Called when entity is killed.
 */
//todo
public class PinataDeathEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Player killer;
  private final Entity entity;
  private final Pinata pinata;
  private final List<PinataItem> drops;

  public PinataDeathEvent(Player killer, Entity entity, Pinata pinata, List<PinataItem> drops) {
    this.killer = killer;
    this.entity = entity;
    this.pinata = pinata;
    this.drops = drops;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  /**
   * @return killer of entity.
   */
  public Player getKiller() {
    return this.killer;
  }

  /**
   * @return killed entity.
   */
  public Entity getEntity() {
    return this.entity;
  }

  public Pinata getPinata() {
    return pinata;
  }

  /**
   * Returns List of PinataItem objects.
   *
   * @return drops from entity.
   * @since 2.1.3
   */
  public List<PinataItem> getDrops() {
    return this.drops;
  }
}
