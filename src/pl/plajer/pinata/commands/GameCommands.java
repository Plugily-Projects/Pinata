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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.handlers.language.LanguageManager;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinata.PinataItem;
import pl.plajer.pinata.pinataapi.PinataFactory;
import pl.plajer.pinata.utils.PinataUtils;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.MinigameUtils;
import pl.plajerlair.core.utils.UpdateChecker;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
public class GameCommands extends MainCommand {

  private Main plugin;

  public GameCommands(Main plugin) {
    super(plugin, false);
    this.plugin = plugin;
  }

  public void reloadConfig(CommandSender sender) {
    if (!hasPermission(sender, "pinata.admin.reload")) return;
    try {
      plugin.reloadConfig();
      plugin.getPinataManager().getPinataList().clear();
      plugin.getPinataManager().loadPinatas();
      //todo hmm???
      LanguageManager.init(plugin);
    } catch (Exception e) {
      sender.sendMessage(Utils.colorMessage("Pinata.Config.Reload-Fail"));
      return;
    }
    plugin.getDisabledWorlds().clear();
    for (String world : plugin.getConfig().getStringList("disabled-worlds")) {
      plugin.getDisabledWorlds().add(world);
      plugin.getLogger().info("Pinata creation blocked at world " + world + "!");
    }
    if (plugin.getConfig().getBoolean("update-notify")) {
      try {
        String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
        boolean check = UpdateChecker.checkUpdate(plugin, currentVersion, 46655);
        if (check) {
          String latestVersion = "v" + UpdateChecker.getLatestVersion();
          sender.sendMessage(Utils.colorMessage("Other.Plugin-Up-To-Date").replace("%old%", currentVersion).replace("%new%", latestVersion));
        }
      } catch (Exception ex) {
        sender.sendMessage(Utils.colorMessage("Other.Plugin-Update-Check-Failed").replace("%error%", ex.getMessage()));
      }
    }
    sender.sendMessage(Utils.colorMessage("Pinata.Config.Reload-Success"));
  }

