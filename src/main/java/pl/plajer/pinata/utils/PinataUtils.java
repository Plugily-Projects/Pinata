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

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinata.PinataItem;

/**
 * @author Plajer
 * <p>
 * Created at 01.09.2018
 */
public class PinataUtils {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  public static boolean checkPermissions(Pinata pinata, Player player) {
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

  public static void dropItems(PinataItem item, Entity en, Pinata pinata, Player player) {
    if (ThreadLocalRandom.current().nextDouble(0.0, 100.0) > item.getDropChance()) {
      return;
    }
    final Item dropItem = en.getWorld().dropItemNaturally(en.getLocation(), new ItemStack(item.getItem().getType()));
    dropItem.setPickupDelay(1000);
    if (plugin.isPluginEnabled("HolographicDisplays")) {
      final Hologram hologram = HologramsAPI.createHologram(plugin, dropItem.getLocation().add(0.0, 1.5, 0.0));

      hologram.appendTextLine(Utils.colorRawMessage(item.getItem().getItemMeta().getDisplayName() != null ? item.getItem().getItemMeta().getDisplayName() : item.getItem().getType().name() + " x" + item.getItem().getAmount()));
      new BukkitRunnable() {
        int ticksRun;

        @Override
        public void run() {
          ticksRun++;
          hologram.teleport(dropItem.getLocation().add(0.0, 1.5, 0.0));
          if (ticksRun > pinata.getDropViewTime() * 20) {
            hologram.delete();
            dropItem.remove();
            cancel();
          }
        }
      }.runTaskTimer(plugin, 1L, 1L);
    } else {
      Bukkit.getScheduler().runTaskLater(plugin, dropItem::remove, pinata.getDropViewTime() * 20);
    }
    //todo cmd

    //Adds color
    ItemMeta meta = item.getItem().getItemMeta();
    meta.setDisplayName(Utils.colorRawMessage(item.getItem().getItemMeta().getDisplayName() != null ? item.getItem().getItemMeta().getDisplayName() :
        item.getItem().getType().name()));
    item.getItem().setItemMeta(meta);

    player.getInventory().addItem(item.getItem());
    player.sendMessage(Utils.colorRawMessage(Utils.colorMessage("Pinata.Drop.DropMsg").replace("%item%", item.getItem().getItemMeta().getDisplayName() != null ? item.getItem().getItemMeta().getDisplayName() :
        item.getItem().getType().name()).replace("%amount%", String.valueOf(item.getItem().getAmount()))));
  }

}
