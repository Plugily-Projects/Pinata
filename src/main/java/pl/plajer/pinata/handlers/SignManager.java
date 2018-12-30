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

package pl.plajer.pinata.handlers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.api.PinataFactory;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.XMaterial;

public class SignManager implements Listener {

  private Map<Player, Location> signUsage = new HashMap<>();
  private Main plugin;

  public SignManager(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onSignChange(SignChangeEvent e) {
    if (!e.getLine(0).equalsIgnoreCase("[pinata]")) {
      return;
    }
    if (!e.getPlayer().hasPermission("pinata.admin.sign.set")) {
      e.getPlayer().sendMessage(Utils.colorMessage("Signs.No-Permission"));
      e.setCancelled(true);
    }
    if (e.getLine(1).isEmpty()) {
      e.getPlayer().sendMessage(Utils.colorMessage("Signs.Invalid-Pinata"));
      return;
    }
    e.setLine(0, Utils.colorMessage("Signs.Lines.First"));
    if (e.getLine(1).equalsIgnoreCase("all") || e.getLine(1).equalsIgnoreCase("gui")) {
      e.setLine(1, Utils.colorMessage("Signs.Lines.Second-Every-Pinata"));
      return;
    }
    Pinata pinata = plugin.getPinataManager().getPinataByName(e.getLine(1));
    if (pinata == null) {
      e.getPlayer().sendMessage(Utils.colorMessage("Signs.Invalid-Pinata"));
      return;
    }
    e.setLine(1, Utils.colorMessage("Signs.Lines.Second-Specific-Pinata-Color") + e.getLine(1));
  }

  @EventHandler
  public void onSignDestroy(BlockBreakEvent e) {
    if (e.getBlock().getType().equals(Material.SIGN) || e.getBlock().getType().equals(XMaterial.SIGN.parseMaterial()) || e.getBlock().getType().equals(XMaterial.WALL_SIGN.parseMaterial())) {
      Sign s = (Sign) e.getBlock().getState();
      if (!s.getLine(0).equals(Utils.colorMessage("Signs.Lines.First"))) {
        return;
      }
      if (!e.getPlayer().hasPermission("pinata.admin.sign.destroy")) {
        e.getPlayer().sendMessage(Utils.colorMessage("Signs.No-Permission"));
        e.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onSignClick(PlayerInteractEvent e) {
    //cancel left click action to avoid sign break problems
    if (e.getClickedBlock() == null || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
      return;
    }
    if (e.getClickedBlock().getType().equals(Material.SIGN) || e.getClickedBlock().getType().equals(XMaterial.SIGN.parseMaterial()) || e.getClickedBlock().getType().equals(XMaterial.WALL_SIGN.parseMaterial())) {
      Sign s = (Sign) e.getClickedBlock().getState();
      if (!s.getLine(0).equals(Utils.colorMessage("Signs.Lines.First"))) {
        return;
      }
      if (!plugin.isPluginEnabled("Vault")) {
        e.getPlayer().sendMessage(Utils.colorMessage("Pinata.Command.Vault-Not-Detected"));
        return;
      }
      if (!e.getPlayer().hasPermission("pinata.player.sign")) {
        e.getPlayer().sendMessage(Utils.colorMessage("Sings.No-Permission"));
        e.setCancelled(true);
        return;
      }
      if (!plugin.getConfig().getBoolean("disabled-worlds-exclusions.signs")) {
        if (plugin.getDisabledWorlds().contains(e.getPlayer().getWorld().getName())) {
          e.getPlayer().sendMessage(Utils.colorMessage("Pinata.Create.Disabled-World"));
          return;
        }
      }
      if (s.getLine(1).equals(Utils.colorMessage("Signs.Lines.Second-Every-Pinata"))) {
        signUsage.put(e.getPlayer(), e.getClickedBlock().getLocation());
        Utils.createPinatasGUI("Signs.Inventory-Name", e.getPlayer());
        return;
      }
      String pinataName = ChatColor.stripColor(s.getLine(1));
      for (Pinata pinata : plugin.getPinataManager().getPinataList()) {
        if (pinata.getID().equalsIgnoreCase(pinataName)) {
          Location loc = e.getClickedBlock().getLocation().clone().add(0, 8, 0);
          Location entityLoc = e.getClickedBlock().getLocation().clone().add(0, 3, 0);
          LivingEntity entity = (LivingEntity) entityLoc.getWorld().spawnEntity(entityLoc, pinata.getEntityType());
          entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(pinata.getHealth());
          entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
          if (e.getPlayer().hasPermission("pinata.admin.freeall")) {
            if (PinataFactory.createPinata(loc, e.getPlayer(), entity, pinata)) {
              Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!(entity.isDead())) {
                  entity.damage(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                }
              }, pinata.getCrateTime() * 20);
            }
          } else if (plugin.getEco().getBalance(Bukkit.getOfflinePlayer(e.getPlayer().getUniqueId())) >= pinata.getPrice()) {
            if (PinataFactory.createPinata(loc, e.getPlayer(), entity, pinata)) {
              //Pinata created successfully, now we can withdraw $ from player.
              plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(e.getPlayer().getUniqueId()), pinata.getPrice());
              Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!(entity.isDead())) {
                  entity.damage(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                }
              }, pinata.getCrateTime() * 20);
            }
          } else {
            e.getPlayer().sendMessage(Utils.colorMessage("Pinata.Selling.Cannot-Afford"));
          }
        }
      }
    }
  }

  public Map<Player, Location> getSignUsage() {
    return signUsage;
  }
}
