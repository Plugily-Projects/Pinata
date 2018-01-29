package pl.plajer.pinata;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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

public class CrateManager implements Listener {

    private Map<Location, String> cratesLocations = new HashMap<>();
    private Map<Player, Location> crateUsage = new HashMap<>();
    private Main plugin;

    public CrateManager(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void loadCrates() {
        ConfigurationSection pinata = plugin.getFileManager().getCratesConfig().getConfigurationSection("crates");
        if(pinata != null) {
            FileConfiguration config = plugin.getFileManager().getCratesConfig();
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
        if(!(e.getClickedBlock() == null) && !(e.getClickedBlock().getType() == null) && e.getClickedBlock().getType().equals(Material.CHEST) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            ConfigurationSection csp = plugin.getFileManager().getCratesConfig().getConfigurationSection("crates");
            if(csp != null) {
                for(String key : csp.getKeys(false)) {
                    if(cratesLocations.containsKey(e.getClickedBlock().getLocation())) {
                        e.setCancelled(true);
                        if(!plugin.getVaultUse()) {
                            e.getPlayer().sendMessage(Utils.colorRawMessage("Pinata.Command.Vault-Not-Detected"));
                            return;
                        }
                        if(!e.getPlayer().hasPermission("pinata.player.crate")) {
                            e.getPlayer().sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.No-Permission"));
                            return;
                        }
                        if(!plugin.getConfig().getBoolean("disabled-worlds-exclusions.crates")) {
                            if(plugin.getDisabledWorlds().contains(e.getPlayer().getWorld().getName())) {
                                e.getPlayer().sendMessage(Utils.colorRawMessage("Pinata.Create.Disabled-World"));
                                return;
                            }
                        }
                        crateUsage.put(e.getPlayer(), e.getClickedBlock().getLocation());
                        Utils.createPinatasGUI("Menus.Crate-Menu.Inventory-Name", e.getPlayer());
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCrateDestroy(BlockBreakEvent e) {
        if(e.getBlock().getType().equals(Material.CHEST)) {
            ConfigurationSection pinata = plugin.getFileManager().getCratesConfig().getConfigurationSection("crates");
            if(pinata != null) {
                if(cratesLocations.containsKey(e.getBlock().getLocation())) {
                    for(String key : pinata.getKeys(false)) {
                        if(cratesLocations.get(e.getBlock().getLocation()).equals(key)) {
                            if(!e.getPlayer().hasPermission("pinata.admin.crate.destroy")) {
                                e.getPlayer().sendMessage(Utils.colorRawMessage("Pinata.Crate-Creation.No-Permission"));
                                e.setCancelled(true);
                            }
                            plugin.getFileManager().getCratesConfig().set("crates." + key, null);
                            plugin.getFileManager().saveCratesConfig();
                            cratesLocations.remove(e.getBlock().getLocation());
                            String message = Utils.colorRawMessage("Pinata.Crate-Creation.Destroyed");
                            e.getPlayer().sendMessage(message.replaceAll("%name%", key));
                            return;
                        }
                    }
                }
            }
        }
    }

    public Map<Player, Location> getCrateUsage() {
        return crateUsage;
    }

    public Map<Location, String> getCratesLocations() {
        return cratesLocations;
    }

}
