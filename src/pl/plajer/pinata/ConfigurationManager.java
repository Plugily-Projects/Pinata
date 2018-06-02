package pl.plajer.pinata;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class ConfigurationManager {

    private static Main plugin;

    public static void init(Main plugin) {
        ConfigurationManager.plugin = plugin;
    }

    public static FileConfiguration getConfig(String fileName) {
        File file = new File(plugin.getDataFolder() + File.separator + fileName + ".yml");
        if(!file.exists()) {
            plugin.getLogger().info("Creating " + fileName + ".yml because it does not exist!");
            plugin.saveResource(fileName + ".yml", true);
        }
        file = new File(plugin.getDataFolder(), fileName + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch(InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Cannot load file " + config + ".yml!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Create blank file " + config + ".yml or restart the server!");
        }
        return config;
    }

    public static void saveConfig(FileConfiguration config, String name) {
        try {
            config.save(new File(plugin.getDataFolder(), name + ".yml"));
        } catch(IOException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Cannot save file " + name + ".yml!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Create blank file " + name + ".yml or restart the server!");
        }
    }

    public static String getDefaultLanguageMessage(String message){
        return getConfig("messages").getString(message);
    }

    public static String getLanguageMessage(String message) {
        if(plugin.getLocale() != Main.PinataLocale.ENGLISH){
            return getConfig("messages_" + plugin.getLocale().getPrefix()).getString(message);
        }
        return getConfig("messages").getString(message);
    }

}
