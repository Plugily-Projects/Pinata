/*
 * Pinata plugin - spawn pinata mob and kill it to get drops
 * Copyright (C)2018 Plajer
 *
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pl.plajer.pinata;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;

public class ConfigurationManager {

  private static Main plugin;
  private static Properties properties = new Properties();

  public static void init(Main plugin) {
    ConfigurationManager.plugin = plugin;
  }

  public static void loadProperties() {
    if (plugin.getLocale() == Main.PinataLocale.ENGLISH) return;
    try {
      properties.load(new InputStreamReader(plugin.getResource("locale_" + plugin.getLocale().getPrefix() + ".properties"), Charset.forName("UTF-8")));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static FileConfiguration getConfig(String fileName) {
    File file = new File(plugin.getDataFolder() + File.separator + fileName + ".yml");
    if (!file.exists()) {
      plugin.getLogger().info("Creating " + fileName + ".yml because it does not exist!");
      plugin.saveResource(fileName + ".yml", true);
    }
    file = new File(plugin.getDataFolder(), fileName + ".yml");
    YamlConfiguration config = new YamlConfiguration();
    try {
      config.load(file);
    } catch (InvalidConfigurationException | IOException ex) {
      ex.printStackTrace();
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Cannot load file " + config + ".yml!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Create blank file " + config + ".yml or restart the server!");
    }
    return config;
  }

  public static void saveConfig(FileConfiguration config, String name) {
    try {
      config.save(new File(plugin.getDataFolder(), name + ".yml"));
    } catch (IOException e) {
      e.printStackTrace();
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Cannot save file " + name + ".yml!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Create blank file " + name + ".yml or restart the server!");
    }
  }

  public static String getDefaultLanguageMessage(String message) {
    return getConfig("messages").getString(message);
  }

  public static String getLanguageMessage(String message) {
    if (plugin.getLocale() != Main.PinataLocale.ENGLISH) {
      return properties.getProperty(ChatColor.translateAlternateColorCodes('&', message), "ERR_MESSAGE_NOT_FOUND access string: " + message);
    }
    return getConfig("messages").getString(message);
  }

}
