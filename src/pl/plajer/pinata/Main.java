package pl.plajer.pinata;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajer.pinata.dao.PinataData;
import pl.plajer.pinata.utils.MetricsLite;
import pl.plajer.pinata.utils.UpdateChecker;
import pl.plajer.pinata.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
public class Main extends JavaPlugin {

    private static Main instance;
    public static final int MESSAGES_FILE_VERSION = 9;
    public static final int CONFIG_FILE_VERSION = 5;
    private CrateManager crateManager;
    private Commands commands;
    private FileManager fileManager;
    private PinataManager pinataManager;
    private SignManager signManager;
    private List<String> disabledWorlds = new ArrayList<>();
    private Economy eco = null;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        this.getLogger().log(Level.INFO, "Crack this pinata!");
        instance = this;
        new MetricsLite(this);
        crateManager = new CrateManager(this);
        commands = new Commands(this);
        fileManager = new FileManager(this);
        new MenuHandler(this);
        new PinataListeners(this);
        pinataManager = new PinataManager(this);
        signManager = new SignManager(this);
        saveDefaultConfig();
        fileManager.getFile("messages");
        setupDependencies();
        if(!fileManager.getFile("messages").isSet("File-Version-Do-Not-Edit") || !fileManager.getFile("messages").get("File-Version-Do-Not-Edit").equals(MESSAGES_FILE_VERSION)) {
            getLogger().info("Your messages file is outdated! Updating...");
            fileManager.updateConfig("messages.yml");
            FileConfiguration config = fileManager.getFile("messages");
            config.set("File-Version-Do-Not-Edit", MESSAGES_FILE_VERSION);
            fileManager.saveFile(config, "messages");
            getLogger().info("File successfully updated!");
        }
        if(!getConfig().isSet("File-Version-Do-Not-Edit") || !getConfig().get("File-Version-Do-Not-Edit").equals(CONFIG_FILE_VERSION)) {
            getLogger().info("Your config file is outdated! Updating...");
            fileManager.updateConfig("config.yml");
            getConfig().set("File-Version-Do-Not-Edit", CONFIG_FILE_VERSION);
            saveConfig();
            getLogger().info("File successfully updated!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Warning! Your config.yml file was updated and all comments were removed! If you want to get comments back please generate new config.yml file!");
        }
        for(String world : getConfig().getStringList("disabled-worlds")) {
            disabledWorlds.add(world);
            getLogger().info("Pinata creation blocked at world " + world + "!");
        }
        fileManager.getFile("pinatas");
        fileManager.getFile("crates");
        crateManager.loadCrates();
        pinataManager.loadPinatas3();
        crateManager.particleScheduler();
        if(isPluginEnabled("HolographicDisplays")) hologramScheduler();
        String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
        if(getConfig().getBoolean("update-notify")) {
            switch(UpdateChecker.checkUpdate()){
                case STABLE:
                    Bukkit.getConsoleSender().sendMessage(Utils.colorFileMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", UpdateChecker.getLatestVersion()));
                    break;
                case BETA:
                    Bukkit.getConsoleSender().sendMessage(Utils.colorFileMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", UpdateChecker.getLatestVersion()));
                    //todo beta
                    break;
                case ERROR:
                    Bukkit.getConsoleSender().sendMessage(Utils.colorFileMessage("Other.Plugin-Update-Check-Failed"));
                    break;
                case UPDATED: break;
            }
        }
    }

    @Override
    public void onDisable() {
        for(World world : Bukkit.getServer().getWorlds()) {
            for(Entity entity : Bukkit.getServer().getWorld(world.getName()).getEntities()) {
                if(entity instanceof Sheep) {
                    if(entity.hasMetadata("PinataEntity")){
                        MetadataValue value = entity.getMetadata("PinataData").get(0);
                        PinataData data = (PinataData) value.value();
                        data.getPlayer().sendMessage(Utils.colorFileMessage("Pinata.Config.Reload-Removed"));
                        data.getBuilder().getBlock().setType(Material.AIR);
                        data.getLeash().remove();
                        entity.remove();
                    }
                }
            }
        }
        //check if plugin is already disabled
        if(!getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) return;
        for(Hologram h : HologramsAPI.getHolograms(this)) {
            h.delete();
        }
    }

    private void setupDependencies() {
        if(isPluginEnabled("CrackShot")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Detected CrackShot plugin!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Enabling CrackShot support.");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] CrackShot plugin isn't installed!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Disabling CrackShot support.");
        }
        if(!setupEconomy()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Vault plugin isn't installed!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Disabling Vault support.");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Detected Vault plugin!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Enabling economy support.");
        }
        if(!isPluginEnabled("HolographicDisplays")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Holographic Displays plugin isn't installed!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Disabling holograms support.");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Detected Holographic Displays plugin!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Enabling holograms support.");
        }
    }

    /**
     * Holograms at crates locations
     * Moved to Main class because it throws an error with registering events in CrateManager class
     */
    private void hologramScheduler() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for(Location l : crateManager.getCratesLocations().keySet()) {
                Hologram holo = HologramsAPI.createHologram(this, l.clone().add(0.5, 1.5, 0.5));
                holo.appendTextLine(Utils.colorFileMessage("Hologram.Crate-Hologram").replaceAll("%name%", crateManager.getCratesLocations().get(l)));
                Bukkit.getScheduler().runTaskLater(this, holo::delete, (long) getConfig().getDouble("hologram-refresh") * 20);
            }
        }, (long) this.getConfig().getDouble("hologram-refresh") * 20, (long) this.getConfig().getDouble("hologram-refresh") * 20);
    }

    public boolean isPluginEnabled(String plugin) {
        return getServer().getPluginManager().getPlugin(plugin) != null;
    }

    private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }

}
