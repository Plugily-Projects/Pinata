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
import pl.plajer.pinata.utils.Utils;

class MenuHandler implements Listener {

    private Main plugin;

    public MenuHandler(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuInteract(final InventoryClickEvent e) {
        if(e.getInventory().getName().equals(Utils.colorMessage("Menus.Preview-Menu.Inventory-Name"))) {
            e.setCancelled(true);
            return;
        }
        if(e.getCurrentItem() == null || !e.getCurrentItem().getType().equals(Material.WOOL)) {
            return;
        }
        final ItemMeta item = e.getCurrentItem().getItemMeta();
        final String pinata = item.getDisplayName().replaceAll(Utils.colorRawMessage("&6"), "");
        if(e.getInventory().getName().equals(Utils.colorMessage("Menus.List-Menu.Inventory-Name"))) {
            e.setCancelled(true);
            if(e.getClick() == ClickType.LEFT) {
                e.getWhoClicked().closeInventory();
                //Scheduler to prevent bugged GUI
                Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(e.getWhoClicked(), "pinata preview " + pinata), 1);
            }
            if(e.getClick() == ClickType.RIGHT) {
                if(item.getLore().get(1).equals(Utils.colorMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"))) {
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Selling.Not-For-Sale"));
                    return;
                }
                e.getWhoClicked().closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(e.getWhoClicked(), Utils.colorRawMessage("pinata buy " + item.getDisplayName().replaceAll("&6", ""))), 1);
            }
        }
        if(e.getInventory().getName().equals(Utils.colorMessage("Menus.Crate-Menu.Inventory-Name")) || e.getInventory().getName().equals(Utils.colorMessage("Signs.Inventory-Name"))) {
            e.setCancelled(true);
            if(e.getClick() == ClickType.LEFT) {
                e.getWhoClicked().closeInventory();
                //Scheduler to prevent bugged GUI
                Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(e.getWhoClicked(), "pinata preview " + pinata), 1);
            }
            if(e.getClick() == ClickType.RIGHT) {
                if(item.getLore().get(1).equals(Utils.colorMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"))) {
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Selling.Not-For-Sale"));
                    return;
                }
                Location builderLocation;
                Location entityLocation;
                if(e.getInventory().getName().equals(Utils.colorMessage("Menus.Crate-Menu.Inventory-Name"))) {
                    if(plugin.getCrateManager().getCrateUsage().containsKey(e.getWhoClicked())) {
                        builderLocation = plugin.getCrateManager().getCrateUsage().get(e.getWhoClicked()).clone().add(0, 8, 0);
                        entityLocation = plugin.getCrateManager().getCrateUsage().get(e.getWhoClicked()).clone().add(0, 3, 0);
                    } else {
                        e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Buy-Error"));
                        return;
                    }
                //sign inventory
                } else {
                    if(plugin.getSignManager().getSignUsage().containsKey(e.getWhoClicked())) {
                        builderLocation = plugin.getSignManager().getSignUsage().get(e.getWhoClicked()).clone().add(0, 8, 0);
                        entityLocation = plugin.getSignManager().getSignUsage().get(e.getWhoClicked()).clone().add(0, 3, 0);
                    } else {
                        e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Buy-Error"));
                        return;
                    }
                }
                if(ConfigurationManager.getConfig("pinatas").getInt("pinatas." + pinata + ".cost") == -1) {
                    e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Selling.Not-For-Sale"));
                    e.getWhoClicked().closeInventory();
                    return;
                }
                if(!plugin.getCommands().getUsers().isEmpty()) {
                    if(plugin.getCommands().getUsers().contains(e.getWhoClicked())) {
                        e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Create.Already-Created"));
                        e.getWhoClicked().closeInventory();
                        return;
                    }
                }
                if(plugin.getConfig().getBoolean("using-permissions")) {
                    final String pperm = ConfigurationManager.getConfig("pinatas").get("pinatas." + pinata + ".permission").toString();
                    if(!e.getWhoClicked().hasPermission(pperm)) {
                        e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Create.No-Permission"));
                        e.getWhoClicked().closeInventory();
                        return;
                    }
                }
                if(e.getWhoClicked().hasPermission("pinata.admin.freeall")) {
                    LivingEntity entity = (LivingEntity) entityLocation.getWorld().spawnEntity(entityLocation, EntityType.valueOf(ConfigurationManager.getConfig("pinatas").getString("pinatas." + pinata + ".mob-type").toUpperCase()));
                    entity.setMaxHealth(ConfigurationManager.getConfig("pinatas").getDouble("pinatas." + pinata + ".health"));
                    entity.setHealth(entity.getMaxHealth());
                    plugin.getCommands().getUsers().add((Player) e.getWhoClicked());
                    if(PinataFactory.createPinata(builderLocation, (Player) e.getWhoClicked(), entity, pinata)) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if(!(entity.isDead())) {
                                entity.damage(entity.getMaxHealth());
                            }
                        }, ConfigurationManager.getConfig("pinatas").getInt("pinatas." + pinata + ".crate-time") * 20);
                    }
                } else if(plugin.getEco().getBalance(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId())) >= ConfigurationManager.getConfig("pinatas").getInt("pinatas." + pinata + ".cost")) {
                    LivingEntity entity = (LivingEntity) entityLocation.getWorld().spawnEntity(entityLocation, EntityType.valueOf(ConfigurationManager.getConfig("pinatas").getString("pinatas." + pinata + ".mob-type").toUpperCase()));
                    entity.setMaxHealth(ConfigurationManager.getConfig("pinatas").getDouble("pinatas." + pinata + ".health"));
                    entity.setHealth(entity.getMaxHealth());
                    plugin.getCommands().getUsers().add((Player) e.getWhoClicked());
                    if(PinataFactory.createPinata(builderLocation, (Player) e.getWhoClicked(), entity, pinata)) {
                        //Pinata created successfully, now we can withdraw $ from player.
                        plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId()), ConfigurationManager.getConfig("pinatas").getInt("pinatas." + pinata + ".cost"));
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if(!(entity.isDead())) {
                                entity.damage(entity.getMaxHealth());
                            }
                        }, ConfigurationManager.getConfig("pinatas").getInt("pinatas." + pinata + ".crate-time") * 20);
                    }
                } else {
                    e.getWhoClicked().sendMessage(Utils.colorMessage("Pinata.Selling.Cannot-Afford"));
                }
                e.getWhoClicked().closeInventory();
            }
        }
    }

}
