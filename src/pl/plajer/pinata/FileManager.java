package pl.plajer.pinata;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class FileManager {

    private Main plugin;

    public FileManager(Main plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getFile(String filename) {
        File file = new File(plugin.getDataFolder() + File.separator + filename + ".yml");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException ex) {
                ex.printStackTrace();
                Bukkit.getConsoleSender().sendMessage("Cannot save file " + filename + ".yml!");
                Bukkit.getConsoleSender().sendMessage("Create blank file " + filename + ".yml or restart the server!");
            }
        }
        file = new File(plugin.getDataFolder(), filename + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch(InvalidConfigurationException ex) {
            ex.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("Cannot save file " + filename + ".yml!");
            Bukkit.getConsoleSender().sendMessage("Create blank file " + filename + ".yml or restart the server!");
            return new YamlConfiguration();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("Cannot save file " + filename + ".yml!");
            Bukkit.getConsoleSender().sendMessage("Create blank file " + filename + ".yml or restart the server!");
        } catch(IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public void saveFile(FileConfiguration config, String file){
        try {
            config.save(new File(plugin.getDataFolder(), file + ".yml"));
        } catch(IOException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("Cannot save file " + file + ".yml!");
            Bukkit.getConsoleSender().sendMessage("Create blank file " + file + ".yml or restart the server!");
        }
    }

    public String getLanguageMessage(String message) {
        return getFile("messages").getString(message);
    }

    public void updateConfig(String file) {
        HashMap<String, Object> newConfig = getConfigVals(file);
        FileConfiguration c;
        if(file.equals("config.yml")) {
            c = plugin.getConfig();
        } else {
            c = getFile("messages");
        }
        for(String var : c.getKeys(false)) {
            newConfig.remove(var);
        }
        if(newConfig.size() != 0) {
            for(String key : newConfig.keySet()) {
                c.set(key, newConfig.get(key));
            }
            try {
                c.save(new File(plugin.getDataFolder(), file));
            } catch(IOException ignored) {
            }
        }
    }

    private HashMap<String, Object> getConfigVals(String file) {
        HashMap<String, Object> var = new HashMap<>();
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(stringFromInputStream(Main.class.getResourceAsStream("/" + file)));
        } catch(InvalidConfigurationException ignored) {
        }
        for(String key : config.getKeys(false)) {
            var.put(key, config.get(key));
        }
        return var;
    }

    @SuppressWarnings("resource")
    private String stringFromInputStream(InputStream in) {
        return new Scanner(in).useDelimiter("\\A").next();
    }

}
