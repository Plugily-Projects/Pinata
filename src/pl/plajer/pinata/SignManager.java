package pl.plajer.pinata;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinataapi.PinataFactory;
import pl.plajer.pinata.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class SignManager implements Listener {

    private Map<Player, Location> signUsage = new HashMap<>();
    private Main plugin;

    SignManager(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if(e.getLine(0).equalsIgnoreCase("[pinata]")) {
            if(e.getPlayer().hasPermission("pinata.admin.sign.set")) {
                if(e.getLine(1).isEmpty()) {
                    e.getPlayer().sendMessage(Utils.colorMessage("Signs.Invalid-Pinata"));
                    return;
                }
                e.setLine(0, Utils.colorMessage("Signs.Lines.First"));
                if(e.getLine(1).equalsIgnoreCase("all") || e.getLine(1).equalsIgnoreCase("gui")) {
                    e.setLine(1, Utils.colorMessage("Signs.Lines.Second-Every-Pinata"));
                } else {
                    Pinata pinata = plugin.getPinataManager().getPinataByName(e.getLine(1));
                    if(pinata == null) {
                        e.getPlayer().sendMessage(Utils.colorMessage("Signs.Invalid-Pinata"));
                        return;
                    }
                    e.setLine(1, Utils.colorMessage("Signs.Lines.Second-Specific-Pinata-Color") + e.getLine(1));
                }
            }
        }
    }

    @EventHandler
    public void onSignDestroy(BlockBreakEvent e) {
        if(e.getBlock().getType().equals(Material.SIGN) || e.getBlock().getType().equals(Material.SIGN_POST) || e.getBlock().getType().equals(Material.WALL_SIGN)) {
            Sign s = (Sign) e.getBlock().getState();
            if(s.getLine(0).equals(Utils.colorMessage("Signs.Lines.First"))) {
                if(e.getPlayer().hasPermission("pinata.admin.sign.destroy")) {
                    e.getPlayer().sendMessage(Utils.colorMessage("Signs.Invalid-Pinata"));
                } else {
                    e.getPlayer().sendMessage(Utils.colorMessage("Signs.No-Permission"));
                }
            }
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        //cancel left click action to avoid sign break problems
        if(e.getClickedBlock() == null || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }
        if(e.getClickedBlock().getType().equals(Material.SIGN) || e.getClickedBlock().getType().equals(Material.SIGN_POST) || e.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
            Sign s = (Sign) e.getClickedBlock().getState();
            if(s.getLine(0).equals(Utils.colorMessage("Signs.Lines.First"))) {
                if(!plugin.isPluginEnabled("Vault")) {
                    e.getPlayer().sendMessage(Utils.colorMessage("Pinata.Command.Vault-Not-Detected"));
                    return;
                }
                if(!e.getPlayer().hasPermission("pinata.player.sign")) {
                    e.getPlayer().sendMessage(Utils.colorMessage("Sings.No-Permission"));
                    return;
                }
                if(!plugin.getConfig().getBoolean("disabled-worlds-exclusions.signs")) {
                    if(plugin.getDisabledWorlds().contains(e.getPlayer().getWorld().getName())) {
                        e.getPlayer().sendMessage(Utils.colorMessage("Pinata.Create.Disabled-World"));
                        return;
                    }
                }
                if(s.getLine(1).equals(Utils.colorMessage("Signs.Lines.Second-Every-Pinata"))) {
                    signUsage.put(e.getPlayer(), e.getClickedBlock().getLocation());
                    Utils.createPinatasGUI("Signs.Inventory-Name", e.getPlayer());
                } else {
                    String pinataName = ChatColor.stripColor(s.getLine(1));
                    for(Pinata pinata : plugin.getPinataManager().getPinataList()) {
                        if(pinata.getID().equalsIgnoreCase(pinataName)) {
                            Location loc = e.getClickedBlock().getLocation().clone().add(0, 8, 0);
                            Location entityLoc = e.getClickedBlock().getLocation().clone().add(0, 3, 0);
                            LivingEntity entity = (LivingEntity) entityLoc.getWorld().spawnEntity(entityLoc, pinata.getEntityType());
                            entity.setMaxHealth(pinata.getHealth());
                            entity.setHealth(entity.getMaxHealth());
                            if(e.getPlayer().hasPermission("pinata.admin.freeall")) {
                                if(PinataFactory.createPinata(loc, e.getPlayer(), entity, pinata)) {
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        if(!(entity.isDead())) {
                                            entity.damage(entity.getMaxHealth());
                                        }
                                    }, pinata.getCrateTime() * 20);
                                }
                            } else if(plugin.getEco().getBalance(Bukkit.getOfflinePlayer(e.getPlayer().getUniqueId())) >= pinata.getPrice()) {
                                if(PinataFactory.createPinata(loc, e.getPlayer(), entity, pinata)) {
                                    //Pinata created successfully, now we can withdraw $ from player.
                                    plugin.getEco().withdrawPlayer(Bukkit.getOfflinePlayer(e.getPlayer().getUniqueId()), pinata.getPrice());
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        if(!(entity.isDead())) {
                                            entity.damage(entity.getMaxHealth());
                                        }
                                    }, pinata.getCrateTime() * 20);
                                }
                            } else {
                                e.getPlayer().sendMessage(Utils.colorMessage("Pinata.Selling.Cannot-Afford"));
                            }
                        }
                    }
                }
            }
        }
    }

    public Map<Player, Location> getSignUsage() {
        return signUsage;
    }
}
