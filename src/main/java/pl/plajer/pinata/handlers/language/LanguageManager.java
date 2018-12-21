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

package pl.plajer.pinata.handlers.language;

import java.io.File;

import pl.plajer.pinata.Main;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 01.09.2018
 */
public class LanguageManager {

  private static Main plugin;

  public static void init(Main pl) {
    plugin = pl;
    if (!new File(plugin.getDataFolder() + File.separator + "language.yml").exists()) {
      plugin.saveResource("language.yml", false);
    }
    if (!new File(plugin.getDataFolder() + "/locales/language_pl.yml").exists()) {
      plugin.saveResource("locales/language_pl.yml", false);
    }
    if (!new File(plugin.getDataFolder() + "/locales/language_fr.yml").exists()) {
      plugin.saveResource("locales/language_fr.yml", false);
    }
    if (!new File(plugin.getDataFolder() + "/locales/language_de.yml").exists()) {
      plugin.saveResource("locales/language_de.yml", false);
    }
    if (!new File(plugin.getDataFolder() + "/locales/language_nl.yml").exists()) {
      plugin.saveResource("locales/language_nl.yml", false);
    }
    if (!new File(plugin.getDataFolder() + "/locales/language_es.yml").exists()) {
      plugin.saveResource("locales/language_es.yml", false);
    }
  }

  /*Maybe needed code for future updates
  public static FileConfiguration getLanguageFile() {
    return ConfigUtils.getConfig(plugin, "language");
  }

  public static String getDefaultLanguageMessage(String message) {
    if (ConfigUtils.getConfig(plugin, "language").isSet(message)) {
      return ConfigUtils.getConfig(plugin, "language").getString(message);
    }
    MessageUtils.errorOccurred();
    Bukkit.getConsoleSender().sendMessage("Game message not found!");
    Bukkit.getConsoleSender().sendMessage("Please regenerate your language.yml file! If error still occurs report it to the developer!");
    Bukkit.getConsoleSender().sendMessage("Access string: " + message);
    return "ERR_MESSAGE_NOT_FOUND";
  }

  public static List<String> getLanguageList(String path) {
    if(plugin.getConfig().get("locale").equals("de"))  {
      return ConfigUtils.getConfig(plugin, "language_de").getStringList(path);
    }
    return ConfigUtils.getConfig(plugin, "language").getStringList(path);
  }*/

  public static String getLanguageMessage(String message) {
    switch (plugin.getConfig().getString("locale").toLowerCase()) {
      case "pl":
        return ConfigUtils.getConfig(plugin, "language_pl").getString(message, "ERR_MESSAGE_NOT_FOUND");
      case "fr":
        return ConfigUtils.getConfig(plugin, "language_fr").getString(message, "ERR_MESSAGE_NOT_FOUND");
      case "en":
        return ConfigUtils.getConfig(plugin, "language").getString(message);
      case "es":
        return ConfigUtils.getConfig(plugin, "language_es").getString(message, "ERR_MESSAGE_NOT_FOUND");
      case "nl":
        return ConfigUtils.getConfig(plugin, "language_nl").getString(message, "ERR_MESSAGE_NOT_FOUND");
      case "de":
        return ConfigUtils.getConfig(plugin, "language_de").getString(message, "ERR_MESSAGE_NOT_FOUND");
    }
    return ConfigUtils.getConfig(plugin, "language").getString(message);
  }
}

