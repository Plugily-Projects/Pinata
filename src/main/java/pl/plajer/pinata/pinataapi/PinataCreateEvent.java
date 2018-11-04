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

package pl.plajer.pinata.pinataapi;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import pl.plajer.pinata.pinata.Pinata;

/**
 * Called when entity is created.
 */
//todo
public class PinataCreateEvent extends Event implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Entity entity;
  private final Pinata pinata;
  private Player creator = null;
  private boolean isCancelled;

  public PinataCreateEvent(Player creator, Entity entity, Pinata pinata) {
    this.creator = creator;
    this.entity = entity;
    this.pinata = pinata;
  }

  public PinataCreateEvent(Entity entity, Pinata pinata) {
    this.entity = entity;
    this.pinata = pinata;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public boolean isCancelled() {
    return this.isCancelled;
  }

  public void setCancelled(boolean isCancelled) {
    this.isCancelled = isCancelled;
  }

  /**
   * @return creator of entity.
   * @throws NullPointerException when entity is created without player participation.
   */
  public Player getCreator() {
    return this.creator;
  }

  /**
   * @return event entity.
   */
  public Entity getEntity() {
    return this.entity;
  }

  public Pinata getPinata() {
    return pinata;
  }
}
