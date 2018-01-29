package pl.plajer.pinata;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import net.milkbowl.vault.economy.Economy;
import pl.plajer.pinata.utils.MetricsLite;
import pl.plajer.pinata.utils.UpdateChecker;
import pl.plajer.pinata.utils.Utils;

public class Main extends JavaPlugin {

    private CrateManager crateManager;
    private Commands commands;
    private FileManager fileManager;
    private PinataManager pinataManager;
    private SignManager signManager;

    private List<String> disabledWorlds = new ArrayList<>();
    private Economy econ = null;
    private Boolean usingVault;
    private Boolean usingCrackShot;
    private Boolean usingHolograms;
    private final int MESSAGES_FILE_VERSION = 8;
    private final int CONFIG_FILE_VERSION = 3;
    private static Main instance;

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
        fileManager.saveDefaultMessagesConfig();
        fileManager.reloadMessagesConfig();
        setupDependencies();
        if(!fileManager.getMessagesConfig().isSet("File-Version-Do-Not-Edit") || !fileManager.getMessagesConfig().get("File-Version-Do-Not-Edit").equals(MESSAGES_FILE_VERSION)) {
            getLogger().info("Your messages file is outdated! Updating...");
            fileManager.updateConfig("messages.yml");
            fileManager.getMessagesConfig().set("File-Version-Do-Not-Edit", MESSAGES_FILE_VERSION);
            fileManager.saveMessagesConfig();
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
        fileManager.saveDefaultPinataConfig();
        fileManager.saveDefaultCratesConfig();
        fileManager.reloadPinataConfig();
        fileManager.reloadMessagesConfig();
        crateManager.loadCrates();
        pinataManager.loadPinatas();
        crateManager.particleScheduler();
        if(usingHolograms) {
            hologramScheduler();
        }
        String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
        if(this.getConfig().getBoolean("update-notify")) {
            try {
                UpdateChecker.checkUpdate(currentVersion);
                String latestVersion = UpdateChecker.getLatestVersion();
                if(latestVersion != null) {
                    latestVersion = "v" + latestVersion;
                    Bukkit.getConsoleSender().sendMessage(Utils.colorRawMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
                }
            } catch(Exception ex) {
                Bukkit.getConsoleSender().sendMessage(Utils.colorRawMessage("Other.Plugin-Update-Check-Failed").replaceAll("%error%", ex.getMessage()));
            }
        }
    }

    @Override
    public void onDisable() {
        for(World world : Bukkit.getServer().getWorlds()) {
            for(Entity entity : Bukkit.getServer().getWorld(world.getName()).getEntities()) {
                if(entity instanceof Sheep) {
                    if(commands.getPinata().containsKey(entity)) {
                        commands.getPinata().get(entity).getPlayer().sendMessage(Utils.colorRawMessage("Pinata.Config.Reload-Removed"));
                        commands.getPinata().get(entity).getBuilder().getBlock().setType(Material.AIR);
                        commands.getPinata().get(entity).getLeash().remove();
                        entity.remove();
                        commands.getPinata().remove(entity);
                    }
                }
            }
        }
        if(usingHolograms) {
            for(Hologram holo : HologramsAPI.getHolograms(this)) {
                holo.delete();
            }
        }
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public Commands getCommands() {
        return commands;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public PinataManager getPinataManager() {
        return pinataManager;
    }

    public SignManager getSignManager() {
        return signManager;
    }

    public Economy getEco() {
        return econ;
    }

    public Boolean getVaultUse() {
        return usingVault;
    }

    public Boolean getCrackShotUse() {
        return usingCrackShot;
    }

    public Boolean getHologramsUse() {
        return usingHolograms;
    }

    public static Main getInstance() {
        return instance;
    }

    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    private void setupDependencies() {
        if(setupCrackShot()) {
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
        if(!setupHolographicDisplays()) {
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
                holo.appendTextLine(Utils.colorRawMessage("Hologram.Crate-Hologram").replaceAll("%name%", crateManager.getCratesLocations().get(l)));
                Bukkit.getScheduler().runTaskLater(this, () -> holo.delete(), (long) getConfig().getDouble("hologram-refresh") * 20);
            }
        }, (long) this.getConfig().getDouble("hologram-refresh") * 20, (long) this.getConfig().getDouble("hologram-refresh") * 20);
    }

    private boolean setupCrackShot() {
        if(getServer().getPluginManager().getPlugin("CrackShot") == null) {
            usingCrackShot = false;
            return false;
        }
        usingCrackShot = true;
        return true;
    }

    private boolean setupHolographicDisplays() {
        if(getServer().getPluginManager().getPlugin("HolographicDisplays") == null) {
            usingHolograms = false;
            return false;
        }
        usingHolograms = true;
        return true;
    }

    private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") == null) {
            usingVault = false;
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            usingVault = false;
            return false;
        }
        econ = rsp.getProvider();
        usingVault = true;
        return econ != null;
    }

}
