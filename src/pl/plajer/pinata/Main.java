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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.pinata.commands.MainCommand;
import pl.plajer.pinata.creator.CreatorChatEvents;
import pl.plajer.pinata.creator.CreatorEvents;
import pl.plajer.pinata.creator.SelectorEvents;
import pl.plajer.pinata.handlers.CrateManager;
import pl.plajer.pinata.handlers.MenuHandler;
import pl.plajer.pinata.handlers.PinataManager;
import pl.plajer.pinata.handlers.SignManager;
import pl.plajer.pinata.handlers.language.LanguageManager;
import pl.plajer.pinata.utils.MetricsLite;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.services.ServiceRegistry;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.UpdateChecker;

public class Main extends JavaPlugin {

  private List<String> filesToGenerate = Arrays.asList("crates", "pinatas", "messages", "pinata_storage");
  private CrateManager crateManager;
  private MainCommand commands;
  private PinataManager pinataManager;
  private SignManager signManager;
  private CreatorChatEvents creatorChatEvents;
  private List<String> disabledWorlds = new ArrayList<>();
  private Economy econ = null;

  @Override
  public void onEnable() {
    ServiceRegistry.registerService(this);
    LanguageManager.init(this);
    getLogger().log(Level.INFO, "Crack this pinata!");
    initializeClasses();
    if (isPluginEnabled("HolographicDisplays")) hologramScheduler();
    String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
    if (getConfig().getBoolean("update-notify")) {
      try {
        boolean check = UpdateChecker.checkUpdate(this, currentVersion, 46655);
        if (check) {
          String latestVersion = "v" + UpdateChecker.getLatestVersion();
          Bukkit.getConsoleSender().sendMessage(Utils.colorMessage("Other.Plugin-Up-To-Date").replace("%old%", currentVersion).replace("%new%", latestVersion));
        }
      } catch (Exception ex) {
        Bukkit.getConsoleSender().sendMessage(Utils.colorMessage("Other.Plugin-Update-Check-Failed").replace("%error%", ex.getMessage()));
      }
    }
  }

  @Override
  public void onDisable() {
    for (World world : Bukkit.getServer().getWorlds()) {
      for (Entity entity : Bukkit.getServer().getWorld(world.getName()).getEntities()) {
        if (commands.getPinata().containsKey(entity)) {
          if (commands.getPinata().get(entity).getPlayer() != null) {
            commands.getPinata().get(entity).getPlayer().sendMessage(Utils.colorMessage("Pinata.Config.Reload-Removed"));
          }
          commands.getPinata().get(entity).getFence().getBlock().setType(Material.AIR);
          commands.getPinata().get(entity).getLeash().remove();
          entity.remove();
          commands.getPinata().remove(entity);
        }
      }
    }
    //check if plugin is already disabled
    if (!getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) return;
    for (Hologram h : HologramsAPI.getHolograms(this)) {
      h.delete();
    }
  }

  private void initializeClasses() {
    new MetricsLite(this);
    crateManager = new CrateManager(this);
    commands = new MainCommand(this, true);
    new MenuHandler(this);
    new PinataListeners(this);
    new SelectorEvents(this);
    pinataManager = new PinataManager(this);
    signManager = new SignManager(this);
    creatorChatEvents = new CreatorChatEvents(this);
    saveDefaultConfig();
    for (String file : filesToGenerate) {
      ConfigUtils.getConfig(this, file);
    }
    setupDependencies();
    //todo LanguageMigrator
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

  public MainCommand getCommands() {
    return commands;
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

}
