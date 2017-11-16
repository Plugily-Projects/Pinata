package pl.plajer.pinata;

/*
 * TODO
 * Pinata creation for each crate?
 */

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class CrateManager implements Listener {
	
	private HashMap<Player, Location> crateuse = new HashMap<Player, Location>();
	private Main plugin;
	
	public CrateManager(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	/**
	 * Holograms at crates locations
	 */
 	public void hologramScheduler(){
 		Bukkit.getScheduler().runTaskTimer(plugin, new Runnable(){
 			@Override
			public void run(){
 				for(String holo : plugin.getFileManager().getCratesConfig().getConfigurationSection("crates").getKeys(false)){
 		 			FileConfiguration config = plugin.getFileManager().getCratesConfig();
 		 			Location loc = new Location(Bukkit.getWorld(config.getString("crates." + holo + ".world")), config.getDouble("crates." + holo + ".x") + 0.5, config.getDouble("crates." + holo + ".y"), config.getDouble("crates." + holo + ".z") + 0.5);
 		 			final Hologram hologram = HologramsAPI.createHologram(plugin, loc.add(0, 1.5, 0));
 		 			String holoname = Utils.colorRawMessage("Hologram.Crate-Hologram");
 		 			hologram.appendTextLine(holoname.replaceAll("%name%", plugin.getFileManager().getCratesConfig().getString("crates." + holo + ".name")));
 					new BukkitRunnable() {
 						int ticksRun;
 						@Override
 						public void run() {
 							ticksRun++;
 							if (ticksRun > plugin.getConfig().getDouble("hologram-refresh") * 20) {
 								hologram.delete();
 								cancel();
 							}
 						}
 					}.runTaskTimer(plugin, 1, 1);
 		 		}
 			}
 		}, (long) plugin.getConfig().getDouble("hologram-refresh") * 20, (long) plugin.getConfig().getDouble("hologram-refresh") * 20);
	}
 	
 	/**
 	 * Particles at crates locations
 	 */
 	public void particleScheduler(){
 		Bukkit.getScheduler().runTaskTimer(plugin, new Runnable(){
 			@Override
			public void run(){
 				for(String holo : plugin.getFileManager().getCratesConfig().getConfigurationSection("crates").getKeys(false)){
 		 			FileConfiguration config = plugin.getFileManager().getCratesConfig();
 		 			Location loc = new Location(Bukkit.getWorld(config.getString("crates." + holo + ".world")), config.getDouble("crates." + holo + ".x") + 0.5, config.getDouble("crates." + holo + ".y"), config.getDouble("crates." + holo + ".z") + 0.5);
 		 			loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
 				}
 			}
 		}, (long) plugin.getConfig().getDouble("particle-refresh") * 20, (long) plugin.getConfig().getDouble("particle-refresh") * 20);
	}
 	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onCrateClick(PlayerInteractEvent e){
		if(!(e.getClickedBlock() == null) && !(e.getClickedBlock().getType() == null) && e.getClickedBlock().getType().equals(Material.CHEST) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			ConfigurationSection csp = plugin.getFileManager().getCratesConfig().getConfigurationSection("crates");
			if (csp != null) {
			    for(String key : csp.getKeys(false)) {
			    	FileConfiguration config = plugin.getFileManager().getCratesConfig();
			    	if(e.getClickedBlock().getLocation().equals(new Location(Bukkit.getWorld(config.getString("crates." + key + ".world")), config.getDouble("crates." + key + ".x"), config.getDouble("crates." + key + ".y"), config.getDouble("crates." + key + ".z")))){
			    		e.setCancelled(true);
						if(!plugin.getVaultUse()){
							e.getPlayer().sendMessage(Utils.colorRawMessage("Pinata.Command.Vault-Not-Detected"));
			    			return;
						}
			    		if(!e.getPlayer().hasPermission("pinata.player.crate")){
			    			e.getPlayer().sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.No-Permission"));
			    			return;
			    		}
						int rows = 1;
						float trick = plugin.getPinataManager().getPinatalist().size() / 9;
						if(!((int) trick % 9 == 0)){
							rows++;
						}
						crateuse.put(e.getPlayer(), e.getClickedBlock().getLocation());
						Inventory pinatasMenu = Bukkit.createInventory(null, rows*9, Utils.colorRawMessage("Menus.Crate-Menu.Inventory-Name"));
						for(int i = 0; i < plugin.getPinataManager().getPinatalist().size(); i++){
							String pinata = plugin.getPinataManager().getPinatalist().get(i).toString();
							ItemStack item = new ItemStack(Material.WOOL, 1, DyeColor.valueOf(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".color").toString().toUpperCase()).getDyeData());
							ItemMeta meta = item.getItemMeta();
							meta.setDisplayName("§6" + pinata);
							ArrayList<String> lore = new ArrayList<String>();
							if(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".type").equals("private")){
								lore.add(Utils.colorRawMessage("Menus.List-Menu.Pinata-Types.Type-Private"));
							} else{
								lore.add(Utils.colorRawMessage("Menus.List-Menu.Pinata-Types.Type-Public"));
							}
							if(Integer.parseInt(plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".cost").toString()) == -1){
								lore.add(Utils.colorRawMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"));
							} else{
								String cost = Utils.colorRawMessage("Menus.List-Menu.Pinata-Cost");
								lore.add(cost.replaceAll("%money%", plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".cost").toString()) + "$");
								lore.add(Utils.colorRawMessage("Menus.List-Menu.Click-Selection.Right-Click"));
							}
							lore.add(Utils.colorRawMessage("Menus.List-Menu.Click-Selection.Left-Click"));
							meta.setLore(lore);
				        	item.setItemMeta(meta);
							pinatasMenu.setItem(i, item);
						}
						e.getPlayer().openInventory(pinatasMenu);
						return;
			    	}
			    }
			}
		}
	}
	
	@EventHandler
	public void onCrateDestroy(BlockBreakEvent e){
		if(e.getBlock().getType().equals(Material.CHEST)){
			ConfigurationSection pinata = plugin.getFileManager().getCratesConfig().getConfigurationSection("crates");
			if (pinata != null) {
			    for(String key : pinata.getKeys(false)) {
			    	FileConfiguration config = plugin.getFileManager().getCratesConfig();
			    	if(e.getBlock().getLocation().equals(new Location(e.getPlayer().getWorld(), config.getInt("crates." + key + ".x"), config.getInt("crates." + key + ".y"), config.getInt("crates." + key + ".z")))){
			    		if(!e.getPlayer().hasPermission("pinata.admin.crate.destroy")){
							e.getPlayer().sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.No-Permission"));
							e.setCancelled(true);
						}
			    		plugin.getFileManager().getCratesConfig().set("crates." + key, null);
					    plugin.getFileManager().saveCratesConfig();
					    String message = Utils.colorRawMessage("Pinata.Crate-Creation.Destroyed");
			    		e.getPlayer().sendMessage(message.replaceAll("%name%", key));
			    	}
			    }
			}
		}
	}

	public HashMap<Player, Location> getCrateuse() {
		return crateuse;
	}

}
