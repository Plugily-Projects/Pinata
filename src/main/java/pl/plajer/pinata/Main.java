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

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.pinata.commands.arguments.ArgumentsRegistry;
import pl.plajer.pinata.creator.CreatorChatEvents;
import pl.plajer.pinata.creator.CreatorEvents;
import pl.plajer.pinata.creator.SelectorEvents;
import pl.plajer.pinata.handlers.CrateManager;
import pl.plajer.pinata.handlers.MenuHandler;
import pl.plajer.pinata.handlers.PinataManager;
import pl.plajer.pinata.handlers.SignManager;
import pl.plajer.pinata.handlers.language.LanguageManager;
import pl.plajer.pinata.handlers.language.LanguageMigrator;
import pl.plajer.pinata.utils.MessageUtils;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.services.update.UpdateChecker;
import pl.plajerlair.core.utils.ConfigUtils;

public class Main extends JavaPlugin {

  private List<String> filesToGenerate = Arrays.asList("crates", "config", "language", "pinata_storage");
  private CrateManager crateManager;
  private Storage storage;
  private PinataManager pinataManager;
  private SignManager signManager;
  private CreatorChatEvents creatorChatEvents;
  private List<String> disabledWorlds = new ArrayList<>();
  private Economy econ = null;
  private boolean forceDisable = false;
  private boolean needNormalUpdate = false;
  private boolean needBetaUpdate = false;
  private String newestVersion;
  private boolean placeholderAPI;

