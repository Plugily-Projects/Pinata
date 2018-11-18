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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
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
import pl.plajer.pinata.api.PinataFactory;
import pl.plajer.pinata.creator.CreatorMenu;
import pl.plajer.pinata.handlers.language.LanguageManager;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinata.PinataItem;
import pl.plajer.pinata.utils.PinataUtils;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.MinigameUtils;

/**
 * @author Plajer
 * <p>
 * Created at 31.10.2018
 */
public class ArgumentsRegistry extends MainCommand implements CommandExecutor {

  private Map<String, List<CommandArgument>> mappedArguments = new HashMap<>();

  public ArgumentsRegistry(Main plugin) {
    super(plugin, false);
    plugin.getCommand("pinata").setExecutor(this);
    mapArgument("pinata", new CommandArgument("create", "pinata.admin.create", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
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
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(pinata.getHealth());
            entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
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
          if (!hasPermission(sender, "pinata.admin.create.others")) {
            return;
          }
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
    });
    mapArgument("pinata", new CommandArgument("createnew", "pinata.admin.createpinata", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
          sender.sendMessage(Utils.colorRawMessage("%prefix% Please type pinata name!"));
        } else {
          if (plugin.getPinataManager().getPinataByName(args[1]) != null) {
            sender.sendMessage(Utils.colorRawMessage("%prefix% This pinata already exists!"));
            return;
          }
          PinataItem item = new PinataItem(new ItemStack(Material.PAPER, 1), 100.0);

          Pinata pinata = new Pinata(args[1], args[1], EntityType.SHEEP, DyeColor.WHITE, Pinata.PinataType.PRIVATE,
              Pinata.DropType.DEATH, 20.0, 10, -1, 5, "pinata.use." + args[1],
              true, 15, false, Collections.singletonList(item));
          plugin.getPinataManager().getPinataList().add(pinata);

          FileConfiguration config = ConfigUtils.getConfig(plugin, "pinata_storage");
          config.set("storage." + args[1] + ".display-name", args[1]);
          config.set("storage." + args[1] + ".timer-display", 5);
          config.set("storage." + args[1] + ".color", "WHITE");
          config.set("storage." + args[1] + ".permission-string", "pinata.use." + args[1]);
          config.set("storage." + args[1] + ".crate-buy-cost", -1);
          config.set("storage." + args[1] + ".pinata-access-type", "PRIVATE");
          config.set("storage." + args[1] + ".blindness-activated", true);
          config.set("storage." + args[1] + ".blindness-duration", 15);
          config.set("storage." + args[1] + ".full-blindness-activated", false);
          config.set("storage." + args[1] + ".crate-display-time-alive", 10);
          config.set("storage." + args[1] + ".health-amount", 5.0);
          config.set("storage." + args[1] + ".mob-entity-type", "SHEEP");
          config.set("storage." + args[1] + ".items-drop-type", "DEATH");
          config.set("storage." + args[1] + ".drops",
              new ArrayList<ItemStack>() {{
                add(item.getItem());
              }});
          ConfigUtils.saveConfig(plugin, config, "pinata_storage");
          sender.sendMessage(Utils.colorRawMessage("%prefix% New pinata with ID " + args[1] + " created!"));
        }
      }
    });
    mapArgument("pinata", new CommandArgument("edit", "pinata.admin.edit", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
          sender.sendMessage(Utils.colorRawMessage("%prefix% Please type pinata name!"));
        } else {
          if (plugin.getPinataManager().getPinataByName(args[1]) == null) {
            sender.sendMessage(Utils.colorRawMessage("%prefix% Pinata doesn't exist!"));
            return;
          }
          new CreatorMenu(args[1]).openInventory((Player) sender);
        }
      }
    });
    mapArgument("pinata", new CommandArgument("preview", "pinata.command.preview", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
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
    });
    mapArgument("pinata", new CommandArgument("reloadconfig", "pinata.admin.reload", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
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
        if (plugin.isNormalUpdate()) {
          sender.sendMessage(Utils.colorMessage("Other.Plugin-Up-To-Date").replace("%old%", plugin.getDescription().getVersion()).replace("%new%", plugin.getNewestVersion()));
        }
        sender.sendMessage(Utils.colorMessage("Pinata.Config.Reload-Success"));
      }
    });
    mapArgument("pinata", new CommandArgument("list", "pinata.command.list", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", (Player) sender);
      }
    });
    mapArgument("pinata", new CommandArgument("buy", "pinata.command.buy", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        final Player p = (Player) sender;
        if (!plugin.isPluginEnabled("Vault")) {
          p.sendMessage(Utils.colorMessage("Pinata.Command.Vault-Not-Detected"));
          return;
        }
        if (!ArgumentsRegistry.super.getUsers().isEmpty()) {
          if (ArgumentsRegistry.super.getUsers().contains(p)) {
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
        if (!PinataUtils.checkForSale(pinata, p)) {
          return;
        }
        if (!PinataUtils.checkPermissions(pinata, p)) {
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
    });
    mapArgument("pinata", new CommandArgument("setchance", "pinata.admin.setchance", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
          sender.sendMessage(Utils.colorRawMessage("%prefix% Please type chance!"));
        } else {
          Player p = (Player) sender;
          if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() == Material.AIR) {
            sender.sendMessage(Utils.colorRawMessage("%prefix% You must hold any item!"));
            return;
          }
          if (!NumberUtils.isNumber(args[1])) {
            sender.sendMessage(Utils.colorRawMessage("%prefix% Chance argument isn't a number!"));
            return;
          }
          double chance = Double.parseDouble(args[1]);
          ItemStack item = p.getInventory().getItemInMainHand();
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
            p.sendMessage(Utils.colorRawMessage("%prefix% Chance updated to " + chance));
          } else {
            MinigameUtils.addLore(item, "#!Chance:" + chance);
            p.sendMessage(Utils.colorRawMessage("%prefix% Chance set to " + chance));
          }
        }
      }
    });
    mapArgument("pinata", new CommandArgument("cratelist", "pinata.admin.crate.list", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
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
    });
    mapArgument("pinata", new CommandArgument("setcrate", "pinata.admin.crate.set", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
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
    });
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equals("pinata") && args.length == 0) {
      sender.sendMessage(Utils.colorMessage("Pinata.Command.Help-Command.Header"));
      sender.sendMessage(Utils.colorMessage("Pinata.Command.Help-Command.Description"));
      return true;
    }
    for (String mainCommand : mappedArguments.keySet()) {
      if (cmd.getName().equals(mainCommand)) {
        for (CommandArgument argument : mappedArguments.get(mainCommand)) {
          if (argument.getArgumentName().equals(args[0])) {
            if (checkSenderIsExecutorType(sender, argument.getValidExecutors()) && hasPermission(sender, argument.getPermission())) {
              argument.execute(sender, args);
            }
            //return true even if sender is not good executor or hasn't got permission
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean checkSenderIsExecutorType(CommandSender sender, CommandArgument.ExecutorType type) {
    switch (type) {
      case BOTH:
        return sender instanceof ConsoleCommandSender || sender instanceof Player;
      case CONSOLE:
        return sender instanceof ConsoleCommandSender;
      case PLAYER:
        if (sender instanceof Player) {
          return true;
        }
        sender.sendMessage(Utils.colorMessage("Pinata.Command.Only-Player"));
        return false;
      default:
        return false;
    }
  }

  /**
   * Maps new argument to the main command
   *
   * @param mainCommand mother command ex. /mm
   * @param argument    argument to map ex. leave (for /mm leave)
   */
  public void mapArgument(String mainCommand, CommandArgument argument) {
    List<CommandArgument> args = mappedArguments.getOrDefault(mainCommand, new ArrayList<>());
    args.add(argument);
    mappedArguments.put(mainCommand, args);
  }

}
