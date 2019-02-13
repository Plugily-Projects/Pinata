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

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.pinata.Main;
import pl.plajer.pinata.utils.MessageUtils;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.MigratorUtils;

/**
 * @author Plajer
 * <p>
 * Created at 01.09.2018
 */
//should migrate within the class
@Deprecated
public class LanguageMigrator {

  public static final int LANGUAGE_FILE_VERSION = 11;
  public static final int CONFIG_FILE_VERSION = 6;
  private static Main plugin = JavaPlugin.getPlugin(Main.class);
  private static List<String> migratable = Arrays.asList("config", "language");

  public static void configUpdate() {
    if (plugin.getConfig().getInt("File-Version-Do-Not-Edit") == CONFIG_FILE_VERSION) {
      return;
    }
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Pinata] System notify >> Your config file is outdated! Updating...");
    File file = new File(plugin.getDataFolder() + "/config.yml");

    int version = plugin.getConfig().getInt("Version", 0);
    updateConfigVersionControl(version);

    for (int i = version; i < CONFIG_FILE_VERSION; i++) {
      switch (version) {
        case 5:
          MigratorUtils.removeLineFromFile(file, "update-notify:");
          MigratorUtils.insertAfterLine(file, "#Notify about plugin updates?", "Update-Notifier:\n" +
              "  Enabled: true\n" +
              "  Notify-Beta-Versions: true\n\n");
          break;
      }
      version++;
    }
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] [System notify] Config updated, no comments were removed :)");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] [System notify] You're using latest config file version! Nice!");
  }

  public static void languageFileUpdate() {
    if (ConfigUtils.getConfig(plugin, "language").getString("File-Version-Do-Not-Edit", "").equals(String.valueOf(LANGUAGE_FILE_VERSION))) {
      return;
    }
    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Pinata] [System notify] Your language file is outdated! Updating...");

    int version = 0;
    if (NumberUtils.isNumber(ConfigUtils.getConfig(plugin, "language").getString("File-Version-Do-Not-Edit"))) {
      version = Integer.valueOf(ConfigUtils.getConfig(plugin, "language").getString("File-Version-Do-Not-Edit"));
    }
    updateLanguageVersionControl(version);

    File file = new File(plugin.getDataFolder() + "/language.yml");

    for (int i = version; i < LANGUAGE_FILE_VERSION; i++) {
      switch (version) {
        case 10:
          MigratorUtils.insertAfterLine(file, "Pinata:", "  Prefix: \"&6&lPinata &a>>\" ");
          break;
      }
      version++;
    }
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] [System notify] Language file updated! Nice!");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] [System notify] You're using latest language file version! Nice!");
  }

  public static void migrateToNewFormat() {
    MessageUtils.gonnaMigrate();
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Pinata is migrating all files to the new file format...");
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Don't worry! Old files will be renamed not overridden!");
    for (String file : migratable) {
      if (ConfigUtils.getFile(plugin, file).exists()) {
        ConfigUtils.getFile(plugin, file).renameTo(new File(plugin.getDataFolder(), "PN2_" + file + ".yml"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Renamed file " + file + ".yml");
      }
    }
    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Done! Enabling Pinata...");
  }

  private static void updateLanguageVersionControl(int oldVersion) {
    File file = new File(plugin.getDataFolder() + "/language.yml");
    MigratorUtils.removeLineFromFile(file, "File-Version-Do-Not-Edit: " + oldVersion);
    MigratorUtils.addNewLines(file, "File-Version-Do-Not-Edit: " + LANGUAGE_FILE_VERSION);
  }

  private static void updateConfigVersionControl(int oldVersion) {
    File file = new File(plugin.getDataFolder() + "/config.yml");
    MigratorUtils.removeLineFromFile(file, "File-Version-Do-Not-Edit: " + oldVersion);
    MigratorUtils.addNewLines(file, "File-Version-Do-Not-Edit: " + CONFIG_FILE_VERSION);
  }

}