  @Override
  public void onEnable() {
    //removed since plcore will not support it
    //ServiceRegistry.registerService(this);
    String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    LanguageManager.init(this);
    saveDefaultConfig();
    //check if using releases before 3.0.0
    if (ConfigUtils.getFile(this, "messages.yml").exists()) {
      LanguageMigrator.migrateToNewFormat();
    }
    LanguageMigrator.configUpdate();
    LanguageMigrator.languageFileUpdate();
    if (!(version.equalsIgnoreCase("v1_9_R1") || version.equalsIgnoreCase("v1_10_R1") || version.equalsIgnoreCase("v1_11_R1")
        || version.equalsIgnoreCase("v1_12_R1") || version.equalsIgnoreCase("v1_13_R1") || version.equalsIgnoreCase("v1_13_R2"))) {
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Your server version is not supported by Pinata plugin!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Sadly, we must shut off. Maybe you consider changing your server version?");
      forceDisable = true;
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    try {
      Class.forName("org.spigotmc.SpigotConfig");
    } catch (Exception e) {
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Your server software is not supported by Pinata plugin!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "We support only Spigot and Spigot forks only! Shutting off...");
      forceDisable = true;
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    getLogger().log(Level.INFO, "Crack this pinata!");
    initializeClasses();
    if (isPluginEnabled("HolographicDisplays")) {
      hologramScheduler();
    }
    checkUpdate();
  }

  @Override
  public void onDisable() {
    if (forceDisable) {
      return;
    }
    for (World world : Bukkit.getServer().getWorlds()) {
      for (Entity entity : Bukkit.getServer().getWorld(world.getName()).getEntities()) {
        if (storage.getPinata().containsKey(entity)) {
          if (storage.getPinata().get(entity).getPlayer() != null) {
            storage.getPinata().get(entity).getPlayer().sendMessage(Utils.colorMessage("Pinata.Config.Reload-Removed"));
          }
          storage.getPinata().get(entity).getFence().getBlock().setType(Material.AIR);
          storage.getPinata().get(entity).getLeash().remove();
          entity.remove();
          storage.getPinata().remove(entity);
        }
      }
    }
    //check if plugin is already disabled
    if (!getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
      return;
    }
    for (Hologram h : HologramsAPI.getHolograms(this)) {
      h.delete();
    }
  }

  private void initializeClasses() {
    new Metrics(this);
    crateManager = new CrateManager(this);
    storage = new Storage();
    new MenuHandler(this);
    new PinataListeners(this);
    new SelectorEvents(this);
    new ArgumentsRegistry(this);
    pinataManager = new PinataManager(this);
    signManager = new SignManager(this);
    creatorChatEvents = new CreatorChatEvents(this);
    for (String file : filesToGenerate) {
      ConfigUtils.getConfig(this, file);
    }
    setupDependencies();
    for (String world : getConfig().getStringList("disabled-worlds")) {
      disabledWorlds.add(world);
      getLogger().info("Pinata creation blocked at world " + world + "!");
    }
    crateManager.loadCrates();
    pinataManager.loadPinatas();
    new CreatorEvents(this);
    crateManager.particleScheduler();
  }

  public CrateManager getCrateManager() {
    return crateManager;
  }

  @Deprecated
  public Storage getStorage() {
    return storage;
  }

  public PinataManager getPinataManager() {
    return pinataManager;
  }

  public SignManager getSignManager() {
    return signManager;
  }

  public CreatorChatEvents getCreatorChatEvents() {
    return creatorChatEvents;
  }

  public Economy getEco() {
    return econ;
  }

  public List<String> getDisabledWorlds() {
    return disabledWorlds;
  }

  private void setupDependencies() {
    if (isPluginEnabled("CrackShot")) {
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Detected CrackShot plugin!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Enabling CrackShot support.");
    } else {
      Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] CrackShot plugin isn't installed!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Disabling CrackShot support.");
    }
    if (!setupEconomy()) {
      Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Vault plugin isn't installed!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Disabling Vault support.");
    } else {
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Detected Vault plugin!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Enabling economy support.");
    }
    if (!isPluginEnabled("HolographicDisplays")) {
      Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Holographic Displays plugin isn't installed!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Disabling holograms support.");
    } else {
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Detected Holographic Displays plugin!");
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Pinata] Enabling holograms support.");
    }
    /* if we want PlaceHolderAPI
    if (!isPluginEnabled("PlaceholderAPI")) {
      Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] §c✖ §4PlaceholderAPI");
      Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[Pinata] Disabling PlaceholderAPI support.");
      placeholderAPI = false;
    } else {
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " §a✔ §ePlaceholderAPI §7| §aVersion§7:§e " + PlaceholderAPIPlugin.getInstance().getDescription().getVersion());
      placeholderAPI = true;
    }*/
  }

  /**
   * Holograms at crates locations
   * Moved to Main class because it throws an error with registering events in CrateManager class
   */
  private void hologramScheduler() {
    Bukkit.getScheduler().runTaskTimer(this, () -> {
      for (Location l : crateManager.getCratesLocations().keySet()) {
        Hologram holo = HologramsAPI.createHologram(this, l.clone().add(0.5, 1.5, 0.5));
        holo.appendTextLine(Utils.colorMessage("Hologram.Crate-Hologram").replace("%name%", crateManager.getCratesLocations().get(l)));
        Bukkit.getScheduler().runTaskLater(this, holo::delete, (long) getConfig().getDouble("hologram-refresh") * 20);
      }
    }, (long) this.getConfig().getDouble("hologram-refresh") * 20, (long) this.getConfig().getDouble("hologram-refresh") * 20);
  }

  public boolean isPluginEnabled(String plugin) {
    return getServer().getPluginManager().getPlugin(plugin) != null;
  }

  private boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    econ = rsp.getProvider();
    return econ != null;
  }

  private void checkUpdate() {
    if (getConfig().getBoolean("Update-Notifier.Enabled", true)) {
      UpdateChecker.init(this, 46655).requestUpdateCheck().whenComplete((result, exception) -> {
        if (result.requiresUpdate()) {
          newestVersion = result.getNewestVersion();
          if (result.getNewestVersion().contains("b")) {
            if (getConfig().getBoolean("Update-Notifier.Notify-Beta-Versions", true)) {
              needBetaUpdate = true;
              Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Your software is ready for update! However it's a BETA VERSION. Proceed with caution.");
              Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Pinata] Current version %old%, latest version %new%".replace("%old%", getDescription().getVersion()).replace("%new%",
                  result.getNewestVersion()));
            }
            return;
          }
          MessageUtils.updateIsHere();
          needNormalUpdate = true;
          Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Your Pinata plugin is outdated! Download it to keep with latest changes and fixes.");
          Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Disable this option in config.yml if you wish.");
          Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + getDescription().getVersion() + ChatColor.YELLOW + " Latest version: " + ChatColor.GREEN + result.getNewestVersion());
        }
      });
    }
  }

  public boolean isBetaUpdate() {
    return needBetaUpdate;
  }

  public boolean isNormalUpdate() {
    return needNormalUpdate;
  }

  public String getNewestVersion() {
    return newestVersion;
  }

  public boolean isPlaceholderAPIEnabled() {
    return placeholderAPI;
  }

}
