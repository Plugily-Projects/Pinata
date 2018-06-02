package pl.plajer.pinata.commands;

import org.bukkit.Bukkit;
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
import pl.plajer.pinata.ConfigurationManager;
import pl.plajer.pinata.Main;
import pl.plajer.pinata.PinataItem;
import pl.plajer.pinata.pinataapi.PinataFactory;
import pl.plajer.pinata.utils.UpdateChecker;
import pl.plajer.pinata.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2018
 */
public class ArgumentsManager extends MainCommand {

    private Main plugin;

    public ArgumentsManager(Main plugin) {
        super(plugin, false);
        this.plugin = plugin;
    }

    public void reloadConfig(CommandSender sender) {
        if(!hasPermission(sender, "pinata.admin.reload")) return;
        try {
            plugin.reloadConfig();
            plugin.getPinataManager().getPinataList().clear();
            plugin.getPinataManager().loadPinatas();
            plugin.setupLocale();
        } catch(Exception e) {
            sender.sendMessage(Utils.colorMessage("Pinata.Config.Reload-Fail"));
            return;
        }
        plugin.getDisabledWorlds().clear();
        for(String world : plugin.getConfig().getStringList("disabled-worlds")) {
            plugin.getDisabledWorlds().add(world);
            plugin.getLogger().info("Pinata creation blocked at world " + world + "!");
        }
        String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
        if(plugin.getConfig().getBoolean("update-notify")) {
            try {
                UpdateChecker.checkUpdate(currentVersion);
                String latestVersion = UpdateChecker.getLatestVersion();
                if(latestVersion != null) {
                    latestVersion = "v" + latestVersion;
                    sender.sendMessage(Utils.colorMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
                }
            } catch(Exception ex) {
                sender.sendMessage(Utils.colorMessage("Other.Plugin-Update-Check-Failed").replaceAll("%error%", ex.getMessage()));
            }
        }
        sender.sendMessage(Utils.colorMessage("Pinata.Config.Reload-Success"));
    }

    public void setCrate(CommandSender sender, String[] args) {
        if(!hasPermission(sender, "pinata.admin.crate.set")) return;
        if(!isSenderPlayer(sender)) return;
        if(args.length != 2) {
            sender.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Specify-Name"));
            return;
        }
        Player p = (Player) sender;
        Block l = p.getTargetBlock(null, 20);
        if(l.getType().equals(Material.CHEST)) {
            if(plugin.getCrateManager().getCratesLocations().containsKey(l.getLocation())) {
                p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Is-Set-Here"));
                return;
            }
            if(ConfigurationManager.getConfig("crates").isSet("crates." + args[1])) {
                p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Already-Exists"));
                return;
            }

            FileConfiguration config = ConfigurationManager.getConfig("crates");
            config.set("crates." + args[1] + ".world", l.getWorld().getName());
            config.set("crates." + args[1] + ".x", l.getX());
            config.set("crates." + args[1] + ".y", l.getY());
            config.set("crates." + args[1] + ".z", l.getZ());
            config.set("crates." + args[1] + ".name", args[1]);
            ConfigurationManager.saveConfig(config, "crates");

            plugin.getCrateManager().getCratesLocations().put(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()), args[1]);
            p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Create-Success").replaceAll("%name%", args[1]));
            return;
        } else {
            p.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.Target-Block-Not-Chest"));
            return;
        }
    }

    public void printCrateList(CommandSender sender) {
        if(!hasPermission(sender, "pinata.admin.crate.list")) return;
        int num = 0;
        sender.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.List"));
        for(Location l : plugin.getCrateManager().getCratesLocations().keySet()) {
            sender.sendMessage(Utils.colorRawMessage("&a" + plugin.getCrateManager().getCratesLocations().get(l) + " - X: " + l.getX() + " Y: " + l.getY() + " Z: " + l.getZ()));
            num++;
        }
        if(num == 0) {
            sender.sendMessage(Utils.colorMessage("Pinata.Crate-Creation.List-Empty"));
        }
    }

