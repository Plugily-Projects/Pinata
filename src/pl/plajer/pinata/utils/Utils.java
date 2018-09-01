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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.pinata.ConfigurationManager;
import pl.plajer.pinata.Main;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinataapi.PinataFactory;

public class Utils {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  public static String colorMessage(String message) {
    return ChatColor.translateAlternateColorCodes('&', ConfigurationManager.getLanguageMessage(message));
  }

  public static String colorRawMessage(String message) {
    return ChatColor.translateAlternateColorCodes('&', message);
  }

  public static int serializeInt(Integer i) {
    if ((i % 9) == 0) return i;
    else return (int) ((Math.ceil(i / 9) * 9) + 9);
  }

  public static boolean createPinataAtPlayer(Player p, Location l, Pinata pinata) {
    Location loc = l.clone().add(0, 7, 0);
    LivingEntity entity = (LivingEntity) l.getWorld().spawnEntity(l.clone().add(0, 2, 0), pinata.getEntityType());
    entity.setMaxHealth(pinata.getHealth());
    entity.setHealth(entity.getMaxHealth());
    return PinataFactory.createPinata(loc, p, entity, pinata);
  }

  public static void createPinatasGUI(String name, Player p) {
    int rows = serializeInt(plugin.getPinataManager().getPinataList().size());
    Inventory pinatasMenu = Bukkit.createInventory(null, rows, Utils.colorMessage(name));
    for (int i = 0; i < plugin.getPinataManager().getPinataList().size(); i++) {
      Pinata pinata = plugin.getPinataManager().getPinataList().get(i);
      ItemStack item = new ItemStack(Material.WOOL, 1);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(Utils.colorRawMessage("&6") + pinata.getID());
      List<String> lore = new ArrayList<>();
      if (pinata.getPinataType() == Pinata.PinataType.PRIVATE) {
        lore.add(colorMessage("Menus.List-Menu.Pinata-Types.Type-Private"));
      } else {
        lore.add(colorMessage("Menus.List-Menu.Pinata-Types.Type-Public"));
      }
      if (pinata.getPrice() == -1) {
        lore.add(colorMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"));
      } else {
        String cost = colorMessage("Menus.List-Menu.Pinata-Cost");
        lore.add(cost.replaceAll("%money%", String.valueOf(pinata.getPrice())) + "$");
        lore.add(colorMessage("Menus.List-Menu.Click-Selection.Right-Click"));
      }
      lore.add(colorMessage("Menus.List-Menu.Click-Selection.Left-Click"));
      meta.setLore(lore);
      item.setItemMeta(meta);
      pinatasMenu.setItem(i, item);
    }
    p.openInventory(pinatasMenu);
  }

  public static void addLore(ItemStack itemStack, String string) {
    ItemMeta meta = itemStack.getItemMeta();
    List<String> lore = new ArrayList<>();
    if (meta != null && meta.hasLore()) lore.addAll(meta.getLore());
    lore.add(string);
    meta.setLore(lore);
    itemStack.setItemMeta(meta);
  }

}
