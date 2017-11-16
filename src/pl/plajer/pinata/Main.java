package pl.plajer.pinata;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	private CrateManager crateManager;
	private Commands commands;
	private FileManager fileManager;
	private MenuHandler menuHandler;
	private PinataListeners pinataListeners;
	private PinataManager pinataManager;

	private Economy econ = null;
	private Boolean usevault;
	private Boolean usecrackshot;
	private Boolean useholograms;
	private String currentVersion;
	private String latestVersion;
	private int messagesFileVersion = 4;
	private int configFileVersion = 1;
	private static Main instance;

	@Override
	public void onEnable() {
		this.getLogger().log(Level.INFO, "Happy Halloween!");
		instance = this;
		new MetricsLite(this);
		crateManager = new CrateManager(this);
		commands = new Commands(this);
		fileManager = new FileManager(this);
		menuHandler = new MenuHandler(this);
		pinataListeners = new PinataListeners(this);
		pinataManager = new PinataManager(this);
		setupDependencies();
		saveDefaultConfig();
		getFileManager().saveDefaultMessagesConfig();
		getFileManager().reloadMessagesConfig();
		if(!getFileManager().getMessagesConfig().isSet("File-Version-Do-Not-Edit") || !getFileManager().getMessagesConfig().get("File-Version-Do-Not-Edit").equals(messagesFileVersion)) {
			getLogger().info("Your messages file is outdated! Updating...");
			getFileManager().updateConfig("messages.yml");
			getFileManager().getMessagesConfig().set("File-Version-Do-Not-Edit", messagesFileVersion);
			getFileManager().saveMessagesConfig();
			getLogger().info("File successfully updated!");
		}
		if(!getConfig().isSet("File-Version-Do-Not-Edit") || !getConfig().get("File-Version-Do-Not-Edit").equals(configFileVersion)) {
			getLogger().info("Your config file is outdated! Updating...");
			getFileManager().updateConfig("config.yml");
			getConfig().set("File-Version-Do-Not-Edit", configFileVersion);
			saveConfig();
			getLogger().info("File successfully updated!");
			Bukkit.getConsoleSender().sendMessage("§c[Pinata] Warning! Your config.yml file was updated and all comments were removed! If you want to get comments back please generate new config.yml file!");
		}
		getFileManager().saveDefaultPinataConfig();
		getFileManager().reloadPinataConfig();
		getFileManager().reloadMessagesConfig();
		getPinataManager().loadPinatas();
		getCrateManager().particleScheduler();
		currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
		if (this.getConfig().getBoolean("update-notify")){
			try {
				UpdateChecker.checkUpdate(currentVersion);
				latestVersion = UpdateChecker.getLatestVersion();
				if (latestVersion != null) {
					latestVersion = "v" + latestVersion;
					Bukkit.getConsoleSender().sendMessage(Utils.colorRawMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
				}
			} catch (Exception ex) {
				Bukkit.getConsoleSender().sendMessage(Utils.colorRawMessage("Other.Plugin-Update-Check-Failed").replaceAll("%error%", ex.getMessage()));
			}
		}
	}

	@Override
	public void onDisable() {
		for(World world : Bukkit.getServer().getWorlds()){
			for(Entity entity : Bukkit.getServer().getWorld(world.getName()).getEntities()){
				if(entity instanceof Sheep){
					if(getCommands().getPinatas().containsKey(entity)){
						getCommands().getPinatas().get(entity).sendMessage(Utils.colorRawMessage("Pinata.Config.Reload-Removed"));
						getCommands().getBuilder().get(entity).getBlock().setType(Material.AIR);
						getCommands().getPinatas().remove(entity);
						getCommands().getLeash().get(entity).remove();
						getCommands().getLeash().remove(entity);
						((Sheep) entity).remove();
					}
				}
			}
		}
		if(useholograms) {
			for(Hologram holo : HologramsAPI.getHolograms(this)){
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

	public MenuHandler getMenuHandler() {
		return menuHandler;
	}

	public PinataListeners getPinataListeners() {
		return pinataListeners;
	}

	public PinataManager getPinataManager() {
		return pinataManager;
	}

	public Economy getEco() {
		return econ;
	}

	public Boolean getVaultUse(){
		return usevault;
	}

	public Boolean getCrackshotUse(){
		return usecrackshot;
	}

	public Boolean getHologramsUse(){
		return useholograms;
	}

	public int getMessagesFileVersion() {
		return messagesFileVersion;
	}

	public static Main getInstance() {
		return instance;
	}
	
	private void setupDependencies() {
		if(setupCrackshot()) {
			Bukkit.getConsoleSender().sendMessage("§a[Pinata] Detected CrackShot plugin!");
			Bukkit.getConsoleSender().sendMessage("§a[Pinata] Enabling CrackShot support.");
		} else {
			Bukkit.getConsoleSender().sendMessage("§c[Pinata] CrackShot plugin isn't installed!");
			Bukkit.getConsoleSender().sendMessage("§c[Pinata] Disabling CrackShot support.");
		}
		if(!setupEconomy()) {
			Bukkit.getConsoleSender().sendMessage("§c[Pinata] Vault plugin isn't installed!");
			Bukkit.getConsoleSender().sendMessage("§c[Pinata] Disabling Vault support.");
		} else {
			Bukkit.getConsoleSender().sendMessage("§a[Pinata] Detected Vault plugin!");
			Bukkit.getConsoleSender().sendMessage("§a[Pinata] Enabling economy support.");
		}
		if(!setupHolographicDisplays()) {
			Bukkit.getConsoleSender().sendMessage("§c[Pinata] Holographic Displays plugin isn't installed!");
			Bukkit.getConsoleSender().sendMessage("§c[Pinata] Disabling holograms support.");
		} else {
			getCrateManager().hologramScheduler();
			Bukkit.getConsoleSender().sendMessage("§a[Pinata] Detected Holographic Displays plugin!");
			Bukkit.getConsoleSender().sendMessage("§a[Pinata] Enabling holograms support.");
		}
	}

	private boolean setupCrackshot() {
		if (getServer().getPluginManager().getPlugin("CrackShot") == null) {
			usecrackshot = false;
			return false;
		}
		usecrackshot = true;
		return usecrackshot != null;
	}

	private boolean setupHolographicDisplays() {
		if (getServer().getPluginManager().getPlugin("HolographicDisplays") == null) {
			useholograms = false;
			return false;
		}
		useholograms = true;
		return useholograms != null;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			usevault = false;
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			usevault = false;
			return false;
		}
		econ = rsp.getProvider();
		usevault = true;
		return econ != null;
	}

}
