package pl.plajer.pinata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.pinata.pinataapi.PinataFactory;

public class Commands implements CommandExecutor{

	private HashMap<Sheep, Player> pinatas = new HashMap<Sheep, Player>();
	private HashMap<Sheep, Location> builder = new HashMap<Sheep, Location>();
	private HashMap<Sheep, Entity> leash = new HashMap<Sheep, Entity>();
	private ArrayList<Player> users = new ArrayList<Player>();
	private Main plugin;

	public Commands(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("pinata").setExecutor(this);
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("pinata")){
			if(sender.hasPermission("pinata.command")){
				if(args.length == 0){
					sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Help-Command.Header"));
					sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Help-Command.Description"));
					return true;
				}
				if(args[0].equalsIgnoreCase("list")){
					if(!(sender instanceof Player)){
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Only-Player"));
						return true;
					}
					if(!sender.hasPermission("pinata.command.list")){
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
						return true;
					}
					Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", (Player) sender);
					return true;
				}
				if(args[0].equalsIgnoreCase("preview")){
					if(!(sender instanceof Player)){
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Only-Player"));
						return true;
					}
					if(!sender.hasPermission("pinata.command.preview")){
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
						return true;
					}
					if(args.length == 1){
						sender.sendMessage(Utils.colorRawMessage("Pinata.Specify-Name"));
						return true;
					}
					if(!plugin.getPinataManager().getPinatalist().contains(args[1])) {
						sender.sendMessage(Utils.colorRawMessage("Pinata.Not-Found"));
						return true;
					}
					int rows = 1;
					float trick = plugin.getPinataManager().getPinataDrop().get(args[1]).size() / 9;
					//Using this little "trick" with casting you lose numbers after decimal point
					//and modulo will check if rows are enough to hold drops :>
					if(!((int) trick % 9 == 0)){
						rows++;
					}
					Inventory previewMenu = Bukkit.createInventory(null, rows*9, Utils.colorRawMessage("Menus.Preview-Menu.Inventory-Name"));
					for(int i = 0; i < plugin.getPinataManager().getPinataDrop().get(args[1]).size(); i++){
						String drop = plugin.getPinataManager().getPinataDrop().get(args[1]).get(i);
						final String[] parts = drop.split(";");
						ItemStack item = null;
						ItemMeta meta = null;
						ArrayList<String> lore = new ArrayList<String>();
						String droplore = Utils.colorRawMessage("Menus.Preview-Menu.Drop-Chance");
						switch(parts[0]) {
							case "item":
								item = new ItemStack(Material.getMaterial(parts[1].toUpperCase()), Integer.parseInt(parts[2]));
								meta = item.getItemMeta();
								meta.setDisplayName(Utils.colorMessage(parts[3]));
								lore.add(droplore.replaceAll("%chance%", parts[4]));
								break;
							case "command":
								item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("command-item").toUpperCase()), 1);
								meta = item.getItemMeta();
								meta.setDisplayName(Utils.colorMessage(parts[2]));
								String cmdlore = Utils.colorRawMessage("Menus.Preview-Menu.Command-To-Execute");
								lore.add(cmdlore.replaceAll("%command%", parts[1].replaceAll("%player%", sender.getName())));
								break;
							case "money":
								item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("money-item").toUpperCase()), 1);
								meta = item.getItemMeta();
								meta.setDisplayName(Utils.colorMessage(parts[2]));
								String moneylore = Utils.colorRawMessage("Menus.Preview-Menu.Money-Reward");
								lore.add(moneylore.replaceAll("%money%", parts[1]));
								break;
							case "gun":
								item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("gun-item").toUpperCase()), 1);
								meta = item.getItemMeta();
								meta.setDisplayName(Utils.colorMessage(parts[2]));
								lore.add(droplore.replaceAll("%chance%", parts[3]));
								break;
						}
						meta.setLore(lore);
						item.setItemMeta(meta);
						previewMenu.setItem(i, item);
					}
					((Player) sender).openInventory(previewMenu);
					return true;
				}
				if(args[0].equalsIgnoreCase("buy")){
					if(sender instanceof Player){
						final Player p = (Player) sender;
						if(!plugin.getVaultUse()){
							sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Vault-Not-Detected"));
							return true;
						}
						if(!p.hasPermission("pinata.command.buy")){
							sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
							return true;
						}
						if(!users.isEmpty()){
							if(users.contains(p)) {
								sender.sendMessage(Utils.colorRawMessage("Pinata.Create.Already-Created"));
								return true;
							}
						}
						if(args.length == 1){
							Utils.createPinatasGUI("Menus.List-Menu.Inventory-Name", p);
							return true;
						}
						if(!plugin.getPinataManager().getPinatalist().contains(args[1])) {
							sender.sendMessage(Utils.colorRawMessage("Pinata.Not-Found"));
							return true;
						}
						final String pname = args[1];
						if(plugin.getFileManager().getPinataConfig().getInt("pinatas." + pname + ".cost") == -1){
							p.sendMessage(Utils.colorRawMessage("Pinata.Selling.Not-For-Sale"));
							return true;
						}
						if(plugin.getConfig().getBoolean("using-permissions")){
							final String pperm = plugin.getFileManager().getPinataConfig().get("pinatas." + pname + ".permission").toString();
							if(!p.hasPermission(pperm)){
								p.sendMessage(Utils.colorRawMessage("Pinata.Create.No-Permission"));
								return true;
							}
						}
						if(p.hasPermission("pinata.admin.freeall")){
							Location loc = p.getLocation().add(0, 7, 0);
							Sheep sheep = p.getWorld().spawn(p.getLocation().add(0, 2, 0), Sheep.class);
							PinataFactory.createPinata(loc, p, sheep, pname);
							return true;
						} else if(plugin.getEco().getBalance(Bukkit.getOfflinePlayer(p.getUniqueId())) >= plugin.getFileManager().getPinataConfig().getInt("pinatas." + pname + ".cost")){
							Location loc = p.getLocation().add(0, 7, 0);
							Sheep sheep = p.getWorld().spawn(p.getLocation().add(0, 2, 0), Sheep.class);
							if(PinataFactory.createPinata(loc, p, sheep, pname)){
								//Pinata created successfully, now we can withdraw $ from player.
								plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), plugin.getFileManager().getPinataConfig().getInt("pinatas." + pname + ".cost"));
							}
							return true;
						} else{
							sender.sendMessage(Utils.colorRawMessage("Pinata.Selling.Cannot-Afford"));
							return true;
						}
					} else{
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Only-Player"));
						return true;
					}
				}
				if(args[0].equalsIgnoreCase("reloadconfig")){
					if(sender.hasPermission("pinata.admin.reload")){
						try {
							plugin.reloadConfig();
							plugin.getPinataManager().getPinatalist().clear();
							plugin.saveDefaultConfig();
							plugin.getFileManager().saveDefaultMessagesConfig();
							plugin.getFileManager().saveDefaultPinataConfig();
							plugin.reloadConfig();
							plugin.getFileManager().reloadMessagesConfig();
							plugin.getFileManager().reloadPinataConfig();
							plugin.getPinataManager().loadPinatas();
						} catch (Exception e) {
							sender.sendMessage(Utils.colorRawMessage("Pinata.Config.Reload-Fail"));
							return true;
						}
						String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
						if (plugin.getConfig().getBoolean("update-notify")){
							try {
								UpdateChecker.checkUpdate(currentVersion);
								String latestVersion = UpdateChecker.getLatestVersion();
								if (latestVersion != null) {
									latestVersion = "v" + latestVersion;
									sender.sendMessage(Utils.colorRawMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
								}
							} catch (Exception ex) {
								sender.sendMessage(Utils.colorRawMessage("Other.Plugin-Update-Check-Failed").replaceAll("%error%", ex.getMessage()));
							}
						}
						sender.sendMessage(Utils.colorRawMessage("Pinata.Config.Reload-Success"));
						return true;
					} else{
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
						return true;
					}
				}
				if(args[0].equalsIgnoreCase("create")){
					if(sender.hasPermission("pinata.admin.create")){
						if(args.length == 1){
							sender.sendMessage(Utils.colorRawMessage("Pinata.Specify-Name"));
							return true;
						}
						Player user = null;
						if(sender instanceof ConsoleCommandSender){
							if(!(args.length == 3)){
								sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Console-Specify-Player"));
								return true;
							}
							if(Bukkit.getPlayer(args[2]) != null){
								user = Bukkit.getPlayer(args[2]);
								user.sendMessage(Utils.colorRawMessage("Pinata.Create.By-Console"));
							} else {
								sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Player-Not-Found"));
								return true;
							}
						} else{
							if(!sender.hasPermission("pinata.admin.create.others")){
								sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
								return true;
							}
							if(!(args.length == 3)){
								user = (Player) sender;
							} else{
								if(Bukkit.getPlayer(args[2]) != null){
									user = Bukkit.getPlayer(args[2]);
								} else {
									sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Player-Not-Found"));
									return true;
								}
							}
						}
						if(!plugin.getPinataManager().getPinatalist().contains(args[1])) {
							sender.sendMessage(Utils.colorRawMessage("Pinata.Not-Found"));
							return true;
						}
						Location loc = user.getLocation().add(0, 7, 0);
						Sheep sheep = user.getWorld().spawn(user.getLocation().add(0, 2, 0), Sheep.class);
						PinataFactory.createPinata(loc, user, sheep, args[1]);
						return true;
					} else{
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
						return true;
					}
				}
				if(args[0].equalsIgnoreCase("setcrate")){
					if(!sender.hasPermission("pinata.admin.crate.set")){
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
						return true;
					}
					if(!(args.length == 2)){
						sender.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.Specify-Name"));
						return true;
					}
					if(!(sender instanceof Player)){
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.Only-Player"));
						return true;
					}
					Player p = (Player) sender;
					Block l = p.getTargetBlock((Set<Material>) null, 20);
					if(l.getType().equals(Material.CHEST)){
						ConfigurationSection pinata = plugin.getFileManager().getCratesConfig().getConfigurationSection("crates");
						if(pinata != null) {
							for(String key : pinata.getKeys(false)) {
								World world = Bukkit.getWorld(plugin.getFileManager().getCratesConfig().get("crates." + key + ".world").toString());
								FileConfiguration config = plugin.getFileManager().getCratesConfig();
								if(l.getLocation().equals(new Location(world, config.getInt("crates." + key + ".x"), config.getInt("crates." + key + ".y"), config.getInt("crates." + key + ".z")))){
									p.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.Is-Set-Here"));
									return true;
								}
							}
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".world", l.getWorld().getName());
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".x", l.getLocation().getX());
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".y", l.getLocation().getY());
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".z", l.getLocation().getZ());
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".name", args[1]);
							plugin.getFileManager().saveCratesConfig();
							String string = Utils.colorRawMessage("Pinata.Crate-Creation.Create-Success");
							p.sendMessage(string.replaceAll("%name%", args[1]));
							return true;
						} else{
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".world", l.getWorld().getName());
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".x", l.getLocation().getX());
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".y", l.getLocation().getY());
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".z", l.getLocation().getZ());
							plugin.getFileManager().getCratesConfig().set("crates." + args[1] + ".name", args[2]);
							plugin.getFileManager().saveCratesConfig();
							String string = Utils.colorRawMessage("Pinata.Crate-Creation.Create-Success");
							p.sendMessage(string.replaceAll("%name%", args[1]));
							return true;
						}
					} else{
						p.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.Target-Block-Not-Chest"));
						return true;
					}
				}
				if(args[0].equalsIgnoreCase("cratelist")){
					if(!sender.hasPermission("pinata.admin.crate.list")){
						sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
						return true;
					}
					ConfigurationSection pinata = plugin.getFileManager().getCratesConfig().getConfigurationSection("crates");
					if(pinata != null) {
						int num = 0;
						sender.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.List"));
						for(String key : pinata.getKeys(false)) {
							sender.sendMessage("§a" + key + " - X: " + plugin.getFileManager().getCratesConfig().get("crates." + key + ".x") + " Y: " + plugin.getFileManager().getCratesConfig().get("crates." + key + ".y") + " Z: "  + plugin.getFileManager().getCratesConfig().get("crates." + key + ".z"));
							num++;
						}
						if(num == 0){
							sender.sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.List-Empty"));
						}
					}
					return true;
				}
				/*if(args[0].equalsIgnoreCase("guicreator")){
					if(!(args.length == 2)){
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Pinata.Specify-Name").toString()));
						return true;
					}
					ConfigurationSection pinata = plugin.getFileManager().getPinataConfig().getConfigurationSection("pinatas");
					for(String key : pinata.getKeys(false)) {
				    	if(key.equals(args[1])){
				    		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Pinata.Creator.Already-Exists").toString()));
							return true;
				    	}
					}
					plugin.getFileManager().getPinataConfig().set("pinatas." + args[1] + ".Timer", 5);
					plugin.getFileManager().getPinataConfig().set("pinatas." + args[1] + ".Permission", "pinata.use." + args[1]);
					plugin.getFileManager().getPinataConfig().set("pinatas." + args[1] + ".Color", "white");
					plugin.getFileManager().getPinataConfig().set("pinatas." + args[1] + ".Cost", "-1");
					plugin.getFileManager().getPinataConfig().set("pinatas." + args[1] + ".Type", "private");
					plugin.getFileManager().getPinataConfig().set("pinatas." + args[1] + ".Crate-Display-Time", 10);
					plugin.getFileManager().getPinataConfig().set("pinatas." + args[1] + ".Drops", null);
					String string = ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Pinata.Crate-Creation.Create-Success").toString());
				    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', string.replaceAll("%name%", args[1])));
				    Inventory pinatasMenu = Bukkit.createInventory(null, 3, ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Menus.Creator-Menu.Inventory-Name").toString()));
					ItemStack item = new ItemStack()
				    for(int i = 0; i < pinatas.size(); i++){
						//String pinata = pinatas.get(i);
						ItemStack item = new ItemStack(Material.WOOL, 1,  DyeColor.valueOf(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".color").toString().toUpperCase()).getDyeData());
						ItemStack item = new ItemStack(Material.NAME_TAG, 1);
						ItemMeta meta = item.getItemMeta();
						meta.setDisplayName("§6" + pinata);
						ArrayList<String> lore = new ArrayList<String>();
						if(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".type").equals("private")){
							lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Menus.List-Menu.Pinata-Types.Type-Private").toString()));
						} else{
							lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Menus.List-Menu.Pinata-Types.Type-Public").toString()));
						}
						if(Integer.parseInt(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".cost").toString()) == -1){
							lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Menus.List-Menu.Pinata-Cost-Not-For-Sale").toString()));
						} else{
							String cost = ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Menus.List-Menu.Pinata-Cost").toString());
							lore.add(cost.replaceAll("%money%", plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".cost").toString()) + "$");
							lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Menus.List-Menu.Click-Selection.Right-Click").toString()));
						}
						lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().get("Menus.List-Menu.Click-Selection.Left-Click").toString()));
						meta.setLore(lore);
			        	item.setItemMeta(meta);
						pinatasMenu.setItem(i, item);
					}
					((Player) sender).openInventory(pinatasMenu);
					return true;
				}*/
			} else{
				sender.sendMessage(Utils.colorRawMessage("Pinata.Command.No-Permission"));
				return true;
			}
		}
		return false;
	}

	public HashMap<Sheep, Player> getPinatas() {
		return pinatas;
	}

	public HashMap<Sheep, Location> getBuilder() {
		return builder;
	}

	public HashMap<Sheep, Entity> getLeash() {
		return leash;
	}

	public List<Player> getUsers() {
		return users;
	}
}
