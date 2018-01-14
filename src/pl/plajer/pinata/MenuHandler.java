package pl.plajer.pinata;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.pinata.pinataapi.PinataFactory;

public class MenuHandler implements Listener {

	private Main plugin;

	public MenuHandler(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onMenuInteract(final InventoryClickEvent e){
		if(e.getInventory().getName().equals(Utils.colorRawMessage("Menus.Preview-Menu.Inventory-Name"))){
			e.setCancelled(true);
			return;
		}
		if(e.getCurrentItem() == null || !e.getCurrentItem().getType().equals(Material.WOOL)){
			return;
		}
		final ItemMeta item = e.getCurrentItem().getItemMeta();
		final String pinata = item.getDisplayName().replaceAll(Utils.colorMessage("&6"), "");
		if(e.getInventory().getName().equals(Utils.colorRawMessage("Menus.List-Menu.Inventory-Name"))){
			e.setCancelled(true);
			if(e.getClick() == ClickType.LEFT){
				e.getWhoClicked().closeInventory();
				//Scheduler to prevent bugged GUI
				Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(e.getWhoClicked(), "pinata preview " + pinata), 1);
			}
			if(e.getClick() == ClickType.RIGHT){
				if(item.getLore().get(1).equals(Utils.colorRawMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"))){
					e.getWhoClicked().closeInventory();
					e.getWhoClicked().sendMessage(Utils.colorRawMessage("Pinata.Selling.Not-For-Sale"));
					return;
				}
				e.getWhoClicked().closeInventory();
				Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(e.getWhoClicked(), Utils.colorMessage("pinata buy " + item.getDisplayName().replaceAll("&6", ""))), 1);
			}
		}
		if(e.getInventory().getName().equals(Utils.colorRawMessage("Menus.Crate-Menu.Inventory-Name"))){
			e.setCancelled(true);
			if(e.getClick() == ClickType.LEFT){
				e.getWhoClicked().closeInventory();
				//Scheduler to prevent bugged GUI
				Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(e.getWhoClicked(), "pinata preview " + pinata), 1);
			}
			if(e.getClick() == ClickType.RIGHT){
				if(item.getLore().get(1).equals(Utils.colorRawMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"))){
					e.getWhoClicked().closeInventory();
					e.getWhoClicked().sendMessage(Utils.colorRawMessage("Pinata.Selling.Not-For-Sale"));
					return;
				}
				if(plugin.getCrateManager().getCrateUsage().containsKey(e.getWhoClicked())){
					Location chest = plugin.getCrateManager().getCrateUsage().get(e.getWhoClicked());
					if(plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".cost") == -1){
						e.getWhoClicked().sendMessage(Utils.colorRawMessage("Pinata.Selling.Not-For-Sale"));
						e.getWhoClicked().closeInventory();
						return;
					}
					if(plugin.getConfig().getBoolean("using-permissions")){
						final String pperm = plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".permission").toString();
						if(!e.getWhoClicked().hasPermission(pperm)){
							e.getWhoClicked().sendMessage(Utils.colorRawMessage("Pinata.Create.No-Permission"));
							e.getWhoClicked().closeInventory();
							return;
						}
					}
					if(e.getWhoClicked().hasPermission("pinata.admin.freeall")){
						Location loc = chest.clone().add(0, 7, 0);
						Location entityLoc = chest.clone().add(0, 2, 0);
						final LivingEntity entity = (LivingEntity) chest.getWorld().spawnEntity(entityLoc, EntityType.valueOf(plugin.getFileManager().getPinataConfig().getString("pinatas." + pinata + ".mob-type").toUpperCase()));
						entity.setMaxHealth(plugin.getFileManager().getPinataConfig().getDouble("pinatas." + pinata + ".health"));
						entity.setHealth(entity.getMaxHealth());
						if(PinataFactory.createPinata(loc, (Player) e.getWhoClicked(), entity, pinata)){
							Bukkit.getScheduler().runTaskLater(plugin, () -> {
								if(!(entity.isDead())){
									entity.damage(entity.getMaxHealth());
								}
							}, plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".crate-time") * 20);
						}
					} else if(plugin.getEco().getBalance(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId())) >= plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".cost")){
						Location loc = chest.clone().add(0, 7, 0);
						Location entityLoc = chest.clone().add(0, 2, 0);
						final LivingEntity entity = (LivingEntity) chest.getWorld().spawnEntity(entityLoc, EntityType.valueOf(plugin.getFileManager().getPinataConfig().getString("pinatas." + pinata + ".mob-type").toUpperCase()));
						entity.setMaxHealth(plugin.getFileManager().getPinataConfig().getDouble("pinatas." + pinata + ".health"));
						entity.setHealth(entity.getMaxHealth());
						if(PinataFactory.createPinata(loc, (Player) e.getWhoClicked(), entity, pinata)){
							//Pinata created successfully, now we can withdraw $ from player.
							plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId()), plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".cost"));
							Bukkit.getScheduler().runTaskLater(plugin, () -> {
								if(!(entity.isDead())){
									entity.damage(entity.getMaxHealth());
								}
							}, plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".crate-time") * 20);
						}
					} else{
						e.getWhoClicked().sendMessage(Utils.colorRawMessage("Pinata.Selling.Cannot-Afford"));
					}
				} else{
					e.getWhoClicked().sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.Buy-Error"));
				}
				e.getWhoClicked().closeInventory();
			}
		}
	}

}
