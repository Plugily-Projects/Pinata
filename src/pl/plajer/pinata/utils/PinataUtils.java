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

package pl.plajer.pinata.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.pinata.Pinata;

/**
 * @author Plajer
 * <p>
 * Created at 01.09.2018
 */
public class PinataUtils {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  public static boolean checkPermissions(Pinata pinata, Player player){
    if (plugin.getConfig().getBoolean("using-permissions")) {
      if (!player.hasPermission(pinata.getPermission())) {
        player.sendMessage(Utils.colorMessage("Pinata.Create.No-Permission"));
        player.closeInventory();
        return false;
      }
    }
    return true;
  }

  public static boolean checkForSale(Pinata pinata, Player player) {
    if (pinata.getPrice() == -1) {
      player.sendMessage(Utils.colorMessage("Pinata.Selling.Not-For-Sale"));
      player.closeInventory();
      return false;
    }
    return true;
  }

}
