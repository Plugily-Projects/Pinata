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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.pinata.ConfigurationManager;

/**
 * @author Plajer
 * <p>
 * Created at 15.06.2018
 */
public class SelectorInventories {

  private Map<SelectorType, Inventory> selectorInventories = new HashMap<>();

  public SelectorInventories(String name) {

    ItemStack back = new ItemStack(Material.BARRIER, 1);
    ItemMeta backMeta = back.getItemMeta();
    backMeta.setDisplayName(ChatColor.RED + "Back to editor");
    back.setItemMeta(backMeta);

    Inventory dmg = Bukkit.createInventory(null, 9, "Modify damage type: " + name);

    ItemStack item = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta meta = item.getItemMeta();

    meta.setDisplayName(ChatColor.RED + "Private");
    meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to set private type", ChatColor.GRAY + "Only creator of pinata will be able to damage pinata"));
    item.setItemMeta(meta);
    dmg.setItem(0, item);

    meta.setDisplayName(ChatColor.GREEN + "Public");
    meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to set public type", ChatColor.GRAY + "Everyone will be able to damage pinata"));
    item.setItemMeta(meta);
    dmg.setItem(1, item);
    dmg.setItem(8, back);

    Inventory drop = Bukkit.createInventory(null, 9, "Modify drop type: " + name);
    meta.setDisplayName(ChatColor.GOLD + "On punch");
    meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to set punch drop type", ChatColor.GRAY + "Items will drop when pinata is damaged"));
    item.setItemMeta(meta);
    drop.setItem(0, item);

    meta.setDisplayName(ChatColor.DARK_RED + "On death");
    meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to set death drop type", ChatColor.GRAY + "Items will drop when pinata is killed"));
    item.setItemMeta(meta);
    drop.setItem(1, item);
    drop.setItem(8, back);

    FileConfiguration config = ConfigurationManager.getConfig("pinata_storage");
    Inventory itemStorage = Bukkit.createInventory(null, 9 * 5, "Modify drops: " + name);
    for (int i = 0; i < config.getList("storage." + name + ".drops", new ArrayList<>()).size(); i++) {
      if (config.getList("storage." + name + ".drops").get(i) == null) continue;
      ItemStack stack = (ItemStack) config.getList("storage." + name + ".drops").get(i);
      itemStorage.addItem(stack);
    }

    selectorInventories.put(SelectorType.DAMAGE_MODIFIER, dmg);
    selectorInventories.put(SelectorType.DROP_MODIFIER, drop);
    selectorInventories.put(SelectorType.ITEM_EDITOR, itemStorage);
  }

  public void openInventory(Player p, SelectorType inv) {
    p.openInventory(selectorInventories.get(inv));
  }

  public enum SelectorType {
    DAMAGE_MODIFIER, DROP_MODIFIER, ITEM_EDITOR
  }
}