    public void createPinata(CommandSender sender, String[] args) {
        if(!hasPermission(sender, "pinata.admin.create")) return;
        if(args.length == 6) {
            //custom location is used
            try {
                Random r = new Random();
                World world = Bukkit.getWorld(args[1]);
                int x, y, z;
                if(args[2].contains("~")) {
                    String[] rand = args[2].split("~");
                    Integer randomNum = r.nextInt(Integer.parseInt(rand[1]) - Integer.parseInt(rand[0]));
                    x = Integer.parseInt(rand[0] + randomNum);
                } else {
                    x = Integer.parseInt(args[2]);
                }
                if(args[3].contains("~")) {
                    String[] rand = args[3].split("~");
                    Integer randomNum = r.nextInt(Integer.parseInt(rand[1]) - Integer.parseInt(rand[0]));
                    y = Integer.parseInt(rand[0] + randomNum);
                } else {
                    y = Integer.parseInt(args[3]);
                }
                if(args[4].contains("~")) {
                    String[] rand = args[4].split("~");
                    Integer randomNum = r.nextInt(Integer.parseInt(rand[1]) - Integer.parseInt(rand[0]));
                    z = Integer.parseInt(rand[0] + randomNum);
                } else {
                    z = Integer.parseInt(args[4]);
                }
                if(!plugin.getPinataManager().getPinataList().contains(args[5])) {
                    sender.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
                    return;
                }
                Location l = new Location(world, x, y, z);
                LivingEntity entity = (LivingEntity) l.getWorld().spawnEntity(l.clone().add(0, 2, 0), EntityType.valueOf(ConfigurationManager.getConfig("pinatas").getString("pinatas." + args[5] + ".mob-type").toUpperCase()));
                entity.setMaxHealth(ConfigurationManager.getConfig("pinatas").getDouble("pinatas." + args[5] + ".health"));
                entity.setHealth(entity.getMaxHealth());
                PinataFactory.createPinata(l.clone().add(0, 7, 0), entity, args[5]);
                sender.sendMessage(Utils.colorMessage("Pinata.Create.Success").replaceAll("%name%", args[5]));
                return;
            } catch(Exception e) {
                sender.sendMessage(Utils.colorMessage("Pinata.Command.Custom-Location-Create-Error"));
                return;
            }
        }
        if(args.length == 1) {
            sender.sendMessage(Utils.colorMessage("Pinata.Specify-Name"));
            return;
        }
        Player user;
        if(sender instanceof ConsoleCommandSender) {
            if(args.length != 3) {
                sender.sendMessage(Utils.colorMessage("Pinata.Command.Console-Specify-Player"));
                return;
            }
            if(Bukkit.getPlayer(args[2]) != null) {
                user = Bukkit.getPlayer(args[2]);
                user.sendMessage(Utils.colorMessage("Pinata.Create.By-Console"));
            } else {
                sender.sendMessage(Utils.colorMessage("Pinata.Command.Player-Not-Found"));
                return;
            }
        } else {
            if(!hasPermission(sender, "pinata.admin.create.others")) return;
            if(args.length != 3) {
                user = (Player) sender;
            } else {
                if(Bukkit.getPlayer(args[2]) != null) {
                    user = Bukkit.getPlayer(args[2]);
                } else {
                    sender.sendMessage(Utils.colorMessage("Pinata.Command.Player-Not-Found"));
                    return;
                }
            }
        }
        if(!plugin.getPinataManager().getPinataList().contains(args[1])) {
            sender.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
            return;
        }
        Utils.createPinataAtPlayer(user, user.getLocation(), args[1]);
    }

