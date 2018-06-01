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
        if(e.getInventory().getName().equals(Utils.colorRawMessage("Menus.Preview-Menu.Inventory-Name"))) {
            e.setCancelled(true);
            return;
        }
        if(e.getCurrentItem() == null || !e.getCurrentItem().getType().equals(Material.WOOL)) {
            return;
        }
        final ItemMeta item = e.getCurrentItem().getItemMeta();
        final String pinata = item.getDisplayName().replaceAll(Utils.colorMessage("&6"), "");
        final Player p = e.getWhoClicked();
        if(e.getInventory().getName().equals(Utils.colorRawMessage("Menus.List-Menu.Inventory-Name"))) {
            e.setCancelled(true);
            if(e.getClick() == ClickType.LEFT) {
                p.closeInventory();
                //Scheduler to prevent bugged GUI
                Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(p, "pinata preview " + pinata), 1);
            }
            if(e.getClick() == ClickType.RIGHT) {
                if(item.getLore().get(1).equals(Utils.colorRawMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"))) {
                    p.closeInventory();
                    p.sendMessage(Utils.colorRawMessage("Pinata.Selling.Not-For-Sale"));
                    return;
                }
                p.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(p, Utils.colorMessage("pinata buy " + item.getDisplayName().replaceAll("&6", ""))), 1);
            }
        }
        if(e.getInventory().getName().equals(Utils.colorRawMessage("Menus.Crate-Menu.Inventory-Name")) || e.getInventory().getName().equals(Utils.colorRawMessage("Signs.Inventory-Name"))) {
            e.setCancelled(true);
            if(e.getClick() == ClickType.LEFT) {
                p.closeInventory();
                //Scheduler to prevent bugged GUI
                Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(p, "pinata preview " + pinata), 1);
            }
            if(e.getClick() == ClickType.RIGHT) {
                if(item.getLore().get(1).equals(Utils.colorRawMessage("Menus.List-Menu.Pinata-Cost-Not-For-Sale"))) {
                    p.closeInventory();
                    p.sendMessage(Utils.colorRawMessage("Pinata.Selling.Not-For-Sale"));
                    return;
                }
                Location builderLocation;
                Location entityLocation;
                if(e.getInventory().getName().equals(Utils.colorRawMessage("Menus.Crate-Menu.Inventory-Name"))) {
                    if(plugin.getCrateManager().getCrateUsage().containsKey(p)) {
                        builderLocation = plugin.getCrateManager().getCrateUsage().get(p).clone().add(0, 8, 0);
                        entityLocation = plugin.getCrateManager().getCrateUsage().get(p).clone().add(0, 3, 0);
                    } else {
                        p.sendMessage(Utils.colorRawMessage("Pinata.Buy-Error"));
                        return;
                    }
                //sign inventory
                } else {
                    if(plugin.getSignManager().getSignUsage().containsKey(p)) {
                        builderLocation = plugin.getSignManager().getSignUsage().get(p).clone().add(0, 8, 0);
                        entityLocation = plugin.getSignManager().getSignUsage().get(p).clone().add(0, 3, 0);
                    } else {
                        p.sendMessage(Utils.colorRawMessage("Pinata.Buy-Error"));
                        return;
                    }
                }
                if(plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".cost") == -1) {
                    p.sendMessage(Utils.colorRawMessage("Pinata.Selling.Not-For-Sale"));
                    p.closeInventory();
                    return;
                }
                if(!plugin.getCommands().getUsers().isEmpty()) {
                    if(plugin.getCommands().getUsers().contains(p)) {
                        p.sendMessage(Utils.colorRawMessage("Pinata.Create.Already-Created"));
                        p.closeInventory();
                        return;
                    }
                }
                if(plugin.getConfig().getBoolean("using-permissions")) {
                    final String pperm = plugin.getFileManager().getPinataConfig().get("pinatas." + pinata + ".permission").toString();
                    if(!p.hasPermission(pperm)) {
                        p.sendMessage(Utils.colorRawMessage("Pinata.Create.No-Permission"));
                        p.closeInventory();
                        return;
                    }
                }
                if(p.hasPermission("pinata.admin.freeall")) {
                    LivingEntity entity = (LivingEntity) entityLocation.getWorld().spawnEntity(entityLocation, EntityType.valueOf(plugin.getFileManager().getPinataConfig().getString("pinatas." + pinata + ".mob-type").toUpperCase()));
                    entity.setMaxHealth(plugin.getFileManager().getPinataConfig().getDouble("pinatas." + pinata + ".health"));
                    entity.setHealth(entity.getMaxHealth());
                    plugin.getCommands().getUsers().add(p);
                    if(PinataFactory.createPinata(builderLocation, p, entity, pinata)) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if(!(entity.isDead())) {
                                entity.damage(entity.getMaxHealth());
                            }
                        }, plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".crate-time") * 20);
                    }
                } else if(plugin.getEco().getBalance(Bukkit.getOfflinePlayer(p.getUniqueId())) >= plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".cost")) {
                    LivingEntity entity = (LivingEntity) entityLocation.getWorld().spawnEntity(entityLocation, EntityType.valueOf(plugin.getFileManager().getPinataConfig().getString("pinatas." + pinata + ".mob-type").toUpperCase()));
                    entity.setMaxHealth(plugin.getFileManager().getPinataConfig().getDouble("pinatas." + pinata + ".health"));
                    entity.setHealth(entity.getMaxHealth());
                    plugin.getCommands().getUsers().add(p);
                    if(PinataFactory.createPinata(builderLocation, p, entity, pinata)) {
                        //Pinata created successfully, now we can withdraw $ from player.
                        plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".cost"));
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if(!(entity.isDead())) {
                                entity.damage(entity.getMaxHealth());
                            }
                        }, plugin.getFileManager().getPinataConfig().getInt("pinatas." + pinata + ".crate-time") * 20);
                    }
                } else {
                    p.sendMessage(Utils.colorRawMessage("Pinata.Selling.Cannot-Afford"));
                }
                p.closeInventory();
            }
        }
    }

}
