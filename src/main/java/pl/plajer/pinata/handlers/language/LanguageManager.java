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
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import pl.plajer.pinata.Main;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 01.09.2018
 */
public class LanguageManager {

  private static List<String> localesPrefixes = Arrays.asList("pl", "fr", "hu", "de", "nl", "es");
  private static FileConfiguration languageConfig;

  public static void init(Main plugin) {
    if (!new File(plugin.getDataFolder() + File.separator + "language.yml").exists()) {
      plugin.saveResource("language.yml", false);
    }
    for (String locale : localesPrefixes) {
      if (!new File(plugin.getDataFolder() + "/locales/language_" + locale + ".yml").exists()) {
        plugin.saveResource("/locales/language_" + locale + ".yml", false);
      }
    }
    String locale = plugin.getConfig().getString("locale").toLowerCase();
    switch (locale) {
      case "pl":
      case "fr":
      case "hu":
      case "es":
      case "nl":
      case "de":
      case "br":
        languageConfig = ConfigUtils.getConfig(plugin, "locales/language_" + locale);
        break;
      case "en":
      default:
          languageConfig = ConfigUtils.getConfig(plugin, "language");
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
    return languageConfig.getString(message, "ERR_MESSAGE_NOT_FOUND");
  }
}