    public void buyPinata(CommandSender sender, String[] args) {
        if(!isSenderPlayer(sender)) return;
        final Player p = (Player) sender;
        if(!plugin.isPluginEnabled("Vault")) {
            p.sendMessage(Utils.colorMessage("Pinata.Command.Vault-Not-Detected"));
            return;
        }
        if(!hasPermission(sender, "pinata.command.buy")) return;
        if(!super.getUsers().isEmpty()) {
            if(super.getUsers().contains(p)) {
                p.sendMessage(Utils.colorMessage("Pinata.Create.Already-Created"));
                return;
            }
        }
        if(plugin.getDisabledWorlds().contains(p.getWorld().getName())) {
            p.sendMessage(Utils.colorMessage("Pinata.Create.Disabled-World"));
            return;
        }
        if(args.length == 1) {
            Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", p);
            return;
        }
        if(!plugin.getPinataManager().getPinataList().contains(args[1])) {
            p.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
            return;
        }
        if(ConfigurationManager.getConfig("pinatas").getInt("pinatas." + args[1] + ".cost") == -1) {
            p.sendMessage(Utils.colorMessage("Pinata.Selling.Not-For-Sale"));
            return;
        }
        if(plugin.getConfig().getBoolean("using-permissions")) {
            final String pinataPermission = ConfigurationManager.getConfig("pinatas").get("pinatas." + args[1] + ".permission").toString();
            if(!p.hasPermission(pinataPermission)) {
                p.sendMessage(Utils.colorMessage("Pinata.Create.No-Permission"));
                return;
            }
        }
        if(p.hasPermission("pinata.admin.freeall")) {
            Utils.createPinataAtPlayer(p, p.getLocation(), args[1]);
        } else if(plugin.getEco().getBalance(Bukkit.getOfflinePlayer(p.getUniqueId())) >= ConfigurationManager.getConfig("pinatas").getDouble("pinatas." + args[1] + ".cost")) {
            if(Utils.createPinataAtPlayer(p, p.getLocation(), args[1])) {
                plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), ConfigurationManager.getConfig("pinatas").getDouble("pinatas." + args[1] + ".cost"));
            }
        } else {
            sender.sendMessage(Utils.colorMessage("Pinata.Selling.Cannot-Afford"));
        }
    }

    public void openPreviewMenu(CommandSender sender, String[] args) {
        if(!isSenderPlayer(sender)) return;
        if(!hasPermission(sender, "pinata.command.preview")) return;
        if(args.length == 1) {
            sender.sendMessage(Utils.colorMessage("Pinata.Specify-Name"));
            return;
        }
        if(!plugin.getPinataManager().getPinataList().contains(args[1])) {
            sender.sendMessage(Utils.colorMessage("Pinata.Not-Found"));
            return;
        }
        int rows = Utils.serializeInt(plugin.getPinataManager().getPinataDrop().get(args[1]).size());
        Inventory previewMenu = Bukkit.createInventory(null, rows, Utils.colorMessage("Menus.Preview-Menu.Inventory-Name"));
        int i = 0;
        for(PinataItem item : plugin.getPinataManager().getPinataDrop().get(args[1])) {
            ItemStack stack = new ItemStack(item.getRepresentedMaterial(), item.getAmount());
            ItemMeta meta = stack.getItemMeta();
            List<String> lore = new ArrayList<>();
            String dropLore = Utils.colorMessage("Menus.Preview-Menu.Drop-Chance").replaceAll("%chance%", String.valueOf(item.getDropChance()));
            switch(item.getItemType()) {
                case ITEM:
                    meta.setDisplayName(item.getItem().getItemMeta().getDisplayName());
                    if(item.getItem().getItemMeta().hasLore()) {
                        lore.addAll(item.getItem().getItemMeta().getLore());
                    }
                    break;
                case COMMAND:
                    meta.setDisplayName(item.getHologramName());
                    lore.add(Utils.colorMessage("Menus.Preview-Menu.Command-To-Execute").replaceAll("%command%", item.getCommand().replaceAll("%player%", sender.getName())));
                    break;
                case GUN:
                    meta.setDisplayName(item.getHologramName());
                    break;
                case MONEY:
                    meta.setDisplayName(item.getHologramName());
                    lore.add(Utils.colorMessage("Menus.Preview-Menu.Money-Reward").replaceAll("%money%", String.valueOf(item.getMoneyValue())));
                    break;
            }
            lore.add(dropLore);
            meta.setLore(lore);
            stack.setItemMeta(meta);
            previewMenu.setItem(i, stack);
            i++;
        }
        ((Player) sender).openInventory(previewMenu);
    }

}
