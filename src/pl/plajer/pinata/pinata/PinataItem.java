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

import org.bukkit.inventory.ItemStack;

/**
 * @author Plajer
 * <p>
 * Created at 15.03.2018
 */
public class PinataItem {

  private double dropChance;
  private ItemStack item;
  //private String command;

  public PinataItem(ItemStack item, double dropChance) {
    this.item = item;
    this.dropChance = dropChance;
  }

  public double getDropChance() {
    return dropChance;
  }

  public ItemStack getItem() {
    return item;
  }

    /*public String getCommand() {
        return command;
    }*/

    /*public void setCommand(String command) {
        this.command = command;
    }*/
}
