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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 15.06.2018
 */
public class SelectorEvents implements Listener {

  private Main plugin;

  public SelectorEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (e.getInventory().getName() == null || e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || !e.getCurrentItem().getItemMeta().hasDisplayName()) {
      return;
    }
    if (e.getInventory().getName().contains("Modify damage type: ")) {
      e.setCancelled(true);
      FileConfiguration config = ConfigUtils.getConfig(plugin, "pinata_storage");
      Pinata pinata = plugin.getPinataManager().getPinataByName(e.getInventory().getName().replace("Modify damage type: ", ""));
      if (pinata == null) {
        return;
      }
      if (e.getCurrentItem().getItemMeta().getDisplayName().contains("Private")) {
        pinata.setPinataType(Pinata.PinataType.PRIVATE);
        config.set("storage." + pinata.getID() + ".pinata-access-type", "PRIVATE");
        e.getWhoClicked().sendMessage("Pinata access type set to " + e.getCurrentItem().getItemMeta().getDisplayName());
      } else if (e.getCurrentItem().getItemMeta().getDisplayName().contains("Public")) {
        pinata.setPinataType(Pinata.PinataType.PUBLIC);
        config.set("storage." + pinata.getID() + ".pinata-access-type", "PUBLIC");
        e.getWhoClicked().sendMessage("Pinata access type set to " + e.getCurrentItem().getItemMeta().getDisplayName());
      } else if (e.getCurrentItem().getItemMeta().getDisplayName().contains("Back to editor")) {
        e.getWhoClicked().closeInventory();
        new CreatorMenu(pinata.getID()).openInventory(((Player) e.getWhoClicked()));
      }
      ConfigUtils.saveConfig(plugin, config, "pinata_storage");
    } else if (e.getInventory().getName().contains("Modify drop type: ")) {
      e.setCancelled(true);
      FileConfiguration config = ConfigUtils.getConfig(plugin, "pinata_storage");
      Pinata pinata = plugin.getPinataManager().getPinataByName(e.getInventory().getName().replace("Modify drop type: ", ""));
      if (pinata == null) {
        return;
      }
      if (e.getCurrentItem().getItemMeta().getDisplayName().contains("On punch")) {
        pinata.setDropType(Pinata.DropType.PUNCH);
        config.set("storage." + pinata.getID() + ".items-drop-type", "PUNCH");
        e.getWhoClicked().sendMessage("Pinata items drop type set to " + e.getCurrentItem().getItemMeta().getDisplayName());
      } else if (e.getCurrentItem().getItemMeta().getDisplayName().contains("On death")) {
        pinata.setDropType(Pinata.DropType.DEATH);
        config.set("storage." + pinata.getID() + ".items-drop-type", "DEATH");
        e.getWhoClicked().sendMessage("Pinata items drop type set to " + e.getCurrentItem().getItemMeta().getDisplayName());
      } else if (e.getCurrentItem().getItemMeta().getDisplayName().contains("Back to editor")) {
        e.getWhoClicked().closeInventory();
        new CreatorMenu(pinata.getID()).openInventory(((Player) e.getWhoClicked()));
      }
      ConfigUtils.saveConfig(plugin, config, "pinata_storage");
    }
  }

}
