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

package pl.plajer.pinata.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.pinata.LivingPinata;
import pl.plajer.pinata.utils.Utils;

@Deprecated
public class MainCommand {

  private Map<Entity, LivingPinata> pinata = new HashMap<>();
  private List<Player> users = new ArrayList<>();
  private Main plugin;

  public MainCommand(Main plugin, boolean register) {
    if (register) {
      this.plugin = plugin;
    }
  }

  public boolean hasPermission(CommandSender sender, String permission) {
    if (!sender.hasPermission(permission)) {
      sender.sendMessage(Utils.colorMessage("Pinata.Command.No-Permission"));
      return false;
    }
    return true;
  }

  public Map<Entity, LivingPinata> getPinata() {
    return pinata;
  }

  public List<Player> getUsers() {
    return users;
  }

  public void setUsers(List<Player> users) {
    this.users = users;
  }
}
