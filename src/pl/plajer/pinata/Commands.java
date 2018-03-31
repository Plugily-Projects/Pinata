package pl.plajer.pinata;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.plajer.pinata.dao.Pinata;
import pl.plajer.pinata.dao.PinataData;
import pl.plajer.pinata.dao.PinataItem;
import pl.plajer.pinata.pinataapi.PinataFactory;
import pl.plajer.pinata.utils.UpdateChecker;
import pl.plajer.pinata.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Commands implements CommandExecutor {

    @Getter
    private Map<Entity, PinataData> pinata = new HashMap<>();
    @Getter
    @Setter
    private List<Player> users = new ArrayList<>();
    private Main plugin;

    Commands(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("pinata").setExecutor(this);
    }

    private boolean isSenderPlayer(CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorFileMessage("Pinata.Command.Only-Player"));
            return false;
        }
        return true;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if(!sender.hasPermission(permission)) {
            sender.sendMessage(Utils.colorFileMessage("Pinata.Command.No-Permission"));
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("pinata")) {
            if(!hasPermission(sender, "pinata.command")) return true;
            if(args.length == 0) {
                sender.sendMessage(Utils.colorFileMessage("Pinata.Command.Help-Command.Header"));
                sender.sendMessage(Utils.colorFileMessage("Pinata.Command.Help-Command.Description"));
                return true;
            }
            if(args[0].equalsIgnoreCase("list")) {
                if(!isSenderPlayer(sender)) return true;
                if(!hasPermission(sender, "pinata.command.list")) return true;
                Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", (Player) sender);
                return true;
            } else if(args[0].equalsIgnoreCase("preview")) {
                if(!isSenderPlayer(sender)) return true;
                if(!hasPermission(sender, "pinata.command.preview")) return true;
                if(args.length == 1) {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Specify-Name"));
                    return true;
                }
                for(Pinata pinata : plugin.getPinataManager().getPinataList()) {
                    if(pinata.getID().equals(args[1])) {
                        Inventory previewMenu = Bukkit.createInventory(null, Utils.serializeInt(pinata.getDrops().size()), Utils.colorFileMessage("Menus.Preview-Menu.Inventory-Name"));
                        for(PinataItem is : pinata.getDrops()) {
                            ItemMeta meta = is.getItem().getItemMeta();
                            List<String> lore = meta.getLore();
                            lore.add(Utils.colorFileMessage("Menus.Preview-Menu.Drop-Chance").replaceAll("%chance%", String.valueOf(is.getDropChance())));
                            meta.setLore(lore);
                            is.getItem().setItemMeta(meta);
                            previewMenu.addItem(is.getItem());
                        }
                        ((Player) sender).openInventory(previewMenu);
                        return true;
                    }
                }
                sender.sendMessage(Utils.colorFileMessage("Pinata.Not-Found"));
                return true;
            } else if(args[0].equalsIgnoreCase("buy")) {
                if(!isSenderPlayer(sender)) return true;
                final Player p = (Player) sender;
                if(!plugin.isPluginEnabled("Vault")) {
                    p.sendMessage(Utils.colorFileMessage("Pinata.Command.Vault-Not-Detected"));
                    return true;
                }
                if(!hasPermission(sender, "pinata.command.buy")) return true;
                if(!users.isEmpty()) {
                    if(users.contains(p)) {
                        p.sendMessage(Utils.colorFileMessage("Pinata.Create.Already-Created"));
                        return true;
                    }
                }
                if(plugin.getDisabledWorlds().contains(p.getWorld().getName())) {
                    p.sendMessage(Utils.colorFileMessage("Pinata.Create.Disabled-World"));
                    return true;
                }
                if(args.length == 1) {
                    Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", p);
                    return true;
                }
                for(Pinata pinata : plugin.getPinataManager().getPinataList()) {
                    if(pinata.getID().equals(args[1])) {
                        if(pinata.getPrice() == -1) {
                            p.sendMessage(Utils.colorFileMessage("Pinata.Selling.Not-For-Sale"));
                            return true;
                        }
                        if(plugin.getConfig().getBoolean("using-permissions")) {
                            if(!p.hasPermission(pinata.getPermission())) {
                                p.sendMessage(Utils.colorFileMessage("Pinata.Create.No-Permission"));
                                return true;
                            }
                        }
                        if(p.hasPermission("pinata.admin.freeall")) {
                            Utils.createPinataAtPlayer(p, p.getLocation(), pinata);
                            return true;
                        } else if(plugin.getEco().getBalance(Bukkit.getOfflinePlayer(p.getUniqueId())) >= plugin.getFileManager().getPinataConfig().getDouble("pinatas." + args[1] + ".cost")) {
                            if(Utils.createPinataAtPlayer(p, p.getLocation(), pinata)) {
                                plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), plugin.getFileManager().getPinataConfig().getDouble("pinatas." + args[1] + ".cost"));
                            }
                            return true;
                        } else {
                            sender.sendMessage(Utils.colorFileMessage("Pinata.Selling.Cannot-Afford"));
                            return true;
                        }
                    }
                }
                sender.sendMessage(Utils.colorFileMessage("Pinata.Not-Found"));
                return true;
            } else if(args[0].equalsIgnoreCase("reloadconfig")) {
                if(!hasPermission(sender, "pinata.admin.reload")) return true;
                try {
                    plugin.reloadConfig();
                    plugin.getPinataManager().getPinataList().clear();
                    plugin.saveDefaultConfig();
                    plugin.getFileManager().saveDefaultMessagesConfig();
                    plugin.getFileManager().saveDefaultPinataConfig();
                    plugin.reloadConfig();
                    plugin.getFileManager().reloadMessagesConfig();
                    plugin.getFileManager().reloadPinataConfig();
                    plugin.getPinataManager().loadPinatas3();
                    plugin.setupLocale();
                } catch(Exception e) {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Config.Reload-Fail"));
                    return true;
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
                            sender.sendMessage(Utils.colorFileMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
                        }
                    } catch(Exception ex) {
                        sender.sendMessage(Utils.colorFileMessage("Other.Plugin-Update-Check-Failed").replaceAll("%error%", ex.getMessage()));
                    }
                }
                sender.sendMessage(Utils.colorFileMessage("Pinata.Config.Reload-Success"));
                return true;
            } else if(args[0].equalsIgnoreCase("create")) {
                if(!hasPermission(sender, "pinata.admin.create")) return true;
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
                        for(Pinata pinata : plugin.getPinataManager().getPinataList()) {
                            if(pinata.getID().equals(args[5])) {
                                Location l = new Location(world, x, y, z);
                                LivingEntity entity = (LivingEntity) l.getWorld().spawnEntity(l.clone().add(0, 2, 0), EntityType.valueOf(Main.getInstance().getFileManager().getPinataConfig().getString("pinatas." + args[5] + ".mob-type").toUpperCase()));
                                entity.setMaxHealth(Main.getInstance().getFileManager().getPinataConfig().getDouble("pinatas." + args[5] + ".health"));
                                entity.setHealth(entity.getMaxHealth());
                                PinataFactory.createPinata(l.clone().add(0, 7, 0), entity, args[5]);
                                sender.sendMessage(Utils.colorFileMessage("Pinata.Create.Success").replaceAll("%name%", args[5]));
                                return true;
                            }
                        }
                        sender.sendMessage(Utils.colorFileMessage("Pinata.Not-Found"));
                        return true;
                    } catch(Exception e) {
                        sender.sendMessage(Utils.colorFileMessage("Pinata.Command.Custom-Location-Create-Error"));
                        return true;
                    }
                }
                if(args.length == 1) {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Specify-Name"));
                    return true;
                }
                Player user;
                if(sender instanceof ConsoleCommandSender) {
                    if(args.length != 3) {
                        sender.sendMessage(Utils.colorFileMessage("Pinata.Command.Console-Specify-Player"));
                        return true;
                    }
                    if(Bukkit.getPlayer(args[2]) != null) {
                        user = Bukkit.getPlayer(args[2]);
                        user.sendMessage(Utils.colorFileMessage("Pinata.Create.By-Console"));
                    } else {
                        sender.sendMessage(Utils.colorFileMessage("Pinata.Command.Player-Not-Found"));
                        return true;
                    }
                } else {
                    if(!hasPermission(sender, "pinata.admin.create.others")) return true;
                    if(args.length != 3) {
                        user = (Player) sender;
                    } else {
                        if(Bukkit.getPlayer(args[2]) != null) {
                            user = Bukkit.getPlayer(args[2]);
                        } else {
                            sender.sendMessage(Utils.colorFileMessage("Pinata.Command.Player-Not-Found"));
                            return true;
                        }
                    }
                }
                for(Pinata pinata : plugin.getPinataManager().getPinataList()) {
                    if(pinata.getID().equals(args[1])) {
                        Utils.createPinataAtPlayer(user, user.getLocation(), pinata);
                        return true;
                    }
                }
                sender.sendMessage(Utils.colorFileMessage("Pinata.Not-Found"));
                return true;
            } else if(args[0].equalsIgnoreCase("setcrate")) {
                if(!hasPermission(sender, "pinata.admin.crate.set")) return true;
                if(!isSenderPlayer(sender)) return true;
                if(args.length != 2) {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Crate-Creation.Specify-Name"));
                    return true;
                }
                Player p = (Player) sender;
                Block l = p.getTargetBlock(null, 20);
                if(l.getType().equals(Material.CHEST)) {
                    if(plugin.getCrateManager().getCratesLocations().containsKey(l.getLocation())) {
                        p.sendMessage(Utils.colorFileMessage("Pinata.Crate-Creation.Is-Set-Here"));
                        return true;
                    }
                    if(plugin.getFileManager().getCratesConfig().isSet("crates." + args[1])) {
                        p.sendMessage(Utils.colorFileMessage("Pinata.Crate-Creation.Already-Exists"));
                        return true;
                    }
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".world", l.getWorld().getName());
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".x", l.getX());
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".y", l.getY());
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".z", l.getZ());
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".name", args[1]);
                    plugin.getFileManager().saveCratesConfig();
                    plugin.getCrateManager().getCratesLocations().put(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()), args[1]);
                    p.sendMessage(Utils.colorFileMessage("Pinata.Crate-Creation.Create-Success").replaceAll("%name%", args[1]));
                    return true;
                } else {
                    p.sendMessage(Utils.colorFileMessage("Pinata.Crate-Creation.Target-Block-Not-Chest"));
                    return true;
                }
            } else if(args[0].equalsIgnoreCase("cratelist")) {
                if(!hasPermission(sender, "pinata.admin.crate.list")) return true;
                int num = 0;
                sender.sendMessage(Utils.colorFileMessage("Pinata.Crate-Creation.List"));
                for(Location l : plugin.getCrateManager().getCratesLocations().keySet()) {
                    sender.sendMessage(Utils.colorMessage("&a" + plugin.getCrateManager().getCratesLocations().get(l) + " - X: " + l.getX() + " Y: " + l.getY() + " Z: " + l.getZ()));
                    num++;
                }
                if(num == 0) {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Crate-Creation.List-Empty"));
                }
                return true;
            } else if(args[0].equalsIgnoreCase("edit")) {
                if(!hasPermission(sender, "pinata.admin.editor")) return true;
                if(!isSenderPlayer(sender)) return true;
                if(args.length == 1) {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Specify-Name"));
                    return true;
                }
                for(Pinata pinata : plugin.getPinataManager().getPinataList()) {
                    if(pinata.getID().equals(args[1])) {
                        Inventory inv = Bukkit.createInventory(null, 9 * 5, Utils.colorMessage("&lEdit items of pinata " + args[1]));
                        for(int i = 0; i < plugin.getFileManager().getPinataConfig().getList("Pinatas." + pinata.getID() + ".Drops").size(); i++) {
                            ItemStack item = (ItemStack) plugin.getFileManager().getPinataConfig().getList("Pinatas." + pinata.getID() + ".Drops").get(i);
                            inv.addItem(item);
                        }
                        ((Player) sender).openInventory(inv);
                        return true;
                    }
                }
                sender.sendMessage(Utils.colorFileMessage("Pinata.Not-Found"));
                return true;
            } else if(args[0].equalsIgnoreCase("setchance")) {
                if(!hasPermission(sender, "pinata.admin.editor")) return true;
                if(!isSenderPlayer(sender)) return true;
                if(args.length == 1) {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Specify-Amount"));
                    return true;
                }
                if(!NumberUtils.isNumber(args[1])) {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Not-Number"));
                    return true;
                }
                Player p = (Player) sender;
                if(p.getItemInHand() == null || p.getItemInHand().getType().equals(Material.AIR)) {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Not-Holding-Anything"));
                    return true;
                }
                ItemStack item = p.getItemInHand();
                ItemMeta meta = item.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(p.getItemInHand().hasItemMeta() && p.getItemInHand().getItemMeta().hasLore()) {
                    lore.addAll(meta.getLore());
                }
                lore.add(args[1]);
                meta.setLore(lore);
                item.setItemMeta(meta);
                if(item.hasItemMeta()) {
                    if(item.getItemMeta().hasLore()) {
                        sender.sendMessage(Utils.colorFileMessage("Pinata.Lore-Set"));
                        p.setItemInHand(item);
                        return true;
                    } else {
                        sender.sendMessage(Utils.colorFileMessage("Pinata.Lore-Set"));
                        p.setItemInHand(item);
                        return true;
                    }
                } else {
                    sender.sendMessage(Utils.colorFileMessage("Pinata.Lore-Set"));
                    p.setItemInHand(item);
                    return true;
                }
            }
        } else {
            sender.sendMessage(Utils.colorFileMessage("Pinata.Command.No-Permission"));
            return true;
        }
        return false;
    }
}
