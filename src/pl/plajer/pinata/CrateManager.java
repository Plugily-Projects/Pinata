package pl.plajer.pinata;

import lombok.Getter;
import org.bukkit.Bukkit;
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
import pl.plajer.pinata.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CrateManager implements Listener {

    @Getter
    private Map<Location, String> cratesLocations = new HashMap<>();
    @Getter
    private Map<Player, Location> crateUsage = new HashMap<>();
    private Main plugin;

    CrateManager(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void loadCrates() {
        ConfigurationSection pinata = plugin.getFileManager().getFile("crates").getConfigurationSection("crates");
        if(pinata != null) {
            FileConfiguration config = plugin.getFileManager().getFile("crates");
            for(String crate : pinata.getKeys(false)) {
                Location crateLoc = new Location(Bukkit.getWorld(config.getString("crates." + crate + ".world")), config.getDouble("crates." + crate + ".x"), config.getDouble("crates." + crate + ".y"), config.getDouble("crates." + crate + ".z"));
                cratesLocations.put(crateLoc, crate);
                plugin.getLogger().log(Level.INFO, "Loaded crate " + crate + " at location " + config.getString("crates." + crate + ".world") + " " + config.getDouble("crates." + crate + ".x") + " " + config.getDouble("crates." + crate + ".y") + " " + config.getDouble("crates." + crate + ".z"));
            }
        }
    }

    /**
     * Particles at crates locations
     */
    public void particleScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for(Location l : cratesLocations.keySet()) {
                l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, 1);
            }
        }, (long) plugin.getConfig().getDouble("particle-refresh") * 20, (long) plugin.getConfig().getDouble("particle-refresh") * 20);
    }

    @EventHandler
    public void onCrateClick(PlayerInteractEvent e) {
        if(e.getClickedBlock() != null && e.getClickedBlock().getType() != null && e.getClickedBlock().getType().equals(Material.CHEST) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if(cratesLocations.containsKey(e.getClickedBlock().getLocation())) {
                e.setCancelled(true);
                if(!plugin.isPluginEnabled("Vault")) {
                    e.getPlayer().sendMessage(Utils.colorFileMessage("Pinata.Command.Vault-Not-Detected"));
                    return;
                }
                if(!e.getPlayer().hasPermission("pinata.player.crate")) {
                    e.getPlayer().sendMessage(Utils.colorFileMessage("Pinata.Crate-Creation.No-Permission"));
                    return;
                }
                if(!plugin.getConfig().getBoolean("disabled-worlds-exclusions.crates")) {
                    if(plugin.getDisabledWorlds().contains(e.getPlayer().getWorld().getName())) {
                        e.getPlayer().sendMessage(Utils.colorFileMessage("Pinata.Create.Disabled-World"));
                        return;
                    }
                }
                crateUsage.put(e.getPlayer(), e.getClickedBlock().getLocation());
                Utils.createPinatasGUI("Menus.Crate-Menu.Inventory-Name", e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onCrateDestroy(BlockBreakEvent e) {
        if(e.getBlock().getType().equals(Material.CHEST)) {
            ConfigurationSection pinata = plugin.getFileManager().getFile("crates").getConfigurationSection("crates");
            if(pinata != null) {
                if(cratesLocations.containsKey(e.getBlock().getLocation())) {
                    for(String key : pinata.getKeys(false)) {
                        if(cratesLocations.get(e.getBlock().getLocation()).equals(key)) {
                            if(!e.getPlayer().hasPermission("pinata.admin.crate.destroy")) {
                                e.getPlayer().sendMessage(Utils.colorFileMessage("Pinata.Crate-Creation.No-Permission"));
                                e.setCancelled(true);
                            }
                            FileConfiguration config = plugin.getFileManager().getFile("crates");
                            config.set("crates." + key, null);
                            plugin.getFileManager().saveFile(config, "crates");
                            cratesLocations.remove(e.getBlock().getLocation());
                            String message = Utils.colorFileMessage("Pinata.Crate-Creation.Destroyed");
                            e.getPlayer().sendMessage(message.replaceAll("%name%", key));
                            return;
                        }
                    }
                }
            }
        }
    }

}
