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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import pl.plajer.pinata.pinata.LivingPinata;
import pl.plajer.pinata.pinata.Pinata;
import pl.plajer.pinata.pinata.PinataItem;
import pl.plajer.pinata.pinataapi.PinataDeathEvent;
import pl.plajer.pinata.utils.PinataUtils;
import pl.plajer.pinata.utils.Utils;
import pl.plajerlair.core.utils.UpdateChecker;

class PinataListeners implements Listener {

  private Main plugin;

  PinataListeners(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPinataDamage(EntityDamageByEntityEvent e) {
    for (Pinata pinata : plugin.getPinataManager().getPinataList()) {
      if (!pinata.getName().equals(e.getEntity().getCustomName())) {
        continue;
      }
      if (plugin.getCommands().getPinata().get(e.getEntity()) == null) {
        continue;
      }
      if (plugin.getCommands().getPinata().get(e.getEntity()).getPlayer() == null) {
        //the type MUST be public, because pinata creator is not assigned
        e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 10);
        e.setCancelled(false);
        return;
      }
      if (pinata.getPinataType() == Pinata.PinataType.PUBLIC) {
        e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 10);
        //override World Guard blocking
        e.setCancelled(false);
      } else /* the type is private */ {
        if (plugin.getCommands().getPinata().get(e.getEntity()).getPlayer().equals(e.getDamager())) {
          if (plugin.getConfig().getBoolean("halloween-mode")) {
            if (!Bukkit.getServer().getVersion().contains("1.8")) {
              e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GHAST_HURT, 1, 1);
            }
          }
          e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 10);
          e.setCancelled(false);
        } else {
          e.getDamager().sendMessage(Utils.colorMessage("Pinata.Not-Own"));
          e.setCancelled(true);
        }
      }
      if (plugin.getConfig().getDouble("damage-modifier") != 0.0) {
        e.setDamage(plugin.getConfig().getDouble("damage-modifier"));
      }
      return;
    }
  }

  @EventHandler
  public void onBatDamage(EntityDamageEvent e) {
    if (e.getEntityType().equals(EntityType.BAT) || (e.getEntity().getCustomName() != null && e.getEntity().getCustomName().equals(Utils.colorRawMessage("&6Halloween!")))) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPinataPunch(EntityDamageByEntityEvent e) {
    LivingPinata livingPinata = plugin.getCommands().getPinata().get(e.getEntity());
    if (livingPinata == null || !(e.getDamager() instanceof Player)) {
      return;
    }
    Pinata pinata = livingPinata.getData();
    if (pinata.getDropType() != Pinata.DropType.PUNCH) {
      return;
    }
    if (plugin.getCommands().getPinata().get(e.getEntity()).getPlayer() != null) {
      //MUST be public is player is not assigned
      if (!plugin.getCommands().getPinata().get(e.getEntity()).getPlayer().equals(e.getDamager()) && pinata.getPinataType() == Pinata.PinataType.PRIVATE) {
        e.setCancelled(true);
        return;
      }
    }
    Player p = (Player) e.getDamager();
    for (PinataItem item : pinata.getDrops()) {
      PinataUtils.dropItems(item, e.getEntity(), pinata, p);
    }
  }

  @EventHandler
  public void onPinataDeath(final EntityDeathEvent e) {
    LivingPinata livingPinata = plugin.getCommands().getPinata().get(e.getEntity());
    if (livingPinata == null) {
      return;
    }
    Pinata pinata = livingPinata.getData();
    if (plugin.getCommands().getPinata().get(e.getEntity()).getPlayer() != null) {
      if (plugin.getCommands().getUsers().contains(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer())) {
        List<Player> users = new ArrayList<>(plugin.getCommands().getUsers());
        users.remove(plugin.getCommands().getPinata().get(e.getEntity()).getPlayer());
        plugin.getCommands().setUsers(users);
        users.clear();
      }
    }
    if (plugin.getConfig().getBoolean("halloween-mode")) {
      e.getEntity().getWorld().strikeLightningEffect(e.getEntity().getLocation());
      Random r = new Random();
      if (Bukkit.getServer().getVersion().contains("1.8")) {
        if (r.nextBoolean()) {
          e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.valueOf("WOLF_HOWL"), 1, 1);
        } else {
          e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.valueOf("WITHER_DEATH"), 1, 1);
        }
      } else {
        if (r.nextBoolean()) {
          e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_WOLF_HOWL, 1, 1);
        } else {
          e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1);
        }
      }
      final List<Entity> bats = new ArrayList<>();
      for (int i = 0; i < 5; i++) {
        final Entity bat = e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(), EntityType.BAT);
        bat.setCustomName(Utils.colorRawMessage("&6Halloween!"));
        bats.add(bat);
      }
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        for (Entity bat : bats) {
          bat.getWorld().playEffect(bat.getLocation(), Effect.SMOKE, 3);
          bat.remove();
        }
        bats.clear();
      }, 30);
    }
    e.getEntity().setLeashHolder(null);
    e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation().add(0, 1, 0), Effect.POTION_BREAK, 10);
    e.getDrops().clear();
    e.setDroppedExp(0);
    plugin.getCommands().getPinata().get(e.getEntity()).getFence().getBlock().setType(Material.AIR);
    plugin.getCommands().getPinata().get(e.getEntity()).getLeash().remove();
    final List<Item> itemsToGive = new ArrayList<>();
    final Player p = e.getEntity().getKiller() instanceof Player ? e.getEntity().getKiller() : plugin.getCommands().getPinata().get(e.getEntity()).getPlayer();
    //drops won't show if killer is environment and pinata player is not assigned. This pinata will be always in our hearts [*]
    if (p == null) return;
    int i = 0;
    List<PinataItem> items = new ArrayList<>();
    if (pinata.isBlindnessEnabled()) {
      if (p.hasPotionEffect(PotionEffectType.BLINDNESS)) {
        p.removePotionEffect(PotionEffectType.BLINDNESS);
      }
      if (pinata.isFullBlindness()) {
        p.removePotionEffect(PotionEffectType.NIGHT_VISION);
      }
    }
    if (pinata.getDropType() == Pinata.DropType.DEATH) {
      for (PinataItem item : pinata.getDrops()) {
        PinataUtils.dropItems(item, e.getEntity(), pinata, p);
        items.add(item);
        i++;
      }
      if (i == 0) {
        p.sendMessage(Utils.colorMessage("Pinata.Drop.No-Drops"));
      }
    }
    PinataDeathEvent pde = new PinataDeathEvent(e.getEntity().getKiller(), e.getEntity(), pinata, items);
    Bukkit.getPluginManager().callEvent(pde);
    plugin.getCommands().getPinata().remove(e.getEntity());
    itemsToGive.clear();
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    for (Entity en : Bukkit.getServer().getWorld(e.getPlayer().getWorld().getName()).getEntities()) {
      if (plugin.getCommands().getPinata().containsKey(en)) {
        if (plugin.getCommands().getPinata().get(en).getPlayer().equals(e.getPlayer())) {
          plugin.getCommands().getPinata().get(en).getFence().getBlock().setType(Material.AIR);
          plugin.getCommands().getPinata().get(en).getLeash().remove();
          en.remove();
          plugin.getCommands().getPinata().remove(en);
          plugin.getCommands().getUsers().remove(e.getPlayer());
        }
      }
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    if (!e.getPlayer().hasPermission("pinata.admin.notify")) {
      return;
    }
    if (plugin.getConfig().getBoolean("update-notify")) {
      try {
        String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
        boolean check = UpdateChecker.checkUpdate(plugin, currentVersion, 46655);
        if (check) {
          String latestVersion = "v" + UpdateChecker.getLatestVersion();
          e.getPlayer().sendMessage(Utils.colorMessage("Other.Plugin-Up-To-Date").replace("%old%", currentVersion).replace("%new%", latestVersion));
        }
      } catch (Exception ex) {
        e.getPlayer().sendMessage(Utils.colorMessage("Other.Plugin-Update-Check-Failed").replace("%error%", ex.getMessage()));
      }
    }
  }

}
