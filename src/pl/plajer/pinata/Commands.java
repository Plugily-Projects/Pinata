package pl.plajer.pinata;

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
import pl.plajer.pinata.pinataapi.PinataFactory;
import pl.plajer.pinata.utils.UpdateChecker;
import pl.plajer.pinata.utils.Utils;

import java.util.*;

public class Commands implements CommandExecutor {

    private Map<Entity, PinataData> pinata = new HashMap<>();
    private List<Player> users = new ArrayList<>();
    private Main plugin;

    Commands(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("pinata").setExecutor(this);
    }

    private boolean isSenderPlayer(CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Only-Player"));
            return false;
        }
        return true;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if(!sender.hasPermission(permission)) {
            sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("pinata")) {
            if(!hasPermission(sender, "pinata.command")) return true;
            if(args.length == 0) {
                sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Help-Command.Header"));
                sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Help-Command.Description"));
                return true;
            }
            if(args[0].equalsIgnoreCase("list")) {
                if(!isSenderPlayer(sender)) return true;
                if(!hasPermission(sender, "pinata.command.list")) return true;
                Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", (Player) sender);
                return true;
            }
            if(args[0].equalsIgnoreCase("preview")) {
                if(!isSenderPlayer(sender)) return true;
                if(!hasPermission(sender, "pinata.command.preview")) return true;
                if(args.length == 1) {
                    sender.sendMessage(Utils.colorRawMessage("Pinata.Specify-Name"));
                    return true;
                }
                if(!plugin.getPinataManager().getPinataList().contains(args[1])) {
                    sender.sendMessage(Utils.colorRawMessage("Pinata.Not-Found"));
                    return true;
                }
                int rows = Utils.serializeInt(plugin.getPinataManager().getPinataDrop().get(args[1]).size());
                Inventory previewMenu = Bukkit.createInventory(null, rows, Utils.colorRawMessage("Menus.Preview-Menu.Inventory-Name"));
                int i = 0;
                for(PinataItem item : plugin.getPinataManager().getPinataDrop().get(args[1])){
                    ItemStack stack = new ItemStack(item.getRepresentedMaterial(), item.getAmount());
                    ItemMeta meta = stack.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    String dropLore = Utils.colorRawMessage("Menus.Preview-Menu.Drop-Chance").replaceAll("%chance%", String.valueOf(item.getDropChance()));
                    switch(item.getItemType()){
                        case ITEM:
                            meta.setDisplayName(item.getItem().getItemMeta().getDisplayName());
                            if(item.getItem().getItemMeta().hasLore()) {
                                lore.addAll(item.getItem().getItemMeta().getLore());
                            }
                            break;
                        case COMMAND:
                            meta.setDisplayName(item.getHologramName());
                            lore.add(Utils.colorRawMessage("Menus.Preview-Menu.Command-To-Execute").replaceAll("%command%", item.getCommand().replaceAll("%player%", sender.getName())));
                            break;
                        case GUN:
                            meta.setDisplayName(item.getHologramName());
                            break;
                        case MONEY:
                            meta.setDisplayName(item.getHologramName());
                            lore.add(Utils.colorRawMessage("Menus.Preview-Menu.Money-Reward").replaceAll("%money%", String.valueOf(item.getMoneyValue())));
                            break;
                    }
                    lore.add(dropLore);
                    meta.setLore(lore);
                    stack.setItemMeta(meta);
                    previewMenu.setItem(i, stack);
                    i++;
                }
                ((Player) sender).openInventory(previewMenu);
                return true;
            }
            if(args[0].equalsIgnoreCase("buy")) {
                if(!isSenderPlayer(sender)) return true;
                final Player p = (Player) sender;
                if(!plugin.isPluginEnabled("Vault")) {
                    p.sendMessage(Utils.colorRawMessage("Pinata.Command.Vault-Not-Detected"));
                    return true;
                }
                if(!hasPermission(sender, "pinata.command.buy")) return true;
                if(!users.isEmpty()) {
                    if(users.contains(p)) {
                        p.sendMessage(Utils.colorRawMessage("Pinata.Create.Already-Created"));
                        return true;
                    }
                }
                if(plugin.getDisabledWorlds().contains(p.getWorld().getName())) {
                    p.sendMessage(Utils.colorRawMessage("Pinata.Create.Disabled-World"));
                    return true;
                }
                if(args.length == 1) {
                    Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", p);
                    return true;
                }
                if(!plugin.getPinataManager().getPinataList().contains(args[1])) {
                    p.sendMessage(Utils.colorRawMessage("Pinata.Not-Found"));
                    return true;
                }
                if(plugin.getFileManager().getPinataConfig().getInt("pinatas." + args[1] + ".cost") == -1) {
                    p.sendMessage(Utils.colorRawMessage("Pinata.Selling.Not-For-Sale"));
                    return true;
                }
                if(plugin.getConfig().getBoolean("using-permissions")) {
                    final String pinataPermission = plugin.getFileManager().getPinataConfig().get("pinatas." + args[1] + ".permission").toString();
                    if(!p.hasPermission(pinataPermission)) {
                        p.sendMessage(Utils.colorRawMessage("Pinata.Create.No-Permission"));
                        return true;
                    }
                }
                if(p.hasPermission("pinata.admin.freeall")) {
                    Utils.createPinataAtPlayer(p, p.getLocation(), args[1]);
                    return true;
                } else if(plugin.getEco().getBalance(Bukkit.getOfflinePlayer(p.getUniqueId())) >= plugin.getFileManager().getPinataConfig().getDouble("pinatas." + args[1] + ".cost")) {
                    if(Utils.createPinataAtPlayer(p, p.getLocation(), args[1])) {
                        plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), plugin.getFileManager().getPinataConfig().getDouble("pinatas." + args[1] + ".cost"));
                    }
                    return true;
                } else {
                    sender.sendMessage(Utils.colorRawMessage("Pinata.Selling.Cannot-Afford"));
                    return true;
                }
            }
            if(args[0].equalsIgnoreCase("reloadconfig")) {
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
                    plugin.getPinataManager().loadPinatas();
                    plugin.setupLocale();
                } catch(Exception e) {
                    sender.sendMessage(Utils.colorRawMessage("Pinata.Config.Reload-Fail"));
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
                            sender.sendMessage(Utils.colorRawMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
                        }
                    } catch(Exception ex) {
                        sender.sendMessage(Utils.colorRawMessage("Other.Plugin-Update-Check-Failed").replaceAll("%error%", ex.getMessage()));
                    }
                }
                sender.sendMessage(Utils.colorRawMessage("Pinata.Config.Reload-Success"));
                return true;
            }
            if(args[0].equalsIgnoreCase("create")) {
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
                        if(!plugin.getPinataManager().getPinataList().contains(args[5])) {
                            sender.sendMessage(Utils.colorRawMessage("Pinata.Not-Found"));
                            return true;
                        }
                        Location l = new Location(world, x, y, z);
                        LivingEntity entity = (LivingEntity) l.getWorld().spawnEntity(l.clone().add(0, 2, 0), EntityType.valueOf(Main.getInstance().getFileManager().getPinataConfig().getString("pinatas." + args[5] + ".mob-type").toUpperCase()));
                        entity.setMaxHealth(Main.getInstance().getFileManager().getPinataConfig().getDouble("pinatas." + args[5] + ".health"));
                        entity.setHealth(entity.getMaxHealth());
                        PinataFactory.createPinata(l.clone().add(0, 7, 0), entity, args[5]);
                        sender.sendMessage(Utils.colorRawMessage("Pinata.Create.Success").replaceAll("%name%", args[5]));
                        return true;
                    } catch(Exception e) {
                        sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Custom-Location-Create-Error"));
                        return true;
                    }
                }
                if(args.length == 1) {
                    sender.sendMessage(Utils.colorRawMessage("Pinata.Specify-Name"));
                    return true;
                }
                Player user;
                if(sender instanceof ConsoleCommandSender) {
                    if(args.length != 3) {
                        sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Console-Specify-Player"));
                        return true;
                    }
                    if(Bukkit.getPlayer(args[2]) != null) {
                        user = Bukkit.getPlayer(args[2]);
                        user.sendMessage(Utils.colorRawMessage("Pinata.Create.By-Console"));
                    } else {
                        sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Player-Not-Found"));
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
                            sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Player-Not-Found"));
                            return true;
                        }
                    }
                }
                if(!plugin.getPinataManager().getPinataList().contains(args[1])) {
                    sender.sendMessage(Utils.colorRawMessage("Pinata.Not-Found"));
                    return true;
                }
                Utils.createPinataAtPlayer(user, user.getLocation(), args[1]);
                return true;
            }
            if(args[0].equalsIgnoreCase("setcrate")) {
                if(!hasPermission(sender, "pinata.admin.crate.set")) return true;
                if(!isSenderPlayer(sender)) return true;
                if(args.length != 2) {
                    sender.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.Specify-Name"));
                    return true;
                }
                Player p = (Player) sender;
                Block l = p.getTargetBlock(null, 20);
                if(l.getType().equals(Material.CHEST)) {
                    if(plugin.getCrateManager().getCratesLocations().containsKey(l.getLocation())) {
                        p.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.Is-Set-Here"));
                        return true;
                    }
                    if(plugin.getFileManager().getCratesConfig().isSet("crates." + args[1])) {
                        p.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.Already-Exists"));
                        return true;
                    }
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".world", l.getWorld().getName());
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".x", l.getX());
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".y", l.getY());
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".z", l.getZ());
                    plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".name", args[1]);
                    plugin.getFileManager().saveCratesConfig();
                    plugin.getCrateManager().getCratesLocations().put(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ()), args[1]);
                    p.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.Create-Success").replaceAll("%name%", args[1]));
                    return true;
                } else {
                    p.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.Target-Block-Not-Chest"));
                    return true;
                }
            }
            if(args[0].equalsIgnoreCase("cratelist")) {
                if(!hasPermission(sender, "pinata.admin.crate.list")) return true;
                int num = 0;
                sender.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.List"));
                for(Location l : plugin.getCrateManager().getCratesLocations().keySet()) {
                    sender.sendMessage(Utils.colorMessage("&a" + plugin.getCrateManager().getCratesLocations().get(l) + " - X: " + l.getX() + " Y: " + l.getY() + " Z: " + l.getZ()));
                    num++;
                }
                if(num == 0) {
                    sender.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.List-Empty"));
                }
                return true;
            }
        } else {
            sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
            return true;
        }
        return false;
    }

    public Map<Entity, PinataData> getPinata() {
        return pinata;
    }

    public List<Player> getUsers() {
        return users;
    }

    public void setUsers(List<Player> users) {
        this.users = users;
    }
}
