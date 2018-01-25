package pl.plajer.pinata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class FileManager {

	private FileConfiguration messagesConfig = null;
	private File messagesConfigFile = null;
	private FileConfiguration cratesConfig = null;
	private File cratesConfigFile = null;
	private FileConfiguration pinataConfig = null;
	private File pinataConfigFile = null;
	private JavaPlugin plugin;

	public FileManager(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	/*
	 * messages.yml
	 */

	public void saveDefaultMessagesConfig() {
		if (messagesConfigFile == null) {
			messagesConfigFile = new File(plugin.getDataFolder(), "messages.yml");
		}
		if (!messagesConfigFile.exists()) {            
			plugin.saveResource("messages.yml", false);
		}
	}

	public FileConfiguration getMessagesConfig() {
		if (messagesConfig == null) {
			reloadMessagesConfig();
		}
		return messagesConfig;
	}

	public void reloadMessagesConfig() {
		if (messagesConfigFile == null) {
			messagesConfigFile = new File(plugin.getDataFolder(), "messages.yml");
		}
		messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile);

		// Look for defaults in the jar
		try{
			Reader defConfigStream = new InputStreamReader(plugin.getResource("messages.yml"));
			if (defConfigStream != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				messagesConfig.setDefaults(defConfig);
			}
		} catch(Exception e){
			System.out.println("[Pinata] Error occured while trying to reload configuration!");
			e.printStackTrace();
		}
	}

	public void saveMessagesConfig() {
		if (messagesConfig == null || messagesConfigFile == null) {
			return;
		}
		try {
			getMessagesConfig().save(messagesConfigFile);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + messagesConfigFile, ex);
		}
	}

	/*
	 * crates.yml
	 */

	public void saveDefaultCratesConfig() {
		if (cratesConfigFile == null) {
			cratesConfigFile = new File(plugin.getDataFolder(), "crates.yml");
		}
		if (!cratesConfigFile.exists()) {            
			plugin.saveResource("crates.yml", false);
		}
	}

	public FileConfiguration getCratesConfig() {
		if (cratesConfig == null) {
			reloadCratesConfig();
		}
		return cratesConfig;
	}

	public void reloadCratesConfig() {
		if (cratesConfigFile == null) {
			cratesConfigFile = new File(plugin.getDataFolder(), "crates.yml");
		}
		cratesConfig = YamlConfiguration.loadConfiguration(cratesConfigFile);

		// Look for defaults in the jar
		Reader defConfigStream = new InputStreamReader(plugin.getResource("crates.yml"));
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			cratesConfig.setDefaults(defConfig);
		}
	}

	public void saveCratesConfig() {
		if (cratesConfig == null || cratesConfigFile == null) {
			return;
		}
		try {
			getCratesConfig().save(cratesConfigFile);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + cratesConfigFile, ex);
		}
	}

	/*
	 * pinatas.yml
	 */

	public void saveDefaultPinataConfig() {
		if (pinataConfigFile == null) {
			pinataConfigFile = new File(plugin.getDataFolder(), "pinatas.yml");
		}
		if (!pinataConfigFile.exists()) {            
			plugin.saveResource("pinatas.yml", false);
		}
	}

	public FileConfiguration getPinataConfig() {
		if (pinataConfig == null) {
			reloadPinataConfig();
		}
		return pinataConfig;
	}

	public void reloadPinataConfig() {
		if (pinataConfigFile == null) {
			pinataConfigFile = new File(plugin.getDataFolder(), "pinatas.yml");
		}
		pinataConfig = YamlConfiguration.loadConfiguration(pinataConfigFile);

		// Look for defaults in the jar
		Reader defConfigStream = new InputStreamReader(plugin.getResource("pinatas.yml"));
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			pinataConfig.setDefaults(defConfig);
		}
	}

	public void savePinataConfig() {
		if (pinataConfig == null || pinataConfigFile == null) {
			return;
		}
		try {
			getPinataConfig().save(pinataConfigFile);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + pinataConfigFile, ex);
		}
	}

	public void updateConfig(String file) {
		HashMap<String, Object> newConfig = getConfigVals(file);
		FileConfiguration c;
		if(file.equals("config.yml")) {
			c = plugin.getConfig();
		} else{
			c = getMessagesConfig();
		}
		for (String var : c.getKeys(false)) {
			newConfig.remove(var);
		}
		if (newConfig.size()!=0) {
			for (String key : newConfig.keySet()) {
				c.set(key, newConfig.get(key));
			}
			try {
				c.save(new File(plugin.getDataFolder(), file));
			} catch (IOException ignored) {}
		}
	}

	private HashMap<String, Object> getConfigVals(String file) {
		HashMap<String, Object> var = new HashMap<>();
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.loadFromString(stringFromInputStream(Main.class.getResourceAsStream("/" + file)));
		} catch (InvalidConfigurationException ignored) {}
		for (String key : config.getKeys(false)) {
			var.put(key, config.get(key));
		}
		return var;
	}

	@SuppressWarnings("resource")
    private String stringFromInputStream(InputStream in) {
		return new Scanner(in).useDelimiter("\\A").next();
	}

}
