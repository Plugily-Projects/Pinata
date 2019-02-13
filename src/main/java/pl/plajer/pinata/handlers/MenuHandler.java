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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.api.PinataFactory;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.utils.PinataUtils;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.XMaterial;

public class MenuHandler implements Listener {

  private Main plugin;

  public MenuHandler(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onMenuInteract(final InventoryClickEvent e) {
    if (e.getInventory().getName().equals(Utils.colorMessage("Menus.Preview-Menu.Inventory-Name"))) {
      e.setCancelled(true);
      return;
    }
    if (e.getCurrentItem() == null || !e.getCurrentItem().getType().equals(XMaterial.WHITE_WOOL.parseMaterial())) {
      return;
    }
    final ItemMeta item = e.getCurrentItem().getItemMeta();
    final String pinataName = item.getDisplayName().replace(Utils.colorRawMessage("&6"), "");
    Pinata pinata = plugin.getPinataManager().getPinataByName(pinataName);
    if (pinata == null) {
      return;
    }
    if (e.getInventory().getName().equals(Utils.colorMessage("Menus.List-Menu.Inventory-Name"))) {
      e.setCancelled(true);
      if (e.getClick() == ClickType.LEFT) {
        e.getWhoClicked().closeInventory();
        //Scheduler to prevent bugged GUI
        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(e.getWhoClicked(), "pinata preview " + pinataName), 1);
      }
      if (e.getClick() == ClickType.RIGHT) {
        if (item.getLore().get(1).equals(Utils.colorMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"))) {
          e.getWhoClicked().closeInventory();
          e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Selling.Not-For-Sale"));
          return;
        }
        e.getWhoClicked().closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(e.getWhoClicked(), Utils.colorRawMessage("pinata buy " + ChatColor.stripColor(item.getDisplayName()))), 1);
      }
    }
    if (e.getInventory().getName().equals(Utils.colorMessage("Menus.Crate-Menu.Inventory-Name")) || e.getInventory().getName().equals(Utils.colorMessage("Signs.Inventory-Name"))) {
      e.setCancelled(true);
      if (e.getClick() == ClickType.LEFT) {
        e.getWhoClicked().closeInventory();
        //Scheduler to prevent bugged GUI
        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(e.getWhoClicked(), "pinata preview " + pinataName), 1);
      }
      if (e.getClick() == ClickType.RIGHT) {
        if (item.getLore().get(1).equals(Utils.colorMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"))) {
          e.getWhoClicked().closeInventory();
          e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Selling.Not-For-Sale"));
          return;
        }
        Location builderLocation;
        Location entityLocation;
        if (e.getInventory().getName().equals(Utils.colorMessage("Menus.Crate-Menu.Inventory-Name"))) {
          if (plugin.getCrateManager().getCrateUsage().containsKey(e.getWhoClicked())) {
            builderLocation = plugin.getCrateManager().getCrateUsage().get(e.getWhoClicked()).clone().add(0, 8, 0);
            entityLocation = plugin.getCrateManager().getCrateUsage().get(e.getWhoClicked()).clone().add(0, 3, 0);
          } else {
            e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Buy-Error"));
            return;
          }
          //sign inventory
        } else {
          if (plugin.getSignManager().getSignUsage().containsKey(e.getWhoClicked())) {
            builderLocation = plugin.getSignManager().getSignUsage().get(e.getWhoClicked()).clone().add(0, 8, 0);
            entityLocation = plugin.getSignManager().getSignUsage().get(e.getWhoClicked()).clone().add(0, 3, 0);
          } else {
            e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Buy-Error"));
            return;
          }
        }
        if (!PinataUtils.checkForSale(pinata, (Player) e.getWhoClicked())) {
          return;
        }
        if (!plugin.getStorage().getUsers().isEmpty()) {
          if (plugin.getStorage().getUsers().contains(e.getWhoClicked())) {
            e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Create.Already-Created"));
            e.getWhoClicked().closeInventory();
            return;
          }
        }
        if (!PinataUtils.checkPermissions(pinata, (Player) e.getWhoClicked())) {
          return;
        }
        if (e.getWhoClicked().hasPermission("pinata.admin.freeall")) {
          LivingEntity entity = (LivingEntity) entityLocation.getWorld().spawnEntity(entityLocation, pinata.getEntityType());
          entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(pinata.getHealth());
          entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
          plugin.getStorage().getUsers().add((Player) e.getWhoClicked());
          if (PinataFactory.createPinata(builderLocation, (Player) e.getWhoClicked(), entity, pinata)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
              if (!(entity.isDead())) {
                entity.damage(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
              }
            }, pinata.getCrateTime() * 20);
          }
        } else if (plugin.getEco().getBalance(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId())) >= pinata.getPrice()) {
          LivingEntity entity = (LivingEntity) entityLocation.getWorld().spawnEntity(entityLocation, pinata.getEntityType());
          entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(pinata.getHealth());
          entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
          plugin.getStorage().getUsers().add((Player) e.getWhoClicked());
          if (PinataFactory.createPinata(builderLocation, (Player) e.getWhoClicked(), entity, pinata)) {
            //Pinata created successfully, now we can withdraw $ from player.
            plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId()), pinata.getPrice());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
              if (!(entity.isDead())) {
                entity.damage(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
              }
            }, pinata.getCrateTime() * 20);
          }
        } else {
          e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Selling.Cannot-Afford"));
        }
        e.getWhoClicked().closeInventory();
      }
    }
  }

}