  public void setCrate(CommandSender sender, String[] args) {
    if (!hasPermission(sender, "pinata.admin.crate.set")) return;
    if (!isSenderPlayer(sender)) return;
    if (args.length != 2) {
      sender.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Specify-Name"));
      return;
    }
    Player p = (Player) sender;
    Block l = p.getTargetBlock(null, 20);
    if (l.getType().equals(Material.CHEST)) {
      if (plugin.getCrateManager().getCratesLocations().containsKey(l.getLocation())) {
        p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Is-Set-Here"));
        return;
      }
      if (ConfigUtils.getConfig(plugin, "crates").isSet("crates." + args[1])) {
        p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Already-Exists"));
        return;
      }

      FileConfiguration config = ConfigUtils.getConfig(plugin, "crates");
      config.set("crates." + args[1] + ".world", l.getWorld().getName());
      config.set("crates." + args[1] + ".x", l.getX());
      config.set("crates." + args[1] + ".y", l.getY());
      config.set("crates." + args[1] + ".z", l.getZ());
      config.set("crates." + args[1] + ".name", args[1]);
      ConfigUtils.saveConfig(plugin, config, "crates");

      plugin.getCrateManager().getCratesLocations().put(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()), args[1]);
      p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Create-Success").replace("%name%", args[1]));
    } else {
      p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Target-Block-Not-Chest"));
    }
  }

  public void printCrateList(CommandSender sender) {
    if (!hasPermission(sender, "pinata.admin.crate.list")) return;
    int num = 0;
    sender.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.List"));
    for (Location l : plugin.getCrateManager().getCratesLocations().keySet()) {
      sender.sendMessage(Utils.colorRawMessage("&a" + plugin.getCrateManager().getCratesLocations().get(l) + " - X: " + l.getX() + " Y: " + l.getY() + " Z: " + l.getZ()));
      num++;
    }
    if (num == 0) {
      sender.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.List-Empty"));
    }
  }

  public void createPinata(CommandSender sender, String[] args) {
    if (!hasPermission(sender, "pinata.admin.create")) return;
    if (args.length == 6) {
      //custom location is used
      try {
        Random r = new Random();
        World world = Bukkit.getWorld(args[1]);
        int x, y, z;
        if (args[2].contains("~")) {
          String[] rand = args[2].split("~");
          Integer randomNum = r.nextInt(Integer.parseInt(rand[1]) - Integer.parseInt(rand[0]));
          x = Integer.parseInt(rand[0] + randomNum);
        } else {
          x = Integer.parseInt(args[2]);
        }
        if (args[3].contains("~")) {
          String[] rand = args[3].split("~");
          Integer randomNum = r.nextInt(Integer.parseInt(rand[1]) - Integer.parseInt(rand[0]));
          y = Integer.parseInt(rand[0] + randomNum);
        } else {
          y = Integer.parseInt(args[3]);
        }
        if (args[4].contains("~")) {
          String[] rand = args[4].split("~");
          Integer randomNum = r.nextInt(Integer.parseInt(rand[1]) - Integer.parseInt(rand[0]));
          z = Integer.parseInt(rand[0] + randomNum);
        } else {
          z = Integer.parseInt(args[4]);
        }
        Pinata pinata = plugin.getPinataManager().getPinataByName(args[5]);
        if (pinata == null) {
          sender.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
          return;
        }
        Location l = new Location(world, x, y, z);
        LivingEntity entity = (LivingEntity) l.getWorld().spawnEntity(l.clone().add(0, 2, 0), pinata.getEntityType());
        entity.setMaxHealth(pinata.getHealth());
        entity.setHealth(entity.getMaxHealth());
        PinataFactory.createPinata(l.clone().add(0, 7, 0), entity, pinata);
        sender.sendMessage(Utils.colorMessage("Pinata.Create.Success").replace("%name%", args[5]));
        return;
      } catch (Exception e) {
        sender.sendMessage(Utils.colorMessage("Pinata.Command.Custom-Location-Create-Error"));
        return;
      }
    }
    if (args.length == 1) {
      sender.sendMessage(Utils.colorMessage("Pinata.Specify-Name"));
      return;
    }
    Player user;
    if (sender instanceof ConsoleCommandSender) {
      if (args.length != 3) {
        sender.sendMessage(Utils.colorMessage("Pinata.Command.Console-Specify-Player"));
        return;
      }
      if (Bukkit.getPlayer(args[2]) != null) {
        user = Bukkit.getPlayer(args[2]);
        user.sendMessage(Utils.colorMessage("Pinata.Create.By-Console"));
      } else {
        sender.sendMessage(Utils.colorMessage("Pinata.Command.Player-Not-Found"));
        return;
      }
    } else {
      if (!hasPermission(sender, "pinata.admin.create.others")) return;
      if (args.length != 3) {
        user = (Player) sender;
      } else {
        if (Bukkit.getPlayer(args[2]) != null) {
          user = Bukkit.getPlayer(args[2]);
        } else {
          sender.sendMessage(Utils.colorMessage("Pinata.Command.Player-Not-Found"));
          return;
        }
      }
    }
    Pinata pinata = plugin.getPinataManager().getPinataByName(args[1]);
    if (pinata == null) {
      sender.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
      return;
    }
    Utils.createPinataAtPlayer(user, user.getLocation(), pinata);
  }

  public void buyPinata(CommandSender sender, String[] args) {
    if (!isSenderPlayer(sender)) return;
    final Player p = (Player) sender;
    if (!plugin.isPluginEnabled("Vault")) {
      p.sendMessage(Utils.colorMessage("Pinata.Command.Vault-Not-Detected"));
      return;
    }
    if (!hasPermission(sender, "pinata.command.buy")) return;
    if (!super.getUsers().isEmpty()) {
      if (super.getUsers().contains(p)) {
        p.sendMessage(Utils.colorMessage("Pinata.Create.Already-Created"));
        return;
      }
    }
    if (plugin.getDisabledWorlds().contains(p.getWorld().getName())) {
      p.sendMessage(Utils.colorMessage("Pinata.Create.Disabled-World"));
      return;
    }
    if (args.length == 1) {
      Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", p);
      return;
    }
    Pinata pinata = plugin.getPinataManager().getPinataByName(args[1]);
    if (pinata == null) {
      p.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
      return;
    }
    if(!PinataUtils.checkForSale(pinata, p)) {
      return;
    }
    if(!PinataUtils.checkPermissions(pinata, p)) {
      return;
    }
    if (p.hasPermission("pinata.admin.freeall")) {
      Utils.createPinataAtPlayer(p, p.getLocation(), pinata);
    } else if (plugin.getEco().getBalance(Bukkit.getOfflinePlayer(p.getUniqueId())) >= pinata.getPrice()) {
      if (Utils.createPinataAtPlayer(p, p.getLocation(), pinata)) {
        plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), pinata.getPrice());
      }
    } else {
      sender.sendMessage(Utils.colorMessage("Pinata.Selling.Cannot-Afford"));
    }
  }


  public void openPreviewMenu(CommandSender sender, String[] args) {
    if (!isSenderPlayer(sender)) return;
    if (!hasPermission(sender, "pinata.command.preview")) return;
    if (args.length == 1) {
      sender.sendMessage(Utils.colorMessage("Pinata.Specify-Name"));
      return;
    }
    Pinata pinata = plugin.getPinataManager().getPinataByName(args[1]);
    if (pinata == null) {
      sender.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
      return;
    }
    int rows = MinigameUtils.serializeInt(pinata.getDrops().size());
    Inventory previewMenu = Bukkit.createInventory(null, rows, Utils.colorMessage("Menus.Preview-Menu.Inventory-Name"));
    int i = 0;
    //todo cmd
    for (PinataItem item : pinata.getDrops()) {
      ItemMeta meta = item.getItem().getItemMeta();
      List<String> lore = new ArrayList<>();
      String dropLore = Utils.colorMessage("Menus.Preview-Menu.Drop-Chance").replace("%chance%", String.valueOf(item.getDropChance()));
      lore.add(dropLore);
      meta.setLore(lore);
      ItemStack stack = item.getItem();
      stack.setItemMeta(meta);
      previewMenu.setItem(i, stack);
      i++;
    }
    ((Player) sender).openInventory(previewMenu);
  }

  public void createNewPinata(CommandSender sender, String pinataID) {
    //todo perm check
    //todo console check
    if (plugin.getPinataManager().getPinataByName(pinataID) != null) {
      sender.sendMessage("This pinata already exists!");
      return;
    }
    PinataItem item = new PinataItem(new ItemStack(Material.PAPER, 1), 100.0);

    Pinata pinata = new Pinata(pinataID, pinataID, EntityType.SHEEP, DyeColor.WHITE, Pinata.PinataType.PRIVATE,
            Pinata.DropType.DEATH, 20.0, 10, -1, 5, "pinata.use." + pinataID,
            true, 15, false, Collections.singletonList(item));
    plugin.getPinataManager().getPinataList().add(pinata);

    FileConfiguration config = ConfigUtils.getConfig(plugin, "pinata_storage");
    config.set("storage." + pinataID + ".display-name", pinataID);
    config.set("storage." + pinataID + ".timer-display", 5);
    config.set("storage." + pinataID + ".color", "WHITE");
    config.set("storage." + pinataID + ".permission-string", "pinata.use." + pinataID);
    config.set("storage." + pinataID + ".crate-buy-cost", -1);
    config.set("storage." + pinataID + ".pinata-access-type", "PRIVATE");
    config.set("storage." + pinataID + ".blindness-activated", true);
    config.set("storage." + pinataID + ".blindness-duration", 15);
    config.set("storage." + pinataID + ".full-blindness-activated", false);
    config.set("storage." + pinataID + ".crate-display-time-alive", 10);
    config.set("storage." + pinataID + ".health-amount", 5.0);
    config.set("storage." + pinataID + ".mob-entity-type", "SHEEP");
    config.set("storage." + pinataID + ".items-drop-type", "DEATH");
    config.set("storage." + pinataID + ".drops",
            new ArrayList<ItemStack>() {{
              add(item.getItem());
            }});
    ConfigUtils.saveConfig(plugin, config, "pinata_storage");
    sender.sendMessage("New pinata with ID " + pinataID + " created!");
  }

  public void applyChanceToItem(CommandSender sender, String str) {
    if (!isSenderPlayer(sender)) return;
    if (!hasPermission(sender, "pinata.admin.setchance")) return;
    Player p = (Player) sender;
    if (p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
      sender.sendMessage("You must hold any item!");
      return;
    }
    if (!NumberUtils.isNumber(str)) {
      sender.sendMessage("Chance argument isn't a number!");
      return;
    }
    double chance = Double.parseDouble(str);
    ItemStack item = p.getItemInHand();
    if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
      ItemMeta meta = item.getItemMeta();
      List<String> lore = item.getItemMeta().getLore();
      for (String search : lore) {
        if (search.contains("#!Chance")) {
          lore.remove(search);
          break;
        }
      }
      lore.add(0, "#!Chance:" + chance);
      meta.setLore(lore);
      item.setItemMeta(meta);
      p.sendMessage("Chance updated to " + chance);
    } else {
      MinigameUtils.addLore(item, "#!Chance:" + chance);
      p.sendMessage("Chance set to " + chance);
    }
  }

}
